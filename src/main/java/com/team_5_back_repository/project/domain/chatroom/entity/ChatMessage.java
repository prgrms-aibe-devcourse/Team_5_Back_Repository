package com.team_5_back_repository.project.domain.chatroom.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId;  // 채팅방 ID

    @Column(nullable = false)
    private Long senderId;  // 발신자 ID

    @Column(nullable = false)
    private String senderNickname;  // 발신자 닉네임

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;  // 메시지 타입

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // 메시지 내용

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatMessage(Long chatRoomId, Long senderId, String senderNickname,
                       MessageType type, String content) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.type = type;
        this.content = content;
    }

    /**
     * 메시지 타입
     * - ENTER: 입장 메시지
     * - TALK: 일반 대화
     * - LEAVE: 퇴장 메시지
     */
    public enum MessageType {
        ENTER, TALK, LEAVE
    }
}