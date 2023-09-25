package com.example.boardservice.repository;

import com.example.boardservice.config.JpaConfig;
import com.example.boardservice.domain.Article;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA Connection tests")
@Import(JpaConfig.class)
// also enables transaction control for all test methods, will roll back any changes to before test
@DataJpaTest
class JpaRepositoryTest {
  private final ArticleRepository articleRepository;
  private final ArticleCommentRepository articleCommentRepository;

  public JpaRepositoryTest(@Autowired ArticleRepository articleRepository, @Autowired ArticleCommentRepository articleCommentRepository) {
    this.articleRepository = articleRepository;
    this.articleCommentRepository = articleCommentRepository;
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
    // given
    long previousCount = articleRepository.count();
    // when
    articleRepository.save(Article.of("new article", "new content", "#spring"));
    // then
    assertThat(articleRepository.count())
        .isEqualTo(previousCount + 1);
  }

  @DisplayName("Test UPDATE")
  @Test
  void givenTestData_whenUpdating_thenWorks() {
    // given
    Article article = articleRepository.findById(1L).orElseThrow();
    String updatedHashtag = "#springboot";
    article.setHashtag(updatedHashtag);
    // when
    // flush to actually apply update to db
    Article updatedArticle = articleRepository.saveAndFlush(article);
    // then
    assertThat(updatedArticle)
        .hasFieldOrPropertyWithValue("hashtag", updatedHashtag);
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
}