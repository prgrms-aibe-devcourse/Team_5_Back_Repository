package com.team_5_back_repository.project.domain.chatroom.service;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatMessageSendRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatMessageResponse;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatMessage;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatMessageRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatParticipantRepository participantRepository;
    private final RedisChatService redisChatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 메시지 전송 및 저장
     */
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageSendRequest request, Long senderId, String senderNickname) {
        // 채팅방 참여자인지 확인
        if (!participantRepository.existsByChatRoomIdAndMemberId(request.getChatRoomId(), senderId)) {
            throw new IllegalStateException("채팅방 참여자만 메시지를 보낼 수 있습니다.");
        }

        // 메시지 저장 (MySQL)
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(request.getChatRoomId())
                .senderId(senderId)
                .senderNickname(senderNickname)
                .type(request.getType())
                .content(request.getContent())
                .build();

        ChatMessage savedMessage = messageRepository.save(message);
        ChatMessageResponse response = ChatMessageResponse.from(savedMessage);

        // Redis에도 저장 (실시간 조회용)
        redisChatService.saveMessage(request.getChatRoomId(), response);

        log.info("메시지 전송: chatRoomId={}, senderId={}, type={}",
                request.getChatRoomId(), senderId, request.getType());

        return response;
    }

    /**
     * 채팅방 메시지 이력 조회 (페이징)
     */
    public Page<ChatMessageResponse> getChatMessages(Long chatRoomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ChatMessage> messages = messageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);

        return messages.map(ChatMessageResponse::from);
    }

    /**
     * 채팅방 최근 메시지 조회 (Redis 우선, 없으면 DB)
     */
    public List<ChatMessageResponse> getRecentMessages(Long chatRoomId, int count) {
        // 1. Redis에서 먼저 조회
        List<ChatMessageResponse> redisMessages = redisChatService.getRecentMessages(chatRoomId, count);

        if (!redisMessages.isEmpty()) {
            log.info("Redis에서 메시지 조회: chatRoomId={}, count={}", chatRoomId, redisMessages.size());
            return redisMessages;
        }

        // 2. Redis에 없으면 DB에서 조회
        log.info("DB에서 메시지 조회: chatRoomId={}", chatRoomId);
        List<ChatMessage> dbMessages = messageRepository
                .findTop50ByChatRoomIdOrderByCreatedAtDesc(chatRoomId);

        List<ChatMessageResponse> responses = dbMessages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());

        // 3. DB에서 조회한 메시지를 Redis에 다시 저장
        responses.forEach(msg -> redisChatService.saveMessage(chatRoomId, msg));

        return responses;
    }

    /**
     * 입장 메시지 자동 생성 + WebSocket 브로드캐스트
     */
    @Transactional
    public ChatMessageResponse createEnterMessage(Long chatRoomId, Long memberId, String nickname) {
        ChatMessage enterMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(memberId)
                .senderNickname(nickname)
                .type(ChatMessage.MessageType.ENTER)
                .content(nickname + "님이 입장하셨습니다.")
                .build();

        ChatMessage saved = messageRepository.save(enterMessage);
        ChatMessageResponse response = ChatMessageResponse.from(saved);

        // Redis 저장
        redisChatService.saveMessage(chatRoomId, response);
        redisChatService.addParticipant(chatRoomId, memberId);

        // WebSocket 브로드캐스트 (모든 구독자에게 전송)
        String destination = "/topic/chatroom/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, response);

        log.info("✅ 입장 메시지 전송 완료: chatRoomId={}, nickname={}, destination={}",
                chatRoomId, nickname, destination);

        return response;
    }

    /**
     * 퇴장 메시지 자동 생성 + WebSocket 브로드캐스트
     */
    @Transactional
    public ChatMessageResponse createLeaveMessage(Long chatRoomId, Long memberId, String nickname) {
        ChatMessage leaveMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(memberId)
                .senderNickname(nickname)
                .type(ChatMessage.MessageType.LEAVE)
                .content(nickname + "님이 퇴장하셨습니다.")
                .build();

        ChatMessage saved = messageRepository.save(leaveMessage);
        ChatMessageResponse response = ChatMessageResponse.from(saved);

        // Redis 저장
        redisChatService.saveMessage(chatRoomId, response);
        redisChatService.removeParticipant(chatRoomId, memberId);

        // WebSocket 브로드캐스트 (모든 구독자에게 전송)
        String destination = "/topic/chatroom/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, response);

        log.info("✅ 퇴장 메시지 전송 완료: chatRoomId={}, nickname={}, destination={}",
                chatRoomId, nickname, destination);

        return response;
    }

    /**
     * 강퇴 메시지 생성 및 브로드캐스트
     * - DB 저장
     * - Redis 저장
     * - WebSocket 브로드캐스트
     */
    @Transactional
    public ChatMessageResponse createKickMessage(Long chatRoomId, Long memberId, String nickname) {
        log.info("🚫 강퇴 메시지 생성: chatRoomId={}, memberId={}, nickname={}",
                chatRoomId, memberId, nickname);

        // 1. 강퇴 메시지 생성
        ChatMessage kickMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(memberId)
                .senderNickname(nickname)
                .type(ChatMessage.MessageType.KICK)
                .content(nickname + "님이 강퇴되었습니다.")
                .build();

        // 2. DB 저장
        ChatMessage saved = messageRepository.save(kickMessage);
        ChatMessageResponse response = ChatMessageResponse.from(saved);

        // 3. Redis 저장
        redisChatService.saveMessage(chatRoomId, response);

        // 4. WebSocket 브로드캐스트 (모든 참여자에게 전송)
        String destination = "/topic/chatroom/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, response);

        log.info("✅ 강퇴 메시지 전송 완료: chatRoomId={}, nickname={}", chatRoomId, nickname);

        return response;
    }

    /**
     * 권한 이양 메시지 생성 및 브로드캐스트 (신규 추가)
     */
    @Transactional
    public ChatMessageResponse createTransferMessage(
            Long chatRoomId,
            String formerCreatorNickname,
            String newCreatorNickname) {

        log.info("권한 이양 메시지 생성: chatRoomId={}, {} → {}",
                chatRoomId, formerCreatorNickname, newCreatorNickname);

        // 1. 권한 이양 메시지 생성
        ChatMessage transferMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(0L) // 시스템 메시지
                .senderNickname("시스템")
                .type(ChatMessage.MessageType.TRANSFER)
                .content(String.format("방장 권한이 %s님에서 %s님으로 이양되었습니다.",
                        formerCreatorNickname, newCreatorNickname))
                .build();

        // 2. DB 저장
        ChatMessage saved = messageRepository.save(transferMessage);
        ChatMessageResponse response = ChatMessageResponse.from(saved);

        // 3. Redis 저장
        redisChatService.saveMessage(chatRoomId, response);

        // 4. WebSocket 브로드캐스트
        String destination = "/topic/chatroom/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, response);

        log.info("✅ 권한 이양 메시지 전송 완료: chatRoomId={}", chatRoomId);

        return response;
    }
}