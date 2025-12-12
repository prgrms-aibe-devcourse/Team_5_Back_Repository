package com.team_5_back_repository.project.global.init;

import com.team_5_back_repository.project.domain.comment.dto.CommentRequest;
import com.team_5_back_repository.project.domain.comment.service.CommentService;
import com.team_5_back_repository.project.domain.member.dto.request.MemberJoinRequest;
import com.team_5_back_repository.project.domain.member.dto.dto.RegionDto;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import com.team_5_back_repository.project.domain.post.dto.PostRequest;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class MemberInit {
    @Lazy
    @Autowired
    private MemberInit self;

    private final MemberService memberService;
    private final PostService postService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
        };

    }

    @Transactional
    public void work1() {
        if (memberService.countMembers() > 0) return;

        RegionDto regionDto = new RegionDto();
        regionDto.setCode("11110186");
        regionDto.setSmall("신영동");
        regionDto.setFull("서울특별시 종로구 신영동");

        RegionDto regionDto2 = new RegionDto();
        regionDto2.setCode("11110120");
        regionDto2.setSmall("신문로1가");
        regionDto2.setFull("서울특별시 종로구 신문로1가");

        List<RegionDto> regionDtoList = new ArrayList<>();
        regionDtoList.add(regionDto);
        regionDtoList.add(regionDto2);

        memberService.join(
                MemberJoinRequest.builder()
                        .email("test@test.com")
                        .password("test")
                        .nickname("테스트계정")
                        .regions(regionDtoList)
                        .build()
        );

        for(int i = 0; i < 11; i ++) {
            postService.createPost(
                    PostRequest.builder()
                            .title("테스트 게시글 " + (i + 1))
                            .content("이것은 테스트 게시글입니다.")
                            .postType(PostType.FREE)
                            .tags(Set.of("테스트", "게시글"))
                            .build(),
                    new ArrayList<>(),
                    1L
            );
            commentService.createComment(i+1L, 1L, new CommentRequest("테스트 댓글 " + (i + 1)));
        }

        for(int i = 0; i < 11; i ++) {
            postService.createPost(
                    PostRequest.builder()
                            .title("테스트 게시글 " + (i + 1))
                            .content("이것은 테스트 게시글입니다.")
                            .postType(PostType.TIP)
                            .tags(Set.of("테스트", "게시글"))
                            .build(),
                    new ArrayList<>(),
                    1L
            );
            commentService.createComment(i+11L, 1L, new CommentRequest("테스트 댓글 " + (i + 1)));
        }
    }

    @Autowired
    private CommentService commentService;
}