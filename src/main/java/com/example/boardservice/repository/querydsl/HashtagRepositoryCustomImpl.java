package com.example.boardservice.repository.querydsl;

import com.example.boardservice.domain.Hashtag;
import com.example.boardservice.domain.QHashtag;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class HashtagRepositoryCustomImpl extends QuerydslRepositorySupport implements HashtagRepositoryCustom {

  public HashtagRepositoryCustomImpl() {
    super(Hashtag.class);
  }

  @Override
  public List<String> findAllHashtagNames() {
    QHashtag hashtag = QHashtag.hashtag;

    return from(hashtag)
        .select(hashtag.hashtagName)
        .fetch();
  }

}