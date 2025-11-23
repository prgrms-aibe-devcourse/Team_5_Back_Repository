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
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;  // 채팅방 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type;  // 소모임 or 공동구매

    @Column(nullable = false)
    private Long creatorId;  // 채팅방 생성자 ID (방장)

    @Column(nullable = false)
    private String region;  // 지역 (동 단위)

    @Column(length = 500)
    private String description;  // 채팅방 설명

    @Column(nullable = false)
    private Integer maxParticipants;  // 최대 인원

    @Column(nullable = false)
    private Integer currentParticipants = 0;  // 현재 인원

    @Column(nullable = false)
    private Boolean isActive = true;  // 활성화 여부

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatRoom(String name, ChatRoomType type, Long creatorId, String region,
                    String description, Integer maxParticipants) {
        this.name = name;
        this.type = type;
        this.creatorId = creatorId;
        this.region = region;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = 1;  // 생성자 포함
    }

    // 참여자 증가
    public void increaseParticipant() {
        if (this.currentParticipants >= this.maxParticipants) {
            throw new IllegalStateException("채팅방 인원이 가득 찼습니다.");
        }
        this.currentParticipants++;
    }

    // 참여자 감소
    public void decreaseParticipant() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    // 방장 변경
    public void changeCreator(Long newCreatorId) {
        this.creatorId = newCreatorId;
    }

    // 채팅방 비활성화
    public void deactivate() {
        this.isActive = false;
    }
}