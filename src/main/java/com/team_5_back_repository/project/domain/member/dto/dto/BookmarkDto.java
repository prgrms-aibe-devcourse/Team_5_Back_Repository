package com.team_5_back_repository.project.domain.member.dto.dto;

import com.team_5_back_repository.project.domain.post.entity.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookmarkDto {
    private Long id;
    private PostType category;
    private String title;
    private String author;
    private LocalDateTime date;
}


//id: 1,
//category: "꿀팁",
//title: "혼자 살 때 꼭 알아야 할 생활비 절약법",
//author: "절약마스터",
//date: "1주 전",