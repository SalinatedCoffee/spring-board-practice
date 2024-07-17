package com.example.boardservice.controller;

import com.example.boardservice.config.SecurityConfig;
import com.example.boardservice.config.TestSecurityConfig;
import com.example.boardservice.domain.constant.FormStatus;
import com.example.boardservice.domain.constant.SearchType;
import com.example.boardservice.dto.ArticleDto;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.dto.request.ArticleRequest;
import com.example.boardservice.dto.response.ArticleResponse;
import com.example.boardservice.service.ArticleService;
import com.example.boardservice.service.PaginationService;
import com.example.boardservice.util.FormDataEncoder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View controller: articles")
// configure tests to go through spring security
@Import({TestSecurityConfig.class, FormDataEncoder.class})
// only load relevant bean
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

  private final MockMvc mvc;
  private final FormDataEncoder formDataEncoder;

  // mock ArticleController's articleService instead of using the actual thing during testing
  @MockBean private ArticleService articleService;
  @MockBean private PaginationService paginationService;

  public ArticleControllerTest(@Autowired MockMvc mvc, @Autowired FormDataEncoder formDataEncoder) {
    this.mvc = mvc;
    this.formDataEncoder = formDataEncoder;
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

  @DisplayName("[View][GET] Single article page - redirect to login page when not authenticated")
  @Test
  public void givenNothing_whenRequestingArticlePage_thenRedirectsToLoginPage() throws Exception {
    // given
    long articleId = 1L;
    // when & then
    mvc.perform(get("/articles/" + articleId))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    then(articleService).shouldHaveNoInteractions();
    then(articleService).shouldHaveNoInteractions();
  }

  @WithMockUser 
  @DisplayName("[View][GET] Single article page - normal call when authenticated")
  @Test
  public void givenNothing_whenRequestingArticleView_thenReturnsArticleView() throws Exception {
    // given
    Long articleId = 1L;
    // articleService, when given .getArticle(articleId), should return create...Dto().
    // so mock articleService accordingly
    given(articleService.getArticleWithComments(articleId)).willReturn(createArticleWithCommentsDto());
    // when & then
    mvc.perform(get("/articles/" + articleId))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/detail"))
        .andExpect(model().attributeExists("article"))
        .andExpect(model().attributeExists("articleComments"));
    then(articleService).should().getArticleWithComments(articleId);
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

  // just need to mock authenticated user, doesn't necessarily need to be in database
  @WithMockUser
  @DisplayName("[View][GET] Post new article page")
  @Test
  void givenNothing_whenRequesting_thenReturnsNewArticlePage() throws Exception {
    // Given

    // When & Then
    mvc.perform(get("/articles/form"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/form"))
        .andExpect(model().attribute("formStatus", FormStatus.CREATE));
  }
  // code that is being tested needs information about an authenticated user actually in database
  // so mock that user using @WithUserDetails
  // also specify when the mock should be set up
  // notice that the user 'unoTest' has been mocked inside TestSecurityConfig, and doesn't actually exist in the mysql db
  @WithUserDetails(value = "unoTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("[View][POST] Register new article - normal call")
  @Test
  void givenNewArticleInfo_whenRequesting_thenSavesNewArticle() throws Exception {
    // Given
    ArticleRequest articleRequest = ArticleRequest.of("new title", "new content", "#new");
    willDoNothing().given(articleService).saveArticle(any(ArticleDto.class));

    // When & Then
    mvc.perform(
            post("/articles/form")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(formDataEncoder.encode(articleRequest))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/articles"))
        .andExpect(redirectedUrl("/articles"));
    then(articleService).should().saveArticle(any(ArticleDto.class));
  }

  @DisplayName("[View][GET] Edit article page redirects to login page when not authenticated")
  @Test
  void givenNothing_whenRequesting_thenRedirectsToLoginPage() throws Exception {
    // Given
    long articleId = 1L;

    // When & Then
    mvc.perform(get("/articles/" + articleId + "/form"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    then(articleService).shouldHaveNoInteractions();
  }

  @WithUserDetails(value = "unoTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("[View][GET] Edit article page when authenticated")
  @Test
  void givenNothing_whenRequesting_thenReturnsUpdatedArticlePage() throws Exception {
    // Given
    long articleId = 1L;
    ArticleDto dto = createArticleDto();
    given(articleService.getArticle(articleId)).willReturn(dto);

    // When & Then
    mvc.perform(get("/articles/" + articleId + "/form"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(view().name("articles/form"))
        .andExpect(model().attribute("article", ArticleResponse.from(dto)))
        .andExpect(model().attribute("formStatus", FormStatus.UPDATE));
    then(articleService).should().getArticle(articleId);
  }

  @WithUserDetails(value = "unoTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("[View][POST] Edit (update) article - normal call when authenticated")
  @Test
  void givenUpdatedArticleInfo_whenRequesting_thenUpdatesNewArticle() throws Exception {
    // Given
    long articleId = 1L;
    ArticleRequest articleRequest = ArticleRequest.of("new title", "new content", "#new");
    willDoNothing().given(articleService).updateArticle(eq(articleId), any(ArticleDto.class));

    // When & Then
    mvc.perform(
            post("/articles/" + articleId + "/form")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(formDataEncoder.encode(articleRequest))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/articles/" + articleId))
        .andExpect(redirectedUrl("/articles/" + articleId));
    then(articleService).should().updateArticle(eq(articleId), any(ArticleDto.class));
  }

  @WithUserDetails(value = "unoTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("[View][POST] Delete article - normal call when authenticated ")
  @Test
  void givenArticleIdToDelete_whenRequesting_thenDeletesArticle() throws Exception {
    // Given
    long articleId = 1L;
    String userId = "unoTest";
    willDoNothing().given(articleService).deleteArticle(articleId, userId);

    // When & Then
    mvc.perform(
            post("/articles/" + articleId + "/delete")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/articles"))
        .andExpect(redirectedUrl("/articles"));
    then(articleService).should().deleteArticle(articleId, userId);
  }


  private ArticleDto createArticleDto() {
    return ArticleDto.of(
        createUserAccountDto(),
        "title",
        "content",
        "#java"
    );
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