package com.team_5_back_repository.project.domain.member.controller;

import com.team_5_back_repository.project.domain.comment.entity.Comment;
import com.team_5_back_repository.project.domain.comment.service.CommentService;
import com.team_5_back_repository.project.domain.member.dto.dto.MemberDto;
import com.team_5_back_repository.project.domain.member.dto.dto.MemberEditDto;
import com.team_5_back_repository.project.domain.member.dto.dto.MyPageDto;
import com.team_5_back_repository.project.domain.member.dto.dto.PostDto;
import com.team_5_back_repository.project.domain.member.dto.request.MemberEditRequest;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.exception.MemberException;
import com.team_5_back_repository.project.domain.member.service.MemberService;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.service.PostService;
import com.team_5_back_repository.project.global.security.Rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "MyPage Controller", description = "회원이 자신의 정보 를 조회 및 수정하는 기능 제공")
public class MyPageController {

    private final MemberService memberService;
    private final CommentService commentService;
    private final PostService postService;
    private final Rq rq;

    private Member getMember() {
        Member actor = rq.getActor();
        if(actor == null){
            throw new MemberException("401-1", "로그인 후 이용해주세요.");
        }
        return actor;
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    @Operation(summary = "마이페이지 조회", description = "회원이 자신의 정보를 조회합니다.")
    public MyPageDto getMyPage(@PathVariable Long id) {
        return memberService.retrieveMemberById(id);
    }

    @Transactional(readOnly = true)
    @GetMapping("/edit/{id}")
    @Operation(summary = "회원 정보 수정 페이지 조회", description = "회원이 자신의 정보를 수정하기 위해 현재 정보를 조회합니다.")
    public MemberEditDto getMyPageForEdit(@PathVariable Long id) {
        return memberService.retrieveModifyMemberById(id);
    }

    @Transactional
    @PutMapping(value = "/{id}",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    @Operation(summary = "마이페이지 수정", description = "회원이 자신의 정보를 수정합니다.")
    public MemberDto editMyPage(@PathVariable Long id,
                                @RequestPart("request") @Valid MemberEditRequest memberEditRequest,
                                @RequestPart(value= "profileImage",  required = false) MultipartFile profileImage) throws IOException {
        return memberService.modifyMember(id, memberEditRequest, profileImage);
    }

    @Transactional(readOnly = true)
    @GetMapping("/comments")
    @Operation(summary = "내가 작성한 댓글 조회", description = "회원이 자신이 작성한 댓글을 조회합니다.")
    public Page<Comment> getCommentsByMember(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentService.getCommentByMember(getMember(), pageable);
    }

    //TODO 북마크, 팔로잉

    @Transactional(readOnly = true)
    @GetMapping("/posts")
    @Operation(summary = "내가 작성한 게시글 조회", description = "회원이 자신이 작성한 게시글을 조회합니다.")
    public Page<PostDto> getPostsByMember(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "5") int size,
                                          @RequestParam(required = false) PostType postType) {
        Pageable pageable = PageRequest.of(page, size);
        return postService.getPostsByMember(getMember(), pageable, postType);
    }

}
