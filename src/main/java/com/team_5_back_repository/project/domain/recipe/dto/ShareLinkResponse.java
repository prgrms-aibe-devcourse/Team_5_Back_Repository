package com.team_5_back_repository.project.domain.recipe.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareLinkResponse {
    private String shareToken;
    private String shareUrl;
}

