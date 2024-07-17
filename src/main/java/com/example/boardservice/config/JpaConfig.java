package com.example.boardservice.config;

import com.example.boardservice.dto.security.BoardPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

// enable auditing
@EnableJpaAuditing
@Configuration
public class JpaConfig {
  @Bean
  public AuditorAware<String> auditorAware() {
    // temporarily attach arbitrary username for auditing
    return () -> Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getPrincipal)
            .map(BoardPrincipal.class::cast)
            .map(BoardPrincipal::getUsername);
  }
}
