package com.team_5_back_repository.project.domain.chatroom.service;

import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisChatService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHAT_ROOM_PREFIX = "chatroom:";
    private static final String MESSAGE_KEY_SUFFIX = ":messages";
    private static final long MESSAGE_EXPIRE_TIME = 24; // 24시간

    /**
     * Redis에 메시지 저장 (최근 100개만 유지)
     */
    public void saveMessage(Long chatRoomId, ChatMessageResponse message) {
        String key = CHAT_ROOM_PREFIX + chatRoomId + MESSAGE_KEY_SUFFIX;

        try {
            // List의 왼쪽에 추가 (최신 메시지가 앞에 오도록)
            redisTemplate.opsForList().leftPush(key, message);

            // 최근 100개만 유지 (0~99번째만 남기고 나머지 삭제)
            redisTemplate.opsForList().trim(key, 0, 99);

            // 24시간 후 자동 삭제
            redisTemplate.expire(key, MESSAGE_EXPIRE_TIME, TimeUnit.HOURS);

            log.info("Redis에 메시지 저장: chatRoomId={}, messageId={}", chatRoomId, message.getId());
        } catch (Exception e) {
            log.error("Redis 메시지 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * Redis에서 최근 메시지 조회
     */
    public List<ChatMessageResponse> getRecentMessages(Long chatRoomId, int count) {
        String key = CHAT_ROOM_PREFIX + chatRoomId + MESSAGE_KEY_SUFFIX;

        try {
            List<Object> messages = redisTemplate.opsForList().range(key, 0, count - 1);

            if (messages == null || messages.isEmpty()) {
                return new ArrayList<>();
            }

            return messages.stream()
                    .filter(obj -> obj instanceof ChatMessageResponse)
                    .map(obj -> (ChatMessageResponse) obj)
                    .toList();

        } catch (Exception e) {
            log.error("Redis 메시지 조회 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 채팅방 참여자 추가 (Redis Set 사용)
     */
    public void addParticipant(Long chatRoomId, Long memberId) {
        String key = CHAT_ROOM_PREFIX + chatRoomId + ":participants";
        redisTemplate.opsForSet().add(key, memberId);
    }

    /**
     * 채팅방 참여자 제거
     */
    public void removeParticipant(Long chatRoomId, Long memberId) {
        String key = CHAT_ROOM_PREFIX + chatRoomId + ":participants";
        redisTemplate.opsForSet().remove(key, memberId);
    }

    /**
     * 채팅방 현재 접속자 수 조회
     */
    public Long getParticipantCount(Long chatRoomId) {
        String key = CHAT_ROOM_PREFIX + chatRoomId + ":participants";
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 채팅방의 모든 접속자 조회
     */
    public Set<Object> getParticipants(Long chatRoomId) {
        String key = CHAT_ROOM_PREFIX + chatRoomId + ":participants";
        return redisTemplate.opsForSet().members(key);
    }
}