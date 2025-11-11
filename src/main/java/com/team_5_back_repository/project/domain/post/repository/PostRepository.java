package com.team_5_back_repository.project.domain.post.repository;

import com.team_5_back_repository.project.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
