package com.example.boardservice.controller;

import com.example.boardservice.config.SecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

  public ArticleControllerTest(@Autowired MockMvc mvc) {
    this.mvc = mvc;
  }

  @DisplayName("[View][GET] Article list page (board) - normal call")
  @Test
  public void givenNothing_whenRequestingArticlesView_thenReturnsArticlesView() throws Exception {
    // given
    // when & then
    mvc.perform(get("/articles"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/index"))
        .andExpect(model().attributeExists("articles"));
  }

  @DisplayName("[View][GET] Single article page - normal call")
  @Test
  public void givenNothing_whenRequestingArticleView_thenReturnsArticleView() throws Exception {
    // given
    // when & then
    mvc.perform(get("/articles/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/detail"))
        .andExpect(model().attributeExists("article"))
        .andExpect(model().attributeExists("articleComments"));
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
}