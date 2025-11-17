package com.team_5_back_repository.project.domain.restaurant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team_5_back_repository.project.domain.restaurant.entity.Restaurant;
import com.team_5_back_repository.project.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiV1RestaurantControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
    }

    @Test
    @DisplayName("식당 생성 후 단건 조회")
    void create_and_get() throws Exception {
        String reqJson = "{" +
                "\"name\":\"테스트식당\"," +
                "\"jibunAddress\":\"서울시 중구 태평로1가\"," +
                "\"roadAddress\":\"서울특별시 중구 세종대로 110\"," +
                "\"phone\":\"02-000-0000\"," +
                "\"latitude\":37.5665," +
                "\"longitude\":126.9780" +
                "}";

        String createContent = mockMvc.perform(post("/api/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("CREATED"))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode createNode = objectMapper.readTree(createContent);
        Long id = createNode.get("data").get("id").asLong();

        mockMvc.perform(get("/api/v1/restaurants/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("테스트식당"))
                .andExpect(jsonPath("$.data.phone").value("02-000-0000"))
                .andExpect(jsonPath("$.data.jibunAddress").value("서울시 중구 태평로1가"))
                .andExpect(jsonPath("$.data.roadAddress").value("서울특별시 중구 세종대로 110"));
    }

    @Test
    @DisplayName("식당 수정 및 삭제 후 404 확인")
    void update_and_delete() throws Exception {
        Restaurant saved = restaurantRepository.save(Restaurant.builder()
                .name("원본식당")
                .phone("02-123-4567")
                .jibunAddress("서울시 어딘가 1-1")
                .roadAddress("서울시 어딘가로 1")
                .latitude(37.5665)
                .longitude(126.9780)
                .averageRating(0.0)
                .reviewCount(0L)
                .build());

        String updateJson = "{" +
                "\"name\":\"수정식당\"," +
                "\"jibunAddress\":\"서울시 어딘가 2-2\"," +
                "\"roadAddress\":\"서울시 어딘가로 2\"," +
                "\"phone\":\"02-765-4321\"," +
                "\"latitude\":37.5650," +
                "\"longitude\":126.9900" +
                "}";

        mockMvc.perform(put("/api/v1/restaurants/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("UPDATED"))
                .andExpect(jsonPath("$.data.name").value("수정식당"))
                .andExpect(jsonPath("$.data.phone").value("02-765-4321"));

        mockMvc.perform(delete("/api/v1/restaurants/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("DELETED"));

        mockMvc.perform(get("/api/v1/restaurants/{id}", saved.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-001"));
    }

    @Test
    @DisplayName("내 주변 식당 거리순 정렬 및 반경필터")
    void nearby_sorted_and_filtered() throws Exception {
        double baseLat = 37.5665;
        double baseLng = 126.9780;

        Restaurant r1 = Restaurant.builder()
                .name("가까운식당")
                .phone("02-111-1111")
                .jibunAddress("주소1")
                .roadAddress("도로명1")
                .latitude(37.5668)
                .longitude(126.9783)
                .averageRating(0.0)
                .reviewCount(0L)
                .build();
        Restaurant r2 = Restaurant.builder()
                .name("조금먼식당")
                .phone("02-222-2222")
                .jibunAddress("주소2")
                .roadAddress("도로명2")
                .latitude(37.5740)
                .longitude(126.9830)
                .averageRating(0.0)
                .reviewCount(0L)
                .build();
        Restaurant r3 = Restaurant.builder()
                .name("반경밖식당")
                .phone("02-333-3333")
                .jibunAddress("주소3")
                .roadAddress("도로명3")
                .latitude(37.6000)
                .longitude(126.9900)
                .averageRating(0.0)
                .reviewCount(0L)
                .build();

        restaurantRepository.save(r1);
        restaurantRepository.save(r2);
        restaurantRepository.save(r3);

        String res = mockMvc.perform(get("/api/v1/restaurants/nearby")
                        .param("lat", String.valueOf(baseLat))
                        .param("lng", String.valueOf(baseLng))
                        .param("radiusKm", "2.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("가까운식당"))
                .andExpect(jsonPath("$.data[1].name").value("조금먼식당"))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(res).get("data");
        double d0 = node.get(0).get("distanceKm").asDouble();
        double d1 = node.get(1).get("distanceKm").asDouble();
        assertThat(d0).isLessThan(d1);
        assertThat(d0).isGreaterThanOrEqualTo(0.0);
    }
}
