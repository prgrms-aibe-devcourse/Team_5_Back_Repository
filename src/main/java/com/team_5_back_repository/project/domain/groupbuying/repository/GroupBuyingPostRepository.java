package com.team_5_back_repository.project.domain.groupbuying.repository;

import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingPost;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBuyingPostRepository extends JpaRepository<GroupBuyingPost, Long> {

    // 채팅방 ID로 조회
    Optional<GroupBuyingPost> findByChatRoomId(Long chatRoomId);

    // 채팅방 ID 존재 여부 확인
    boolean existsByChatRoomId(Long chatRoomId);

    // 지역별 조회
    List<GroupBuyingPost> findByRegion(String region);

    // 상태별 조회
    List<GroupBuyingPost> findByStatus(GroupBuyingStatus status);

    // 지역 + 상태별 조회
    List<GroupBuyingPost> findByRegionAndStatus(String region, GroupBuyingStatus status);

    // 작성자별 조회
    List<GroupBuyingPost> findByCreatorId(Long creatorId);

    // 전체 조회 (최신순)
    List<GroupBuyingPost> findAllByOrderByCreatedAtDesc();
}