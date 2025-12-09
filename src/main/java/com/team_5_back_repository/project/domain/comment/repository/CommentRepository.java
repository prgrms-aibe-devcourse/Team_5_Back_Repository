package com.team_5_back_repository.project.domain.comment.repository;

import com.team_5_back_repository.project.domain.comment.entity.Comment;
import com.team_5_back_repository.project.domain.member.dto.dto.CommentDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(Long postId);
    Optional<Comment> findByIdAndDeletedFalse(Long id);

    @Query("""
    SELECT new com.team_5_back_repository.project.domain.member.dto.dto.CommentDto(
        c.post.id,
        c.post.title,
        c.content,
        c.createdAt
    )
    FROM Comment c
    WHERE c.member = :member
    AND c.deleted = false
""")
    Page<CommentDto> findByMemberAndDeletedFalse(
            @Param("member") Member member,
            Pageable pageable
    );

    Long countByMemberAndDeletedFalse(Member member);
}
