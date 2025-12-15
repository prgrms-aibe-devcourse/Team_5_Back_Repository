package com.team_5_back_repository.project.domain.post.repository;

import com.team_5_back_repository.project.domain.member.dto.dto.PostDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
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

    @Query("""
    SELECT p FROM Post p
    WHERE p.postType = :postType
""")
    Page<Post> findByPostType(Pageable pageable, @Param("postType") PostType postType);

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
    Page<Post> findByIsHotTrue(Pageable pageable);

    @Query("""
    SELECT p FROM Post p
    WHERE p.postType = :type
    AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)
    """)
    Page<Post> searchByType(
            @Param("type") PostType type,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT p FROM Post p
    WHERE (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)
    """)
    Page<Post> searchAll(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT p FROM Post p
    WHERE p.isHot = true
    AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)
    """)
    Page<Post> searchHot(@Param("keyword") String keyword, Pageable pageable);
}
