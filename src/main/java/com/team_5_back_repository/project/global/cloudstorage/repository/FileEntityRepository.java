package com.team_5_back_repository.project.global.cloudstorage.repository;

import com.team_5_back_repository.project.global.cloudstorage.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
}
