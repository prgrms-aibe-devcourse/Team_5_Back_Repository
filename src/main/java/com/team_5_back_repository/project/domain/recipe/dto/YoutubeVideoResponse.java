package com.team_5_back_repository.project.domain.recipe.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class YoutubeVideoResponse {
    private final String videoId;
    private final String title;
    private final String thumbnailUrl;
    private final String embedUrl;
}

