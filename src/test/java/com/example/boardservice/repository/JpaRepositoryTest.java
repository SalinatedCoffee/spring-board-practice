package com.example.boardservice.repository;

import com.example.boardservice.config.JpaConfig;
import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.Hashtag;
import com.example.boardservice.domain.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA Connection tests")
@Import(JpaRepositoryTest.TestJpaConfig.class)
// also enables transaction control for all test methods, will roll back any changes to before test
@DataJpaTest
class JpaRepositoryTest {
  private final ArticleRepository articleRepository;
  private final ArticleCommentRepository articleCommentRepository;
  private final UserAccountRepository userAccountRepository;
  private final HashtagRepository hashtagRepository;

  public JpaRepositoryTest(@Autowired ArticleRepository articleRepository,
                           @Autowired ArticleCommentRepository articleCommentRepository,
                           @Autowired UserAccountRepository userAccountRepository,
                           @Autowired HashtagRepository hashtagRepository) {
    this.articleRepository = articleRepository;
    this.articleCommentRepository = articleCommentRepository;
    this.userAccountRepository = userAccountRepository;
    this.hashtagRepository = hashtagRepository;
  }

  // g-w-t template for testing CRUD
  @DisplayName("Test SELECT")
  @Test
  void givenTestData_whenSelecting_thenWorks() {
    // given
    // when
    List<Article> articles = articleRepository.findAll();
    // then
    assertThat(articles)
        .isNotNull()
        .hasSize(123);
  }

  @DisplayName("Test INSERT")
  @Test
  void givenTestData_whenInserting_thenWorks() {
    // insert new article and see if article count changes
    // Given
    long previousCount = articleRepository.count();
    UserAccount userAccount = userAccountRepository.save(UserAccount.of("newUno", "pw", null, null, null));
    Article article = Article.of(userAccount, "new article", "new content");
    article.addHashtags(Set.of(Hashtag.of("spring")));

    // When
    articleRepository.save(article);

    // Then
    assertThat(articleRepository.count()).isEqualTo(previousCount + 1);
  }

  @DisplayName("Test UPDATE")
  @Test
  void givenTestData_whenUpdating_thenWorks() {
    // Given
    Article article = articleRepository.findById(1L).orElseThrow();
    Hashtag updatedHashtag = Hashtag.of("springboot");
    article.clearHashtags();
    article.addHashtags(Set.of(updatedHashtag));

    // When
    // flush to actually apply change to db
    Article savedArticle = articleRepository.saveAndFlush(article);

    // Then
    assertThat(savedArticle.getHashtags())
        .hasSize(1)
        .extracting("hashtagName", String.class)
        .containsExactly(updatedHashtag.getHashtagName());
  }

  @DisplayName("Test DELETE")
  @Test
  void givenTestData_whenDeleting_thenWorks() {
    // given
    Article article = articleRepository.findById(1L).orElseThrow();
    long previousArticleCount = articleRepository.count();
    // also test for cascading deletion of comments
    long previousArticleCommentCount = articleCommentRepository.count();
    int deletedCommentsSize = article.getArticleComments().size();
    // when
    articleRepository.delete(article);
    // then
    assertThat(articleRepository.count()).isEqualTo(previousArticleCount - 1);
    assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - deletedCommentsSize);
  }

  @DisplayName("[Querydsl] search for name among entire list of hashtags")
  @Test
  void givenNothing_whenQueryingHashtags_thenReturnsHashtagNames() {
    // Given

    // When
    List<String> hashtagNames = hashtagRepository.findAllHashtagNames();

    // Then
    assertThat(hashtagNames).hasSize(19);
  }

  @DisplayName("[Querydsl] search paginated articles with hashtag")
  @Test
  void givenHashtagNamesAndPageable_whenQueryingArticles_thenReturnsArticlePage() {
    // Given
    List<String> hashtagNames = List.of("blue", "crimson", "fuscia");
    Pageable pageable = PageRequest.of(0, 5, Sort.by(
        Sort.Order.desc("hashtags.hashtagName"),
        Sort.Order.asc("title")
    ));

    // When
    Page<Article> articlePage = articleRepository.findByHashtagNames(hashtagNames, pageable);

    // Then
    assertThat(articlePage.getContent()).hasSize(pageable.getPageSize());
    assertThat(articlePage.getContent().get(0).getTitle()).isEqualTo("Fusce posuere felis sed lacus.");
    assertThat(articlePage.getContent().get(0).getHashtags())
        .extracting("hashtagName", String.class)
        .containsExactly("fuscia");
    assertThat(articlePage.getTotalElements()).isEqualTo(17);
    assertThat(articlePage.getTotalPages()).isEqualTo(4);
  }

  @EnableJpaAuditing
  // only register bean during testing
  @TestConfiguration
  // replace JpaConfig's auditorAware config during test runs
  public static class TestJpaConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
      return () -> Optional.of("uno");
    }
  }
}