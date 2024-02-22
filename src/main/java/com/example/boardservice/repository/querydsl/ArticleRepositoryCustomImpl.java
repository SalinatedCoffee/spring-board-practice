package com.example.boardservice.repository.querydsl;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.QArticle;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class ArticleRepositoryCustomImpl extends QuerydslRepositorySupport implements ArticleRepositoryCustom {
  public ArticleRepositoryCustomImpl() {
    super(Article.class);
  }

  @Override
  // custom DB query using QueryDSL
  public List<String> findAllDistinctHashtags() {
    QArticle article = QArticle.article;

    return from(article)
        .distinct()
        .select(article.hashtag) // only search on hashtag column
        .where(article.hashtag.isNotNull())
        .fetch();
  }
}
