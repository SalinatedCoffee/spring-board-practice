package com.example.boardservice.config;

import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.dto.security.BoardPrincipal;
import com.example.boardservice.repository.UserAccountRepository;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
public class SecurityConfig {
  @Bean
  public MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
    return new MvcRequestMatcher.Builder(introspector);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(MvcRequestMatcher.Builder mvc, HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // don't enforce authorization on static resources (css, js, images... etc
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .requestMatchers(mvc.pattern(HttpMethod.GET, "/")).permitAll()
            .requestMatchers(mvc.pattern(HttpMethod.GET, "/articles")).permitAll()
            .requestMatchers(mvc.pattern(HttpMethod.GET, "/articles/search-hashtag")).permitAll()
            .anyRequest().authenticated())
            // .formLogin() marked for deprecation in 7
            // instead use .formLogin(Customizer.withDefaults()) to use defaults
        .formLogin(Customizer.withDefaults())
        .logout(logout -> logout.logoutSuccessUrl("/"));
    return http.build();
  }

//  @Bean
//  public WebSecurityCustomizer webSecurityCustomizer() {
//    // don't protect static resources(stylesheets, js, images... etc)
//    return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//  }

  @Bean
  public UserDetailsService userDetailsService(UserAccountRepository userAccountRepository) {
    return username -> userAccountRepository
            .findById(username)
            .map(UserAccountDto::from)
            .map(BoardPrincipal::from)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: username=" + username));
  }

  @Bean
  // implementation of this method is required when using spring security
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}
