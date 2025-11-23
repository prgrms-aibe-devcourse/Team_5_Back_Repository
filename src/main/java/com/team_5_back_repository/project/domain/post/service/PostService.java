package com.team_5_back_repository.project.domain.post.service;

import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.post.dto.PostRequest;
import com.team_5_back_repository.project.domain.post.dto.PostResponse;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.repository.PostRepository;
import com.team_5_back_repository.project.domain.post.repository.TagRepository;
import com.team_5_back_repository.project.domain.post.entity.Tag;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;

    public Long createPost(PostRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("사용자 없음"));

        PostType postType = Objects.requireNonNullElse(request.getPostType(), PostType.FREE);
        if(postType.isAdminOnly())//추후 관리자 권한 추가 Ex) && !member.isAdmin()
        {
            throw new RuntimeException("관리자만 작성 가능");
        }
        Set<Tag> tags = processTags(request.getTags());

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .member(member)
                .postType(postType)
                .viewCount(0L)
                .tags(tags)
                .build();

        Post saved = postRepository.save(post);
        return saved.getId();
    }
    @Transactional(readOnly = true)
    public PostResponse getPost(Long id, boolean increaseView) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));
        if (increaseView) {
            post.increaseViewCount();
        }
        return PostResponse.from(post);
    }
    @Transactional(readOnly = true)
    public Page<PostResponse> listPosts(PostType postType, Pageable pageable) {
        return postRepository.findByPostType(postType, pageable)
                .map(PostResponse::from);
    }

    public PostResponse updatePost(Long id, PostRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("사용자 없음"));
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("수정 권한 없음");
        }
        Set<Tag> tags = processTags(request.getTags());
        post.update(
                request.getTitle(),
                request.getContent(),
                request.getAttachmentPath(),
                request.getPostType(),
                tags
        );
        return PostResponse.from(post);
    }

    public  void deletePost(Long id, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("사용자 없음"));
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("삭제 권한 없음");
        }
        postRepository.delete(post);
    }

    private Set<Tag> processTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>();

        for (String name : tagNames) {
            String trimmedName = name.trim();
            String cleanName = trimmedName.startsWith("#") ? trimmedName.substring(1) : trimmedName;
            cleanName = cleanName.toLowerCase();
            if (cleanName.isEmpty()) continue;

            String finalCleanName = cleanName;

            Tag tag = tagRepository.findTagByName(finalCleanName)
                    .orElseGet(() -> tagRepository.save(
                            Tag.builder().name(finalCleanName).build()
                    ));
            tags.add(tag);
        }
        return tags;
    }
}