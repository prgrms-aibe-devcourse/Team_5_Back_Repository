package com.team_5_back_repository.project.domain.member.controller;

import com.team_5_back_repository.project.domain.member.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/region")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @GetMapping("/search")
    public ResponseEntity<?> searchRegion(@RequestParam String query,
                                          @RequestParam Integer page,
                                          @RequestParam (defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(regionService.searchRegion(query, page, pageSize));
    }
}
