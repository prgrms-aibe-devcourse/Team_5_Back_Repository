package com.team_5_back_repository.project.domain.comment.service;


import com.team_5_back_repository.project.domain.comment.dto.CommentRequest;
import com.team_5_back_repository.project.domain.comment.dto.CommentResponse;
import com.team_5_back_repository.project.domain.comment.entity.Comment;
import com.team_5_back_repository.project.domain.comment.repository.CommentRepository;
import com.team_5_back_repository.project.domain.member.dto.dto.CommentDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public Long createComment(Long postId, Long memberId, CommentRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .member(member)
                .build();

        Comment saved = commentRepository.save(comment);
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {

        List<Comment> comments = commentRepository.findByPostIdAndDeletedFalseOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CommentResponse updateComment(Long commentId, Long memberId, CommentRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        if (!comment.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("수정 권한 없음");
        }

        comment.update(request.getContent());
        return CommentResponse.from(comment);
    }

    public void deleteComment(Long commentId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        if (!comment.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("삭제 권한 없음");
        }

        comment.softDelete();
    }

    @Transactional(readOnly = true)
    public Page<CommentDto> getCommentByMember(Member member, Pageable pageable) {
        return commentRepository.findByMemberAndDeletedFalse(member, pageable);
    }

    @Transactional(readOnly = true)
    public Long countCommentByMember(Member member) {
        return commentRepository.countByMemberAndDeletedFalse(member);
    }

}
