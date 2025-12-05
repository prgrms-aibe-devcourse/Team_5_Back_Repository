package com.team_5_back_repository.project.domain.chatroom.service;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomListResponse;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomResponse;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoom;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatMessageRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatParticipantRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatRoomRepository;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingParticipant;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingStatus;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingPost;
import com.team_5_back_repository.project.domain.groupbuying.repository.GroupBuyingPostRepository;
import com.team_5_back_repository.project.domain.groupbuying.repository.GroupBuyingParticipantRepository;
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
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageService chatMessageService;

    // 공동구매 검증을 위해 추가
    private final GroupBuyingPostRepository groupBuyingPostRepository;
    private final GroupBuyingParticipantRepository groupBuyingParticipantRepository;

    // Redis 참여자 관리를 위한 서비스 (선택사항)
    // private final RedisChatService redisChatService;

    /**
     * 채팅방 생성
     */
    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request, Long creatorId) {
        // Member 조회
        Member creator = memberRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(request.getName())
                .type(request.getType())
                .creatorId(creatorId)
                .region(request.getRegion())
                .description(request.getDescription())
                .maxParticipants(request.getMaxParticipants())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 생성자를 참여자로 추가 (Member 객체 사용)
        ChatParticipant creatorParticipant = ChatParticipant.builder()
                .chatRoomId(savedRoom.getId())
                .member(creator)
                .isCreator(true)
                .build();

        participantRepository.save(creatorParticipant);

        log.info("채팅방 생성 완료: id={}, name={}, creator={}",
                savedRoom.getId(), savedRoom.getName(), creatorId);

        return ChatRoomResponse.from(savedRoom);
    }

    /**
     * 채팅방 목록 조회 (지역 + 타입)
     */
    public ChatRoomListResponse getChatRooms(String region, ChatRoomType type) {
        List<ChatRoom> chatRooms;

        if (region != null && type != null) {
            chatRooms = chatRoomRepository.findByRegionAndTypeAndIsActiveTrue(region, type);
        } else if (region != null) {
            chatRooms = chatRoomRepository.findByRegionAndIsActiveTrue(region);
        } else if (type != null) {
            chatRooms = chatRoomRepository.findByTypeAndIsActiveTrue(type);
        } else {
            chatRooms = chatRoomRepository.findAll();
        }

        List<ChatRoomResponse> responses = chatRooms.stream()
                .map(ChatRoomResponse::from)
                .collect(Collectors.toList());

        return ChatRoomListResponse.builder()
                .chatRooms(responses)
                .totalCount(responses.size())
                .build();
    }

    /**
     * 채팅방 상세 조회
     */
    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        return ChatRoomResponse.from(chatRoom);
    }

    /**
     * 채팅방 참여 (공동구매 참여자 검증 추가)
     */
    @Transactional
    public void joinChatRoom(Long chatRoomId, Long memberId) {
        log.info("채팅방 참여 시작: chatRoomId={}, memberId={}", chatRoomId, memberId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 공동구매 채팅방인 경우 참여자 검증
        if (chatRoom.getType() == ChatRoomType.GROUP_PURCHASE) {
            // 공동구매 게시글 조회
            GroupBuyingPost post = groupBuyingPostRepository.findByChatRoomId(chatRoomId)
                    .orElseThrow(() -> new IllegalArgumentException("연결된 공동구매 게시글을 찾을 수 없습니다."));

            // 공동구매 참여자인지 확인
            boolean isParticipant = groupBuyingParticipantRepository
                    .existsByGroupBuyingPostIdAndMember_Id(post.getId(), memberId);

            if (!isParticipant) {
                throw new IllegalStateException("공동구매 참여자만 채팅방에 입장할 수 있습니다.");
            }

            log.info("공동구매 참여자 확인 완료: postId={}, memberId={}", post.getId(), memberId);
        }

        // Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 이미 참여 중인지 확인
        if (participantRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId)) {
            log.info("이미 참여 중인 채팅방: chatRoomId={}, memberId={}", chatRoomId, memberId);
            return; // 이미 참여 중이면 그냥 리턴
        }

        // 인원 확인
        chatRoom.increaseParticipant();

        // 참여자 추가 (Member 객체 사용)
        ChatParticipant participant = ChatParticipant.builder()
                .chatRoomId(chatRoomId)
                .member(member)
                .isCreator(false)
                .build();

        participantRepository.save(participant);
        chatRoomRepository.save(chatRoom);

        log.info("채팅방 참여 완료: chatRoomId={}, memberId={}", chatRoomId, memberId);
    }

    /**
     * 채팅방 나가기 (상태만 자동 업데이트)
     * - 방장 권한 이양
     * - 공동구매 참여자도 함께 나가기
     * - 자리 생기면 상태 RECRUITING으로 변경
     * - 빈 채팅방 및 게시글 삭제
     */
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        log.info("채팅방 나가기 시작: chatRoomId={}, memberId={}", chatRoomId, memberId);

        // 0. 공동구매 채팅방인 경우 먼저 공동구매에서 나가기
        if (chatRoom.getType() == ChatRoomType.GROUP_PURCHASE) {
            Optional<GroupBuyingPost> postOpt = groupBuyingPostRepository.findByChatRoomId(chatRoomId);

            if (postOpt.isPresent()) {
                GroupBuyingPost post = postOpt.get();

                // 일반 참여자인 경우 (주최자 아님)
                if (!post.getCreatorId().equals(memberId)) {
                    log.info("공동구매 일반 참여자 나가기 처리: postId={}, memberId={}", post.getId(), memberId);

                    // 공동구매 참여자 조회
                    Optional<GroupBuyingParticipant> participantOpt =
                            groupBuyingParticipantRepository.findByGroupBuyingPostIdAndMember_Id(post.getId(), memberId);

                    if (participantOpt.isPresent()) {
                        GroupBuyingParticipant participant = participantOpt.get();

                        // 기여 금액 차감
                        if (participant.getContributedAmount() > 0) {
                            post.addAmount(-participant.getContributedAmount());
                            log.info("기여 금액 차감: {}원", participant.getContributedAmount());
                        }

                        // 참여자 수 감소
                        post.decreaseParticipant();
                        log.info("참여자 수 감소: currentParticipants={}", post.getCurrentParticipants());

                        // 다시 생기거나 금액이 미달이면 상태를 RECRUITING으로 변경
                        // 마감일은 주최자가 직접 수정할 수 있도록 유지
                        if (post.getCurrentParticipants() < post.getTargetParticipants() ||
                                post.getCurrentAmount() < post.getTargetAmount()) {
                            post.updateStatus(GroupBuyingStatus.RECRUITING);
                            log.info("✅ 모집 상태 변경: COMPLETED → RECRUITING (인원: {}/{}, 금액: {}/{})",
                                    post.getCurrentParticipants(), post.getTargetParticipants(),
                                    post.getCurrentAmount(), post.getTargetAmount());
                        }

                        // 공동구매 참여자 삭제
                        groupBuyingParticipantRepository.delete(participant);

                        // 게시글 저장
                        groupBuyingPostRepository.save(post);

                        log.info("✅ 공동구매 참여자 삭제 완료: postId={}, memberId={}", post.getId(), memberId);
                    }
                }
            }
        }

        // 1. 방장이 나가는 경우 처리
        boolean isCreatorLeaving = chatRoom.getCreatorId().equals(memberId);

        if (isCreatorLeaving) {
            log.info("방장이 채팅방을 나갑니다: chatRoomId={}, memberId={}", chatRoomId, memberId);

            // 입장 순서대로 정렬된 참여자 목록 조회
            List<ChatParticipant> remainingParticipants = participantRepository
                    .findByChatRoomIdOrderByJoinedAtAsc(chatRoomId)
                    .stream()
                    .filter(p -> !p.getMemberId().equals(memberId))
                    .toList();

            if (!remainingParticipants.isEmpty()) {
                // 가장 먼저 참여한 사람(첫 번째)을 새 방장으로
                ChatParticipant nextCreator = remainingParticipants.get(0);
                Long newCreatorId = nextCreator.getMemberId();
                Member newCreatorMember = nextCreator.getMember();

                chatRoom.changeCreator(newCreatorId);

                // 기존 참여자 데이터 삭제
                participantRepository.delete(nextCreator);
                participantRepository.flush();

                // 새로운 방장 참여자 생성
                ChatParticipant newCreatorParticipant = ChatParticipant.builder()
                        .chatRoomId(chatRoomId)
                        .member(newCreatorMember)
                        .isCreator(true)
                        .build();
                participantRepository.save(newCreatorParticipant);

                log.info("✅ 방장 권한 이양 (입장 순서): {} → {}", memberId, newCreatorId);
            }
        }

        // 2. 참여자 삭제
        participantRepository.deleteByChatRoomIdAndMemberId(chatRoomId, memberId);

        // 3. 인원 감소
        chatRoom.decreaseParticipant();

        // 4. 참여자가 0명이면 채팅방 완전 삭제
        if (chatRoom.getCurrentParticipants() == 0) {
            log.info("⚠마지막 참여자가 나가서 채팅방을 삭제합니다: chatRoomId={}", chatRoomId);

            // 4-1. 공동구매 채팅방이면 게시글도 삭제
            if (chatRoom.getType() == ChatRoomType.GROUP_PURCHASE) {
                groupBuyingPostRepository.findByChatRoomId(chatRoomId)
                        .ifPresent(post -> {
                            log.info("공동구매 게시글 발견: postId={}", post.getId());

                            // 공동구매 참여자 먼저 삭제
                            groupBuyingParticipantRepository.deleteByGroupBuyingPostId(post.getId());
                            log.info("✅ 공동구매 참여자 삭제 완료: postId={}", post.getId());

                            // 게시글 삭제
                            groupBuyingPostRepository.delete(post);
                            log.info("✅ 채팅방 삭제로 인한 게시글 자동 삭제: postId={}", post.getId());
                        });
            }

            // 4-2. 채팅 메시지 삭제
            messageRepository.deleteByChatRoomId(chatRoomId);
            log.info("✅ 채팅 메시지 삭제 완료: chatRoomId={}", chatRoomId);

            // 4-3. 채팅방 삭제
            chatRoomRepository.delete(chatRoom);

            log.info("✅ 채팅방 삭제 완료: chatRoomId={}", chatRoomId);
            return;
        }

        // 5. 변경사항 저장
        chatRoomRepository.save(chatRoom);

        log.info("채팅방 퇴장 완료: chatRoomId={}, memberId={}, 남은 인원={}",
                chatRoomId, memberId, chatRoom.getCurrentParticipants());
    }

    /**
     * 내가 참여한 채팅방 목록
     */
    public ChatRoomListResponse getMyChatRooms(Long memberId) {
        List<ChatParticipant> participants = participantRepository.findByMemberId(memberId);

        List<ChatRoomResponse> responses = participants.stream()
                .map(p -> chatRoomRepository.findById(p.getChatRoomId()))
                .filter(opt -> opt.isPresent() && opt.get().getIsActive())
                .map(opt -> ChatRoomResponse.from(opt.get()))
                .collect(Collectors.toList());

        return ChatRoomListResponse.builder()
                .chatRooms(responses)
                .totalCount(responses.size())
                .build();
    }

    /**
     * 채팅방 참여자 목록 조회 (입장 순서대로)
     */
    public List<ChatParticipant> getChatRoomParticipants(Long chatRoomId) {
        return participantRepository.findByChatRoomIdOrderByJoinedAtAsc(chatRoomId);
    }

    /**
     * 참여자 강퇴 (방장만 가능)
     */
    @Transactional
    public void kickParticipant(Long chatRoomId, Long kickerId, Long targetMemberId) {
        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 2. 강퇴 실행자가 방장인지 확인
        if (!chatRoom.getCreatorId().equals(kickerId)) {
            throw new IllegalStateException("방장만 참여자를 강퇴할 수 있습니다.");
        }

        // 3. 자기 자신은 강퇴할 수 없음
        if (kickerId.equals(targetMemberId)) {
            throw new IllegalArgumentException("자기 자신은 강퇴할 수 없습니다.");
        }

        // 4. 대상 참여자 조회
        ChatParticipant targetParticipant = participantRepository
                .findByChatRoomIdAndMemberId(chatRoomId, targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 사용자입니다."));

        // 5. 대상 회원 정보 조회
        Member targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        log.info("🚫 강퇴 시작: chatRoomId={}, 강퇴자={}, 대상={}",
                chatRoomId, kickerId, targetMemberId);

        // 6. 강퇴 메시지 생성 및 브로드캐스트 (참여자 삭제 전에!)
        chatMessageService.createKickMessage(chatRoomId, targetMember.getId(), targetMember.getNickname());

        // 7. 참여자 삭제
        participantRepository.delete(targetParticipant);
        log.info("✅ 참여자 삭제 완료: memberId={}", targetMemberId);

        // 8. 채팅방 인원 감소
        chatRoom.decreaseParticipant();
        chatRoomRepository.save(chatRoom);

        log.info("✅ 강퇴 완료: chatRoomId={}, 남은 인원={}",
                chatRoomId, chatRoom.getCurrentParticipants());
    }
}