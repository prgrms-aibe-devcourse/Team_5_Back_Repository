package com.team_5_back_repository.project.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/h2-console/**") // H2 콘솔은 CSRF 무시
            )
            .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin()) // iframe 허용
            )
            .authorizeHttpRequests(
                    auth -> auth
                            .requestMatchers("favicon.ico").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .anyRequest().permitAll()
            );
        return http.build();
    }
}