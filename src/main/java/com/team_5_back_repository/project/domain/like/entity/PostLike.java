package com.team_5_back_repository.project.domain.like.entity;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name ="post_likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "post_id"})
        }
)
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    private ReactionType type;

    private boolean deleted = false;

    public void react(ReactionType type) {
        this.type = type;
        this.deleted = false;
    }

    public void cancel() {
        this.deleted = true;
    }
}
