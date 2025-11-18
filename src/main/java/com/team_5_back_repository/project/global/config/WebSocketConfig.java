package com.team_5_back_repository.project.global.config;

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

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    /**
     * WebSocket 메시지 인터셉터 등록
     * - 연결 시 헤더의 userId, nickname을 세션에 저장
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

                    if (userId != null && nickname != null) {
                        // 세션에 저장
                        accessor.getSessionAttributes().put("userId", userId);
                        accessor.getSessionAttributes().put("nickname", nickname);

                        System.out.println("✅ WebSocket 사용자 인증: userId=" + userId + ", nickname=" + nickname);
                    } else {
                        System.out.println("⚠️ WebSocket 연결 시 userId 또는 nickname 헤더가 없음");
                    }
                }

                return message;
            }
        });
    }
}