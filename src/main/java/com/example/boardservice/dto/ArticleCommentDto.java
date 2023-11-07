package com.example.boardservice.dto;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.ArticleComment;

import java.time.LocalDateTime;

// Java records are similar to Python's @dataclass; it represents a class that only carries immutable data
// read more on https://docs.oracle.com/en/java/javase/14/language/records.html
public record ArticleCommentDto(
    Long id,
    Long articleId,
    UserAccountDto userAccountDto,
    String content,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime modifiedAt,
    String modifiedBy
) {
  public static ArticleCommentDto of(Long id, Long articleId, UserAccountDto userAccountDto, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
    return new ArticleCommentDto(id, articleId, userAccountDto, content, createdAt, createdBy, modifiedAt, modifiedBy);
  }

  public static ArticleCommentDto from(ArticleComment entity) {
    return new ArticleCommentDto(
        entity.getId(),
        entity.getArticle().getId(),
        UserAccountDto.from(entity.getUserAccount()),
        entity.getContent(),
        entity.getCreatedAt(),
        entity.getCreatedBy(),
        entity.getModifiedAt(),
        entity.getModifiedBy()
    );
  }

  public ArticleComment toEntity(Article entity) {
    return ArticleComment.of(
        entity,
        userAccountDto.toEntity(),
        content
    );
  }

}
