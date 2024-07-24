package com.example.boardservice.repository;

import com.example.boardservice.domain.Hashtag;
import com.example.boardservice.repository.querydsl.HashtagRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface HashtagRepository extends
    JpaRepository<Hashtag, Long>,
    HashtagRepositoryCustom,
    QuerydslPredicateExecutor<Hashtag> {
  Optional<Hashtag> findByHashtagName(String hashtagName);
  List<Hashtag> finByHashtagNameIn(Set<String> hashtagNames);
}
