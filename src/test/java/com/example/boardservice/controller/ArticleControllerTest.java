package com.example.boardservice.controller;

import com.example.boardservice.config.SecurityConfig;
import com.example.boardservice.domain.type.SearchType;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.service.ArticleService;
import com.example.boardservice.service.PaginationService;
import io.micrometer.core.instrument.search.Search;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("View controller: articles")
// configure tests to go through spring security
@Import(SecurityConfig.class)
// only load relevant bean
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

  private final MockMvc mvc;

  // mock ArticleController's articleService instead of using the actual thing during testing
  @MockBean private ArticleService articleService;
  @MockBean private PaginationService paginationService;

  public ArticleControllerTest(@Autowired MockMvc mvc) {
    this.mvc = mvc;
  }

  @DisplayName("[View][GET] Article list page (board) - normal call")
  @Test
  public void givenNothing_whenRequestingArticlesView_thenReturnsArticlesView() throws Exception {
    // given
    given(articleService.searchArticles(eq(null), eq(null), any(Pageable.class))).willReturn(Page.empty());
    // this test does nothing
    given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(0, 1, 2, 3, 4));
    // when & then
    mvc.perform(get("/articles"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/index"))
        .andExpect(model().attributeExists("articles"))
        .andExpect(model().attributeExists("paginationBarNumbers"))
        .andExpect(model().attributeExists("searchTypes"));
    then(articleService).should().searchArticles(eq(null), eq(null), any(Pageable.class));
    then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
  }


  @DisplayName("[View][GET] Article list page (board) - call with search keyword")
  @Test
  public void givenSearchKeyword_whenSearchingArticlesView_thenReturnsArticlesView() throws Exception {
    // given
    SearchType searchType = SearchType.TITLE;
    String searchKeyword = "title";
    given(articleService.searchArticles(eq(searchType), eq(searchKeyword),
        any(Pageable.class))).willReturn(Page.empty());
    // this test does nothing
    given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(0, 1, 2, 3, 4));
    // when & then
    mvc.perform(
        get("/articles")
            .queryParam("searchType", searchType.name())
            .queryParam("searchKeyword", searchKeyword))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/index"))
        .andExpect(model().attributeExists("articles"))
        .andExpect(model().attributeExists("searchTypes"));
    then(articleService).should().searchArticles(eq(searchType), eq(searchKeyword), any(Pageable.class));
    then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
  }


  @DisplayName("[View][GET] Article list page (board) - pagination and sorting")
  @Test
  void givenPagingAndSortingParams_whenSearchingArticlesView_thenReturnsArticlesView() throws Exception {
    // Given
    String sortName = "title";
    String direction = "desc";
    int pageNumber = 0;
    int pageSize = 5;
    Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc(sortName)));
    List<Integer> barNumbers = List.of(1, 2, 3, 4, 5);
    given(articleService.searchArticles(null, null, pageable)).willReturn(Page.empty());
    given(paginationService.getPaginationBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages())).willReturn(barNumbers);

    // When & Then
    mvc.perform(
            get("/articles")
                .queryParam("page", String.valueOf(pageNumber))
                .queryParam("size", String.valueOf(pageSize))
                .queryParam("sort", sortName + "," + direction)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/index"))
        .andExpect(model().attributeExists("articles"))
        .andExpect(model().attribute("paginationBarNumbers", barNumbers));
    then(articleService).should().searchArticles(null, null, pageable);
    then(paginationService).should().getPaginationBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages());
  }

  @DisplayName("[View][GET] Single article page - normal call")
  @Test
  public void givenNothing_whenRequestingArticleView_thenReturnsArticleView() throws Exception {
    // given
    Long articleId = 1L;
    // articleService, when given .getArticle(articleId), should return create...Dto().
    // so mock articleService accordingly
    given(articleService.getArticle(articleId)).willReturn(createArticleWithCommentsDto());
    // when & then
    mvc.perform(get("/articles/" + articleId))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/detail"))
        .andExpect(model().attributeExists("article"))
        .andExpect(model().attributeExists("articleComments"));
    then(articleService).should().getArticle(articleId);
  }

  @Disabled("Not yet implemented")
  @DisplayName("[View][GET] Article search page - normal call")
  @Test
  public void givenNothing_whenRequestingArticleSearchView_thenReturnsArticleSearchView() throws Exception {
    // given
    // when & then
    mvc.perform(get("/articles/search"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/search"));
  }

  @DisplayName("[View][GET] Article hashtag search page - normal call")
  @Test
  public void givenNothing_whenRequestingArticleSearchHashtagView_thenReturnsArticleHashtagSearchView() throws Exception {
    // given
    List<String> hashtags = List.of("#java", "#spring", "#boot");
    given(articleService.searchArticlesViaHashtag(eq(null), any(Pageable.class))).willReturn(Page.empty());
    given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));
    given(articleService.getHashtags()).willReturn(hashtags);
    // when & then
    mvc.perform(get("/articles/search-hashtag"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/search-hashtag"))
        .andExpect(model().attribute("articles", Page.empty()))
        .andExpect(model().attribute("hashtags", hashtags))
        .andExpect(model().attributeExists("paginationBarNumbers"));
    then(articleService).should().searchArticlesViaHashtag(eq(null), any(Pageable.class));
    then(articleService).should().getHashtags();
    then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
  }

  @DisplayName("[View][GET] Article hashtag search page - normal call with hashtag keyword")
  @Test
  public void givenHashtag_whenRequestingArticleSearchHashtagView_thenReturnsArticleHashtagSearchView() throws Exception {
    // given
    String hashtag = "#java";
    List<String> hashtags = List.of("#java", "#spring", "#boot");
    given(articleService.searchArticlesViaHashtag(eq(hashtag), any(Pageable.class))).willReturn(Page.empty());
    given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));
    given(articleService.getHashtags()).willReturn(hashtags);
    // when & then
    mvc.perform(get("/articles/search-hashtag").queryParam("searchKeyword", hashtag))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/search-hashtag"))
        .andExpect(model().attribute("articles", Page.empty()))
        .andExpect(model().attribute("hashtags", hashtags))
        .andExpect(model().attributeExists("paginationBarNumbers"))
        .andExpect(model().attribute("searchType", SearchType.HASHTAG));
    then(articleService).should().searchArticlesViaHashtag(eq(hashtag), any(Pageable.class));
    then(articleService).should().getHashtags();
    then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
  }

  private ArticleWithCommentsDto createArticleWithCommentsDto() {
    return ArticleWithCommentsDto.of(
        1L,
        createUserAccountDto(),
        Set.of(),
        "title",
        "content",
        "#java",
        LocalDateTime.now(),
        "uno",
        LocalDateTime.now(),
        "uno"
    );
  }

  private UserAccountDto createUserAccountDto() {
    return UserAccountDto.of(
        "uno",
        "pw",
        "uno@mail.com",
        "Uno",
        "memo",
        LocalDateTime.now(),
        "uno",
        LocalDateTime.now(),
        "uno"
    );
  }
}