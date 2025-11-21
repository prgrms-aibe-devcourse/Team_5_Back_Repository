package com.team_5_back_repository.project.domain.review.controller;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.review.dto.ReviewDto;
import com.team_5_back_repository.project.domain.review.dto.ReviewRequest;
import com.team_5_back_repository.project.domain.review.service.ReviewService;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApiV1ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiV1ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private Rq rq;

    @Test
    @DisplayName("리뷰 목록 조회 OK")
    void list_ok() throws Exception {
        ReviewDto dto = new ReviewDto(1L, 10L, 100L, "tester", 5, "맛있음", LocalDateTime.now(), LocalDateTime.now());
        Mockito.when(reviewService.listByRestaurant(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/restaurants/{restaurantId}/reviews", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].rating").value(5));
    }

    @Test
    @DisplayName("리뷰 작성 OK")
    void create_ok() throws Exception {
        Member actor = Member.builder().id(100L).email("a@a.com").nickname("tester").build();
        Mockito.when(rq.getActor()).thenReturn(actor);

        String body = "{\"rating\":5,\"content\":\"맛있음\"}";

        ReviewDto dto = new ReviewDto(1L, 10L, 100L, "tester", 5, "맛있음", LocalDateTime.now(), LocalDateTime.now());
        Mockito.when(reviewService.create(eq(10L), eq(5), eq("맛있음"), eq(100L))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/reviews", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.data.memberNickname").value("tester"));
    }

    @Test
    @DisplayName("리뷰 수정 OK")
    void update_ok() throws Exception {
        Member actor = Member.builder().id(100L).email("a@a.com").nickname("tester").build();
        Mockito.when(rq.getActor()).thenReturn(actor);

        String body = "{\"rating\":4,\"content\":\"괜찮음\"}";
        ReviewDto dto = new ReviewDto(1L, 10L, 100L, "tester", 4, "괜찮음", LocalDateTime.now(), LocalDateTime.now());
        Mockito.when(reviewService.update(eq(1L), eq(4), eq("괜찮음"), eq(100L))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/reviews/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data.rating").value(4));
    }

    @Test
    @DisplayName("리뷰 삭제 OK")
    void delete_ok() throws Exception {
        Member actor = Member.builder().id(100L).email("a@a.com").nickname("tester").build();
        Mockito.when(rq.getActor()).thenReturn(actor);

        mockMvc.perform(delete("/api/v1/reviews/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("리뷰 삭제 성공"));

        Mockito.verify(reviewService).delete(1L, 100L);
    }
}
