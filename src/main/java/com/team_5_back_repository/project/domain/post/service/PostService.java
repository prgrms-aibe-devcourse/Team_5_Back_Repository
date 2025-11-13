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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;

    public Long createPost(PostRequest request, String username) {
        Member member = (Member) memberRepository.findByNickname(username).orElseThrow(() -> new RuntimeException("사용자 없음"));

        PostType postType = Objects.requireNonNullElse(request.getPostType(), PostType.FREE);
        if(postType.isAdminOnly())//추후 관리자 권한 추가 && !member.isAdmin()
        {
            throw new RuntimeException("관리자만 작성 가능");
        }
        Set<Tag> tags = processTags(request.getTags());

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postType(postType)
                .viewCount(0L)
                .tags(tags)
                .build();

        Post saved = postRepository.save(post);
        return saved.getId();
    }
    // --- READ (상세) ---
    public PostResponse getPost(Long id, boolean increaseView) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (increaseView) {
            post.increaseViewCount();
        }

        return PostResponse.from(post);
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