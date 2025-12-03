package com.team_5_back_repository.project.domain.chatroom.service;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomListResponse;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomResponse;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoom;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatMessageRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatParticipantRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatRoomRepository;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final MemberRepository memberRepository;

    /**
     * 채팅방 생성
     */
    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request, Long creatorId) {
        // Member 조회
        Member creator = memberRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(request.getName())
                .type(request.getType())
                .creatorId(creatorId)
                .region(request.getRegion())
                .description(request.getDescription())
                .maxParticipants(request.getMaxParticipants())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 생성자를 참여자로 추가 (Member 객체 사용)
        ChatParticipant creatorParticipant = ChatParticipant.builder()
                .chatRoomId(savedRoom.getId())
                .member(creator)
                .isCreator(true)
                .build();

        participantRepository.save(creatorParticipant);

        log.info("채팅방 생성 완료: id={}, name={}, creator={}",
                savedRoom.getId(), savedRoom.getName(), creatorId);

        return ChatRoomResponse.from(savedRoom);
    }

    /**
     * 채팅방 목록 조회 (지역 + 타입)
     */
    public ChatRoomListResponse getChatRooms(String region, ChatRoomType type) {
        List<ChatRoom> chatRooms;

        if (region != null && type != null) {
            chatRooms = chatRoomRepository.findByRegionAndTypeAndIsActiveTrue(region, type);
        } else if (region != null) {
            chatRooms = chatRoomRepository.findByRegionAndIsActiveTrue(region);
        } else if (type != null) {
            chatRooms = chatRoomRepository.findByTypeAndIsActiveTrue(type);
        } else {
            chatRooms = chatRoomRepository.findAll();
        }

        List<ChatRoomResponse> responses = chatRooms.stream()
                .map(ChatRoomResponse::from)
                .collect(Collectors.toList());

        return ChatRoomListResponse.builder()
                .chatRooms(responses)
                .totalCount(responses.size())
                .build();
    }

    /**
     * 채팅방 상세 조회
     */
    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        return ChatRoomResponse.from(chatRoom);
    }

    /**
     * 채팅방 참여
     */
    @Transactional
    public void joinChatRoom(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 이미 참여 중인지 확인
        if (participantRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId)) {
            throw new IllegalStateException("이미 참여 중인 채팅방입니다.");
        }

        // 인원 확인
        chatRoom.increaseParticipant();

        // 참여자 추가 (Member 객체 사용)
        ChatParticipant participant = ChatParticipant.builder()
                .chatRoomId(chatRoomId)
                .member(member)
                .isCreator(false)
                .build();

        participantRepository.save(participant);
        chatRoomRepository.save(chatRoom);

        log.info("채팅방 참여 완료: chatRoomId={}, memberId={}", chatRoomId, memberId);
    }

    /**
     * 채팅방 나가기
     * 방장 권한 이양 및 빈 채팅방 삭제 기능 추가
     */
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 1. 방장이 나가는 경우 처리
        boolean isCreatorLeaving = chatRoom.getCreatorId().equals(memberId);

        if (isCreatorLeaving) {
            log.info("방장이 채팅방을 나갑니다: chatRoomId={}, memberId={}", chatRoomId, memberId);

            // 입장 순서대로 정렬된 참여자 목록 조회
            List<ChatParticipant> remainingParticipants = participantRepository
                    .findByChatRoomIdOrderByJoinedAtAsc(chatRoomId)
                    .stream()
                    .filter(p -> !p.getMemberId().equals(memberId))
                    .toList();

            if (!remainingParticipants.isEmpty()) {
                // 가장 먼저 참여한 사람(첫 번째)을 새 방장으로
                ChatParticipant nextCreator = remainingParticipants.get(0);
                Long newCreatorId = nextCreator.getMemberId();
                Member newCreatorMember = nextCreator.getMember(); // Member 객체 미리 저장

                chatRoom.changeCreator(newCreatorId);

                // 기존 참여자 데이터 삭제
                participantRepository.delete(nextCreator);
                participantRepository.flush(); // 즉시 DB 반영

                // 새로운 방장 참여자 생성 (Member 객체 재사용)
                ChatParticipant newCreatorParticipant = ChatParticipant.builder()
                        .chatRoomId(chatRoomId)
                        .member(newCreatorMember)
                        .isCreator(true)
                        .build();
                participantRepository.save(newCreatorParticipant);

                log.info("✅ 방장 권한 이양 (입장 순서): {} → {}", memberId, newCreatorId);
            }
        }

        // 2. 참여자 삭제
        participantRepository.deleteByChatRoomIdAndMemberId(chatRoomId, memberId);

        // 3. 인원 감소
        chatRoom.decreaseParticipant();

        // 4. 참여자가 0명이면 채팅방 완전 삭제
        if (chatRoom.getCurrentParticipants() == 0) {
            log.info("⚠️ 마지막 참여자가 나가서 채팅방을 삭제합니다: chatRoomId={}", chatRoomId);

            // 관련 메시지 모두 삭제
            messageRepository.deleteByChatRoomId(chatRoomId);

            // 채팅방 삭제
            chatRoomRepository.delete(chatRoom);

            log.info("✅ 채팅방 삭제 완료: chatRoomId={}", chatRoomId);
            return;
        }

        // 5. 변경사항 저장
        chatRoomRepository.save(chatRoom);

        log.info("채팅방 퇴장 완료: chatRoomId={}, memberId={}, 남은 인원={}",
                chatRoomId, memberId, chatRoom.getCurrentParticipants());
    }

    /**
     * 내가 참여한 채팅방 목록
     */
    public ChatRoomListResponse getMyChatRooms(Long memberId) {
        List<ChatParticipant> participants = participantRepository.findByMemberId(memberId);

        List<ChatRoomResponse> responses = participants.stream()
                .map(p -> chatRoomRepository.findById(p.getChatRoomId()))
                .filter(opt -> opt.isPresent() && opt.get().getIsActive())
                .map(opt -> ChatRoomResponse.from(opt.get()))
                .collect(Collectors.toList());

        return ChatRoomListResponse.builder()
                .chatRooms(responses)
                .totalCount(responses.size())
                .build();
    }

    /**
     * 채팅방 참여자 목록 조회 (입장 순서대로)
     */
    public List<ChatParticipant> getChatRoomParticipants(Long chatRoomId) {
        return participantRepository.findByChatRoomIdOrderByJoinedAtAsc(chatRoomId);
    }
}