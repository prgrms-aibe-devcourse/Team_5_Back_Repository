package com.team_5_back_repository.project.domain.bookmark.entity;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "bookmark",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "bookmark_type", "target_id"})
        }
)
public class Bookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "bookmark_type",nullable = false, length = 30)
    private BookmarkType bookmarkType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

}

