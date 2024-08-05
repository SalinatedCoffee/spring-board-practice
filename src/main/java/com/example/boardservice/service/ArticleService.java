package com.example.boardservice.service;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.Hashtag;
import com.example.boardservice.domain.UserAccount;
import com.example.boardservice.domain.constant.SearchType;
import com.example.boardservice.dto.ArticleDto;
import com.example.boardservice.dto.ArticleWithCommentsDto;
import com.example.boardservice.repository.ArticleRepository;
import com.example.boardservice.repository.HashtagRepository;
import com.example.boardservice.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// what are services anyway, and what logic should live in the service layer?
// read this: https://stackoverflow.com/questions/16862611/domain-dao-and-service-layers

// mark class for logging through Lombok
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  private final UserAccountRepository userAccountRepository;
  private final HashtagRepository hashtagRepository;
  private final HashtagService hashtagService;

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
      case HASHTAG -> articleRepository.findByHashtagNames(Arrays.stream(searchKeyword.split(" ")).toList(), pageable)
          .map(ArticleDto::from);
    };
  }

  @Transactional(readOnly = true)
  public ArticleWithCommentsDto getArticleWithComments(Long articleId) {
    return articleRepository.findById(articleId)
        .map(ArticleWithCommentsDto::from)
        .orElseThrow(() -> new EntityNotFoundException("Article does not exist - articleId: " + articleId));
  }

  @Transactional(readOnly = true)
  public ArticleDto getArticle(Long articleId) {
    return articleRepository.findById(articleId)
        .map(ArticleDto::from)
        .orElseThrow(() -> new EntityNotFoundException("Article does not exist - articleId: " + articleId));
  }
  @Transactional(readOnly = true)
  public Page<ArticleDto> searchArticlesViaHashtag(String hashtagName, Pageable pageable) {
    if (hashtagName == null || hashtagName.isBlank()) {
      return Page.empty(pageable);
    }
    return articleRepository.findByHashtagNames(List.of(hashtagName), pageable)
            .map(ArticleDto::from);
  }

  public void saveArticle(ArticleDto dto) {
    UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());
    Set<Hashtag> hashtags = renewHashtagsFromContent(dto.content());
    Article article = dto.toEntity(userAccount);
    article.addHashtags(hashtags);

    articleRepository.save(article);
  }

  public void updateArticle(Long articleId, ArticleDto dto) {
    try {
      Article article = articleRepository.getReferenceById(articleId);
      UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());
      // only want the original author to be able to edit article
      if (article.getUserAccount().equals(userAccount)) {
        // guard against null values for non-nullable fields
        if (dto.title() != null) {
          article.setTitle(dto.title());
        }
        if (dto.content() != null) {
          article.setContent(dto.content());
        }
//        article.setHashtag(dto.hashtag()); TODO
        // no need for articleRepository.save(article); since ArticleService is annotated with @Transactional
        // changes to entity will be detected automatically and necessary queries will be sent to persistence
        // layer accordingly

        // get set of entity ids of hashtags registered to this article
        Set<Long> hashtagIds = article.getHashtags().stream()
                .map(Hashtag::getId)
                .collect(Collectors.toUnmodifiableSet());
        // remove all hashtags from current article
        article.clearHashtags();
        articleRepository.flush();
        // check each hashtag and remove it from db if it is not bound to any articles
        hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles);
        // get updated list of hashtags for current article
        Set<Hashtag> hashtags = renewHashtagsFromContent(dto.content());
        // bind them to current article
        article.addHashtags(hashtags);
      }
    }
    catch (EntityNotFoundException e) {
      // @Slf4j
      log.warn("Failed to update article: information required to update article not found - {}", e.getLocalizedMessage());
    }
  }

  public void deleteArticle(Long articleId, String userId) {
    Article article = articleRepository.getReferenceById(articleId);
    Set<Long> hashtagIds = article.getHashtags().stream()
                    .map(Hashtag::getId)
                    .collect(Collectors.toUnmodifiableSet());

    articleRepository.deleteByIdAndUserAccount_UserId(articleId, userId);
    articleRepository.flush();

    hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles);
  }


  public List<String> getHashtags() {
    return hashtagRepository.findAllHashtagNames(); // TODO: might want to move this into HashtagService
  }


  public long getArticleCount() {
    return articleRepository.count();
  }

  private Set<Hashtag> renewHashtagsFromContent(String content) {
    // parse hashtags from article body
    Set<String> hashtagNamesInContent = hashtagService.parseHashtagNames(content);
    // generate set of hashtag entities that exist in both the article body and the db
    Set<Hashtag> hashtags = hashtagService.findHashtagsByNames(hashtagNamesInContent);
    // convert entities to strings
    Set<String> existingHashtagNames = hashtags.stream()
            .map(Hashtag::getHashtagName)
            .collect(Collectors.toUnmodifiableSet());
    // for each hashtag from article body, add it to set of hashtag entities
    // if it doesn't exist in db
    hashtagNamesInContent.forEach(newHashtagName -> {
      if (!existingHashtagNames.contains(newHashtagName)) {
        hashtags.add(Hashtag.of(newHashtagName));
      }
    });
    // the reason we had to do this was that for hashtags that already exist in the db,
    // we want the correct entity
    // for those that do not, need to create a new entity separately

    return hashtags;
  }
}
