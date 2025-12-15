package com.team_5_back_repository.project.global.security;

import com.team_5_back_repository.project.domain.member.service.MemberService;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberService memberService;
    private final Rq rq;
    private final CustomOAuth2LoginSuccessHandler customOAuth2LoginSuccessHandler;
    private final CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() {
        return new CustomAuthenticationFilter(memberService, rq);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성
            .logout(AbstractHttpConfigurer::disable) // 로그아웃 기능 비활성화
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2Login -> oauth2Login
                        .successHandler(customOAuth2LoginSuccessHandler)
                        .authorizationEndpoint(
                                authorizationEndpoint -> authorizationEndpoint
                                        .authorizationRequestResolver(customOAuth2AuthorizationRequestResolver)
                        )
                )
            .addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                .frameOptions(
                        HeadersConfigurer.FrameOptionsConfig::sameOrigin
                    )// h2-console 화면을 사용하기 위해 sameOrigin 옵션 설정
            )
            .authorizeHttpRequests(
                    auth -> auth
                            .requestMatchers("favicon.ico").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/api/chatrooms/**").permitAll()  // 채팅 API 모두 허용
                            .requestMatchers("/ws/**").permitAll()              // WebSocket 허용
                            .requestMatchers("/api/**").permitAll()             // 개발 중 모든 API 허용
                            .requestMatchers("/api/v1/region/search").permitAll() // 지역 검색 허용
                            .anyRequest().permitAll()
            );
        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정
        configuration.setAllowedOrigins(
                List.of("http://localhost:3000",
                        "https://www.onelife.mwan.site",
                        "https://onelife.mwan.site"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));

        // 자격 증명 허용 설정
        configuration.setAllowCredentials(true);

        // 허용할 헤더 설정
        configuration.setAllowedHeaders(List.of("*"));
        
        //읽기 허용할 헤더 설정
        configuration.setExposedHeaders(List.of("Authorization"));

        // CORS 설정을 소스에 등록
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}