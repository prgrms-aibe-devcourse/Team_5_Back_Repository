package com.team_5_back_repository.project.domain.bookmark.repository;

import com.team_5_back_repository.project.domain.bookmark.entity.Bookmark;
import com.team_5_back_repository.project.domain.bookmark.entity.BookmarkType;
import com.team_5_back_repository.project.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
