package com.team_5_back_repository.project.domain.post;

import com.team_5_back_repository.project.domain.post.entity.Post;
import com.team_5_back_repository.project.domain.post.entity.PostType;
import com.team_5_back_repository.project.domain.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("게시글 저장 및 조회 테스트")
    void saveAndFindPost() {
        // given (데이터 준비)
        Post post = Post.builder()
                .title("첫 번째 게시글")
                .content("이것은 테스트 게시글입니다.")
                .attachmentPath("uploads/test.txt")
                .postType(PostType.FREE)
                .build();

        // when (DB 저장)
        Post savedPost = postRepository.save(post);

        // then (검증)
        Optional<Post> found = postRepository.findById(savedPost.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("첫 번째 게시글");
        assertThat(found.get().getPostType()).isEqualTo(PostType.FREE);
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updatePost() {
        // given
        Post post = postRepository.save(
                Post.builder()
                        .title("수정 전 제목")
                        .content("수정 전 내용")
                        .postType(PostType.FREE)
                        .build()
        );

        // when
        post.update("수정된 제목", "수정된 내용", "new/path.txt", PostType.TIP, post.getTags());
        Post updated = postRepository.save(post);

        // then
        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getPostType()).isEqualTo(PostType.TIP);
    }
}
