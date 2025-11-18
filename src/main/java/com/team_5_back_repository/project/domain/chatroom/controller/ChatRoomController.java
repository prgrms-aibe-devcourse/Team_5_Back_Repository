package com.team_5_back_repository.project.domain.chatroom.controller;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatRoomJoinRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatMessageResponse;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomListResponse;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomResponse;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import com.team_5_back_repository.project.domain.chatroom.service.ChatMessageService;
import com.team_5_back_repository.project.domain.chatroom.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    /**
     * 채팅방 생성
     * POST /api/chatrooms
     */
    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(
            @Valid @RequestBody ChatRoomCreateRequest request,
            @RequestHeader("X-User-Id") Long userId  // TODO: JWT에서 추출로 변경
    ) {
        ChatRoomResponse response = chatRoomService.createChatRoom(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 목록 조회 (지역, 타입 필터링)
     * GET /api/chatrooms?region=강남구&type=SMALL_GROUP
     */
    @GetMapping
    public ResponseEntity<ChatRoomListResponse> getChatRooms(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) ChatRoomType type
    ) {
        ChatRoomListResponse response = chatRoomService.getChatRooms(region, type);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 상세 조회
     * GET /api/chatrooms/{chatRoomId}
     */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @PathVariable Long chatRoomId
    ) {
        ChatRoomResponse response = chatRoomService.getChatRoom(chatRoomId);
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 참여한 채팅방 목록
     * GET /api/chatrooms/my
     */
    @GetMapping("/my")
    public ResponseEntity<ChatRoomListResponse> getMyChatRooms(
            @RequestHeader("X-User-Id") Long userId  // TODO: JWT에서 추출로 변경
    ) {
        ChatRoomListResponse response = chatRoomService.getMyChatRooms(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 참여
     * POST /api/chatrooms/{chatRoomId}/join
     */
    @PostMapping("/{chatRoomId}/join")
    public ResponseEntity<Void> joinChatRoom(
            @PathVariable Long chatRoomId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Nickname") String nickname
    ) {
        chatRoomService.joinChatRoom(chatRoomId, userId);

        // 입장 메시지 자동 생성
        chatMessageService.createEnterMessage(chatRoomId, userId, nickname);

        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 나가기
     * POST /api/chatrooms/{chatRoomId}/leave
     */
    @PostMapping("/{chatRoomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @PathVariable Long chatRoomId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Nickname") String nickname
    ) {
        // 퇴장 메시지 자동 생성
        chatMessageService.createLeaveMessage(chatRoomId, userId, nickname);

        chatRoomService.leaveChatRoom(chatRoomId, userId);

        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 메시지 이력 조회 (페이징)
     * GET /api/chatrooms/{chatRoomId}/messages?page=0&size=50
     */
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getChatMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Page<ChatMessageResponse> messages = chatMessageService.getChatMessages(chatRoomId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 채팅방 최근 메시지 조회
     * GET /api/chatrooms/{chatRoomId}/messages/recent?count=50
     */
    @GetMapping("/{chatRoomId}/messages/recent")
    public ResponseEntity<List<ChatMessageResponse>> getRecentMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "50") int count
    ) {
        List<ChatMessageResponse> messages = chatMessageService.getRecentMessages(chatRoomId, count);
        return ResponseEntity.ok(messages);
    }
}