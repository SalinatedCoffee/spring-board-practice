package com.example.boardservice.controller;

import com.example.boardservice.config.SecurityConfig;
import com.example.boardservice.dto.ArticleCommentDto;
import com.example.boardservice.dto.request.ArticleCommentRequest;
import com.example.boardservice.service.ArticleCommentService;
import com.example.boardservice.util.FormDataEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@DisplayName("View controller: article comments")
// configure tests to go through spring security
@Import({SecurityConfig.class, FormDataEncoder.class})
// only load relevant bean
@WebMvcTest(ArticleCommentController.class)
class ArticleCommentControllerTest {
  private final MockMvc mvc;
  private final FormDataEncoder formDataEncoder;

  @MockBean private ArticleCommentService articleCommentService;

  public ArticleCommentControllerTest(@Autowired MockMvc mvc, @Autowired FormDataEncoder formDataEncoder) {
    this.mvc = mvc;
    this.formDataEncoder = formDataEncoder;
  }

  @DisplayName("[View][POST] Post comment - normal call")
  @Test
  void givenArticleCommentInfo_whenRequesting_thenSavesNewArticleComment() throws Exception {
    // Given
    long articleId = 1L;
    ArticleCommentRequest request = ArticleCommentRequest.of(articleId, "test comment");
    willDoNothing().given(articleCommentService).saveArticleComment(any(ArticleCommentDto.class));

    // When & Then
    mvc.perform(
            post("/comments/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(formDataEncoder.encode(request))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/articles/" + articleId))
        .andExpect(redirectedUrl("/articles/" + articleId));
    then(articleCommentService).should().saveArticleComment(any(ArticleCommentDto.class));
  }

  @DisplayName("[View][GET] Delete comment - normal call")
  @Test
  void givenArticleCommentIdToDelete_whenRequesting_thenDeletesArticleComment() throws Exception {
    // Given
    long articleId = 1L;
    long articleCommentId = 1L;
    willDoNothing().given(articleCommentService).deleteArticleComment(articleCommentId);

    // When & Then
    mvc.perform(
            post("/comments/" + articleCommentId + "/delete")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(formDataEncoder.encode(Map.of("articleId", articleId)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/articles/" + articleId))
        .andExpect(redirectedUrl("/articles/" + articleId));
    then(articleCommentService).should().deleteArticleComment(articleCommentId);
  }


}