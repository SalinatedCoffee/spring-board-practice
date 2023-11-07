package com.example.boardservice.service;

import com.example.boardservice.dto.ArticleCommentDto;
import com.example.boardservice.repository.ArticleCommentRepository;
import com.example.boardservice.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {

  private final ArticleRepository articleRepository;
  private final ArticleCommentRepository articleCommentRepository;

  @Transactional(readOnly = true)
  public List<ArticleCommentDto> searchArticleComments (Long articleId) {
    return List.of();
  }

  public void saveArticleComment(ArticleCommentDto dto) {
  }
  public void updateArticleComment(ArticleCommentDto dto) {
  }
  public void deleteArticleComment(Long articleCommentId) {
  }

}
