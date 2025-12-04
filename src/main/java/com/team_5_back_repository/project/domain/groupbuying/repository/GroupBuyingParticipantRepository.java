package com.team_5_back_repository.project.domain.groupbuying.repository;

import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBuyingParticipantRepository extends JpaRepository<GroupBuyingParticipant, Long> {

    // 게시글별 참여자 목록 조회
    List<GroupBuyingParticipant> findByGroupBuyingPostId(Long groupBuyingPostId);

    // 특정 사용자의 참여 여부 확인
    boolean existsByGroupBuyingPostIdAndMember_Id(Long groupBuyingPostId, Long memberId);

    // 특정 사용자의 참여 정보 조회
    Optional<GroupBuyingParticipant> findByGroupBuyingPostIdAndMember_Id(Long groupBuyingPostId, Long memberId);

    // 사용자가 참여한 모든 공동구매 조회
    List<GroupBuyingParticipant> findByMember_Id(Long memberId);

    // 게시글별 참여자 삭제
    void deleteByGroupBuyingPostIdAndMember_Id(Long groupBuyingPostId, Long memberId);

    void deleteByGroupBuyingPostId(Long groupBuyingPostId);
}