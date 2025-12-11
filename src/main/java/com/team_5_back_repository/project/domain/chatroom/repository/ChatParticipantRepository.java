package com.team_5_back_repository.project.domain.chatroom.repository;

import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import com.team_5_back_repository.project.domain.member.dto.dto.GroupChatDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    // 채팅방의 모든 참여자 조회 (입장 순서대로 정렬) + Member fetch join
    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.member WHERE cp.chatRoomId = :chatRoomId ORDER BY cp.joinedAt ASC")
    List<ChatParticipant> findByChatRoomIdOrderByJoinedAtAsc(@Param("chatRoomId") Long chatRoomId);

    // 특정 사용자의 모든 채팅방 조회
    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.member.id = :memberId")
    List<ChatParticipant> findByMemberId(@Param("memberId") Long memberId);

    // 특정 사용자가 특정 채팅방에 참여 중인지 확인
    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.member WHERE cp.chatRoomId = :chatRoomId AND cp.member.id = :memberId")
    Optional<ChatParticipant> findByChatRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId, @Param("memberId") Long memberId);

    // 참여 여부 확인
    @Query("SELECT CASE WHEN COUNT(cp) > 0 THEN true ELSE false END FROM ChatParticipant cp WHERE cp.chatRoomId = :chatRoomId AND cp.member.id = :memberId")
    boolean existsByChatRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId, @Param("memberId") Long memberId);

    // 채팅방 참여자 수 카운트
    int countByChatRoomId(Long chatRoomId);

    // 채팅방에서 특정 사용자 삭제
    @Modifying
    @Query("DELETE FROM ChatParticipant cp WHERE cp.chatRoomId = :chatRoomId AND cp.member.id = :memberId")
    void deleteByChatRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId, @Param("memberId") Long memberId);

    @Query(
    value = """
        SELECT new com.team_5_back_repository.project.domain.member.dto.dto.GroupChatDto(
            cp.chatRoomId,
            cr.name,
            cr.currentParticipants,
            cr.maxParticipants,
            cr.createdAt
        )
        FROM ChatParticipant cp
        JOIN ChatRoom cr ON cr.id = cp.chatRoomId
        WHERE cp.member = :member AND cr.type = 'SMALL_GROUP'
    """,
    countQuery = """
        SELECT COUNT(cp)
        FROM ChatParticipant cp
        WHERE cp.member = :member
    """)
    Page<GroupChatDto> findByMember(@Param("member") Member member, Pageable pageable);
}