package com.example.boardservice.repository;

import com.example.boardservice.domain.Article;
import com.example.boardservice.domain.QArticle;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/*
difference between domain (DAO) and repository implementations
domains define (represent) a single data entry in the persistence layer
repositories define (represent) a collection of entries in the persistence layer
read this for further details: https://www.baeldung.com/java-dao-vs-repository
or this: https://stackoverflow.com/questions/8550124/what-is-the-difference-between-dao-and-repository-patterns
 */

@RepositoryRestResource
public interface ArticleRepository extends
    JpaRepository<Article, Long>,
    QuerydslPredicateExecutor<Article>,
    QuerydslBinderCustomizer<QArticle> {

  Page<Article> findByTitleContaining(String title, Pageable pageable);
  Page<Article> findByContentContaining(String content, Pageable pageable);
  Page<Article> findByUserAccount_UserIdContaining(String userId, Pageable pageable);

  Page<Article> findByUserAccount_NicknameContaining(String nickname, Pageable pageable);
  Page<Article> findByHashtag(String hashtag, Pageable pageable);


  @Override
  default void customize(QuerydslBindings bindings, QArticle root) {
    bindings.excludeUnlistedProperties(true);
    bindings.including(root.title, root.content, root.hashtag, root.createdAt, root.createdBy);
    bindings.bind(root.title).first(StringExpression::containsIgnoreCase);
    bindings.bind(root.content).first(StringExpression::containsIgnoreCase);
    bindings.bind(root.hashtag).first(StringExpression::containsIgnoreCase);
    bindings.bind(root.createdAt).first(DateTimeExpression::eq);
    bindings.bind(root.createdBy).first(StringExpression::containsIgnoreCase);
  }
}