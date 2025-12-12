package com.team_5_back_repository.project.domain.bookmark.repository;

import com.team_5_back_repository.project.domain.bookmark.entity.Bookmark;
import com.team_5_back_repository.project.domain.bookmark.entity.BookmarkType;
import com.team_5_back_repository.project.domain.member.dto.dto.BookmarkDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByMemberAndBookmarkTypeAndTargetId(
            Member member,
            BookmarkType bookmarkType,
            Long targetId
    );

    boolean existsByMemberAndBookmarkTypeAndTargetId(
            Member member,
            BookmarkType bookmarkType,
            Long targetId
    );

    List<Bookmark> findAllByMember(Member member);

    Long countByMember(Member member);

    @Query(
    value = """
    SELECT new com.team_5_back_repository.project.domain.member.dto.dto.BookmarkDto(
        p.id,
        p.postType,
        p.title,
        p.member.nickname,
        p.createdAt
    )
    FROM Bookmark b, Post p
    WHERE b.member = :member
    AND p.id = b.targetId
    """,
    countQuery = """
        SELECT COUNT(b)
        FROM Bookmark b
        WHERE b.member = :member
""")
    Page<BookmarkDto> findBookmarksByMember(
            @Param("member") Member member,
            Pageable pageable
    );
}
