package com.team_5_back_repository.project.domain.restaurant.entity;

import com.team_5_back_repository.project.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(indexes = {
        @Index(name = "idx_restaurant_lat_lng", columnList = "latitude, longitude")
})
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false)
    private String jibunAddress;

    @Column(nullable = false)
    private String roadAddress;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private double averageRating;

    @Column(nullable = false)
    private long reviewCount;

    public void applyNewRating(int rating) {
        double total = averageRating * reviewCount + rating;
        reviewCount += 1;
        averageRating = total / reviewCount;
    }

    public void applyUpdatedRating(int oldRating, int newRating) {
        if (reviewCount <= 0) {
            reviewCount = 0;
            averageRating = 0.0;
            return;
        }
        double total = averageRating * reviewCount - oldRating + newRating;
        averageRating = total / reviewCount;
    }

    public void applyDeletedRating(int oldRating) {
        if (reviewCount <= 1) {
            reviewCount = 0;
            averageRating = 0.0;
            return;
        }
        double total = averageRating * reviewCount - oldRating;
        reviewCount -= 1;
        averageRating = total / reviewCount;
    }

    public void update(String name, String phone, String jibunAddress, String roadAddress,
                       double latitude, double longitude) {
        this.name = name;
        this.phone = phone;
        this.jibunAddress = jibunAddress;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
