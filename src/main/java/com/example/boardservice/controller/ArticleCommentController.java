package com.example.boardservice.controller;

import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.dto.request.ArticleCommentRequest;
import com.example.boardservice.service.ArticleCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class ArticleCommentController {

  private final ArticleCommentService articleCommentService;

  @PostMapping("/new")
  public String postNewArticleComment(ArticleCommentRequest articleCommentRequest) {
    articleCommentService.saveArticleComment(articleCommentRequest.toDto(UserAccountDto.of("uno", "pwd", "uno@mail.com", null, null)));
    return "redirect:/articles/" + articleCommentRequest.articleId();
  }

  @PostMapping ("/{commentId}/delete")
  public String deleteArticleComment(@PathVariable Long commentId, Long articleId) {
    articleCommentService.deleteArticleComment(commentId);
    return "redirect:/articles/" + articleId;
  }
}
