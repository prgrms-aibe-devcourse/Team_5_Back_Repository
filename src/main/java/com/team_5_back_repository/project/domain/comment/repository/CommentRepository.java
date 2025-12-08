package com.team_5_back_repository.project.domain.comment.repository;

import com.team_5_back_repository.project.domain.comment.entity.Comment;
import com.team_5_back_repository.project.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(Long postId);
    Optional<Comment> findByIdAndDeletedFalse(Long id);
    Page<Comment> findByMemberAndDeletedFalse(Member member, Pageable pageable);
    Long countByMemberAndDeletedFalse(Member member);
}
