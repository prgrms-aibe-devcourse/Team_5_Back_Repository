package com.team_5_back_repository.project.global.init;

import com.team_5_back_repository.project.domain.member.dto.MemberDto;
import com.team_5_back_repository.project.domain.member.dto.MemberJoinRequest;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class MemberInit {
    @Lazy
    @Autowired
    private MemberInit self;

    private final MemberService memberService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
        };

    }

    @Transactional
    public void work1() {
        if (memberService.countMembers() > 0) return;

        memberService.join(
                MemberJoinRequest.builder()
                        .email("test@test.com")
                        .password("test")
                        .nickname("테스트계정")
                        .build()
        );
    }
}