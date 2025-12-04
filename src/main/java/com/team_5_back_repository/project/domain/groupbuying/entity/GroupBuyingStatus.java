package com.team_5_back_repository.project.domain.groupbuying.entity;

/**
 * 공동구매 게시글 상태
 * - RECRUITING: 모집 중
 * - COMPLETED: 완료
 * - CANCELLED: 취소
 */
public enum GroupBuyingStatus {
    RECRUITING,   // 모집 중
    COMPLETED,    // 완료
    CANCELLED     // 취소
}