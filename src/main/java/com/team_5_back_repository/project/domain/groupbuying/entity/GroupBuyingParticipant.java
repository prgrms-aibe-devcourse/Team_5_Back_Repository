package com.team_5_back_repository.project.domain.groupbuying.entity;

import com.team_5_back_repository.project.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "group_buying_participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_buying_post_id", "member_id"}))
public class GroupBuyingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "group_buying_post_id")
    private Long groupBuyingPostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Integer contributedAmount = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public GroupBuyingParticipant(Long groupBuyingPostId, Member member, Integer contributedAmount) {
        this.groupBuyingPostId = groupBuyingPostId;
        this.member = member;
        this.contributedAmount = contributedAmount != null ? contributedAmount : 0;
    }

    public void addContribution(Integer amount) {
        this.contributedAmount += amount;
    }

    public Long getMemberId() {
        return member != null ? member.getId() : null;
    }

    public String getMemberNickname() {
        return member != null ? member.getNickname() : null;
    }
}