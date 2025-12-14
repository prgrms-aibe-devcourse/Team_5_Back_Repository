package com.team_5_back_repository.project.domain.recipe.service;

import com.team_5_back_repository.project.domain.recipe.dto.YoutubeVideoResponse;
import com.team_5_back_repository.project.domain.recipe.infra.YoutubeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class YoutubeService {

    private final YoutubeClient youtubeClient;

    public YoutubeVideoResponse searchByTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("제목이 비어있습니다.");
        }

        var result = youtubeClient.searchFirstVideoByTitle(title.trim())
                .orElseThrow(() -> new IllegalArgumentException("관련 유튜브 영상을 찾을 수 없습니다."));

        String embedUrl = "https://www.youtube.com/embed/" + result.videoId();

        return YoutubeVideoResponse.builder()
                .videoId(result.videoId())
                .title(result.title())
                .thumbnailUrl(result.thumbnailUrl())
                .embedUrl(embedUrl)
                .build();
    }
}