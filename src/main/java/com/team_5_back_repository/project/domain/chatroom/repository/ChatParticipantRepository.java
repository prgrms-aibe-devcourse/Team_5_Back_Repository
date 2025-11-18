package com.team_5_back_repository.project.domain.chatroom.repository;

import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    // 채팅방의 모든 참여자 조회
    List<ChatParticipant> findByChatRoomId(Long chatRoomId);

    // 특정 사용자의 모든 채팅방 조회
    List<ChatParticipant> findByMemberId(Long memberId);

    // 특정 사용자가 특정 채팅방에 참여 중인지 확인
    Optional<ChatParticipant> findByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

    // 참여 여부 확인
    boolean existsByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

    // 채팅방 참여자 수 카운트
    int countByChatRoomId(Long chatRoomId);

    // 채팅방에서 특정 사용자 삭제
    void deleteByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);
}