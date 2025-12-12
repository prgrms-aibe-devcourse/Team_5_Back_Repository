package com.team_5_back_repository.project.domain.recipe.controller;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.recipe.dto.RecipeGenerateRequest;
import com.team_5_back_repository.project.domain.recipe.dto.RecipeResponse;
import com.team_5_back_repository.project.domain.recipe.dto.RecipeSaveRequest;
import com.team_5_back_repository.project.domain.recipe.dto.ShareLinkResponse;
import com.team_5_back_repository.project.domain.recipe.dto.YoutubeVideoResponse;
import com.team_5_back_repository.project.domain.recipe.service.RecipeService;
import com.team_5_back_repository.project.domain.recipe.service.YoutubeService;
import com.team_5_back_repository.project.global.rsData.RsData;
import com.team_5_back_repository.project.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;
    private final YoutubeService youtubeService;

    // AI 레시피 생성 (회원/비회원)
    @PostMapping("/generate")
    public RsData<List<RecipeResponse>> generate(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody RecipeGenerateRequest req) throws Exception {
        Member member = null;

        // 회원이면 member 생성
        if (user != null) {
            member = Member.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .email(user.getUsername())
                    .build();
        }

        List<RecipeResponse> recipes = recipeService.generate(member, req);

        return new RsData<>(
                "200-1",
                "AI 레시피 생성 성공",
                recipes
        );
    }

    // 회원이 AI 레시피 저장
    @PostMapping
    public RsData<Long> save(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody RecipeSaveRequest req) throws Exception {
        if (user == null) {
            return new RsData<>("401-1", "로그인이 필요합니다.", null);
        }

        Member member = Member.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getUsername())
                .build();

        long savedId = recipeService.save(member, req);

        return new RsData<>(
                "200-2",
                "레시피 저장 성공",
                savedId
        );
    }

    // 회원이 생성한 모든 AI 레시피 목록 조회
    @GetMapping("/generated")
    public RsData<List<RecipeResponse>> generated(
            @AuthenticationPrincipal SecurityUser user) throws Exception {

        if (user == null) {
            return new RsData<>("401-1", "로그인 후 이용 가능합니다.", null);
        }

        List<RecipeResponse> list = recipeService.getGeneratedRecipes(user.getId());

        return new RsData<>("200-3", "생성한 레시피 목록 조회 성공", list);
    }

    // 회원이 저장한 AI 레시피만 목록 조회
    @GetMapping("/saved")
    public RsData<List<RecipeResponse>> saved(
            @AuthenticationPrincipal SecurityUser user) throws Exception {

        if (user == null) {
            return new RsData<>("401-1", "로그인 후 이용 가능합니다.", null);
        }

        List<RecipeResponse> list = recipeService.getSavedRecipes(user.getId());

        return new RsData<>("200-4", "저장한 레시피 목록 조회 성공", list);
    }

    // 회원이 저장한 레시피 삭제
    @DeleteMapping("/{id}")
    public RsData<Void> delete(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long id) {
        if (user == null) {
            return new RsData<>("401-1", "로그인이 필요합니다.", null);
        }

        recipeService.delete(id, user.getId());

        return new RsData<>(
                "200-5",
                "레시피 삭제 성공",
                null
        );
    }

    // 공유 링크 생성
    @PostMapping("/{id}/share")
    public RsData<ShareLinkResponse> createShareLink(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long id,
            @RequestParam(required = false) String frontendBaseUrl) {
        if (user == null) {
            return new RsData<>("401-1", "로그인이 필요합니다.", null);
        }

        // frontendBaseUrl이 없으면 기본값 사용
        if (frontendBaseUrl == null || frontendBaseUrl.isEmpty()) {
            frontendBaseUrl = "http://localhost:3000"; // 기본값
        }

        ShareLinkResponse shareLink = recipeService.createShareLink(id, user.getId(), frontendBaseUrl);

        return new RsData<>(
                "200-6",
                "공유 링크 생성 성공",
                shareLink
        );
    }

    // 공유 링크로 레시피 조회 (공개 API)
    @GetMapping("/shared/{shareToken}")
    public RsData<RecipeResponse> getRecipeByShareToken(
            @PathVariable String shareToken) throws Exception {
        RecipeResponse recipe = recipeService.getRecipeByShareToken(shareToken);

        return new RsData<>(
                "200-7",
                "공유 레시피 조회 성공",
                recipe
        );
    }

    // 레시피 제목으로 관련 유튜브 영상 검색
    @GetMapping("/youtube")
    public RsData<YoutubeVideoResponse> searchYoutubeByTitle(@RequestParam String title) {
        YoutubeVideoResponse video = youtubeService.searchByTitle(title);

        return new RsData<>(
                "200-8",
                "유튜브 영상 검색 성공",
                video
        );
    }
}