package com.team_5_back_repository.project.domain.chatroom.repository;

import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoom;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 지역별 채팅방 조회
    List<ChatRoom> findByRegionAndIsActiveTrue(String region);

    // 타입별 채팅방 조회 (소모임 or 공동구매)
    List<ChatRoom> findByTypeAndIsActiveTrue(ChatRoomType type);

    // 지역 + 타입으로 채팅방 조회
    List<ChatRoom> findByRegionAndTypeAndIsActiveTrue(String region, ChatRoomType type);

    // 생성자로 채팅방 조회
    List<ChatRoom> findByCreatorIdAndIsActiveTrue(Long creatorId);

    // 채팅방 이름으로 검색
    List<ChatRoom> findByNameContainingAndIsActiveTrue(String name);
}