package com.team_5_back_repository.project.domain.post.repository;

import com.team_5_back_repository.project.domain.member.dto.dto.PostDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.post.dto.PostResponse;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :id")
    int incrementViewCount(@Param("id") Long id);

    // 멤버 별 게시글 수
    long countByMember(Member member);

    /* ==============================
     * 🔥 게시글 목록 (댓글 수 포함)
     * ============================== */

    @Query("""
    SELECT new com.team_5_back_repository.project.domain.post.dto.PostResponse(
        p.id,
        p.title,
        p.content,
        p.viewCount,
        p.likeCount,
        p.dislikeCount,
        COUNT(c),
        p.createdAt,
        p.updatedAt,
        p.isHot
    )
    FROM Post p
    LEFT JOIN Comment c
        ON c.post = p AND c.deleted = false
    GROUP BY p
    """)
    Page<PostResponse> findAllWithCommentCount(Pageable pageable);

    @Query("""
    SELECT new com.team_5_back_repository.project.domain.post.dto.PostResponse(
        p.id,
        p.title,
        p.content,
        p.viewCount,
        p.likeCount,
        p.dislikeCount,
        COUNT(c),
        p.createdAt,
        p.updatedAt,
        p.isHot
    )
    FROM Post p
    LEFT JOIN Comment c
        ON c.post = p AND c.deleted = false
    WHERE p.postType = :postType
    GROUP BY p
    """)
    Page<PostResponse> findByPostTypeWithCommentCount(
            @Param("postType") PostType postType,
            Pageable pageable
    );

    @Query("""
    SELECT new com.team_5_back_repository.project.domain.member.dto.dto.PostDto(
        p.id,
        p.title,
        p.postType,
        p.viewCount,
        p.likeCount,
        (SELECT COUNT(c)
         FROM Comment c
         WHERE c.post = p AND c.deleted = false),
        p.createdAt,
        p.updatedAt
    )
    FROM Post p
    WHERE p.member = :member
      AND p.postType = :type
    """)
    Page<PostDto> findPostByMemberAndType(
            @Param("member") Member member,
            @Param("type") PostType type,
            Pageable pageable
    );

    @Query("""
    SELECT new com.team_5_back_repository.project.domain.member.dto.dto.PostDto(
        p.id,
        p.title,
        p.postType,
        p.viewCount,
        p.likeCount,
        (SELECT COUNT(c)
         FROM Comment c
         WHERE c.post = p AND c.deleted = false),
        p.createdAt,
        p.updatedAt
    )
    FROM Post p
    WHERE p.member = :member
    """)
    Page<PostDto> findPostByMember(
            @Param("member") Member member,
            Pageable pageable
    );
}
