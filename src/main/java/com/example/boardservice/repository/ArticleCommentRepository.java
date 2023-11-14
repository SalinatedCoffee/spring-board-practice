package com.example.boardservice.repository;

import com.example.boardservice.domain.ArticleComment;
import com.example.boardservice.domain.QArticleComment;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface ArticleCommentRepository extends
    JpaRepository<ArticleComment, Long>,
    QuerydslPredicateExecutor<ArticleComment>,
    QuerydslBinderCustomizer<QArticleComment> {

  // JPA query methods: Spring automagically derives queries through specially formatted method names
  // read more here: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-keywords
  // underbar means that element after underbar is mapped under the element before underbar
  // just think of it as accessing an object parameter like this: Article.Id
  List<ArticleComment> findByArticle_Id(Long articleId);

  @Override
  default void customize(QuerydslBindings bindings, QArticleComment root) {
    bindings.excludeUnlistedProperties(true);
    bindings.including(root.content, root.createdAt, root.createdBy);
    bindings.bind(root.content).first(StringExpression::containsIgnoreCase);
    bindings.bind(root.createdAt).first(DateTimeExpression::eq);
    bindings.bind(root.createdBy).first(StringExpression::containsIgnoreCase);
  }
}