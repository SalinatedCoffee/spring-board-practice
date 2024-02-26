package com.example.boardservice.controller;

import com.example.boardservice.domain.constant.FormStatus;
import com.example.boardservice.domain.constant.SearchType;
import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.dto.request.ArticleRequest;
import com.example.boardservice.dto.response.ArticleResponse;
import com.example.boardservice.dto.response.ArticleWithCommentsResponse;
import com.example.boardservice.service.ArticleService;
import com.example.boardservice.service.PaginationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/articles")
@Controller
public class ArticleController {

  private final ArticleService articleService;
  private final PaginationService paginationService;

  @GetMapping
  public String articles(
      @RequestParam(required = false) SearchType searchType,
      @RequestParam(required = false) String searchKeyword,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
      ModelMap map
  ) {
    Page<ArticleResponse> articles = articleService.searchArticles(searchType, searchKeyword, pageable).map(ArticleResponse::from);
    List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());

    map.addAttribute("articles", articles);
    map.addAttribute("paginationBarNumbers", barNumbers);
    map.addAttribute("searchTypes", SearchType.values());

    return "articles/index";
  }

  @GetMapping("/{articleId}")
  public String article(@PathVariable Long articleId, ModelMap map) {
    ArticleWithCommentsResponse article = ArticleWithCommentsResponse.from(articleService.getArticleWithComments(articleId));

    map.addAttribute("article", article);
    map.addAttribute("articleComments", article.articleCommentsResponse());
    map.addAttribute("totalCount", articleService.getArticleCount());

    return "articles/detail";
  }

  @GetMapping("/search-hashtag")
  public String searchArticleHashtag(@RequestParam(required = false) String searchKeyword,
                              @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                              ModelMap map) {
    Page<ArticleResponse> articles = articleService.searchArticlesViaHashtag(searchKeyword, pageable).map(ArticleResponse::from);
    List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());
    List<String> hashtags = articleService.getHashtags();
    map.addAttribute("articles", articles);
    map.addAttribute("hashtags", hashtags);
    map.addAttribute("paginationBarNumbers", barNumbers);
    map.addAttribute("searchType", SearchType.HASHTAG);

    return "articles/search-hashtag";
  }

  @GetMapping("/form")
  public String articleForm(ModelMap map) {
    map.addAttribute("formStatus", FormStatus.CREATE);

    return "articles/form";
  }

  @PostMapping("/form")
  public String postNewArticle(ArticleRequest articleRequest) {
    // TODO: Need to provide authentication credentials.
    articleService.saveArticle(articleRequest.toDto(UserAccountDto.of(
        "uno", "asdf1234", "uno@mail.com", "Uno", "memo"
    )));

    return "redirect:/articles";
  }

  @GetMapping("/{articleId}/form")
  public String updateArticleForm(@PathVariable Long articleId, ModelMap map) {
    ArticleResponse article = ArticleResponse.from(articleService.getArticle(articleId));

    map.addAttribute("article", article);
    map.addAttribute("formStatus", FormStatus.UPDATE);

    return "articles/form";
  }

  @PostMapping ("/{articleId}/form")
  public String updateArticle(@PathVariable Long articleId, ArticleRequest articleRequest) {
    // TODO: Need to provide authentication credentials
    articleService.updateArticle(articleId, articleRequest.toDto(UserAccountDto.of(
        "uno", "asdf1234", "uno@mail.com", "Uno", "memo"
    )));

    return "redirect:/articles/" + articleId;
  }

  @PostMapping ("/{articleId}/delete")
  public String deleteArticle(@PathVariable Long articleId) {
    // TODO: Need to provide authentication credentials
    articleService.deleteArticle(articleId);

    return "redirect:/articles";
  }
}
