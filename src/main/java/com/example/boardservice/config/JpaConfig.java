package com.example.boardservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

// enable auditing
@EnableJpaAuditing
@Configuration
public class JpaConfig {
  @Bean
  public AuditorAware<String> auditorAware() {
    // temporarily attach arbitrary username for auditing
    return () -> Optional.of("sc"); // TODO: Change this after implementing auth using Spring Security
  }
}
