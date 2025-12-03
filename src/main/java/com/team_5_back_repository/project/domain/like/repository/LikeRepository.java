package com.team_5_back_repository.project.domain.like.repository;

import com.team_5_back_repository.project.domain.like.entity.PostLike;
import com.team_5_back_repository.project.domain.like.entity.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId);
    long countByPostIdAndTypeAndDeletedFalse(Long postId, ReactionType type);
}
