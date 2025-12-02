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

import com.team_5_back_repository.project.domain.review.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

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
    public RestaurantDto create(RestaurantCreateRequest req, Long ownerId) {
        double lat = req.latitude();
        double lng = req.longitude();
        double tolKm = 0.05; // 50 meters
        double tolLat = tolKm / 111.32;
        double tolLng = tolKm / (111.32 * Math.cos(Math.toRadians(lat == 0.0 ? 0.0001 : lat)));
        double minLat = lat - tolLat;
        double maxLat = lat + tolLat;
        double minLng = lng - tolLng;
        double maxLng = lng + tolLng;

        List<Restaurant> candidates = restaurantRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng);
        for (Restaurant c : candidates) {
            double distKm = haversine(lat, lng, c.getLatitude(), c.getLongitude());
            if (distKm <= tolKm) {
                return RestaurantDto.of(c, distKm);
            }
        }

        Restaurant.RestaurantBuilder builder = Restaurant.builder()
                .name(req.name())
                .jibunAddress(req.jibunAddress())
                .roadAddress(req.roadAddress())
                .phone(req.phone())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .averageRating(0.0)
                .reviewCount(0L);

        if (ownerId != null) {
            builder.ownerId(ownerId);
        }

        Restaurant restaurant = builder.build();
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
        try {
            reviewRepository.deleteByRestaurantId(id);
        } catch (Exception e) {

        }
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

    public List<RestaurantDto> list(String keyword) {
        List<Restaurant> all = restaurantRepository.findAll();
        return all.stream()
                .filter(r -> keyword == null || keyword.isBlank() || r.getName().contains(keyword))
                .map(r -> RestaurantDto.of(r, 0.0))
                .collect(Collectors.toList());
    }
}
