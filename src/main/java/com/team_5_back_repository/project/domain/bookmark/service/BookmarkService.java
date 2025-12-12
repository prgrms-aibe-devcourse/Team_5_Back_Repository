package com.team_5_back_repository.project.domain.bookmark.service;

import com.team_5_back_repository.project.domain.bookmark.dto.BookmarkResponse;
import com.team_5_back_repository.project.domain.bookmark.entity.Bookmark;
import com.team_5_back_repository.project.domain.bookmark.entity.BookmarkType;
import com.team_5_back_repository.project.domain.bookmark.repository.BookmarkRepository;
import com.team_5_back_repository.project.domain.member.dto.dto.BookmarkDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;

    public BookmarkResponse addBookmark(Long memberId, BookmarkType type, Long targetId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        if (bookmarkRepository.existsByMemberAndBookmarkTypeAndTargetId(member, type, targetId)) {
            throw new RuntimeException("이미 북마크된 항목입니다.");
        }

        Bookmark bookmark = Bookmark.builder()
                .member(member)
                .bookmarkType(type)
                .targetId(targetId)
                .build();

        bookmarkRepository.save(bookmark);

        return BookmarkResponse.from(bookmark);
    }

    public void removeBookmark(Long memberId, BookmarkType type, Long targetId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Bookmark bookmark = bookmarkRepository
                .findByMemberAndBookmarkTypeAndTargetId(member, type, targetId)
                .orElseThrow(() -> new RuntimeException("북마크가 존재하지 않음"));

        bookmarkRepository.delete(bookmark);
    }

    public Page<BookmarkDto> getBookmarksByMember(Member member, Pageable pageable) {
        return bookmarkRepository.findBookmarksByMember(member, pageable);
    }

    public Long countBookmarksByMember(Member member) {
        return bookmarkRepository.countByMember(member);
    }
}
