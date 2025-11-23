package com.team_5_back_repository.project.domain.chatroom.controller;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatMessageSendRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatMessageResponse;
import com.team_5_back_repository.project.domain.chatroom.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트가 /app/chat/message 로 메시지를 보내면
     * 이 메서드가 처리하고, 채팅방 구독자들에게 전달합니다.
     *
     * 흐름:
     * 1. 클라이언트 -> /app/chat/message (메시지 전송)
     * 2. 서버에서 처리 (DB, Redis 저장)
     * 3. 서버 -> /topic/chatroom/{chatRoomId} (구독자들에게 브로드캐스트)
     */
    @MessageMapping("/chat/message")
    public void sendMessage(
            @Payload ChatMessageSendRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        try {
            // WebSocket 세션에서 사용자 정보 추출
            Long userId = getUserIdFromSession(headerAccessor);
            String nickname = getNicknameFromSession(headerAccessor);

            log.info("WebSocket 메시지 수신: chatRoomId={}, userId={}, type={}",
                    request.getChatRoomId(), userId, request.getType());

            // 메시지 저장 및 처리
            ChatMessageResponse response = chatMessageService.sendMessage(request, userId, nickname);

            // 해당 채팅방을 구독한 모든 클라이언트에게 메시지 전송
            String destination = "/topic/chatroom/" + request.getChatRoomId();
            messagingTemplate.convertAndSend(destination, response);

            log.info("메시지 브로드캐스트 완료: destination={}", destination);

        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);

            // 에러 메시지를 보낸 사용자에게만 전달
            String errorDestination = "/queue/errors";
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getUser().getName(),
                    errorDestination,
                    "메시지 전송에 실패했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * 입장 메시지 처리
     */
    @MessageMapping("/chat/enter")
    public void enterChatRoom(
            @Payload ChatMessageSendRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Long userId = getUserIdFromSession(headerAccessor);
        String nickname = getNicknameFromSession(headerAccessor);

        log.info("채팅방 입장: chatRoomId={}, userId={}, nickname={}",
                request.getChatRoomId(), userId, nickname);

        ChatMessageResponse enterMessage = chatMessageService
                .createEnterMessage(request.getChatRoomId(), userId, nickname);

        String destination = "/topic/chatroom/" + request.getChatRoomId();
        messagingTemplate.convertAndSend(destination, enterMessage);
    }

    /**
     * 퇴장 메시지 처리
     */
    @MessageMapping("/chat/leave")
    public void leaveChatRoom(
            @Payload ChatMessageSendRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Long userId = getUserIdFromSession(headerAccessor);
        String nickname = getNicknameFromSession(headerAccessor);

        log.info("채팅방 퇴장: chatRoomId={}, userId={}, nickname={}",
                request.getChatRoomId(), userId, nickname);

        ChatMessageResponse leaveMessage = chatMessageService
                .createLeaveMessage(request.getChatRoomId(), userId, nickname);

        String destination = "/topic/chatroom/" + request.getChatRoomId();
        messagingTemplate.convertAndSend(destination, leaveMessage);
    }

    /**
     * WebSocket 세션에서 사용자 ID 추출
     */
    private Long getUserIdFromSession(SimpMessageHeaderAccessor headerAccessor) {
        Object userId = headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return Long.parseLong(userId.toString());
    }

    /**
     * WebSocket 세션에서 닉네임 추출
     */
    private String getNicknameFromSession(SimpMessageHeaderAccessor headerAccessor) {
        Object nickname = headerAccessor.getSessionAttributes().get("nickname");
        if (nickname == null) {
            return "익명";
        }
        return nickname.toString();
    }
}