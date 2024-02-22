package com.example.boardservice.domain.type;

import lombok.Getter;

// in Java, enums can have multiple params. Read: https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
public enum SearchType {
  TITLE("Title"),
  CONTENT("Content"),
  ID("User ID"),
  NICKNAME("Nickname"),
  HASHTAG("Hashtag");

  @Getter private final String description;

  SearchType(String description) {
    this.description = description;
  }
}
