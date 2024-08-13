package com.example.boardservice.dto.response;

import com.example.boardservice.dto.ArticleCommentDto;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.dto.HashtagDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public record ArticleWithCommentsResponse(
    Long id,
    String title,
    String content,
    Set<String> hashtags,
    LocalDateTime createdAt,
    String email,
    String nickname,
    String userId,
    Set<ArticleCommentResponse> articleCommentsResponse
) implements Serializable {

  public static ArticleWithCommentsResponse of(Long id, String title, String content, Set<String> hashtags, LocalDateTime createdAt, String email, String nickname, String userId, Set<ArticleCommentResponse> articleCommentResponses) {
    return new ArticleWithCommentsResponse(id, title, content, hashtags, createdAt, email, nickname, userId, articleCommentResponses);
  }

  public static ArticleWithCommentsResponse from(ArticleWithCommentsDto dto) {
    String nickname = dto.userAccountDto().nickname();
    if (nickname == null || nickname.isBlank()) {
      nickname = dto.userAccountDto().userId();
    }

    return new ArticleWithCommentsResponse(
        dto.id(),
        dto.title(),
        dto.content(),
        dto.hashtagDtos().stream()
            .map(HashtagDto::hashtagName)
            .collect(Collectors.toUnmodifiableSet()),
        dto.createdAt(),
        dto.userAccountDto().email(),
        nickname,
        dto.userAccountDto().userId(),
        organizeChildComments(dto.articleCommentDtos())
    );
  }

  private static Set<ArticleCommentResponse> organizeChildComments(Set<ArticleCommentDto> dtos) {
    // convert set of articlecomment dtos into map of articlecommentresponses
    // where the keys are the ids of each articlecomment
    Map<Long, ArticleCommentResponse> map = dtos.stream()
        .map(ArticleCommentResponse::from)
        .collect(Collectors.toMap(ArticleCommentResponse::id, Function.identity()));

    // then find all child comments and 'register' them to their parents
    map.values().stream()
//        .filter(ArticleCommentResponse::hasParentComment)
        .filter(comment -> comment.hasParentComment())
        .forEach(comment -> {
          ArticleCommentResponse parentComment = map.get(comment.parentCommentId());
          parentComment.childComments().add(comment);
        });

    // finally, filter out comments that are not children and order the remaining comments,
    // sort them, then return an ordered set
    return map.values().stream()
        .filter(comment -> !comment.hasParentComment())
        .collect(Collectors.toCollection(() ->
            new TreeSet<>(Comparator
                .comparing(ArticleCommentResponse::createdAt)
                .reversed()
                .thenComparingLong(ArticleCommentResponse::id))));
    // essentially, the original set of all comments bound to the article are 'collapsed' into a 2-level
    // structure
  }
}