package com.example.boardservice.controller;

import com.example.boardservice.config.SecurityConfig;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.service.ArticleService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("View controller: articles")
// configure tests to go through spring security
@Import(SecurityConfig.class)
// only load relevant bean
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

  private final MockMvc mvc;

  // mock ArticleController's articleService instead of using the actual thing during testing
  @MockBean private ArticleService articleService;

  public ArticleControllerTest(@Autowired MockMvc mvc) {
    this.mvc = mvc;
  }

  @DisplayName("[View][GET] Article list page (board) - normal call")
  @Test
  public void givenNothing_whenRequestingArticlesView_thenReturnsArticlesView() throws Exception {
    // given
    given(articleService.searchArticles(eq(null), eq(null), any(Pageable.class))).willReturn(Page.empty());
    // when & then
    mvc.perform(get("/articles"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/index"))
        .andExpect(model().attributeExists("articles"));
    then(articleService).should().searchArticles(eq(null), eq(null), any(Pageable.class));
  }

  @DisplayName("[View][GET] Single article page - normal call")
  @Test
  public void givenNothing_whenRequestingArticleView_thenReturnsArticleView() throws Exception {
    // given
    Long articleId = 1L;
    // articleService, when given .getArticle(articleId), should return create...Dto().
    // so mock articleService accordingly
    given(articleService.getArticle(articleId)).willReturn(createArticleWithCommentsDto());
    // when & then
    mvc.perform(get("/articles/" + articleId))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/detail"))
        .andExpect(model().attributeExists("article"))
        .andExpect(model().attributeExists("articleComments"));
    then(articleService).should().getArticle(articleId);
  }

  @Disabled("Not yet implemented")
  @DisplayName("[View][GET] Article search page - normal call")
  @Test
  public void givenNothing_whenRequestingArticleSearchView_thenReturnsArticleSearchView() throws Exception {
    // given
    // when & then
    mvc.perform(get("/articles/search"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/search"));
  }

  @Disabled("Not yet implemented")
  @DisplayName("[View][GET] Article hashtag search page - normal call")
  @Test
  public void givenNothing_whenRequestingArticleHashtagSearchView_thenReturnsArticleHashtagSearchView() throws Exception {
    // given
    // when & then
    mvc.perform(get("/articles/search-hashtag"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/search-hashtag"));
  }

  private ArticleWithCommentsDto createArticleWithCommentsDto() {
    return ArticleWithCommentsDto.of(
        1L,
        createUserAccountDto(),
        Set.of(),
        "title",
        "content",
        "#java",
        LocalDateTime.now(),
        "uno",
        LocalDateTime.now(),
        "uno"
    );
  }

  private UserAccountDto createUserAccountDto() {
    return UserAccountDto.of(1L,
        "uno",
        "pw",
        "uno@mail.com",
        "Uno",
        "memo",
        LocalDateTime.now(),
        "uno",
        LocalDateTime.now(),
        "uno"
    );
  }
}