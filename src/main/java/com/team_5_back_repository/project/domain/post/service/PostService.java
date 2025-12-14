package com.team_5_back_repository.project.domain.post.service;


import com.team_5_back_repository.project.domain.bookmark.entity.BookmarkType;
import com.team_5_back_repository.project.domain.bookmark.repository.BookmarkRepository;
import com.team_5_back_repository.project.domain.comment.repository.CommentRepository;
import com.team_5_back_repository.project.domain.member.dto.dto.PostDto;
import com.team_5_back_repository.project.domain.member.entity.Member;
import com.team_5_back_repository.project.domain.member.repository.MemberRepository;
import com.team_5_back_repository.project.domain.post.dto.PostRequest;
import com.team_5_back_repository.project.domain.post.dto.PostResponse;
import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.repository.PostRepository;
import com.team_5_back_repository.project.domain.post.repository.TagRepository;
import com.team_5_back_repository.project.domain.post.entity.Tag;
import com.team_5_back_repository.project.domain.post.util.SecurityUtil;
import com.team_5_back_repository.project.global.cloudstorage.entity.FileEntity;
import com.team_5_back_repository.project.global.cloudstorage.repository.FileEntityRepository;
import com.team_5_back_repository.project.global.cloudstorage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;
    private final StorageService storageService;
    private final FileEntityRepository fileEntityRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommentRepository commentRepository;

    public Long createPost(PostRequest request, List<MultipartFile> files, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("사용자 없음"));

        PostType postType = Objects.requireNonNullElse(request.getPostType(), PostType.FREE);
        if(postType.isAdminOnly() && !member.isAdmin())
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

        if (files != null && !files.isEmpty()) {
            List<FileEntity> uploaded = storageService.multiUpload(files, "posts");
            uploaded.forEach(f -> f.setPost(saved));
            saved.getAttachmentPath().addAll(uploaded);
        }
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id, Long  currentMemberId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Member currentUser = null;
        if (currentMemberId != null) {
            currentUser = memberRepository.findById(currentMemberId)
                    .orElse(null);
        }
        boolean isAdmin = SecurityUtil.isAdmin();
        boolean isBookmarked = false;
        if (currentMemberId != null) {
            isBookmarked = bookmarkRepository.existsByMemberAndBookmarkTypeAndTargetId(
                    currentUser,
                    BookmarkType.POST,
                    post.getId()
            );
        }
        long commentCount = commentRepository.countByPostId(post.getId());


        return PostResponse.from(post, currentMemberId, isAdmin,isBookmarked,commentCount);
    }
    @Transactional(readOnly = true)
    public Page<PostResponse> listPosts(PostType postType, Pageable pageable) {
        Page<PostResponse> posts = postRepository.findByPostTypeWithCommentCount(postType, pageable);

        Long currentMemberId = SecurityUtil.getCurrentUserId();
        boolean isAdmin = SecurityUtil.isAdmin();

        Member currentUser = null;
        if (currentMemberId != null) {
            currentUser = memberRepository.findById(currentMemberId).orElse(null);
        }

        Member finalCurrentUser = currentUser;

        return posts.map(post -> {
            boolean isBookmarked = false;

            if (finalCurrentUser != null) {
                isBookmarked = bookmarkRepository
                        .existsByMemberAndBookmarkTypeAndTargetId(
                                finalCurrentUser,
                                BookmarkType.POST,
                                post.getId()
                        );
            }

            return PostResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .dislikeCount(post.getDislikeCount())
                    .commentCount(post.getCommentCount())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .isHot(post.isHot())
                    .isAuthor(
                            currentMemberId != null &&
                                    post.getMemberId() != null &&
                                    post.getMemberId().equals(currentMemberId)
                    )
                    .isAdmin(isAdmin)
                    .isBookmarked(isBookmarked)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> listAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);

        Long currentMemberId = SecurityUtil.getCurrentUserId();
        boolean isAdmin = SecurityUtil.isAdmin();
        Member currentUser = null;
        if (currentMemberId != null) {
            currentUser = memberRepository.findById(currentMemberId).orElse(null);
        }

        Member finalCurrentUser = currentUser;

        return posts.map(post -> {
            boolean isBookmarked = false;

            if (finalCurrentUser != null) {
                isBookmarked = bookmarkRepository
                        .existsByMemberAndBookmarkTypeAndTargetId(
                                finalCurrentUser,
                                BookmarkType.POST,
                                post.getId()
                        );
            }
            long commentCount = commentRepository.countByPostId(post.getId());
            return PostResponse.from(post, currentMemberId, isAdmin, isBookmarked,commentCount);
        });
    }
    public PostResponse updatePost(Long id, PostRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("사용자 없음"));
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("수정 권한 없음");
        }
        Set<Tag> tags = processTags(request.getTags());

        List<FileEntity> newUrls = new ArrayList<>();
        if (request.getRemainFileUrls() != null) {
            for (String url : request.getRemainFileUrls()) {
                fileEntityRepository.findByImgUrl(url).ifPresent(fe -> {
                    fe.setPost(post);
                    newUrls.add(fe);
                });;
            }
        }

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            List<FileEntity> newFiles = storageService.multiUpload(request.getFiles(), "post");
            for (FileEntity fe : newFiles) {
                fe.setPost(post);
            }
            newUrls.addAll(newFiles);
        }

        post.update(
                request.getTitle(),
                request.getContent(),
                newUrls,
                request.getPostType(),
                tags
        );

        Long currentMemberId = SecurityUtil.getCurrentUserId();
        boolean isAdmin = SecurityUtil.isAdmin();
        Member currentUser = null;
        if (currentMemberId != null) {
            currentUser = memberRepository.findById(currentMemberId).orElse(null);
        }

        boolean isBookmarked = false;
        if (currentUser != null) {
            isBookmarked = bookmarkRepository
                    .existsByMemberAndBookmarkTypeAndTargetId(
                            currentUser,
                            BookmarkType.POST,
                            post.getId()
                    );
        }
        long commentCount = commentRepository.countByPostId(post.getId());
        return PostResponse.from(post, currentMemberId, isAdmin, isBookmarked,commentCount);
    }

    @Transactional
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

    // 멤버 별 게시글 수 조회 (마이 페이지 등에서 사용)
    public Long countPostsByMember(Member member) {
        return postRepository.countByMember(member);
    }

    public void increaseView(Long id) {
        int updated = postRepository.incrementViewCount(id);
        if (updated == 0) throw new RuntimeException("게시글 없음 (id=" + id + ")");
    }

    public Page<PostDto> getPostsByMember(Member member, Pageable pageable, PostType postType) {
        Page<PostDto> posts;
        if(postType == PostType.ALL) {
            posts = postRepository.findPostByMember(member, pageable);
        } else {
            posts = postRepository.findPostByMemberAndType(member, postType, pageable);
        }
        System.out.println("타입 : " + postType);
        return posts;

    }
}