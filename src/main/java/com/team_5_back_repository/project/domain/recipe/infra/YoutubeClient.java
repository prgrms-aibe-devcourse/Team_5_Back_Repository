package com.team_5_back_repository.project.domain.recipe.infra;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class YoutubeClient {

    private static final Logger log = LoggerFactory.getLogger(YoutubeClient.class);
    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3/search";

    private final RestTemplate restTemplate;

    @Value("${youtube.api-key:}")
    private String apiKey;

    public Optional<YoutubeSearchResult> searchFirstVideoByTitle(String title) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Youtube API key is not configured.");
            return Optional.empty();
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("part", "snippet");
        params.add("type", "video");
        params.add("maxResults", "1");
        String query = title == null ? "" : title.trim();
        if (!query.isBlank() && !query.contains("레시피")) {
            query = query + " 레시피";
        }
        params.add("q", query);
        params.add("key", apiKey);

        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParams(params)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                JsonNode.class
        );

        JsonNode root = response.getBody();
        if (root == null || !root.has("items") || root.get("items").isEmpty()) {
            return Optional.empty();
        }

        JsonNode item = root.get("items").get(0);
        JsonNode idNode = item.path("id");
        JsonNode snippetNode = item.path("snippet");

        String videoId = idNode.path("videoId").asText(null);
        String videoTitle = snippetNode.path("title").asText(null);
        String thumbnailUrl = snippetNode.path("thumbnails").path("high").path("url").asText(null);

        if (videoId == null || videoId.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new YoutubeSearchResult(videoId, videoTitle, thumbnailUrl));
    }

    public record YoutubeSearchResult(String videoId, String title, String thumbnailUrl) {
    }
}