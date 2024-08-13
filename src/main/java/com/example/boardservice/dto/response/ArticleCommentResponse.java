package com.example.boardservice.dto.response;

import com.example.boardservice.domain.ArticleComment;
import com.example.boardservice.dto.ArticleCommentDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

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
    String userId,
    Long parentCommentId,
    Set<ArticleCommentResponse> childComments
) implements Serializable {

  public static ArticleCommentResponse of(Long id, String content, LocalDateTime createdAt, String email, String nickname, String userId) {
    return ArticleCommentResponse.of(id, content, createdAt, email, nickname, userId, null);
  }

  public static ArticleCommentResponse of(Long id, String content, LocalDateTime createdAt, String email, String nickname, String userId, Long parentCommentId) {
    // custom comparator that compares comments first based on created datetime, and then their comment ids
    Comparator<ArticleCommentResponse> childCommentComparator = Comparator
        .comparing(ArticleCommentResponse::createdAt)
        .thenComparingLong(ArticleCommentResponse::id);
    // custom comparator above is used here to enforce ordering of elements in a set
    return new ArticleCommentResponse(id, content, createdAt, email, nickname, userId, parentCommentId, new TreeSet<>(childCommentComparator));
  }

  public static ArticleCommentResponse from(ArticleCommentDto dto) {
    String nickname = dto.userAccountDto().nickname();
    if (nickname == null || nickname.isBlank()) {
      nickname = dto.userAccountDto().userId();
    }

    return ArticleCommentResponse.of(
        dto.id(),
        dto.content(),
        dto.createdAt(),
        dto.userAccountDto().email(),
        nickname,
        dto.userAccountDto().userId(),
        dto.parentCommentId()
    );
  }

  public boolean hasParentComment() {
    return parentCommentId != null;
  }
}