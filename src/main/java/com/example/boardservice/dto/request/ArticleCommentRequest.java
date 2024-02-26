package com.example.boardservice.dto.request;

import com.example.boardservice.dto.ArticleCommentDto;
import com.example.boardservice.dto.UserAccountDto;

public record ArticleCommentRequest(Long articleId, String content) {
  public static ArticleCommentRequest of (Long articleId, String content) {
    return new ArticleCommentRequest(articleId, content);
  }

  public ArticleCommentDto toDto(UserAccountDto userAccountDto) {
    return ArticleCommentDto.of(
        articleId,
        userAccountDto,
        content
    );
  }
}
