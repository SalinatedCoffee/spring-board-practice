package com.example.boardservice.service;

import com.example.boardservice.domain.ArticleComment;
import com.example.boardservice.dto.ArticleCommentDto;
import com.example.boardservice.repository.ArticleCommentRepository;
import com.example.boardservice.repository.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {

  private final ArticleRepository articleRepository;
  private final ArticleCommentRepository articleCommentRepository;

  @Transactional(readOnly = true)
  public List<ArticleCommentDto> searchArticleComments (Long articleId) {
    // convert raw domain objects from repository to corresponding dto
    return articleCommentRepository.findByArticle_Id(articleId)
        .stream()
        .map(ArticleCommentDto::from)
        .toList();
  }

  public void saveArticleComment(ArticleCommentDto dto) {
    try {
      articleCommentRepository.save(dto.toEntity(articleRepository.getReferenceById(dto.articleId())));
    } catch (EntityNotFoundException e) {
      log.warn("Failed to save comment. Could not find article of comment - dto: {}", dto);
    }
  }
  public void updateArticleComment(ArticleCommentDto dto) {
    try {
      // find updated comment's id in repository
      ArticleComment articleComment = articleCommentRepository.getReferenceById(dto.id());
      // if updated body is not empty, update comment body by reflecting change in repository
      if (dto.content() != null) {
        articleComment.setContent(dto.content());
      }
    } catch (EntityNotFoundException e) {
      log.warn("Failed to update comment. Could not find comment - dto: {}", dto);
    }
  }
  public void deleteArticleComment(Long articleCommentId) {
    articleCommentRepository.deleteById(articleCommentId);
  }

}
