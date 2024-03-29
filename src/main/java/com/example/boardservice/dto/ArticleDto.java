package com.example.boardservice.dto;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.UserAccount;

import java.time.LocalDateTime;

public record ArticleDto(
    Long id,
    UserAccountDto userAccountDto,
    String title,
    String content,
    String hashtag,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime modifiedAt,
    String modifiedBy
) {
  public static ArticleDto of(UserAccountDto userAccountDto, String title, String content, String hashtag) {
    return new ArticleDto(null, userAccountDto, title, content, hashtag, null, null, null, null);
  }

  public static ArticleDto of(Long id, UserAccountDto userAccountDto, String title, String content, String hashtag, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
    return new ArticleDto(id, userAccountDto, title, content, hashtag, createdAt, createdBy, modifiedAt, modifiedBy);
  }

  // convert between Article entity and Article DTO
  // decouples article->DTO; Article entity can have zero knowledge of DTO
  // DTO still depends on entity
  public static ArticleDto from(Article entity) {
    return new ArticleDto(
        entity.getId(),
        UserAccountDto.from(entity.getUserAccount()),
        entity.getTitle(),
        entity.getContent(),
        entity.getHashtag(),
        entity.getCreatedAt(),
        entity.getCreatedBy(),
        entity.getModifiedAt(),
        entity.getModifiedBy()
    );
  }

  public Article toEntity(UserAccount userAccount) {
    return Article.of(
        userAccount,
        title,
        content,
        hashtag
    );
  }

}
