package com.example.boardservice.service;


import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.Hashtag;
import com.example.boardservice.domain.UserAccount;
import com.example.boardservice.domain.constant.SearchType;
import com.example.boardservice.dto.ArticleDto;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.dto.HashtagDto;
import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.repository.ArticleRepository;
import com.example.boardservice.repository.HashtagRepository;
import com.example.boardservice.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

// lightweight testing without going through Spring's built-in frameworks
@DisplayName("Business logic - Article")
@ExtendWith(MockitoExtension.class)
public class  ArticleServiceTest {
  @InjectMocks private ArticleService sut;

  @Mock private HashtagService hashtagService;
  @Mock private ArticleRepository articleRepository;
  @Mock private UserAccountRepository userAccountRepository;
  @Mock private HashtagRepository hashtagRepository;


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

  @DisplayName("Return article list page when searching for article")
  @Test
  void givenSearchParameters_whenSearchingArticles_thenReturnsArticlePage() {
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
    then(hashtagRepository).shouldHaveNoInteractions();
    then(articleRepository).shouldHaveNoInteractions();
  }

  @DisplayName("Return empty page when searching for a non-existent hashtag")
  @Test
  void givenNonexistentHashtag_whenSearchingArticlesViaHashtag_thenReturnsEmptyPage() {
    // Given
    String hashtagName = "IDoNotExist";
    Pageable pageable = Pageable.ofSize(20);
    given(articleRepository.findByHashtagNames(List.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

    // When
    Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtagName, pageable);

    // Then
    assertThat(articles).isEqualTo(Page.empty(pageable));
    then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
  }

  @DisplayName("Return articles page when searching by hashtag")
  @Test
  void givenHashtag_whenSearchingArticlesViaHashtag_thenReturnsArticlesPage() {
    // Given
    String hashtagName = "java";
    Pageable pageable = Pageable.ofSize(20);
    Article expectedArticle = createArticle();
    given(articleRepository.findByHashtagNames(List.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(expectedArticle), pageable, 1));

    // When
    Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtagName, pageable);

    // Then
    assertThat(articles).isEqualTo(new PageImpl<>(List.of(ArticleDto.from(expectedArticle)), pageable, 1));
    then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
  }


  @DisplayName("Return article with comments when querying article id")
  @Test
  void givenArticleId_whenSearchingArticleWithComments_thenReturnsArticleWithComments() {
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
        .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream()
            .map(HashtagDto::from)
            .collect(Collectors.toUnmodifiableSet()));
    then(articleRepository).should().findById(articleId);
  }

  @DisplayName("Throw exception when a comment's article does not exist")
  @Test
  void givenNonexistentArticleId_whenSearchingArticleWithComments_thenThrowsException() {
    // Given
    Long articleId = 0L;
    given(articleRepository.findById(articleId)).willReturn(Optional.empty());

    // When
    Throwable t = catchThrowable(() -> sut.getArticleWithComments(articleId));

    // Then
    assertThat(t)
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Article does not exist - articleId: " + articleId);
    then(articleRepository).should().findById(articleId);
  }

  @DisplayName("Return article when requesting article")
  @Test
  void givenArticleId_whenSearchingArticle_thenReturnsArticle() {
    // Given
    Long articleId = 1L;
    Article article = createArticle();
    given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

    // When
    ArticleDto dto = sut.getArticle(articleId);

    // Then
    assertThat(dto)
        .hasFieldOrPropertyWithValue("title", article.getTitle())
        .hasFieldOrPropertyWithValue("content", article.getContent())
        .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream()
            .map(HashtagDto::from)
            .collect(Collectors.toUnmodifiableSet())
        );
    then(articleRepository).should().findById(articleId);
  }

  @DisplayName("Throw exception when article does not exist")
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

  @DisplayName("Extract hashtag data from content and create article with that data when article is created")
  @Test
  void givenArticleInfo_whenSavingArticle_thenExtractsHashtagsFromContentAndSavesArticleWithExtractedHashtags() {
    // Given
    ArticleDto dto = createArticleDto();
    Set<String> expectedHashtagNames = Set.of("java", "spring");
    Set<Hashtag> expectedHashtags = new HashSet<>();
    expectedHashtags.add(createHashtag("java"));

    given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
    given(hashtagService.parseHashtagNames(dto.content())).willReturn(expectedHashtagNames);
    given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);
    given(articleRepository.save(any(Article.class))).willReturn(createArticle());

    // When
    sut.saveArticle(dto);

    // Then
    then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
     then(hashtagService).should().parseHashtagNames(dto.content());
    then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
    then(articleRepository).should().save(any(Article.class));
  }

  @DisplayName("Edit article when article ID and modified fields are provided")
  @Test
  void givenModifiedArticleInfo_whenUpdatingArticle_thenUpdatesArticle() {
    // Given
    Article article = createArticle();
    ArticleDto dto = createArticleDto("New Title", "New content #springboot");
    Set<String> expectedHashtagNames = Set.of("springboot");
    Set<Hashtag> expectedHashtags = new HashSet<>();

    given(articleRepository.getReferenceById(dto.id())).willReturn(article);
    given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(dto.userAccountDto().toEntity());
    willDoNothing().given(articleRepository).flush();
    willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());
    given(hashtagService.parseHashtagNames(dto.content())).willReturn(expectedHashtagNames);
    given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);

    // When
    sut.updateArticle(dto.id(), dto);

    // Then
    assertThat(article)
        .hasFieldOrPropertyWithValue("title", dto.title())
        .hasFieldOrPropertyWithValue("content", dto.content())
        .extracting("hashtags", as(InstanceOfAssertFactories.COLLECTION))
        .hasSize(1)
        .extracting("hashtagName")
        .containsExactly("springboot");
    then(articleRepository).should().getReferenceById(dto.id());
    then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
    then(articleRepository).should().flush();
    then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
    then(hashtagService).should().parseHashtagNames(dto.content());
    then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
  }

  @DisplayName("Log warning when attempting to update nonexistent article")
  @Test
  void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing() {
    // Given
    ArticleDto dto = createArticleDto("New Title", "New content");
    given(articleRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

    // When
    sut.updateArticle(dto.id(), dto);

    // Then
    then(articleRepository).should().getReferenceById(dto.id());
    then(userAccountRepository).shouldHaveNoInteractions();
    then(hashtagService).shouldHaveNoInteractions();
  }

  @DisplayName("Do nothing when user other than the author of the article attempts to update it")
  @Test
  void givenModifiedArticleInfoWithDifferentUser_whenUpdatingArticle_thenDoesNothing() {
    // Given
    Long differentArticleId = 22L;
    Article differentArticle = createArticle(differentArticleId);
    differentArticle.setUserAccount(createUserAccount("John"));
    ArticleDto dto = createArticleDto("New Title", "New content");
    given(articleRepository.getReferenceById(differentArticleId)).willReturn(differentArticle);
    given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(dto.userAccountDto().toEntity());

    // When
    sut.updateArticle(differentArticleId, dto);

    // Then
    then(articleRepository).should().getReferenceById(differentArticleId);
    then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
    then(hashtagService).shouldHaveNoInteractions();
  }


  @DisplayName("Delete article when article ID provided")
  @Test
  void givenArticleId_whenDeletingArticle_thenDeletesArticle() {
    // Given
    Long articleId = 1L;
    String userId = "uno";
    given(articleRepository.getReferenceById(articleId)).willReturn(createArticle());
    willDoNothing().given(articleRepository).deleteByIdAndUserAccount_UserId(articleId, userId);
    willDoNothing().given(articleRepository).flush();
    willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());

    // When
    sut.deleteArticle(1L, userId);

    // Then
    then(articleRepository).should().getReferenceById(articleId);
    then(articleRepository).should().deleteByIdAndUserAccount_UserId(articleId, userId);
    then(articleRepository).should().flush();
    then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
  }

  @DisplayName("Return number of articles when querying number of articles")
  @Test
  void givenNothing_whenCountingArticles_thenReturnsArticleCount() {
    // Given
    long expected = 0L;
    given(articleRepository.count()).willReturn(expected);

    // When
    long actual = sut.getArticleCount();

    // Then
    assertThat(actual).isEqualTo(expected);
    then(articleRepository).should().count();
  }

  @DisplayName("Return list of unique hashtags upon request")
  @Test
  void givenNothing_whenCalling_thenReturnsHashtags() {
    // Given
    Article article = createArticle();
    List<String> expectedHashtags = List.of("java", "spring", "boot");
    given(hashtagRepository.findAllHashtagNames()).willReturn(expectedHashtags);

    // When
    List<String> actualHashtags = sut.getHashtags();

    // Then
    assertThat(actualHashtags).isEqualTo(expectedHashtags);
    then(hashtagRepository).should().findAllHashtagNames();
  }

  private UserAccount createUserAccount() {
    return createUserAccount("uno");
  }

  private UserAccount createUserAccount(String userId) {
    return UserAccount.of(
        userId,
        "password",
        "uno@email.com",
        "Uno",
        null
    );
  }

  private Article createArticle() {
    return createArticle(1L);
  }

  private Article createArticle(Long id) {
    Article article = Article.of(
        createUserAccount(),
        "title",
        "content"
    );
    article.addHashtags(Set.of(
        createHashtag(1L, "java"),
        createHashtag(2L, "spring")
    ));
    ReflectionTestUtils.setField(article, "id", id);

    return article;
  }

  private Hashtag createHashtag(String hashtagName) {
    return createHashtag(1L, hashtagName);
  }

  private Hashtag createHashtag(Long id, String hashtagName) {
    Hashtag hashtag = Hashtag.of(hashtagName);
    ReflectionTestUtils.setField(hashtag, "id", id);

    return hashtag;
  }

  private HashtagDto createHashtagDto() {
    return HashtagDto.of("java");
  }


  private ArticleDto createArticleDto() {
    return createArticleDto("title", "content");
  }

  private ArticleDto createArticleDto(String title, String content) {
    return ArticleDto.of(
        1L,
        createUserAccountDto(),
        title,
        content,
        null,
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
