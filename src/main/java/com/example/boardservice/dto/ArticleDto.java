package com.example.boardservice.dto;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.UserAccount;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record ArticleDto(
    Long id,
    UserAccountDto userAccountDto,
    String title,
    String content,
    Set<HashtagDto> hashtagDtos,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime modifiedAt,
    String modifiedBy
) {
  public static ArticleDto of(UserAccountDto userAccountDto, String title, String content, Set<HashtagDto> hashtagDtos) {
    return new ArticleDto(null, userAccountDto, title, content, hashtagDtos, null, null, null, null);
  }

  public static ArticleDto of(Long id, UserAccountDto userAccountDto, String title, String content, Set<HashtagDto> hashtagDtos, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
    return new ArticleDto(id, userAccountDto, title, content, hashtagDtos, createdAt, createdBy, modifiedAt, modifiedBy);
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
        entity.getHashtags().stream()
            .map(HashtagDto::from)
            .collect(Collectors.toUnmodifiableSet()),
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
        content
    );
  }

}
