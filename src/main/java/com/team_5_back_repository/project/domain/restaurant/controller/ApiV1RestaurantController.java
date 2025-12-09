package com.team_5_back_repository.project.domain.restaurant.controller;

import com.team_5_back_repository.project.domain.restaurant.dto.RestaurantCreateRequest;
import com.team_5_back_repository.project.domain.restaurant.dto.RestaurantDto;
import com.team_5_back_repository.project.domain.restaurant.soloVote.service.SoloVoteService;
import com.team_5_back_repository.project.domain.restaurant.soloVote.dto.SoloVoteRequest;
import com.team_5_back_repository.project.domain.restaurant.soloVote.dto.SoloVoteResponse;
import com.team_5_back_repository.project.domain.restaurant.dto.RestaurantUpdateRequest;
import com.team_5_back_repository.project.domain.restaurant.service.RestaurantService;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import com.team_5_back_repository.project.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.List;
import java.util.Map;
import com.team_5_back_repository.project.global.cloudstorage.service.StorageService;
import com.team_5_back_repository.project.global.cloudstorage.entity.FileEntity;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class ApiV1RestaurantController {

    private static final Logger log = LoggerFactory.getLogger(ApiV1RestaurantController.class);

    private final RestaurantService restaurantService;
    private final SoloVoteService soloVoteService;
    private final Rq rq;
    private final StorageService storageService;

    @GetMapping("/nearby")
    public RsData<List<RestaurantDto>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "1.0") double radiusKm
    ) {
        List<RestaurantDto> list = restaurantService.getNearby(lat, lng, radiusKm);
        return new RsData<>("200-1", "OK", list);
    }

    @GetMapping
    public RsData<List<RestaurantDto>> list(@RequestParam(required = false) String keyword) {
        List<RestaurantDto> list = restaurantService.list(keyword);
        return new RsData<>("200-1", "OK", list);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public RsData<RestaurantDto> create(@RequestBody @Valid RestaurantCreateRequest req,
                                        @RequestParam(required = false, defaultValue = "false") boolean asImported) {
        var actor = rq.getActor();
        Long ownerId = null;
        if (!asImported) {
            ownerId = actor == null ? null : actor.getId();
        }
        try {
            log.debug("[ApiV1RestaurantController] create called asImported={} ownerId={} payload.name={} lat={} lng={}", asImported, ownerId, req.name(), req.latitude(), req.longitude());
        } catch (Exception e) {}
        RestaurantDto dto = restaurantService.create(req, ownerId, asImported);
        try {
            log.debug("[ApiV1RestaurantController] create result id={} name={}", dto.getId(), dto.getName());
        } catch (Exception e) {}
        return new RsData<>("200-1", "CREATED", dto);
    }

    @GetMapping("/{id}")
    public RsData<RestaurantDto> get(@PathVariable Long id) {
        RestaurantDto dto = restaurantService.getById(id);
        return new RsData<>("200-1", "OK", dto);
    }

    @GetMapping("/{id}/solo-vote")
    public RsData<SoloVoteResponse> getSoloVote(@PathVariable Long id) {
        var actor = rq.getActor();
        SoloVoteResponse resp = soloVoteService.getSummary(id, actor);
        return new RsData<>("200-1", "OK", resp);
    }

    @PostMapping("/{id}/solo-vote")
    @PreAuthorize("isAuthenticated()")
    public RsData<SoloVoteResponse> postSoloVote(@PathVariable Long id, @RequestBody SoloVoteRequest req) {
        var actor = rq.getActorFromDb();
        SoloVoteResponse resp = soloVoteService.vote(actor, id, req.getWillEatAlone());
        return new RsData<>("200-1", "OK", resp);
    }

    @DeleteMapping("/{id}/solo-vote")
    @PreAuthorize("isAuthenticated()")
    public RsData<SoloVoteResponse> deleteSoloVote(@PathVariable Long id) {
        var actor = rq.getActorFromDb();
        SoloVoteResponse resp = soloVoteService.delete(actor, id);
        return new RsData<>("200-1", "OK", resp);
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

    @PostMapping("/{id}/image")
    @PreAuthorize("isAuthenticated()")
    public RsData<RestaurantDto> uploadImage(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
        var actor = rq.getActorFromDb();
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("image required");
        }
        try {
            FileEntity fe = storageService.upload(image, "restaurants");
            String publicUrl = fe.getImgUrl();
            RestaurantDto dto = restaurantService.setImage(id, publicUrl, actor.getId());
            return new RsData<>("200-1", "OK", dto);
        } catch (Exception e) {
            throw new RuntimeException("failed to store image", e);
        }
    }

    @PostMapping("/{id}/image/url")
    @PreAuthorize("isAuthenticated()")
    public RsData<RestaurantDto> setImageUrl(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var actor = rq.getActorFromDb();
        String imageUrl = body.get("imageUrl");
        if (imageUrl == null || imageUrl.isBlank()) throw new IllegalArgumentException("imageUrl required");
        RestaurantDto dto = restaurantService.setImage(id, imageUrl, actor.getId());
        return new RsData<>("200-1", "OK", dto);
    }
}
