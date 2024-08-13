package com.example.boardservice.dto;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.ArticleComment;
import com.example.boardservice.domain.UserAccount;

import java.time.LocalDateTime;

// Java records are similar to Python's @dataclass; it represents a class that only carries immutable data
// read more on https://docs.oracle.com/en/java/javase/14/language/records.html
public record ArticleCommentDto(
    Long id,
    Long articleId,
    UserAccountDto userAccountDto,
    Long parentCommentId,
    String content,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime modifiedAt,
    String modifiedBy
) {

  public static ArticleCommentDto of(Long articleId, UserAccountDto userAccountDto, String content) {
    return ArticleCommentDto.of(articleId, userAccountDto, null, content);
  }

  public static ArticleCommentDto of(Long articleId, UserAccountDto userAccountDto, Long parentCommentId, String content) {
    return ArticleCommentDto.of(null, articleId, userAccountDto, parentCommentId, content, null, null, null, null);
  }

  public static ArticleCommentDto of(Long id, Long articleId, UserAccountDto userAccountDto, Long parentCommentId, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
    return new ArticleCommentDto(id, articleId, userAccountDto, parentCommentId, content, createdAt, createdBy, modifiedAt, modifiedBy);
  }

  public static ArticleCommentDto from(ArticleComment entity) {
    return new ArticleCommentDto(
        entity.getId(),
        entity.getArticle().getId(),
        UserAccountDto.from(entity.getUserAccount()),
        entity.getParentCommentId(),
        entity.getContent(),
        entity.getCreatedAt(),
        entity.getCreatedBy(),
        entity.getModifiedAt(),
        entity.getModifiedBy()
    );
  }

  public ArticleComment toEntity(Article article, UserAccount userAccount) {
    return ArticleComment.of(
        article,
        userAccount,
        content
    );
  }

}
