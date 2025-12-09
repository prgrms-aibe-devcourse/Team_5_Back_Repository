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
                        "https://resreqonelifefront.vercel.app",
                        "https://onelife.mwan.site")  // Next.js 개발 서버
                .withSockJS();  // SockJS 폴백 활성화
    }

    /**
     * 클라이언트 인바운드 채널 설정 (인증 처리)
     * CONNECT 시점에 사용자 인증 및 세션 저장
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // CONNECT 시 헤더에서 사용자 정보 추출
                    String userId = accessor.getFirstNativeHeader("userId");
                    String nickname = accessor.getFirstNativeHeader("nickname");
                    String accessToken = accessor.getFirstNativeHeader("accessToken");

                    if (userId != null && nickname != null) {
                        // 세션에 저장
                        accessor.getSessionAttributes().put("userId", userId);
                        accessor.getSessionAttributes().put("nickname", nickname);

                        log.info("✅ WebSocket 사용자 인증: userId={}, nickname={}", userId, nickname);

                        // JWT 토큰 검증 (선택 사항)
                        if (accessToken != null) {
                            try {
                                Map<String, Object> payload = memberService.payload(accessToken);
                                if (payload != null) {
                                    long tokenUserId = ((Number) payload.get("id")).longValue();
                                    if (tokenUserId != Long.parseLong(userId)) {
                                        throw new IllegalStateException("토큰의 사용자 ID가 일치하지 않습니다.");
                                    }
                                    log.info("✅ JWT 토큰 검증 성공: userId={}", tokenUserId);
                                }
                            } catch (Exception e) {
                                log.warn("⚠️ JWT 토큰 검증 실패: {}", e.getMessage());
                                // 필요시 연결 거부
                                // throw new IllegalStateException("유효하지 않은 토큰입니다.");
                            }
                        }
                    } else {
                        log.warn("⚠️ WebSocket 연결 시 userId 또는 nickname 헤더가 없음");
                    }
                }

                return message;
            }
        });
    }
}
