package com.team_5_back_repository.project.domain.post.entity;

import com.team_5_back_repository.project.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    @Lob
    private String content;

    private String attachmentPath;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    public void update(String title, String content, String attachmentPath, PostType postType) {
        this.title = title;
        this.content = content;
        this.attachmentPath = attachmentPath;
        this.postType = postType;
    }
}
