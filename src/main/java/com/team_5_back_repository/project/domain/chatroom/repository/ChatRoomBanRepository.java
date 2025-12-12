package com.team_5_back_repository.project.domain.chatroom.repository;

import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 채팅방 강퇴 이력 Repository
 */
@Repository
public interface ChatRoomBanRepository extends JpaRepository<ChatRoomBan, Long> {

    /**
     * 특정 채팅방에서 특정 회원이 강퇴되었는지 확인
     *
     * @param chatRoomId 채팅방 ID
     * @param memberId 회원 ID
     * @return 강퇴 이력 존재 여부
     */
    boolean existsByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

    /**
     * 특정 채팅방의 특정 회원 강퇴 이력 조회
     *
     * @param chatRoomId 채팅방 ID
     * @param memberId 회원 ID
     * @return 강퇴 이력
     */
    Optional<ChatRoomBan> findByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

    /**
     * 특정 채팅방의 모든 강퇴 이력 삭제
     * (채팅방 삭제 시 사용)
     *
     * @param chatRoomId 채팅방 ID
     */
    void deleteByChatRoomId(Long chatRoomId);
}