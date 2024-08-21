package com.example.boardservice.controller;

import com.example.boardservice.config.TestSecurityConfig;
import com.example.boardservice.service.ArticleService;
import com.example.boardservice.service.PaginationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("View controller: authentication")
@Import(TestSecurityConfig.class)
@WebMvcTest(AuthControllerTest.EmptyController.class)
class AuthControllerTest {

  private final MockMvc mvc;

  @MockBean
  private PaginationService paginationService;
  @MockBean
  private ArticleService articleService;

  public AuthControllerTest(@Autowired MockMvc mvc) {
    this.mvc = mvc;
  }

  @DisplayName("[View][GET] Login page (board) - normal call")
  @Test
  public void givenNothing_whenTryingToLogIn_thenReturnsLoginView() throws Exception {
    // given

    // when & then
    mvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    then(articleService).shouldHaveNoInteractions();
    then(paginationService).shouldHaveNoInteractions();
  }

  // optional, but use empty controller to make explicit the fact that this test suite does not require any controllers
  @TestComponent
  static class EmptyController {}
}
