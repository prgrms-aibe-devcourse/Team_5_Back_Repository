package com.team_5_back_repository.project.domain.chatroom.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String senderNickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 메시지 타입
     * - TALK: 일반 채팅 메시지
     * - ENTER: 입장 메시지
     * - LEAVE: 퇴장 메시지
     * - KICK: 강퇴 메시지 (방장이 참여자를 강퇴)
     * - TRANSFER: 방장 권한 이양 메시지
     */
    public enum MessageType {
        TALK,      // 일반 메시지
        ENTER,     // 입장
        LEAVE,     // 퇴장
        KICK,      // 강퇴
        TRANSFER   // 권한 이양
    }
}