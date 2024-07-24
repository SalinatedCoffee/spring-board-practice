package com.example.boardservice.repository.querydsl;

import com.example.boardservice.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface ArticleRepositoryCustom {
  // mark method as deprecated
  @Deprecated
  List<String> findAllDistinctHashtags();

  Page<Article> findByHashtagNames(Collection<String> hashtagNames, Pageable pageable);
}
