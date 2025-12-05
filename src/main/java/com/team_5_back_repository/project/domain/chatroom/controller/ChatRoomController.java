package com.team_5_back_repository.project.domain.chatroom.controller;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatMessageResponse;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatParticipantResponse;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomListResponse;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomResponse;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import com.team_5_back_repository.project.domain.chatroom.service.ChatMessageService;
import com.team_5_back_repository.project.domain.chatroom.service.ChatRoomService;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final Rq rq;

    /**
     * 채팅방 생성 (로그인 필수)
     * POST /api/v1/chatrooms
     */
    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        ChatRoomResponse response = chatRoomService.createChatRoom(request, actor.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 목록 조회 (비회원도 가능)
     * 공동구매 채팅방은 제외 (소모임만 표시)
     * GET /api/v1/chatrooms?region=강남구&type=SMALL_GROUP
     */
    @GetMapping
    public ResponseEntity<ChatRoomListResponse> getChatRooms(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) ChatRoomType type
    ) {
        // type이 null이면 소모임만 조회 (GROUP_PURCHASE 제외)
        ChatRoomType filterType = type != null ? type : ChatRoomType.SMALL_GROUP;

        // GROUP_PURCHASE는 소모임 목록에서 제외
        if (filterType == ChatRoomType.GROUP_PURCHASE) {
            // 빈 목록 반환 또는 에러
            return ResponseEntity.ok(ChatRoomListResponse.builder()
                    .chatRooms(List.of())
                    .totalCount(0)
                    .build());
        }

        ChatRoomListResponse response = chatRoomService.getChatRooms(region, filterType);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 상세 조회 (비회원도 가능)
     * GET /api/v1/chatrooms/{chatRoomId}
     */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @PathVariable Long chatRoomId
    ) {
        ChatRoomResponse response = chatRoomService.getChatRoom(chatRoomId);
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 참여한 채팅방 목록 (로그인 필수)
     * GET /api/v1/chatrooms/my
     */
    @GetMapping("/my")
    public ResponseEntity<ChatRoomListResponse> getMyChatRooms() {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        ChatRoomListResponse response = chatRoomService.getMyChatRooms(actor.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 참여 (로그인 필수)
     * POST /api/v1/chatrooms/{chatRoomId}/join
     */
    @PostMapping("/{chatRoomId}/join")
    public ResponseEntity<Void> joinChatRoom(
            @PathVariable Long chatRoomId
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        chatRoomService.joinChatRoom(chatRoomId, actor.getId());

        // 입장 메시지 자동 생성
        chatMessageService.createEnterMessage(chatRoomId, actor.getId(), actor.getNickname());

        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 나가기 (로그인 필수)
     * POST /api/v1/chatrooms/{chatRoomId}/leave
     */
    @PostMapping("/{chatRoomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @PathVariable Long chatRoomId
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        try {
            // 1. 먼저 실제 퇴장 처리 (방장 권한 이양 포함)
            chatRoomService.leaveChatRoom(chatRoomId, actor.getId());

            // 2. 그 다음 퇴장 메시지 생성 및 WebSocket 브로드캐스트
            //    이 시점에는 이미 방장 권한이 이양된 상태
            chatMessageService.createLeaveMessage(chatRoomId, actor.getId(), actor.getNickname());

            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            // 채팅방이 이미 삭제된 경우 (마지막 참여자가 나간 경우)
            log.info("채팅방이 이미 삭제되었습니다: chatRoomId={}", chatRoomId);
            return ResponseEntity.ok().build();
        }
    }

    /**
     * 채팅방 메시지 이력 조회 (로그인 필수)
     * GET /api/v1/chatrooms/{chatRoomId}/messages?page=0&size=50
     */
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getChatMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Page<ChatMessageResponse> messages = chatMessageService.getChatMessages(chatRoomId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 채팅방 최근 메시지 조회 (비회원도 접근 가능, 비회원은 빈 목록)
     * GET /api/v1/chatrooms/{chatRoomId}/messages/recent?count=50
     */
    @GetMapping("/{chatRoomId}/messages/recent")
    public ResponseEntity<List<ChatMessageResponse>> getRecentMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "50") int count
    ) {
        Member actor = rq.getActor();

        // 비회원이면 빈 목록 반환 (에러 없이)
        if (actor == null) {
            return ResponseEntity.ok(List.of());
        }

        List<ChatMessageResponse> messages = chatMessageService.getRecentMessages(chatRoomId, count);
        return ResponseEntity.ok(messages);
    }

    /**
     * 채팅방 참여자 목록 조회 (비회원도 접근 가능, 비회원은 빈 목록)
     * GET /api/v1/chatrooms/{chatRoomId}/participants
     */
    @GetMapping("/{chatRoomId}/participants")
    public ResponseEntity<List<ChatParticipantResponse>> getChatRoomParticipants(
            @PathVariable Long chatRoomId
    ) {
        Member actor = rq.getActor();

        // 비회원이면 빈 목록 반환 (에러 없이)
        if (actor == null) {
            return ResponseEntity.ok(List.of());
        }

        List<ChatParticipant> participants = chatRoomService.getChatRoomParticipants(chatRoomId);

        List<ChatParticipantResponse> responses = participants.stream()
                .map(ChatParticipantResponse::from)
                .toList();

        log.info("채팅방 참여자 목록 조회: chatRoomId={}, 참여자 수={}", chatRoomId, responses.size());

        return ResponseEntity.ok(responses);
    }

    /**
     * 참여자 강퇴 (방장만 가능)
     * POST /api/v1/chatrooms/{chatRoomId}/kick/{targetMemberId}
     */
    @PostMapping("/{chatRoomId}/kick/{targetMemberId}")
    public ResponseEntity<Void> kickParticipant(
            @PathVariable Long chatRoomId,
            @PathVariable Long targetMemberId
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        try {
            // 강퇴 처리
            chatRoomService.kickParticipant(chatRoomId, actor.getId(), targetMemberId);

            log.info("✅ 참여자 강퇴 완료: chatRoomId={}, kickedBy={}, targetMemberId={}",
                    chatRoomId, actor.getId(), targetMemberId);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("❌ 참여자 강퇴 실패: {}", e.getMessage());
            throw e;
        }
    }
}