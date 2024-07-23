package com.example.boardservice.dto.response;

import com.example.boardservice.dto.ArticleCommentDto;

import java.io.Serializable;
import java.time.LocalDateTime;

// the main reason for implementing separate response dtos is to decouple the original dtos from the controller layer
// because we have implemented a special response dto for use in controllers, the controller layer can have zero
// knowledge of the original dtos
// the service layer is the only layer that has knowledge of both dtos and domain
public record ArticleCommentResponse(
    Long id,
    String content,
    LocalDateTime createdAt,
    String email,
    String nickname,
    String userId
) implements Serializable {

  public static ArticleCommentResponse of(Long id, String content, LocalDateTime createdAt, String email, String nickname, String userId) {
    return new ArticleCommentResponse(id, content, createdAt, email, nickname, userId);
  }

  public static ArticleCommentResponse from(ArticleCommentDto dto) {
    String nickname = dto.userAccountDto().nickname();
    if (nickname == null || nickname.isBlank()) {
      nickname = dto.userAccountDto().userId();
    }

    return new ArticleCommentResponse(
        dto.id(),
        dto.content(),
        dto.createdAt(),
        dto.userAccountDto().email(),
        nickname,
        dto.userAccountDto().userId()
    );
  }

}