package com.example.boardservice.service;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.type.SearchType;
import com.example.boardservice.dto.ArticleDto;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.repository.ArticleRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// what are services anyway, and what logic should live in the service layer?
// read this: https://stackoverflow.com/questions/16862611/domain-dao-and-service-layers

// mark class for logging through Lombok
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  @Transactional(readOnly = true)
  public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
    if (searchKeyword == null || searchKeyword.isBlank()) {
      return articleRepository.findAll(pageable).map(ArticleDto::from);
    }

    return switch (searchType) {
      case TITLE -> articleRepository.findByTitleContaining(searchKeyword, pageable).map(ArticleDto::from);
      case CONTENT -> articleRepository.findByContentContaining(searchKeyword, pageable).map(ArticleDto::from);
      case ID -> articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable).map(ArticleDto::from);
      case NICKNAME -> articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(ArticleDto::from);
      case HASHTAG -> articleRepository.findByHashtag('#'+searchKeyword, pageable).map(ArticleDto::from);
    };
  }

  @Transactional(readOnly = true)
  public ArticleWithCommentsDto getArticle(Long articleId) {
    return articleRepository.findById(articleId)
        .map(ArticleWithCommentsDto::from)
        .orElseThrow(() -> new EntityNotFoundException("Article does not exist - articleId: " + articleId));
  }

  public void saveArticle(ArticleDto dto) {
    articleRepository.save(dto.toEntity());
  }

  public void updateArticle(ArticleDto dto) {
    try {
      Article article = articleRepository.getReferenceById(dto.id());
      // guard against null values for non-nullable fields
      if (dto.title() != null) {
        article.setTitle(dto.title());
      }
      if (dto.content() != null) {
        article.setContent(dto.content());
      }
      article.setHashtag(dto.hashtag());
      // no need for articleRepository.save(article); since ArticleService is annotated with @Transactional
      // changes to entity will be detected automatically and necessary queries will be sent to persistence
      // layer accordingly
    }
    catch (EntityNotFoundException e) {
      // @Slf4j
      log.warn("Failed to update article: article not found - dto: {}", dto);
    }
  }

  public void deleteArticle(Long articleId) {
    articleRepository.deleteById(articleId);
  }

  @Transactional(readOnly = true)
  public Page<ArticleDto> searchArticlesViaHashtag(String hashtag, Pageable pageable) {
    if (hashtag == null || hashtag.isBlank()) {
      return Page.empty(pageable);
    }
    return articleRepository.findByHashtag(hashtag, pageable).map(ArticleDto::from);
  }

  public List<String> getHashtags() {
    return articleRepository.findAllDistinctHashtags();
  }
}
