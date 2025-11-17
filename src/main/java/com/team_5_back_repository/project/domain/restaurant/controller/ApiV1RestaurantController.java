package com.team_5_back_repository.project.domain.restaurant.controller;

import com.team_5_back_repository.project.domain.restaurant.dto.RestaurantCreateRequest;
import com.team_5_back_repository.project.domain.restaurant.dto.RestaurantDto;
import com.team_5_back_repository.project.domain.restaurant.dto.RestaurantUpdateRequest;
import com.team_5_back_repository.project.domain.restaurant.service.RestaurantService;
import com.team_5_back_repository.project.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class ApiV1RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/nearby")
    public RsData<List<RestaurantDto>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "1.0") double radiusKm
    ) {
        List<RestaurantDto> list = restaurantService.getNearby(lat, lng, radiusKm);
        return new RsData<>("200-1", "OK", list);
    }

    @PostMapping
    public RsData<RestaurantDto> create(@RequestBody @Valid RestaurantCreateRequest req) {
        RestaurantDto dto = restaurantService.create(req);
        return new RsData<>("200-1", "CREATED", dto);
    }

    @GetMapping("/{id}")
    public RsData<RestaurantDto> get(@PathVariable Long id) {
        RestaurantDto dto = restaurantService.getById(id);
        return new RsData<>("200-1", "OK", dto);
    }

    @PutMapping("/{id}")
    public RsData<RestaurantDto> update(@PathVariable Long id, @RequestBody @Valid RestaurantUpdateRequest req) {
        RestaurantDto dto = restaurantService.update(id, req);
        return new RsData<>("200-1", "UPDATED", dto);
    }

    @DeleteMapping("/{id}")
    public RsData<Void> delete(@PathVariable Long id) {
        restaurantService.delete(id);
        return new RsData<>("200-1", "DELETED", null);
    }
}
