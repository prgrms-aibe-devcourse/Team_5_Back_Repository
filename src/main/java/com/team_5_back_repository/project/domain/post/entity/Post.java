package com.team_5_back_repository.project.domain.post.entity;

import com.team_5_back_repository.project.domain.comment.entity.Comment;
import com.team_5_back_repository.project.domain.like.entity.PostLike;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.global.cloudstorage.entity.FileEntity;
import com.team_5_back_repository.project.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> attachmentPath = new ArrayList<>();

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

    @Column(nullable = false)
    private boolean isHot = false;

    @Builder.Default
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<PostLike> likes = new ArrayList<>();

    public void update(String title,
                       String content,
                       List<FileEntity> attachmentPath,
                       PostType postType,
                       Set<Tag> tags) {
        this.title = title;
        this.content = content;
        this.attachmentPath.clear();
        if (attachmentPath != null && !attachmentPath.isEmpty()) {
            this.attachmentPath.addAll(attachmentPath);
        }
        this.postType = postType;
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    public void increaseLike() { this.likeCount++;
        if (this.likeCount >= 2) {
            this.isHot = true;
        }}
    public void decreaseLike() { if (this.likeCount > 0) this.likeCount--; }

    public void increaseDislike() { this.dislikeCount++;
}
    public void decreaseDislike() { if (this.dislikeCount > 0) this.dislikeCount--; }

    public int getDislikeCount() {
        return this.dislikeCount;
    }
}
