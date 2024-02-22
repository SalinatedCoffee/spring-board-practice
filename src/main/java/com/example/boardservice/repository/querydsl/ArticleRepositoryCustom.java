package com.example.boardservice.repository.querydsl;

import java.util.List;

public interface ArticleRepositoryCustom {
  List<String> findAllDistinctHashtags();
}
