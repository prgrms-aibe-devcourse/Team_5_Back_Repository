package com.team_5_back_repository.project.global.config;

import com.team_5_back_repository.project.domain.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final MemberService memberService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "https://resreqonelifefront.vercel.app",
                        "https://onelife.mwan.site")
                .setAllowedOrigins("*")
                .withSockJS()
                .setSessionCookieNeeded(true);  // ★ SockJS가 쿠키 전송 허용
    }

    /**
     * CONNECT 시점에 쿠키에서 accessToken 찾아 JWT 인증
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // ★ SockJS의 초기 HTTP 요청을 가져오기
                    HttpServletRequest request =
                            (HttpServletRequest) accessor.getHeader("simpHttpRequest");

                    if (request == null) {
                        log.error("❌ simpHttpRequest 없음 → 쿠키 인증 불가");
                        throw new IllegalStateException("WebSocket 연결 실패: 요청 정보 없음");
                    }

                    // ★ 쿠키에서 accessToken 꺼내기
                    String token = extractTokenFromCookie(request);

                    if (token == null) {
                        log.error("❌ accessToken 쿠키 없음 → 인증 실패");
                        throw new IllegalStateException("로그인이 필요합니다");
                    }

                    try {
                        // JWT 파싱
                        Map<String, Object> payload = memberService.payload(token);

                        Long userId = ((Number) payload.get("id")).longValue();
                        String nickname = (String) payload.get("nickname");

                        // ★ WebSocket 세션 저장
                        accessor.getSessionAttributes().put("userId", userId);
                        accessor.getSessionAttributes().put("nickname", nickname);

                        log.info("✅ WebSocket 쿠키 인증 성공: userId={}, nickname={}", userId, nickname);

                    } catch (Exception e) {
                        log.error("❌ JWT 인증 실패: {}", e.getMessage());
                        throw new IllegalStateException("WebSocket 인증 실패: " + e.getMessage());
                    }
                }

                return message;
            }

            /**
             * 🍪 쿠키에서 accessToken 가져오기
             */
            private String extractTokenFromCookie(HttpServletRequest request) {
                Cookie[] cookies = request.getCookies();
                if (cookies == null) return null;

                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
                return null;
            }
        });
    }
}