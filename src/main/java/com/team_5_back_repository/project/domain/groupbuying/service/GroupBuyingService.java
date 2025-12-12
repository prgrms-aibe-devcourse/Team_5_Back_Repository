package com.team_5_back_repository.project.domain.groupbuying.service;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomResponse;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatMessageRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatParticipantRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatRoomRepository;
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
import com.team_5_back_repository.project.domain.member.dto.dto.GroupBuyDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.global.cloudstorage.entity.FileEntity;
import com.team_5_back_repository.project.global.cloudstorage.repository.FileEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MemberRepository memberRepository;
    private final FileEntityRepository fileEntityRepository;

    /**
     * 공동구매 게시글 생성 + 채팅방 자동 생성 + 주최자 자동 참여 (1인당 금액 자동 기여)
     */
    @Transactional
    public GroupBuyingPostResponse createPost(GroupBuyingCreateRequest request, Long creatorId) {
        log.info("공동구매 게시글 생성 시작: creatorId={}", creatorId);

        ChatRoomCreateRequest chatRoomRequest = ChatRoomCreateRequest.builder()
                .name(request.getChatRoomName())
                .type(ChatRoomType.GROUP_PURCHASE)
                .region(request.getRegion())
                .description(request.getChatRoomDescription())
                .maxParticipants(request.getChatRoomMaxParticipants())
                .build();

        ChatRoomResponse chatRoom = chatRoomService.createChatRoom(chatRoomRequest, creatorId);
        log.info("채팅방 생성 완료: chatRoomId={}", chatRoom.getId());

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

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            log.info("이미지 처리 시작: imageIds={}", request.getImageIds());

            List<String> imageUrls = fileEntityRepository.findAllById(request.getImageIds())
                    .stream()
                    .map(FileEntity::getImgUrl)
                    .collect(Collectors.toList());

            post.setImageList(imageUrls);
            log.info("이미지 URL 설정 완료: {} 개", imageUrls.size());
        }

        GroupBuyingPost savedPost = postRepository.save(post);
        log.info("공동구매 게시글 생성 완료: postId={}, chatRoomId={}", savedPost.getId(), savedPost.getChatRoomId());

        savedPost.increaseParticipant();
        log.info("주최자 참여 인원 증가: currentParticipants={}", savedPost.getCurrentParticipants());

        int amountPerPerson = (int) Math.ceil((double) savedPost.getTargetAmount() / savedPost.getTargetParticipants());
        log.info("1인당 금액 계산: {}원 (목표금액: {}원 ÷ 모집인원: {}명)",
                amountPerPerson, savedPost.getTargetAmount(), savedPost.getTargetParticipants());

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

        savedPost.addAmount(amountPerPerson);
        log.info("현재 금액 추가: currentAmount={}원 (진행률: {}%)",
                savedPost.getCurrentAmount(), savedPost.getProgressPercentage());

        postRepository.save(savedPost);

        log.info("공동구매 게시글 생성 완료: postId={}, currentParticipants={}/{}, currentAmount={}/{}원 ({}%)",
                savedPost.getId(),
                savedPost.getCurrentParticipants(), savedPost.getTargetParticipants(),
                savedPost.getCurrentAmount(), savedPost.getTargetAmount(),
                savedPost.getProgressPercentage());

        return createResponse(savedPost);
    }

    /**
     * 공동구매 참여 + 채팅방 자동 입장
     */
    @Transactional
    public void joinPost(Long postId, Long memberId, GroupBuyingJoinRequest request) {
        log.info("공동구매 참여 시작: postId={}, memberId={}", postId, memberId);

        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (post.getStatus() != GroupBuyingStatus.RECRUITING) {
            throw new IllegalStateException("모집 중인 게시글이 아닙니다.");
        }

        if (post.isExpired()) {
            throw new IllegalStateException("마감된 게시글입니다.");
        }

        boolean isCreator = post.getCreatorId().equals(memberId);

        Optional<GroupBuyingParticipant> existingParticipant =
                participantRepository.findByGroupBuyingPostIdAndMember_Id(postId, memberId);

        if (existingParticipant.isPresent()) {
            if (isCreator) {
                GroupBuyingParticipant participant = existingParticipant.get();

                if (request.getContributedAmount() > 0) {
                    participant.addContribution(request.getContributedAmount());
                    post.addAmount(request.getContributedAmount());
                    participantRepository.save(participant);

                    log.info("주최자 추가 기여 완료: postId={}, memberId={}, 추가금액={}, 총기여금={}",
                            postId, memberId, request.getContributedAmount(), participant.getContributedAmount());
                }

                postRepository.save(post);
                return;
            } else {
                throw new IllegalStateException("이미 참여 중인 게시글입니다.");
            }
        }

        int perPersonAmount = post.getTargetAmount() / post.getTargetParticipants();

        if (request.getContributedAmount() != perPersonAmount) {
            throw new IllegalArgumentException(
                    "1인당 정확한 금액을 입력해주세요. 필요 금액: " + perPersonAmount + "원"
            );
        }

        log.info("금액 검증 통과: 입력금액={}, 필요금액={}",
                request.getContributedAmount(), perPersonAmount);

        post.increaseParticipant();
        post.addAmount(request.getContributedAmount());

        if (post.getCurrentParticipants() >= post.getTargetParticipants()) {
            post.updateStatus(GroupBuyingStatus.COMPLETED);
            log.info("상태 변경: RECRUITING -> COMPLETED (현재 인원: {}/{})",
                    post.getCurrentParticipants(), post.getTargetParticipants());
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        GroupBuyingParticipant participant = GroupBuyingParticipant.builder()
                .groupBuyingPostId(postId)
                .member(member)
                .contributedAmount(request.getContributedAmount())
                .build();

        participantRepository.save(participant);

        if (!chatParticipantRepository.existsByChatRoomIdAndMemberId(post.getChatRoomId(), memberId)) {
            chatRoomService.joinChatRoomWithMessage(post.getChatRoomId(), memberId, member.getNickname());
            log.info("채팅방 입장 완료: chatRoomId={}, memberId={}", post.getChatRoomId(), memberId);
        }

        postRepository.save(post);

        log.info("공동구매 참여 완료: postId={}, memberId={}, currentParticipants={}/{}, currentAmount={}/{}",
                postId, memberId,
                post.getCurrentParticipants(), post.getTargetParticipants(),
                post.getCurrentAmount(), post.getTargetAmount());
    }

    /**
     * 공동구매 나가기 + 채팅방 퇴장
     * 주최자는 나갈 수 없음 (게시글을 삭제해야 함)
     */
    @Transactional
    public void leavePost(Long postId, Long memberId) {
        log.info("공동구매 나가기 시작: postId={}, memberId={}", postId, memberId);

        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (post.getCreatorId().equals(memberId)) {
            throw new IllegalStateException("주최자는 나갈 수 없습니다. 게시글을 삭제해주세요.");
        }

        GroupBuyingParticipant participant = participantRepository
                .findByGroupBuyingPostIdAndMember_Id(postId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 게시글입니다."));

        post.decreaseParticipant();
        post.addAmount(-participant.getContributedAmount());

        if (post.getCurrentParticipants() < post.getTargetParticipants()) {
            post.updateStatus(GroupBuyingStatus.RECRUITING);
            log.info("상태 변경: COMPLETED -> RECRUITING (현재 인원: {}/{})",
                    post.getCurrentParticipants(), post.getTargetParticipants());
        }

        participantRepository.delete(participant);

        try {
            chatRoomService.leaveChatRoom(post.getChatRoomId(), memberId);
            log.info("채팅방 퇴장 완료: chatRoomId={}, memberId={}", post.getChatRoomId(), memberId);
        } catch (Exception e) {
            log.warn("채팅방 퇴장 실패 (무시): {}", e.getMessage());
        }

        postRepository.save(post);

        log.info("공동구매 나가기 완료: postId={}, memberId={}, currentParticipants={}/{}, currentAmount={}/{}",
                postId, memberId,
                post.getCurrentParticipants(), post.getTargetParticipants(),
                post.getCurrentAmount(), post.getTargetAmount());
    }

    /**
     * 공동구매 게시글 삭제 (주최자만 가능)
     * - 다른 참여자가 있으면 삭제 불가
     * - 주최자 혼자 남은 경우에만 삭제 가능
     * - 게시글 삭제 시 채팅방, 참여자 모두 삭제
     */
    @Transactional
    public void deletePost(Long postId, Long memberId) {
        log.info("공동구매 게시글 삭제 시작: postId={}, memberId={}", postId, memberId);

        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (!post.getCreatorId().equals(memberId)) {
            throw new IllegalArgumentException("게시글 작성자만 삭제할 수 있습니다.");
        }

        if (chatRoomRepository.existsById(post.getChatRoomId())) {
            List<ChatParticipant> chatParticipants =
                    chatParticipantRepository.findByChatRoomIdOrderByJoinedAtAsc(post.getChatRoomId());

            if (chatParticipants.size() > 1) {
                throw new IllegalStateException(
                        "다른 참여자가 있어 게시글을 삭제할 수 없습니다. " +
                                "모든 참여자가 나간 후 삭제 가능합니다."
                );
            }

            log.info("주최자만 남아있어 채팅방도 함께 삭제: chatRoomId={}", post.getChatRoomId());

            chatMessageRepository.deleteByChatRoomId(post.getChatRoomId());
            log.info("채팅 메시지 삭제 완료: chatRoomId={}", post.getChatRoomId());

            chatParticipantRepository.deleteAll(chatParticipants);
            log.info("채팅방 참여자 삭제 완료: chatRoomId={}", post.getChatRoomId());

            chatRoomRepository.deleteById(post.getChatRoomId());
            log.info("채팅방 삭제 완료: chatRoomId={}", post.getChatRoomId());
        }

        List<GroupBuyingParticipant> participants = participantRepository.findByGroupBuyingPostId(postId);
        participantRepository.deleteAll(participants);
        log.info("✅ 공동구매 참여자 삭제 완료: postId={}", postId);

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
                .map(this::createResponse)
                .collect(Collectors.toList());

        return GroupBuyingListResponse.builder()
                .posts(responses)
                .totalCount(responses.size())
                .build();
    }

    /**
     * 공동구매 상세 조회 + 조회수 증가
     */
    @Transactional
    public GroupBuyingPostResponse getPost(Long postId) {
        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        post.incrementViewCount();
        postRepository.save(post);

        log.info("조회수 증가: postId={}, viewCount={}", postId, post.getViewCount());

        return createResponse(post);
    }

    /**
     * 공동구매 정보 조회 (조회수 증가 X)
     * 채팅방 등에서 게시글 정보만 필요할 때 사용
     */
    public GroupBuyingPostResponse getPostInfo(Long postId) {
        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        log.info("게시글 정보 조회 (조회수 증가 X): postId={}", postId);

        return createResponse(post);
    }

    /**
     * 내가 참여한 공동구매 목록
     */
    public GroupBuyingListResponse getMyPosts(Long memberId) {
        List<GroupBuyingParticipant> participants = participantRepository.findByMember_Id(memberId);

        List<GroupBuyingPostResponse> responses = participants.stream()
                .map(p -> postRepository.findById(p.getGroupBuyingPostId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::createResponse)
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

        GroupBuyingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (!post.getCreatorId().equals(memberId)) {
            throw new IllegalArgumentException("게시글 작성자만 수정할 수 있습니다.");
        }

        post.updateInfo(
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getRegion(),
                request.getDeadline()
        );

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            log.info("이미지 처리 시작: imageIds={}", request.getImageIds());

            List<String> imageUrls = fileEntityRepository.findAllById(request.getImageIds())
                    .stream()
                    .map(FileEntity::getImgUrl)
                    .collect(Collectors.toList());

            post.setImageList(imageUrls);
            log.info("이미지 URL 설정 완료: {} 개", imageUrls.size());
        }

        GroupBuyingPost updatedPost = postRepository.save(post);

        log.info("공동구매 게시글 수정 완료: postId={}", postId);

        return createResponse(updatedPost);
    }

    /**
     * GroupBuyingPostResponse 생성 헬퍼 메서드
     * - chatRoomMessageCount 포함
     */
    private GroupBuyingPostResponse createResponse(GroupBuyingPost post) {
        GroupBuyingPostResponse response = GroupBuyingPostResponse.from(post);

        try {
            Member creator = memberRepository.findById(post.getCreatorId())
                    .orElse(null);
            if (creator != null) {
                response.setCreatorNickname(creator.getNickname());
            }
        } catch (Exception e) {
            log.warn("작성자 닉네임 조회 실패: creatorId={}, error={}", post.getCreatorId(), e.getMessage());
        }

        try {
            Long messageCount = chatMessageRepository.countByChatRoomId(post.getChatRoomId());
            response.setChatRoomMessageCount(messageCount != null ? messageCount : 0L);
        } catch (Exception e) {
            log.warn("채팅 메시지 수 조회 실패: chatRoomId={}, error={}", post.getChatRoomId(), e.getMessage());
            response.setChatRoomMessageCount(0L);
        }

        return response;
    }

    // 마이페이지 참여중인 공동구매 목록 조회
    public Page<GroupBuyDto> getParticipatingGroupBuys(Member member, Pageable pageable) {
        return participantRepository.findByMember(member, pageable);
    }
}