package com.team_5_back_repository.project.domain.review.controller;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.domain.review.dto.ReviewDto;
import com.team_5_back_repository.project.domain.review.dto.ReviewRequest;
import com.team_5_back_repository.project.domain.review.service.ReviewService;
import com.team_5_back_repository.project.global.rsData.RsData;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiV1ReviewController {

    private final ReviewService reviewService;
    private final Rq rq;

    @GetMapping("/restaurants/{restaurantId}/reviews")
    public RsData<List<ReviewDto>> list(@PathVariable Long restaurantId) {
        List<ReviewDto> list = reviewService.listByRestaurant(restaurantId);
        return new RsData<>("200-1", "OK", list);

    }

    @PostMapping("/restaurants/{restaurantId}/reviews")
    public RsData<ReviewDto> create(
            @PathVariable Long restaurantId,
            @RequestBody @Valid ReviewRequest req
    ) {
        Member actor = rq.getActor();
        if (actor == null) throw new MemberException("401-1", "로그인 후 이용해주세요.");
        ReviewDto dto = reviewService.create(restaurantId, req.getRating(), req.getContent(), actor.getId());
        return new RsData<>("201-1", "리뷰 작성 성공", dto);
    }

    @PutMapping("/reviews/{id}")
    public RsData<ReviewDto> update(
            @PathVariable Long id,
            @RequestBody @Valid ReviewRequest req
    ) {
        Member actor = rq.getActor();
        if (actor == null) throw new MemberException("401-1", "로그인 후 이용해주세요.");
        ReviewDto dto = reviewService.update(id, req.getRating(), req.getContent(), actor.getId());
        return new RsData<>("200-1", "리뷰 수정 성공", dto);
    }

    @DeleteMapping("/reviews/{id}")
    public RsData<Void> delete(@PathVariable Long id) {
        Member actor = rq.getActor();
        if (actor == null) throw new MemberException("401-1", "로그인 후 이용해주세요.");
        reviewService.delete(id, actor.getId());
        return new RsData<>("200-1", "리뷰 삭제 성공", null);
    }
}
