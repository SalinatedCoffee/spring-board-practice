package com.example.boardservice.dto.response;

import com.example.boardservice.dto.ArticleCommentDto;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.dto.HashtagDto;
import com.example.boardservice.dto.UserAccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO - test article response including its comments")
class ArticleWithCommentsResponseTest {

  @DisplayName("When converting dto of articlewithcomments without child comments to an api response, sort comments in desc. order of datetime and asc. order of comment id")
  @Test
  void givenArticleWithCommentsDtoWithoutChildComments_whenMapping_thenOrganizesCommentsWithCertainOrder() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    Set<ArticleCommentDto> articleCommentDtos = Set.of(
        createArticleCommentDto(1L, null, now),
        createArticleCommentDto(2L, null, now.plusDays(1L)),
        createArticleCommentDto(3L, null, now.plusDays(3L)),
        createArticleCommentDto(4L, null, now),
        createArticleCommentDto(5L, null, now.plusDays(5L)),
        createArticleCommentDto(6L, null, now.plusDays(4L)),
        createArticleCommentDto(7L, null, now.plusDays(2L)),
        createArticleCommentDto(8L, null, now.plusDays(7L))
    );
    ArticleWithCommentsDto input = createArticleWithCommentsDto(articleCommentDtos);

    // When
    ArticleWithCommentsResponse actual = ArticleWithCommentsResponse.from(input);

    // Then
    assertThat(actual.articleCommentsResponse())
        .containsExactly(
            createArticleCommentResponse(8L, null, now.plusDays(7L)),
            createArticleCommentResponse(5L, null, now.plusDays(5L)),
            createArticleCommentResponse(6L, null, now.plusDays(4L)),
            createArticleCommentResponse(3L, null, now.plusDays(3L)),
            createArticleCommentResponse(7L, null, now.plusDays(2L)),
            createArticleCommentResponse(2L, null, now.plusDays(1L)),
            createArticleCommentResponse(1L, null, now),
            createArticleCommentResponse(4L, null, now)
        );
  }

  @DisplayName("When converting dto of articlewithcomments to an api response, organize comments by their parent-child relationship")
  @Test
  void givenArticleWithCommentsDto_whenMapping_thenOrganizesParentAndChildCommentsWithCertainOrders() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    Set<ArticleCommentDto> articleCommentDtos = Set.of(
        createArticleCommentDto(1L, null, now),
        createArticleCommentDto(2L, 1L, now.plusDays(1L)),
        createArticleCommentDto(3L, 1L, now.plusDays(3L)),
        createArticleCommentDto(4L, 1L, now),
        createArticleCommentDto(5L, null, now.plusDays(5L)),
        createArticleCommentDto(6L, null, now.plusDays(4L)),
        createArticleCommentDto(7L, 6L, now.plusDays(2L)),
        createArticleCommentDto(8L, 6L, now.plusDays(7L))
    );
    ArticleWithCommentsDto input = createArticleWithCommentsDto(articleCommentDtos);

    // When
    ArticleWithCommentsResponse actual = ArticleWithCommentsResponse.from(input);

    // Then
    assertThat(actual.articleCommentsResponse())
        .containsExactly(
            createArticleCommentResponse(5L, null, now.plusDays(5)),
            createArticleCommentResponse(6L, null, now.plusDays(4)),
            createArticleCommentResponse(1L, null, now)
        )
        .flatExtracting(ArticleCommentResponse::childComments)
        .containsExactly(
            createArticleCommentResponse(7L, 6L, now.plusDays(2L)),
            createArticleCommentResponse(8L, 6L, now.plusDays(7L)),
            createArticleCommentResponse(4L, 1L, now),
            createArticleCommentResponse(2L, 1L, now.plusDays(1L)),
            createArticleCommentResponse(3L, 1L, now.plusDays(3L))
        );
  }

  @DisplayName("When converting dto of articlewithcomments to an api response, the depth of parent-child comment relationship should have no bound on depth")
  @Test
  void givenArticleWithCommentsDto_whenMapping_thenOrganizesParentAndChildCommentsWithoutDepthLimit() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    Set<ArticleCommentDto> articleCommentDtos = Set.of(
        createArticleCommentDto(1L, null, now),
        createArticleCommentDto(2L, 1L, now.plusDays(1L)),
        createArticleCommentDto(3L, 2L, now.plusDays(2L)),
        createArticleCommentDto(4L, 3L, now.plusDays(3L)),
        createArticleCommentDto(5L, 4L, now.plusDays(4L)),
        createArticleCommentDto(6L, 5L, now.plusDays(5L)),
        createArticleCommentDto(7L, 6L, now.plusDays(6L)),
        createArticleCommentDto(8L, 7L, now.plusDays(7L))
    );
    ArticleWithCommentsDto input = createArticleWithCommentsDto(articleCommentDtos);

    // When
    ArticleWithCommentsResponse actual = ArticleWithCommentsResponse.from(input);

    // Then
    Iterator<ArticleCommentResponse> iterator = actual.articleCommentsResponse().iterator();
    long i = 1L;
    while (iterator.hasNext()) {
      ArticleCommentResponse articleCommentResponse = iterator.next();
      assertThat(articleCommentResponse)
          .hasFieldOrPropertyWithValue("id", i)
          .hasFieldOrPropertyWithValue("parentCommentId", i == 1L ? null : i - 1L)
          .hasFieldOrPropertyWithValue("createdAt", now.plusDays(i - 1L));

      iterator = articleCommentResponse.childComments().iterator();
      i++;
    }
  }


  private ArticleWithCommentsDto createArticleWithCommentsDto(Set<ArticleCommentDto> articleCommentDtos) {
    return ArticleWithCommentsDto.of(
        1L,
        createUserAccountDto(),
        articleCommentDtos,
        "title",
        "content",
        Set.of(HashtagDto.of("java")),
        LocalDateTime.now(),
        "uno",
        LocalDateTime.now(),
        "uno"
    );
  }

  private UserAccountDto createUserAccountDto() {
    return UserAccountDto.of(
        "uno",
        "password",
        "uno@mail.com",
        "Uno",
        "This is a memo",
        LocalDateTime.now(),
        "uno",
        LocalDateTime.now(),
        "uno"
    );
  }

  private ArticleCommentDto createArticleCommentDto(Long id, Long parentCommentId, LocalDateTime createdAt) {
    return ArticleCommentDto.of(
        id,
        1L,
        createUserAccountDto(),
        parentCommentId,
        "test comment " + id,
        createdAt,
        "uno",
        createdAt,
        "uno"
    );
  }

  private ArticleCommentResponse createArticleCommentResponse(Long id, Long parentCommentId, LocalDateTime createdAt) {
    return ArticleCommentResponse.of(
        id,
        "test comment " + id,
        createdAt,
        "uno@mail.com",
        "Uno",
        "uno",
        parentCommentId
    );
  }

}