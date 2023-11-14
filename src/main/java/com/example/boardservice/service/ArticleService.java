package com.example.boardservice.service;

import com.example.boardservice.domain.type.SearchType;
import com.example.boardservice.dto.ArticleDto;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// what are services anyway, and what logic should live in the service layer?
// read this: https://stackoverflow.com/questions/16862611/domain-dao-and-service-layers

@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  @Transactional(readOnly = true)
  public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
    return Page.empty();
  }

  @Transactional(readOnly = true)
  public ArticleWithCommentsDto getArticle(Long articleId) {
    return null;
  }

  public void saveArticle(ArticleDto dto) {
  }

  public void updateArticle(ArticleDto dto) {
  }

  public void deleteArticle(Long articleId) {
  }
}