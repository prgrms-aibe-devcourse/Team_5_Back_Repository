package com.team_5_back_repository.project.domain.post.entity;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable=false)
    private String title;

    @Lob
    @Column(nullable=false)
    private String content;

    @Column(nullable = true)
    private String attachmentPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    private Long viewCount = 0L;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int dislikeCount = 0;

    @Builder.Default
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void update(String title, String content, String attachmentPath, PostType postType, Set<Tag> tags) {
        this.title = title;
        this.content = content;
        this.attachmentPath = attachmentPath;
        this.postType = postType;
        this.tags = tags;
    }
    public void increaseViewCount() {
        this.viewCount += 1;
    }
    public void updateType(PostType newType) {
        this.postType = newType;
    }
    public int getRecommendCount() {
        return this.likeCount;
    }
}
