package com.example.boardservice.service;


import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.UserAccount;
import com.example.boardservice.domain.constant.SearchType;
import com.example.boardservice.dto.ArticleDto;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.repository.ArticleRepository;
import com.example.boardservice.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

// lightweight testing without going through Spring's built-in frameworks
@DisplayName("Business logic - Article")
@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {
  @InjectMocks private ArticleService sut;
  @Mock private ArticleRepository articleRepository;
  @Mock private UserAccountRepository userAccountRepository;

  @DisplayName("Return article page when searching without keyword")
  @Test
  void givenNoSearchParameters_whenSearchingArticles_thenReturnsArticlePage() {
    // Given
    Pageable pageable = Pageable.ofSize(20);
    given(articleRepository.findAll(pageable)).willReturn(Page.empty());

    // When
    Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);

    // Then
    assertThat(articles).isEmpty();
    then(articleRepository).should().findAll(pageable);
  }

  @DisplayName("Return article list when searching for article")
  @Test
  void givenSearchParameters_whenSearchingArticles_thenReturnsArticleList() {
    // Given
    SearchType searchType = SearchType.TITLE;
    String searchKeyword = "title";
    Pageable pageable = Pageable.ofSize(20);
    given(articleRepository.findByTitleContaining(searchKeyword, pageable)).willReturn(Page.empty());

    // When
    Page<ArticleDto> articles = sut.searchArticles(searchType, searchKeyword, pageable);

    // Then
    assertThat(articles).isEmpty();
    then(articleRepository).should().findByTitleContaining(searchKeyword, pageable);
  }

  @DisplayName("Return empty article list when searching with hashtag but empty keyword provided")
  @Test
  void givenNoSearchParameters_whenSearchingArticlesViaHashtag_thenReturnsEmptyPage() {
    // Given
    Pageable pageable = Pageable.ofSize(20);

    // When
    Page<ArticleDto> articles = sut.searchArticlesViaHashtag(null, pageable);

    // Then
    assertThat(articles).isEqualTo(Page.empty(pageable));
    then(articleRepository).shouldHaveNoInteractions();
  }

  @DisplayName("Return article list when searching with hashtag")
  @Test
  void givenHashtag_whenSearchingArticlesViaHashtag_thenReturnsArticleList() {
    // Given
    String hashtag = "#java";
    Pageable pageable = Pageable.ofSize(20);
    given(articleRepository.findByHashtag(hashtag, pageable)).willReturn(Page.empty(pageable));

    // When
    Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtag, pageable);

    // Then
    assertThat(articles).isEqualTo(Page.empty(pageable));
    then(articleRepository).should().findByHashtag(hashtag, pageable);
  }

  @DisplayName("Return article when requesting article")
  @Test
  void givenArticleId_whenSearchingArticle_thenReturnsArticle() {
    // Given
    Long articleId = 1L;
    Article article = createArticle();
    given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

    // When
    ArticleWithCommentsDto dto = sut.getArticleWithComments(articleId);

    // Then
    assertThat(dto)
        .hasFieldOrPropertyWithValue("title", article.getTitle())
        .hasFieldOrPropertyWithValue("content", article.getContent())
        .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
    then(articleRepository).should().findById(articleId);
  }

  @DisplayName("Throw exception when requesting nonexistent article")
  @Test
  void givenNonexistentArticleId_whenSearchingArticle_thenThrowsException() {
    // Given
    Long articleId = 0L;
    given(articleRepository.findById(articleId)).willReturn(Optional.empty());

    // When
    Throwable t = catchThrowable(() -> sut.getArticle(articleId));

    // Then
    assertThat(t)
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Article does not exist - articleId: " + articleId);
    then(articleRepository).should().findById(articleId);
  }

  @DisplayName("Create article when fields are provided")
  @Test
  void givenArticleInfo_whenSavingArticle_thenSavesArticle() {
    //g
    // mock DB transaction with Mockito
    // proceeding line written to explicitly outline mock db transaction,
    // in practice has little to no meaning
    ArticleDto dto = createArticleDto();
    given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
    given(articleRepository.save(any(Article.class))).willReturn(createArticle());
    //w
    // attempt to write row into db (in reality this operation never touches the persistence layer as we are mocking the db)
    sut.saveArticle(dto);
    //t
    // check whether db write has been requested
    then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
    then(articleRepository).should().save(any(Article.class));
  }

  @DisplayName("Edit article when article ID and modified fields are provided")
  @Test
  void givenArticleIdAndModifiedInfo_whenUpdatingArticle_thenUpdatesArticle() {
    // Given
    Article article = createArticle();
    ArticleDto dto = createArticleDto("New Title", "New Content", "#springboot");
    given(articleRepository.getReferenceById(dto.id())).willReturn(article);

    // When
    sut.updateArticle(dto.id(), dto);

    // Then
    assertThat(article)
        .hasFieldOrPropertyWithValue("title", dto.title())
        .hasFieldOrPropertyWithValue("content", dto.content())
        .hasFieldOrPropertyWithValue("hashtag", dto.hashtag());
    then(articleRepository).should().getReferenceById(dto.id());
  }

  @DisplayName("Log warning when attempting to update nonexistent article")
  @Test
  void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing() {
    // Given
    ArticleDto dto = createArticleDto("New Title", "New Content", "#springboot");
    given(articleRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

    // When
    sut.updateArticle(dto.id(), dto);

    // Then
    then(articleRepository).should().getReferenceById(dto.id());
  }

  @DisplayName("Delete article when article ID provided")
  @Test
  void givenArticleId_whenDeletingArticle_thenDeletesArticle() {
    // Given
    Long articleId = 1L;
    willDoNothing().given(articleRepository).deleteById(articleId);

    // When
    sut.deleteArticle(1L);

    // Then
    then(articleRepository).should().deleteById(articleId);
  }

  @DisplayName("Return list of unique hashtags upon request")
  @Test
  void givenNothing_whenCalling_thenReturnsHashtags() {
    // Given
    List<String> expectedHashtags = List.of("#java", "#spring", "#boot");
    given(articleRepository.findAllDistinctHashtags()).willReturn(expectedHashtags);

    // When
    List<String> actualHashtags = sut.getHashtags();

    // Then
    assertThat(actualHashtags).isEqualTo(expectedHashtags);
    then(articleRepository).should().findAllDistinctHashtags();
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

  private ArticleDto createArticleDto() {
    return createArticleDto("title", "content", "#java");
  }

  private ArticleDto createArticleDto(String title, String content, String hashtag) {
    return ArticleDto.of(1L,
        createUserAccountDto(),
        title,
        content,
        hashtag,
        LocalDateTime.now(),
        "Uno",
        LocalDateTime.now(),
        "Uno");
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

}
