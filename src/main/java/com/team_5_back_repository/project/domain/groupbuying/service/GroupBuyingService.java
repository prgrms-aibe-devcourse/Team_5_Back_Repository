package com.team_5_back_repository.project.domain.groupbuying.service;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomResponse;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatMessageRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatParticipantRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatRoomRepository;
import com.team_5_back_repository.project.domain.chatroom.service.ChatMessageService;
import com.team_5_back_repository.project.domain.chatroom.service.ChatRoomService;
import com.team_5_back_repository.project.domain.groupbuying.dto.request.GroupBuyingCreateRequest;
import com.team_5_back_repository.project.domain.groupbuying.dto.request.GroupBuyingJoinRequest;
import com.team_5_back_repository.project.domain.groupbuying.dto.response.GroupBuyingListResponse;
import com.team_5_back_repository.project.domain.groupbuying.dto.response.GroupBuyingParticipantResponse;
import com.team_5_back_repository.project.domain.groupbuying.dto.response.GroupBuyingPostResponse;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingParticipant;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingPost;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingStatus;
import com.team_5_back_repository.project.domain.groupbuying.repository.GroupBuyingParticipantRepository;
import com.team_5_back_repository.project.domain.groupbuying.repository.GroupBuyingPostRepository;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupBuyingService {

    private final GroupBuyingPostRepository postRepository;
    private final GroupBuyingParticipantRepository participantRepository;
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository  chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MemberRepository memberRepository;

    /**
     * 공동구매 게시글 생성 + 채팅방 자동 생성 + 주최자 자동 참여 (1인당 금액 자동 기여)
     */
    @Transactional
    public GroupBuyingPostResponse createPost(GroupBuyingCreateRequest request, Long creatorId) {
        log.info("공동구매 게시글 생성 시작: creatorId={}", creatorId);

        // 1. 채팅방 생성 (type=GROUP_PURCHASE)
        ChatRoomCreateRequest chatRoomRequest = ChatRoomCreateRequest.builder()
                .name(request.getChatRoomName())
                .type(ChatRoomType.GROUP_PURCHASE)
                .region(request.getRegion())
                .description(request.getChatRoomDescription())
                .maxParticipants(request.getChatRoomMaxParticipants())
                .build();

        ChatRoomResponse chatRoom = chatRoomService.createChatRoom(chatRoomRequest, creatorId);
        log.info("채팅방 생성 완료: chatRoomId={}", chatRoom.getId());

        // 2. 공동구매 게시글 생성 (채팅방 연결)
        GroupBuyingPost post = GroupBuyingPost.builder()
                .chatRoomId(chatRoom.getId())
                .creatorId(creatorId)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .targetAmount(request.getTargetAmount())
                .targetParticipants(request.getTargetParticipants())
                .deadline(request.getDeadline())
                .region(request.getRegion())
                .build();

        GroupBuyingPost savedPost = postRepository.save(post);
        log.info("공동구매 게시글 생성 완료: postId={}, chatRoomId={}", savedPost.getId(), savedPost.getChatRoomId());

        // 3. 주최자 참여 인원 증가
        savedPost.increaseParticipant();
        log.info("주최자 참여 인원 증가: currentParticipants={}", savedPost.getCurrentParticipants());

        // 4. 1인당 금액 계산
        int amountPerPerson = (int) Math.ceil((double) savedPost.getTargetAmount() / savedPost.getTargetParticipants());
        log.info("1인당 금액 계산: {}원 (목표금액: {}원 ÷ 모집인원: {}명)",
                amountPerPerson, savedPost.getTargetAmount(), savedPost.getTargetParticipants());

        // 5. 주최자를 참여자로 추가 (1인당 금액 자동 기여)
        Member creator = memberRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        GroupBuyingParticipant creatorParticipant = GroupBuyingParticipant.builder()
                .groupBuyingPostId(savedPost.getId())
                .member(creator)
                .contributedAmount(amountPerPerson)
                .build();

        participantRepository.save(creatorParticipant);
        log.info("주최자 참여자 추가 완료: memberId={}, contributedAmount={}원",
                creatorId, amountPerPerson);

        // 6. 현재 금액 추가 (진행률 반영)
        savedPost.addAmount(amountPerPerson);
        log.info("현재 금액 추가: currentAmount={}원 (진행률: {}%)",
                savedPost.getCurrentAmount(), savedPost.getProgressPercentage());

        // 7. 변경사항 저장
        postRepository.save(savedPost);

        log.info("✅ 공동구매 게시글 생성 완료: postId={}, currentParticipants={}/{}, currentAmount={}/{}원 ({}%)",
                savedPost.getId(),
                savedPost.getCurrentParticipants(), savedPost.getTargetParticipants(),
                savedPost.getCurrentAmount(), savedPost.getTargetAmount(),
                savedPost.getProgressPercentage());

        return GroupBuyingPostResponse.from(savedPost);
    }

    /**
     * 공동구매 참여 + 채팅방 자동 입장
     * 주최자도 추가 기여 가능하도록 수정
     * 입장 메시지 중복 제거
     */
    @Transactional
    public void joinPost(Long postId, Long memberId, GroupBuyingJoinRequest request) {
        log.info("공동구매 참여 시작: postId={}, memberId={}", postId, memberId);

        // 1. 게시글 조회
        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 참여 가능 여부 확인
        if (post.getStatus() != GroupBuyingStatus.RECRUITING) {
            throw new IllegalStateException("모집 중인 게시글이 아닙니다.");
        }

        if (post.isExpired()) {
            throw new IllegalStateException("마감된 게시글입니다.");
        }

        // 주최자 여부 확인
        boolean isCreator = post.getCreatorId().equals(memberId);

        // 기존 참여자 조회
        Optional<GroupBuyingParticipant> existingParticipant =
                participantRepository.findByGroupBuyingPostIdAndMember_Id(postId, memberId);

        if (existingParticipant.isPresent()) {
            // 이미 참여 중인 경우
            if (isCreator) {
                // 주최자는 추가 기여 가능
                GroupBuyingParticipant participant = existingParticipant.get();

                if (request.getContributedAmount() > 0) {
                    // 기존 기여금에 추가
                    participant.addContribution(request.getContributedAmount());
                    post.addAmount(request.getContributedAmount());
                    participantRepository.save(participant);

                    log.info("주최자 추가 기여 완료: postId={}, memberId={}, 추가금액={}, 총기여금={}",
                            postId, memberId, request.getContributedAmount(), participant.getContributedAmount());
                }

                postRepository.save(post);
                return;
            } else {
                // 일반 참여자는 중복 참여 불가
                throw new IllegalStateException("이미 참여 중인 게시글입니다.");
            }
        }

        // 3. 신규 참여자 처리
        post.increaseParticipant();

        if (request.getContributedAmount() > 0) {
            post.addAmount(request.getContributedAmount());
        }

        // 4. 참여자 추가
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        GroupBuyingParticipant participant = GroupBuyingParticipant.builder()
                .groupBuyingPostId(postId)
                .member(member)
                .contributedAmount(request.getContributedAmount())
                .build();

        participantRepository.save(participant);

        // 5. 채팅방 자동 입장
        try {
            chatRoomService.joinChatRoom(post.getChatRoomId(), memberId);
            log.info("채팅방 자동 입장 완료: chatRoomId={}, memberId={}", post.getChatRoomId(), memberId);

        } catch (Exception e) {
            log.error("채팅방 입장 실패: {}", e.getMessage());
            throw new IllegalStateException("채팅방 입장에 실패했습니다: " + e.getMessage());
        }

        postRepository.save(post);
        log.info("공동구매 참여 완료: postId={}, memberId={}, contributedAmount={}",
                postId, memberId, request.getContributedAmount());
    }

    /**
     * 공동구매 나가기 + 채팅방 나가기
     */
    @Transactional
    public void leavePost(Long postId, Long memberId) {
        log.info("공동구매 나가기 시작: postId={}, memberId={}", postId, memberId);

        // 1. 게시글 조회
        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 작성자는 나갈 수 없음
        if (post.getCreatorId().equals(memberId)) {
            throw new IllegalStateException("작성자는 게시글을 나갈 수 없습니다. 삭제를 이용해주세요.");
        }

        // 3. 참여자 정보 조회
        GroupBuyingParticipant participant = participantRepository
                .findByGroupBuyingPostIdAndMember_Id(postId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 게시글입니다."));

        // 4. 기여 금액 차감
        if (participant.getContributedAmount() > 0) {
            post.addAmount(-participant.getContributedAmount());
        }

        // 5. 참여자 감소
        post.decreaseParticipant();

        // 6. 자리가 다시 생기거나 금액이 미달이면 상태를 RECRUITING으로 변경
        if (post.getCurrentParticipants() < post.getTargetParticipants() ||
                post.getCurrentAmount() < post.getTargetAmount()) {
            post.updateStatus(GroupBuyingStatus.RECRUITING);
            log.info("✅ 모집 상태 변경: COMPLETED → RECRUITING (인원: {}/{}, 금액: {}/{})",
                    post.getCurrentParticipants(), post.getTargetParticipants(),
                    post.getCurrentAmount(), post.getTargetAmount());
        }

        // 7. 참여자 삭제
        participantRepository.delete(participant);

        // 8. 채팅방 나가기
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

            chatRoomService.leaveChatRoom(post.getChatRoomId(), memberId);
            log.info("채팅방 자동 퇴장 완료: chatRoomId={}, memberId={}", post.getChatRoomId(), memberId);
        } catch (Exception e) {
            log.error("채팅방 퇴장 실패: {}", e.getMessage());
        }

        postRepository.save(post);
        log.info("공동구매 나가기 완료: postId={}, memberId={}", postId, memberId);
    }

    /**
     * 공동구매 게시글 삭제 (메서드 이름 수정)
     * - 주최자 혼자 남은 경우 채팅방도 함께 삭제
     */
    @Transactional
    public void deletePost(Long postId, Long memberId) {
        log.info("공동구매 게시글 삭제 시작: postId={}, memberId={}", postId, memberId);

        // 1. 게시글 조회
        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 작성자 확인
        if (!post.getCreatorId().equals(memberId)) {
            throw new IllegalArgumentException("게시글 작성자만 삭제할 수 있습니다.");
        }

        // 3. 채팅방 존재 여부 및 참여자 수 확인
        if (chatRoomRepository.existsById(post.getChatRoomId())) {
            List<ChatParticipant> chatParticipants =
                    chatParticipantRepository.findByChatRoomIdOrderByJoinedAtAsc(post.getChatRoomId());

            if (chatParticipants.size() > 1) {
                // 주최자 외에 다른 참여자가 있으면 삭제 불가
                throw new IllegalStateException(
                        "다른 참여자가 있어 게시글을 삭제할 수 없습니다. " +
                                "모든 참여자가 나간 후 삭제 가능합니다."
                );
            }

            // 주최자 혼자 남은 경우 → 채팅방도 함께 삭제
            log.info("주최자만 남아있어 채팅방도 함께 삭제: chatRoomId={}", post.getChatRoomId());

            chatMessageRepository.deleteByChatRoomId(post.getChatRoomId());
            log.info("✅ 채팅 메시지 삭제 완료: chatRoomId={}", post.getChatRoomId());

            // 채팅방 참여자 삭제
            chatParticipantRepository.deleteAll(chatParticipants);
            log.info("✅ 채팅방 참여자 삭제 완료: chatRoomId={}", post.getChatRoomId());

            // 채팅방 삭제
            chatRoomRepository.deleteById(post.getChatRoomId());
            log.info("✅ 채팅방 삭제 완료: chatRoomId={}", post.getChatRoomId());
        }

        // 4. 참여자 삭제
        List<GroupBuyingParticipant> participants = participantRepository.findByGroupBuyingPostId(postId);
        participantRepository.deleteAll(participants);
        log.info("✅ 공동구매 참여자 삭제 완료: postId={}", postId);

        // 5. 게시글 삭제
        postRepository.delete(post);

        log.info("✅ 공동구매 게시글 삭제 완료: postId={}", postId);
    }

    /**
     * 공동구매 목록 조회
     */
    public GroupBuyingListResponse getPosts(String region, GroupBuyingStatus status) {
        List<GroupBuyingPost> posts;

        if (region != null && status != null) {
            posts = postRepository.findByRegionAndStatus(region, status);
        } else if (region != null) {
            posts = postRepository.findByRegion(region);
        } else if (status != null) {
            posts = postRepository.findByStatus(status);
        } else {
            posts = postRepository.findAllByOrderByCreatedAtDesc();
        }

        List<GroupBuyingPostResponse> responses = posts.stream()
                .map(GroupBuyingPostResponse::from)
                .collect(Collectors.toList());

        return GroupBuyingListResponse.builder()
                .posts(responses)
                .totalCount(responses.size())
                .build();
    }

    /**
     * 공동구매 상세 조회
     */
    public GroupBuyingPostResponse getPost(Long postId) {
        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        return GroupBuyingPostResponse.from(post);
    }

    /**
     * 내가 참여한 공동구매 목록
     */
    public GroupBuyingListResponse getMyPosts(Long memberId) {
        List<GroupBuyingParticipant> participants = participantRepository.findByMember_Id(memberId);

        List<GroupBuyingPostResponse> responses = participants.stream()
                .map(p -> postRepository.findById(p.getGroupBuyingPostId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> GroupBuyingPostResponse.from(opt.get()))
                .collect(Collectors.toList());

        return GroupBuyingListResponse.builder()
                .posts(responses)
                .totalCount(responses.size())
                .build();
    }

    /**
     * 게시글 참여자 목록 조회
     */
    public List<GroupBuyingParticipantResponse> getParticipants(Long postId) {
        List<GroupBuyingParticipant> participants = participantRepository.findByGroupBuyingPostId(postId);

        return participants.stream()
                .map(GroupBuyingParticipantResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 공동구매 게시글 수정
     */
    @Transactional
    public GroupBuyingPostResponse updatePost(Long postId, com.team_5_back_repository.project.domain.groupbuying.dto.request.GroupBuyingUpdateRequest request, Long memberId) {
        log.info("공동구매 게시글 수정 시작: postId={}, memberId={}", postId, memberId);

        // 1. 게시글 조회
        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 작성자 확인
        if (!post.getCreatorId().equals(memberId)) {
            throw new IllegalArgumentException("게시글 작성자만 수정할 수 있습니다.");
        }

        // 3. 게시글 정보 업데이트
        post.updateInfo(
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getRegion(),
                request.getDeadline()
        );

        // 4. 저장
        GroupBuyingPost updatedPost = postRepository.save(post);

        log.info("✅ 공동구매 게시글 수정 완료: postId={}", postId);

        return GroupBuyingPostResponse.from(updatedPost);
    }
}