package com.team_5_back_repository.project.domain.member.entity;

import com.team_5_back_repository.project.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region extends BaseEntity {
    @Id
    private Long id;

    private String fullName;

    private String shortName;
}
