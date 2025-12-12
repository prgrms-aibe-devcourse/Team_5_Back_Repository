package com.team_5_back_repository.project.domain.groupbuying.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "group_buying_post")
public class GroupBuyingPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "chat_room_id")
    private Long chatRoomId;  // 1:1 연결된 채팅방 ID

    @Column(nullable = false)
    private Long creatorId;  // 작성자 ID

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "category")
    private String category;

    @Column(nullable = false)
    private Integer targetAmount;  // 목표 금액

    @Column(nullable = false)
    private Integer currentAmount = 0;  // 현재 모금액

    @Column(nullable = false)
    private Integer targetParticipants;  // 목표 인원

    @Column(nullable = false)
    private Integer currentParticipants = 1;  // 현재 인원 (작성자 포함)

    @Column(nullable = false)
    private LocalDateTime deadline;  // 마감일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupBuyingStatus status = GroupBuyingStatus.RECRUITING;

    @Column(nullable = false)
    private String region;  // 지역

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public GroupBuyingPost(Long chatRoomId, Long creatorId, String title, String content, String category,
                           Integer targetAmount, Integer targetParticipants,
                           LocalDateTime deadline, String region) {
        this.chatRoomId = chatRoomId;
        this.creatorId = creatorId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.targetAmount = targetAmount;
        this.targetParticipants = targetParticipants;
        this.deadline = deadline;
        this.region = region;
        this.currentAmount = 0;
        this.currentParticipants = 0;
        this.status = GroupBuyingStatus.RECRUITING;
    }

    // 참여자 증가
    public void increaseParticipant() {
        if (this.currentParticipants >= this.targetParticipants) {
            throw new IllegalStateException("목표 인원이 가득 찼습니다.");
        }
        this.currentParticipants++;
    }

    // 참여자 감소
    public void decreaseParticipant() {
        if (this.currentParticipants > 1) {  // 작성자는 항상 포함
            this.currentParticipants--;
        }
    }

    // 모금액 추가
    public void addAmount(Integer amount) {
        this.currentAmount += amount;

        // 목표 달성 시 상태 변경
        if (this.currentAmount >= this.targetAmount &&
                this.currentParticipants >= this.targetParticipants) {
            this.status = GroupBuyingStatus.COMPLETED;
        }
    }

    // 진행률 계산 (%)
    public int getProgressPercentage() {
        if (this.targetAmount == 0) {
            return 0;
        }
        return (int) ((this.currentAmount * 100.0) / this.targetAmount);
    }

    // 공동구매 완료
    public void complete() {
        this.status = GroupBuyingStatus.COMPLETED;
    }

    // 공동구매 취소
    public void cancel() {
        this.status = GroupBuyingStatus.CANCELLED;
    }

    // 마감일 지났는지 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }

    // 상태 변경 메서드 (외부에서 호출 가능하도록 이름 변경)
    public void updateStatus(GroupBuyingStatus status) {
        this.status = status;
    }

    // 마감일 변경 메서드
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    // 게시글 정보 수정 메서드
    public void updateInfo(
            String title,
            String content,
            String category,
            String region,
            LocalDateTime deadline) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.region = region;
        this.deadline = deadline;
    }
}