package com.example.boardservice.domain.constant;

import lombok.Getter;

public enum FormStatus {
  CREATE("Create", false),
  UPDATE("Update", true);

  @Getter private final String description;
  @Getter private final Boolean update;

  FormStatus(String description, Boolean update) {
    this.description = description;
    this.update = update;
  }

}