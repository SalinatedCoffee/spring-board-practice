package com.example.boardservice.service;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.ArticleComment;
import com.example.boardservice.domain.Hashtag;
import com.example.boardservice.domain.UserAccount;
import com.example.boardservice.dto.ArticleCommentDto;
import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.repository.ArticleCommentRepository;
import com.example.boardservice.repository.ArticleRepository;
import com.example.boardservice.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

//@Disabled("Not yet implemented")
@DisplayName("Business logic - ArticleComment")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {
  @InjectMocks private ArticleCommentService sut;

  @Mock private ArticleRepository articleRepository;
  @Mock private ArticleCommentRepository articleCommentRepository;
  @Mock private UserAccountRepository userAccountRepository;

  @DisplayName("Return appropriate comment list when article ID is provided")
  @Test
  void givenArticleId_whenSearchingArticleComments_thenReturnsComments() {
    //g
    Long articleId = 1L;
    ArticleComment expectedParentComment = createArticleComment(1L, "Parent");
    ArticleComment expectedChildComment = createArticleComment(2L, "Child");
    expectedChildComment.setParentCommentId(expectedParentComment.getId());
    given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(List.of(
        expectedParentComment,
        expectedChildComment
    ));
    //w
    List<ArticleCommentDto> actual  = sut.searchArticleComments(articleId);
    //t
    assertThat(actual).hasSize(2);
    assertThat(actual)
        .extracting("id", "articleId", "parentCommentId", "content")
        .containsExactlyInAnyOrder(
            tuple(1L, 1L, null, "Parent"),
            tuple(2L, 1L, 1L, "Child")
        );
    then(articleCommentRepository).should().findByArticle_Id(articleId);
  }

  @DisplayName("Save comment when fields are provided")
  @Test
  void givenArticleCommentInfo_whenSavingArticleComment_thenSavesComment() {
    //g
    ArticleCommentDto dto = createArticleCommentDto("Comment");
    given(articleRepository.getReferenceById(dto.articleId())).willReturn(createArticle());
    given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
    given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);
    //w
    sut.saveArticleComment(dto);
    //t
    then(articleRepository).should().getReferenceById(dto.articleId());
    then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
    then(articleCommentRepository).should(never()).getReferenceById(anyLong());
    then(articleCommentRepository).should().save(any(ArticleComment.class));
  }
  @DisplayName("Log warning when attempting to save comment but no article found")
  @Test
  void givenNonexistentArticle_whenSavingArticleComment_thenLogsSituationAndDoesNothing() {
    // Given
    ArticleCommentDto dto = createArticleCommentDto("Comment");
    given(articleRepository.getReferenceById(dto.articleId())).willThrow(EntityNotFoundException.class);

    // When
    sut.saveArticleComment(dto);

    // Then
    then(articleRepository).should().getReferenceById(dto.articleId());
    then(userAccountRepository).shouldHaveNoInteractions();
    then(articleCommentRepository).shouldHaveNoInteractions();
  }

  @DisplayName("Delete comment when comment id is provided")
  @Test
  void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
    // Given
    Long articleCommentId = 1L;
    String userId = "uno";
    willDoNothing().given(articleCommentRepository).deleteByIdAndUserAccount_UserId(articleCommentId, userId);

    // When
    sut.deleteArticleComment(articleCommentId, userId);

    // Then
    then(articleCommentRepository).should().deleteByIdAndUserAccount_UserId(articleCommentId, userId);
  }

  @DisplayName("Save child comment when given the comment id of its parent and its content")
  @Test
  void givenParentCommentIdAndArticleCommentInfo_whenSaving_thenSavesChildComment() {
    // Given
    Long parentCommentId = 1L;
    ArticleComment parent = createArticleComment(parentCommentId, "Comment");
    ArticleCommentDto child = createArticleCommentDto(parentCommentId, "Child comment");
    given(articleRepository.getReferenceById(child.articleId())).willReturn(createArticle());
    given(userAccountRepository.getReferenceById(child.userAccountDto().userId())).willReturn(createUserAccount());
    given(articleCommentRepository.getReferenceById(child.parentCommentId())).willReturn(parent);

    // When
    sut.saveArticleComment(child);

    // Then
    assertThat(child.parentCommentId()).isNotNull();
    then(articleRepository).should().getReferenceById(child.articleId());
    then(userAccountRepository).should().getReferenceById(child.userAccountDto().userId());
    then(articleCommentRepository).should().getReferenceById(child.parentCommentId());
    then(articleCommentRepository).should(never()).save(any(ArticleComment.class));
  }


  private ArticleCommentDto createArticleCommentDto(String content) {
    return createArticleCommentDto(null, content);
  }

  private ArticleCommentDto createArticleCommentDto(Long parentCommentId, String content) {
    return createArticleCommentDto(1L, parentCommentId, content);
  }

  private ArticleCommentDto createArticleCommentDto(Long id, Long parentCommentId, String content) {
    return ArticleCommentDto.of(
        id,
        1L,
        createUserAccountDto(),
        parentCommentId,
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

  private ArticleComment createArticleComment(Long id, String content) {
    ArticleComment articleComment = ArticleComment.of(
        createArticle(),
        createUserAccount(),
        content
    );
    ReflectionTestUtils.setField(articleComment, "id", id);

    return articleComment;
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
    Article article = Article.of(
        createUserAccount(),
        "title",
        "content"
    );
    ReflectionTestUtils.setField(article, "id", 1L);
    article.addHashtags(Set.of(createHashtag(article)));

    return article;
  }

  private Hashtag createHashtag(Article article) {
    return Hashtag.of("java");
  }

}