package com.team_5_back_repository.project.domain.chatroom.service;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomListResponse;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomResponse;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoom;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatParticipantRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatRoomRepository;
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

    /**
     * 채팅방 생성
     */
    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request, Long creatorId) {
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

        // 생성자를 참여자로 추가
        ChatParticipant creator = ChatParticipant.builder()
                .chatRoomId(savedRoom.getId())
                .memberId(creatorId)
                .isCreator(true)
                .build();

        participantRepository.save(creator);

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

        // 이미 참여 중인지 확인
        if (participantRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId)) {
            throw new IllegalStateException("이미 참여 중인 채팅방입니다.");
        }

        // 인원 확인
        chatRoom.increaseParticipant();

        // 참여자 추가
        ChatParticipant participant = ChatParticipant.builder()
                .chatRoomId(chatRoomId)
                .memberId(memberId)
                .isCreator(false)
                .build();

        participantRepository.save(participant);
        chatRoomRepository.save(chatRoom);

        log.info("채팅방 참여 완료: chatRoomId={}, memberId={}", chatRoomId, memberId);
    }

    /**
     * 채팅방 나가기
     */
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 참여자 삭제
        participantRepository.deleteByChatRoomIdAndMemberId(chatRoomId, memberId);

        // 인원 감소
        chatRoom.decreaseParticipant();

        // 참여자가 0명이면 채팅방 비활성화
        if (chatRoom.getCurrentParticipants() == 0) {
            chatRoom.deactivate();
        }

        chatRoomRepository.save(chatRoom);

        log.info("채팅방 퇴장 완료: chatRoomId={}, memberId={}", chatRoomId, memberId);
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
}