package com.team_5_back_repository.project.domain.chatroom.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 채팅방 강퇴 이력 엔티티
 * - 강퇴당한 사용자가 해당 채팅방에 다시 참여하지 못하도록 관리
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "chat_room_ban",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "member_id"})
)
public class ChatRoomBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 채팅방 ID
     */
    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    /**
     * 강퇴당한 회원 ID
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * 강퇴 실행자 ID (방장)
     */
    @Column(name = "kicked_by", nullable = false)
    private Long kickedBy;

    /**
     * 강퇴 사유
     */
    @Column(length = 500)
    private String reason;

    /**
     * 강퇴 시각
     */
    @CreatedDate
    @Column(name = "kicked_at", nullable = false, updatable = false)
    private LocalDateTime kickedAt;
}