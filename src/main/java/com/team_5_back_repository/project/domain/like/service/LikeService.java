package com.team_5_back_repository.project.domain.like.service;

import com.team_5_back_repository.project.domain.like.entity.Like;
import com.team_5_back_repository.project.domain.like.entity.ReactionType;
import com.team_5_back_repository.project.domain.like.repository.LikeRepository;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;

    public ReactionType react(Long postId, Long memberId, ReactionType newType) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Optional<Like> optional = likeRepository.findByMemberIdAndPostId(memberId, postId);

        if (optional.isEmpty()) {
            Like reaction = Like.builder()
                    .post(post)
                    .member(member)
                    .type(newType)
                    .deleted(false)
                    .build();

            likeRepository.save(reaction);
            return newType;
        }

        Like reaction = optional.get();

        if (reaction.isDeleted()) {
            reaction.react(newType);
            return newType;
        }

        if (reaction.getType() == newType) {
            reaction.cancel();
            return null;
        }

        reaction.react(newType);
        return newType;
    }

    public long countLikes(Long postId) {
        return likeRepository.countByPostIdAndTypeAndDeletedFalse(postId, ReactionType.LIKE);
    }

    public long countDislikes(Long postId) {
        return likeRepository.countByPostIdAndTypeAndDeletedFalse(postId, ReactionType.DISLIKE);
    }
}
