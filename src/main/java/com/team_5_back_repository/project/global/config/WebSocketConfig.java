package com.team_5_back_repository.project.global.config;

import com.team_5_back_repository.project.domain.member.service.MemberService;
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

    /**
     * 메시지 브로커 설정
     * - /topic: 1:N 브로드캐스트 (채팅방 전체)
     * - /queue: 1:1 개인 메시지 (에러 메시지 등)
     * - /app: 클라이언트에서 서버로 메시지 전송 시 prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * WebSocket 엔드포인트 등록
     * - 클라이언트는 ws://localhost:8080/ws 로 연결
     * - SockJS 폴백 지원 (WebSocket 미지원 브라우저 대응)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "https://www.onelife.mwan.site",
                        "https://onelife.mwan.site")
                .withSockJS();
    }

    /**
     * JWT 토큰 기반 인증 인터셉터
     * CONNECT 시점에 JWT 토큰 검증 후 세션에 사용자 정보 저장
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        log.error("❌ WebSocket 연결 거부: Authorization 헤더 없음");
                        throw new IllegalStateException("인증 토큰이 필요합니다.");
                    }

                    try {
                        String token = authHeader.substring(7); // "Bearer " 제거
                        Map<String, Object> payload = memberService.payload(token);

                        if (payload == null) {
                            log.error("❌ WebSocket 연결 거부: 유효하지 않은 토큰");
                            throw new IllegalStateException("유효하지 않은 토큰입니다.");
                        }

                        // JWT에서 추출한 정보만 사용 (클라이언트가 임의로 보낸 값 무시)
                        Long userId = ((Number) payload.get("id")).longValue();
                        String nickname = (String) payload.get("nickname");

                        // 세션에 저장
                        accessor.getSessionAttributes().put("userId", userId);
                        accessor.getSessionAttributes().put("nickname", nickname);

                        log.info("✅ WebSocket 연결 인증 성공: userId={}, nickname={}", userId, nickname);

                    } catch (Exception e) {
                        log.error("❌ WebSocket 인증 실패: {}", e.getMessage());
                        throw new IllegalStateException("인증에 실패했습니다: " + e.getMessage());
                    }
                }

                return message;
            }
        });
    }
}