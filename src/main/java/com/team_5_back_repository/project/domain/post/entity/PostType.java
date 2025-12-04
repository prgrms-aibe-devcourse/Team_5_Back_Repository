package com.team_5_back_repository.project.domain.post.entity;

public enum PostType {
    ALL,
    FREE,
    HOT,
    INFO,
    TIP;

    public boolean isAdminOnly() {
        return this == INFO;
    }
}
