package com.example.boardservice.config;

import com.example.boardservice.domain.UserAccount;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

@Configuration
public class DataRestConfig {
  public RepositoryRestConfigurer repositoryRestConfigurer() {
    return RepositoryRestConfigurer.withConfig((config, cors) ->
        config.exposeIdsFor(UserAccount.class));
  }
}
