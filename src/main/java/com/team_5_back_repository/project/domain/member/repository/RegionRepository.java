package com.team_5_back_repository.project.domain.member.repository;

import com.team_5_back_repository.project.domain.member.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

}
