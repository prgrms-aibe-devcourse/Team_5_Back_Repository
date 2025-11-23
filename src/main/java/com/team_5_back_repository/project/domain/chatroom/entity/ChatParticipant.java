package com.team_5_back_repository.project.domain.chatroom.entity;

import com.team_5_back_repository.project.domain.member.entity.Member;
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
@Table(name = "chat_participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "member_id"}))
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "chat_room_id")
    private Long chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Boolean isCreator = false;  // 방장 여부

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public ChatParticipant(Long chatRoomId, Member member, Boolean isCreator) {
        this.chatRoomId = chatRoomId;
        this.member = member;
        this.isCreator = isCreator;
    }

    // 편의 메서드 추가 (기존 코드 호환성 유지)
    public Long getMemberId() {
        return member != null ? member.getId() : null;
    }

    public String getMemberNickname() {
        return member != null ? member.getNickname() : null;
    }
}