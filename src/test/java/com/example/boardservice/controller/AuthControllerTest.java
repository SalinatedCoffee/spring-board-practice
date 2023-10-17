package com.example.boardservice.controller;

import com.example.boardservice.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View controller: authentication")
@Import(SecurityConfig.class)
@WebMvcTest
class AuthControllerTest {
  private final MockMvc mvc;

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
  }
}
