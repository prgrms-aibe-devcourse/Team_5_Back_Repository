package com.team_5_back_repository.project.domain.post.repository;

import com.team_5_back_repository.project.domain.member.dto.dto.PostDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByPostType(PostType postType, Pageable pageable);

    // 멤버 별 게시글 수 조회
    long countByMember(Member member);

    // 멤버와 게시글 타입 별 게시글 조회
    @Query("""
    SELECT new com.team_5_back_repository.project.domain.member.dto.dto.PostDto(
        p.id,
        p.title,
        p.postType,
        p.viewCount,
        p.likeCount,
        (SELECT COUNT(c) FROM Comment c WHERE c.post = p),
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

    // 멤버 별 게시글 조회
    @Query("""
    SELECT new com.team_5_back_repository.project.domain.member.dto.dto.PostDto(
        p.id,
        p.title,
        p.postType,
        p.viewCount,
        p.likeCount,
        (SELECT COUNT(c) FROM Comment c WHERE c.post = p),
        p.createdAt,
        p.updatedAt
    )
    FROM Post p
    WHERE p.member = :member
""")
    Page<PostDto> findPostByMember(@Param("member") Member member, Pageable pageable);
}
