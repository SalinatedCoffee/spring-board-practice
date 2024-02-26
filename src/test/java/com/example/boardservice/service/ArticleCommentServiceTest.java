package com.example.boardservice.service;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.ArticleComment;
import com.example.boardservice.domain.UserAccount;
import com.example.boardservice.dto.ArticleCommentDto;
import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.repository.ArticleCommentRepository;
import com.example.boardservice.repository.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

//@Disabled("Not yet implemented")
@DisplayName("Business logic - ArticleComment")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {
  @InjectMocks private ArticleCommentService sut;

  @Mock private ArticleRepository articleRepository;
  @Mock private ArticleCommentRepository articleCommentRepository;

  @DisplayName("Return appropriate comment list when article ID is provided")
  @Test
  void givenArticleId_whenSearchingArticleComments_thenReturnsComments() {
    //g
    Long articleId = 1L;
    ArticleComment expected = createArticleComment("content");
    given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(List.of(expected));
    //w
    List<ArticleCommentDto> actual  = sut.searchArticleComments(articleId);
    //t
    assertThat(actual)
        .hasSize(1)
        .first().hasFieldOrPropertyWithValue("content", expected.getContent());
    then(articleCommentRepository).should().findByArticle_Id(articleId);
  }

  @DisplayName("Save comment when fields are provided")
  @Test
  void givenArticleCommentInfo_whenSavingArticleComment_thenSavesComment() {
    //g
    ArticleCommentDto dto = createArticleCommentDto("Comment");
    given(articleRepository.getReferenceById(dto.articleId())).willReturn(createArticle());
    given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);
    //w
    sut.saveArticleComment(dto);
    //t
    then(articleRepository).should().getReferenceById(dto.articleId());
    then(articleCommentRepository).should().save(any(ArticleComment.class));
  }
  @DisplayName("Log warning when attempting to save comment but to article found")
  @Test
  void givenNonexistentArticle_whenSavingArticleComment_thenLogsSituationAndDoesNothing() {
    // Given
    ArticleCommentDto dto = createArticleCommentDto("Comment");
    given(articleRepository.getReferenceById(dto.articleId())).willThrow(EntityNotFoundException.class);

    // When
    sut.saveArticleComment(dto);

    // Then
    then(articleRepository).should().getReferenceById(dto.articleId());
    then(articleCommentRepository).shouldHaveNoInteractions();
  }

  @DisplayName("Update comment when fields are provided")
  @Test
  void givenArticleCommentInfo_whenUpdatingArticleComment_thenUpdatesArticleComment() {
    // Given
    String oldContent = "content";
    String updatedContent = "Comment";
    ArticleComment articleComment = createArticleComment(oldContent);
    ArticleCommentDto dto = createArticleCommentDto(updatedContent);
    given(articleCommentRepository.getReferenceById(dto.id())).willReturn(articleComment);

    // When
    sut.updateArticleComment(dto);

    // Then
    assertThat(articleComment.getContent())
        .isNotEqualTo(oldContent)
        .isEqualTo(updatedContent);
    then(articleCommentRepository).should().getReferenceById(dto.id());
  }

  @DisplayName("Log warning when attempting to update nonexistent comment")
  @Test
  void givenNonexistentArticleComment_whenUpdatingArticleComment_thenLogsWarningAndDoesNothing() {
    // Given
    ArticleCommentDto dto = createArticleCommentDto("Comment");
    given(articleCommentRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

    // When
    sut.updateArticleComment(dto);

    // Then
    then(articleCommentRepository).should().getReferenceById(dto.id());
  }

  @DisplayName("Delete comment when comment id is provided")
  @Test
  void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
    // Given
    Long articleCommentId = 1L;
    willDoNothing().given(articleCommentRepository).deleteById(articleCommentId);

    // When
    sut.deleteArticleComment(articleCommentId);

    // Then
    then(articleCommentRepository).should().deleteById(articleCommentId);
  }

  private ArticleCommentDto createArticleCommentDto(String content) {
    return ArticleCommentDto.of(
        1L,
        1L,
        createUserAccountDto(),
        content,
        LocalDateTime.now(),
        "uno",
        LocalDateTime.now(),
        "uno"
    );
  }

  private UserAccountDto createUserAccountDto() {
    return UserAccountDto.of(
        "uno",
        "password",
        "uno@mail.com",
        "Uno",
        "This is memo",
        LocalDateTime.now(),
        "uno",
        LocalDateTime.now(),
        "uno"
    );
  }

  private ArticleComment createArticleComment(String content) {
    return ArticleComment.of(
        Article.of(createUserAccount(), "title", "content", "hashtag"),
        createUserAccount(),
        content
    );
  }

  private UserAccount createUserAccount() {
    return UserAccount.of(
        "uno",
        "password",
        "uno@email.com",
        "Uno",
        null
    );
  }

  private Article createArticle() {
    return Article.of(
        createUserAccount(),
        "title",
        "content",
        "#java"
    );
  }
}