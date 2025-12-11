package com.team_5_back_repository.project.domain.chatroom.service;

import com.team_5_back_repository.project.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomListResponse;
import com.team_5_back_repository.project.domain.chatroom.dto.response.ChatRoomResponse;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatParticipant;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoom;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomBan;
import com.team_5_back_repository.project.domain.chatroom.entity.ChatRoomType;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatMessageRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatParticipantRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatRoomRepository;
import com.team_5_back_repository.project.domain.chatroom.repository.ChatRoomBanRepository;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingParticipant;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingStatus;
import com.team_5_back_repository.project.domain.member.dto.dto.GroupChatDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.groupbuying.entity.GroupBuyingPost;
import com.team_5_back_repository.project.domain.groupbuying.repository.GroupBuyingPostRepository;
import com.team_5_back_repository.project.domain.groupbuying.repository.GroupBuyingParticipantRepository;
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
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageService chatMessageService;
    private final ChatRoomBanRepository banRepository;

    // 공동구매 검증을 위해 추가
    private final GroupBuyingPostRepository groupBuyingPostRepository;
    private final GroupBuyingParticipantRepository groupBuyingParticipantRepository;

    /**
     * 채팅방 생성
     */
    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request, Long creatorId) {
        Member creator = memberRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        ChatRoom chatRoom = ChatRoom.builder()
                .name(request.getName())
                .type(request.getType())
                .creatorId(creatorId)
                .region(request.getRegion())
                .description(request.getDescription())
                .maxParticipants(request.getMaxParticipants())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

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
     * 채팅방 참여 (기존 이름 유지, 내부에서 입장 메시지 자동 생성)
     * GroupBuyingService에서 호출하는 메서드 - 메시지 생성 안 함
     */
    @Transactional
    public void joinChatRoom(Long chatRoomId, Long memberId) {
        log.info("채팅방 참여 시작: chatRoomId={}, memberId={}", chatRoomId, memberId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 강퇴 이력 확인
        if (banRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId)) {
            throw new IllegalStateException("강퇴된 채팅방에는 다시 참여할 수 없습니다.");
        }

        // 공동구매 채팅방인 경우 참여자 검증
        if (chatRoom.getType() == ChatRoomType.GROUP_PURCHASE) {
            GroupBuyingPost post = groupBuyingPostRepository.findByChatRoomId(chatRoomId)
                    .orElseThrow(() -> new IllegalArgumentException("연결된 공동구매 게시글을 찾을 수 없습니다."));

            boolean isParticipant = groupBuyingParticipantRepository
                    .existsByGroupBuyingPostIdAndMember_Id(post.getId(), memberId);

            if (!isParticipant) {
                throw new IllegalStateException("공동구매 참여자만 채팅방에 입장할 수 있습니다.");
            }

            log.info("공동구매 참여자 확인 완료: postId={}, memberId={}", post.getId(), memberId);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 이미 참여 중인지 확인
        if (participantRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId)) {
            log.info("이미 참여 중인 채팅방: chatRoomId={}, memberId={}", chatRoomId, memberId);
            return;
        }

        // 인원 확인 및 증가
        chatRoom.increaseParticipant();

        // 참여자 추가
        ChatParticipant participant = ChatParticipant.builder()
                .chatRoomId(chatRoomId)
                .member(member)
                .isCreator(false)
                .build();

        participantRepository.save(participant);
        chatRoomRepository.save(chatRoom);

        // 입장 메시지 자동 생성 (GroupBuyingService 호출 시에는 생성 안 됨)
        // 이미 참여 중이면 메시지 생성하지 않음
        log.info("채팅방 참여 완료: chatRoomId={}, memberId={}", chatRoomId, memberId);
    }

    /**
     * 채팅방 참여 + 입장 메시지 (Controller 전용)
     */
    @Transactional
    public void joinChatRoomWithMessage(Long chatRoomId, Long memberId, String nickname) {
        log.info("✅ 채팅방 참여 + 입장 메시지: chatRoomId={}, memberId={}", chatRoomId, memberId);

        // 1. 참여 처리
        joinChatRoom(chatRoomId, memberId);

        // 2. 입장 메시지 생성
        chatMessageService.createEnterMessage(chatRoomId, memberId, nickname);

        log.info("✅ 채팅방 참여 + 입장 메시지 완료: chatRoomId={}, memberId={}", chatRoomId, memberId);
    }

    /**
     * 채팅방 나가기 (기존 이름 유지, 방장 권한 이양 개선)
     */
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        log.info("채팅방 나가기 시작: chatRoomId={}, memberId={}", chatRoomId, memberId);

        boolean isCreator = chatRoom.getCreatorId().equals(memberId);

        // 방장이 나가는 경우: 자동 이양 대신 예외 처리
        if (isCreator && chatRoom.getCurrentParticipants() > 1) {
            throw new IllegalStateException("방장은 다른 참여자에게 권한을 이양한 후 나갈 수 있습니다.");
        }

        // 0. 공동구매 채팅방인 경우 먼저 공동구매에서 나가기
        if (chatRoom.getType() == ChatRoomType.GROUP_PURCHASE) {
            Optional<GroupBuyingPost> postOpt = groupBuyingPostRepository.findByChatRoomId(chatRoomId);

            if (postOpt.isPresent()) {
                GroupBuyingPost post = postOpt.get();

                // 일반 참여자인 경우 (주최자 아님)
                if (!post.getCreatorId().equals(memberId)) {
                    log.info("공동구매 일반 참여자 나가기 처리: postId={}, memberId={}", post.getId(), memberId);

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

                        // 다시 자리 생기거나 금액 미달이면 상태를 RECRUITING으로 변경
                        if (post.getCurrentParticipants() < post.getTargetParticipants() ||
                                post.getCurrentAmount() < post.getTargetAmount()) {
                            post.updateStatus(GroupBuyingStatus.RECRUITING);
                            log.info("✅ 모집 상태 변경: COMPLETED → RECRUITING");
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

        // 참여자 삭제
        participantRepository.deleteByChatRoomIdAndMemberId(chatRoomId, memberId);

        // 인원 감소
        chatRoom.decreaseParticipant();

        // 참여자가 0명이면 채팅방 완전 삭제
        if (chatRoom.getCurrentParticipants() == 0) {
            log.info("⚠️ 마지막 참여자가 나가서 채팅방을 삭제합니다: chatRoomId={}", chatRoomId);

            // 공동구매 채팅방이면 게시글도 삭제
            if (chatRoom.getType() == ChatRoomType.GROUP_PURCHASE) {
                groupBuyingPostRepository.findByChatRoomId(chatRoomId)
                        .ifPresent(post -> {
                            log.info("공동구매 게시글 발견: postId={}", post.getId());
                            groupBuyingParticipantRepository.deleteByGroupBuyingPostId(post.getId());
                            log.info("✅ 공동구매 참여자 삭제 완료: postId={}", post.getId());
                            groupBuyingPostRepository.delete(post);
                            log.info("✅ 채팅방 삭제로 인한 게시글 자동 삭제: postId={}", post.getId());
                        });
            }

            messageRepository.deleteByChatRoomId(chatRoomId);
            banRepository.deleteByChatRoomId(chatRoomId);
            chatRoomRepository.delete(chatRoom);

            log.info("✅ 채팅방 삭제 완료: chatRoomId={}", chatRoomId);
            return;
        }

        chatRoomRepository.save(chatRoom);

        log.info("채팅방 퇴장 완료: chatRoomId={}, memberId={}, 남은 인원={}",
                chatRoomId, memberId, chatRoom.getCurrentParticipants());
    }

    /**
     * 채팅방 나가기 + 퇴장 메시지 (Controller 전용)
     */
    @Transactional
    public void leaveChatRoomWithMessage(Long chatRoomId, Long memberId, String nickname) {
        log.info("✅ 채팅방 나가기 + 퇴장 메시지: chatRoomId={}, memberId={}", chatRoomId, memberId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 퇴장 메시지 생성 (참여자 삭제 전에!)
        chatMessageService.createLeaveMessage(chatRoomId, memberId, nickname);

        // 나가기 처리
        leaveChatRoom(chatRoomId, memberId);

        log.info("✅ 채팅방 나가기 + 퇴장 메시지 완료: chatRoomId={}, memberId={}", chatRoomId, memberId);
    }

    /**
     * 방장 권한 이양 (수동 선택)
     */
    @Transactional
    public void transferCreator(Long chatRoomId, Long currentCreatorId, Long newCreatorId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 현재 방장인지 확인
        if (!chatRoom.getCreatorId().equals(currentCreatorId)) {
            throw new IllegalStateException("방장만 권한을 이양할 수 있습니다.");
        }

        // 자기 자신에게 이양 불가
        if (currentCreatorId.equals(newCreatorId)) {
            throw new IllegalArgumentException("자기 자신에게는 권한을 이양할 수 없습니다.");
        }

        // 새 방장이 참여자인지 확인
        ChatParticipant newCreatorParticipant = participantRepository
                .findByChatRoomIdAndMemberId(chatRoomId, newCreatorId)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 사용자입니다."));

        Member newCreatorMember = newCreatorParticipant.getMember();

        log.info("방장 권한 이양 시작: chatRoomId={}, {} → {}",
                chatRoomId, currentCreatorId, newCreatorId);

        // 채팅방 방장 변경
        chatRoom.changeCreator(newCreatorId);
        chatRoomRepository.save(chatRoom);

        // 기존 방장 참여자 데이터 삭제 후 재생성 (isCreator = false)
        participantRepository.deleteByChatRoomIdAndMemberId(chatRoomId, currentCreatorId);
        participantRepository.flush();

        Member currentCreatorMember = memberRepository.findById(currentCreatorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        ChatParticipant formerCreatorParticipant = ChatParticipant.builder()
                .chatRoomId(chatRoomId)
                .member(currentCreatorMember)
                .isCreator(false)
                .build();
        participantRepository.save(formerCreatorParticipant);

        // 새 방장 참여자 데이터 삭제 후 재생성 (isCreator = true)
        participantRepository.delete(newCreatorParticipant);
        participantRepository.flush();

        ChatParticipant newCreatorAsCreator = ChatParticipant.builder()
                .chatRoomId(chatRoomId)
                .member(newCreatorMember)
                .isCreator(true)
                .build();
        participantRepository.save(newCreatorAsCreator);

        // 권한 이양 메시지 생성
        chatMessageService.createTransferMessage(
                chatRoomId,
                currentCreatorMember.getNickname(),
                newCreatorMember.getNickname()
        );

        log.info("✅ 방장 권한 이양 완료: chatRoomId={}, 새 방장={}", chatRoomId, newCreatorId);
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

    //마이 페이지 용 채팅방 목록 조회
    public Page<GroupChatDto> getGroupsByMember(Member member, Pageable pageable) {
        return participantRepository.findByMember(member, pageable);
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
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (!chatRoom.getCreatorId().equals(kickerId)) {
            throw new IllegalStateException("방장만 참여자를 강퇴할 수 있습니다.");
        }

        if (kickerId.equals(targetMemberId)) {
            throw new IllegalArgumentException("자기 자신은 강퇴할 수 없습니다.");
        }

        ChatParticipant targetParticipant = participantRepository
                .findByChatRoomIdAndMemberId(chatRoomId, targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 사용자입니다."));

        Member targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        log.info("🚫 강퇴 시작: chatRoomId={}, 강퇴자={}, 대상={}",
                chatRoomId, kickerId, targetMemberId);

        // 강퇴 메시지 생성 및 브로드캐스트 (참여자 삭제 전에!)
        chatMessageService.createKickMessage(chatRoomId, targetMember.getId(), targetMember.getNickname());

        // 참여자 삭제
        participantRepository.delete(targetParticipant);

        // 강퇴 이력 저장
        ChatRoomBan ban = ChatRoomBan.builder()
                .chatRoomId(chatRoomId)
                .memberId(targetMemberId)
                .kickedBy(kickerId)
                .reason("방장에 의해 강퇴됨")
                .build();
        banRepository.save(ban);

        // 채팅방 인원 감소
        chatRoom.decreaseParticipant();
        chatRoomRepository.save(chatRoom);

        log.info("강퇴 완료: chatRoomId={}, 남은 인원={}",
                chatRoomId, chatRoom.getCurrentParticipants());
    }
}