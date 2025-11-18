package com.team_5_back_repository.project.domain.post.repository;

import com.team_5_back_repository.project.domain.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag,Long> {
    Optional<Tag> findTagByName(String name);
}
