package com.team_5_back_repository.project.domain.like.service;

import com.team_5_back_repository.project.domain.like.entity.ReactionType;
import com.team_5_back_repository.project.domain.like.repository.LikeRepository;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class LikeServiceTest {
    @Autowired
    private LikeService likeService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Test
    void 게시글_추천수_2이상_되면_HOT으로_변경된다() {

        // given
        Member m1 = memberRepository.save(
                Member.builder()
                        .email("u1@test.com")
                        .password("pass")
                        .nickname("user1")
                        .apiKey("api1")
                        .build()
        );

        Member m2 = memberRepository.save(
                Member.builder()
                        .email("u2@test.com")
                        .password("pass")
                        .nickname("user2")
                        .apiKey("api2")
                        .build()
        );

        Post post = Post.builder()
                .title("test")
                .content("content")
                .postType(PostType.FREE)
                .member(m1)
                .build();

        postRepository.save(post);


        // when: 두 명이 좋아요
        likeService.react(post.getId(), m1.getId(), ReactionType.LIKE);
        likeService.react(post.getId(), m2.getId(), ReactionType.LIKE);

        // then
        Post updated = postRepository.findById(post.getId()).orElseThrow();

        assertEquals(2, updated.getLikeCount());
        assertEquals(PostType.HOT, updated.getPostType());
        assertEquals(0, updated.getDislikeCount());
    }
}
