package com.team_5_back_repository.project.domain.restaurant.service;

import com.team_5_back_repository.project.domain.restaurant.dto.RestaurantDto;
import com.team_5_back_repository.project.domain.restaurant.dto.RestaurantCreateRequest;
import com.team_5_back_repository.project.domain.restaurant.dto.RestaurantUpdateRequest;
import com.team_5_back_repository.project.domain.restaurant.entity.Restaurant;
import com.team_5_back_repository.project.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<RestaurantDto> getNearby(double lat, double lng, double radiusKm) {
        double latDelta = radiusKm / 111.32;
        double lngDelta = radiusKm / (111.32 * Math.cos(Math.toRadians(lat)));
        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLng = lng - lngDelta;
        double maxLng = lng + lngDelta;

        List<Restaurant> candidates = restaurantRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng);

        return candidates.stream()
                .map(r -> {
                    double distanceKm = haversine(lat, lng, r.getLatitude(), r.getLongitude());
                    return new Object[]{r, distanceKm};
                })
                .filter(arr -> (double) arr[1] <= radiusKm)
                .sorted(Comparator.comparingDouble(arr -> (double) arr[1]))
                .map(arr -> RestaurantDto.of((Restaurant) arr[0], (double) arr[1]))
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantDto create(RestaurantCreateRequest req) {
        Restaurant restaurant = Restaurant.builder()
                .name(req.name())
                .jibunAddress(req.jibunAddress())
                .roadAddress(req.roadAddress())
                .phone(req.phone())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .averageRating(0.0)
                .reviewCount(0L)
                .build();
        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantDto.of(saved, 0.0);
    }

    public RestaurantDto getById(Long id) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("restaurant not found"));
        return RestaurantDto.of(r, 0.0);
    }

    @Transactional
    public RestaurantDto update(Long id, RestaurantUpdateRequest req) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("restaurant not found"));
        r.update(req.name(), req.phone(), req.jibunAddress(), req.roadAddress(), req.latitude(), req.longitude());
        return RestaurantDto.of(r, 0.0);
    }

    @Transactional
    public void delete(Long id) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("restaurant not found"));
        restaurantRepository.delete(r);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
