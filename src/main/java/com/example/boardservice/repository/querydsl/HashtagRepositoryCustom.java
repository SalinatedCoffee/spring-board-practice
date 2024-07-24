package com.example.boardservice.repository.querydsl;

import java.util.List;

public interface HashtagRepositoryCustom {
  List<String> findAllHashtagNames();
}