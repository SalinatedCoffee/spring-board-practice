package com.example.boardservice.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
// Java static imports (https://en.wikipedia.org/wiki/Static_import)
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Spring Data REST tests are largely unnecessary, and this implementation is resource-heavy.")
@DisplayName("Data REST: Test API endpoints")
// because we had to declare this test as an integration test suite,
// use rollbacks to avoid modifying contents of the database
@Transactional
@AutoConfigureMockMvc
// declare this test suite as integration tests, in order to load other required beans
// that would otherwise have not been loaded
@SpringBootTest
public class DataRestTest {
  private final MockMvc mvc;

  public DataRestTest(@Autowired MockMvc mvc) {
    this.mvc = mvc;
  }


  @DisplayName("[API] Query Article list")
  @Test
  void givenNothing_whenRequestingArticles_thenReturnsArticlesJsonResponse() throws Exception {
    // given
    // when / then
    mvc.perform(get("/api/articles"))
        .andExpect(status().isOk())
        // HAL contenttype not predefined, define custom mediatype instead
        .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
  }

  @DisplayName("[API] Query single Article")
  @Test
  void givenNothing_whenRequestingArticle_thenReturnsArticleJsonResponse() throws Exception {
    // given
    // when / then
    // assumes Article exists in database
    mvc.perform(get("/api/articles/1"))
        .andExpect(status().isOk())
        // HAL contenttype not predefined, define custom mediatype instead
        .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
  }

  @DisplayName("[API] Query list of ArticleComments given Article")
  @Test
  void givenNothing_whenRequestingArticleCommentsFromArticle_thenReturnsArticleCommentsJsonResponse() throws Exception {
    // given
    // when / then
    mvc.perform(get("/api/articles/1/articleComments"))
        .andExpect(status().isOk())
        // HAL contenttype not predefined, define custom mediatype instead
        .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
  }

  @DisplayName("[API] Query ArticleComments list")
  @Test
  void givenNothing_whenRequestingArticleComments_thenReturnsArticleCommentsJsonResponse() throws Exception {
    // given
    // when / then
    mvc.perform(get("/api/articleComments"))
        .andExpect(status().isOk())
        // HAL contenttype not predefined, define custom mediatype instead
        .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
  }

  @DisplayName("[API] Query single ArticleComment")
  @Test
  void givenNothing_whenRequestingArticleComment_thenReturnsArticleCommentJsonResponse() throws Exception {
    // given
    // when / then
    // assumes ArticleComment exists in database
    mvc.perform(get("/api/articleComments/1"))
        .andExpect(status().isOk())
        // HAL contenttype not predefined, define custom mediatype instead
        .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
  }


  @DisplayName("[API] Don't expose user-related endpoints")
  @Test
  void givenNothing_whenRequestingUserAccounts_thenThrowsException() throws Exception {
    // Given

    // When & Then
    mvc.perform(get("/api/userAccounts")).andExpect(status().isNotFound());
    mvc.perform(post("/api/userAccounts")).andExpect(status().isNotFound());
    mvc.perform(put("/api/userAccounts")).andExpect(status().isNotFound());
    mvc.perform(patch("/api/userAccounts")).andExpect(status().isNotFound());
    mvc.perform(delete("/api/userAccounts")).andExpect(status().isNotFound());
    mvc.perform(head("/api/userAccounts")).andExpect(status().isNotFound());
  }
}
