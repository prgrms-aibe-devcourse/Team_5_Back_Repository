package com.team_5_back_repository.project.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;


public class SecurityUser extends User implements OAuth2User {
    @Getter
    private long id;
    @Getter
    private String nickname;

    public SecurityUser(long id,
                        String email,
                        String password,
                        String nickname,
                        Collection<? extends GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.id = id;
        this.nickname = nickname;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public String getName() {
        return getUsername(); //email 반환
    }
}
