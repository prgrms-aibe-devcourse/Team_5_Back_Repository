package com.team_5_back_repository.project.domain.member.controller;

import com.team_5_back_repository.project.domain.member.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/region")
@Tag(name = "Region Controller", description = "VWorld API 에서 지역 검색하는 기능 제공")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @GetMapping("/search")
    @Operation(summary = "지역 검색", description = "특정 문자열이 포함된 지역을 페이징처리하여 검색합니다.")
    public ResponseEntity<?> searchRegion(@RequestParam String query,
                                          @RequestParam Integer page,
                                          @RequestParam (defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(regionService.searchRegion(query, page, pageSize));
    }
}
