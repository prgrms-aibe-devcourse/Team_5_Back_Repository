package com.team_5_back_repository.project.domain.member.entity;

import com.team_5_back_repository.project.domain.member.dto.MemberDto;
import com.team_5_back_repository.project.domain.member.dto.MemberJoinRequest;
import com.team_5_back_repository.project.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "region_id")
//    private Region region;

    private String introduction;

}
