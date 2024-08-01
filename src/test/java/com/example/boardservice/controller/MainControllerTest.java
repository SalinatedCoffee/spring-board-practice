package com.example.boardservice.controller;

import com.example.boardservice.config.SecurityConfig;
import com.example.boardservice.config.TestSecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(MainController.class)
class MainControllerTest {

  private final MockMvc mvc;

  public MainControllerTest(@Autowired MockMvc mvc) {
    this.mvc = mvc;
  }

  @Test
  void givenNothing_whenRequestingRootPage_thenRedirectsToArticlesPage() throws Exception {
    // g

    // wt
    mvc.perform(get("/"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/articles"))
        .andExpect(redirectedUrl("/articles"))
        .andDo(MockMvcResultHandlers.print());
  }
}