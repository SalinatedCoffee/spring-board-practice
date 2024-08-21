package com.example.boardservice.config;

import com.example.boardservice.dto.UserAccountDto;
import com.example.boardservice.dto.security.BoardPrincipal;
import com.example.boardservice.dto.security.KakaoOAuth2Response;
import com.example.boardservice.repository.UserAccountRepository;
import com.example.boardservice.service.UserAccountService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.UUID;

@Configuration
public class SecurityConfig {
  @Bean
  public MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
    return new MvcRequestMatcher.Builder(introspector);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      MvcRequestMatcher.Builder mvc,
      HttpSecurity http,
      OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService
  ) throws Exception {
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
        .logout(logout -> logout.logoutSuccessUrl("/"))
        .oauth2Login(oAuth -> oAuth
            .userInfoEndpoint(userInfo -> userInfo
                .userService(oAuth2UserService)
            )
        );
    return http.build();
  }

//  @Bean
//  public WebSecurityCustomizer webSecurityCustomizer() {
//    // don't protect static resources(stylesheets, js, images... etc)
//    return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//  }

  @Bean
  public UserDetailsService userDetailsService(UserAccountService userAccountService) {
    return username -> userAccountService
            .searchUser(username)
            .map(BoardPrincipal::from)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: username=" + username));
  }

  @Bean
  public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService(
      UserAccountService userAccountService,
      PasswordEncoder passwordEncoder
  ) {
    final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    return userRequest -> {
      OAuth2User oAuth2User = delegate.loadUser(userRequest);

      KakaoOAuth2Response kakaoResponse = KakaoOAuth2Response.from(oAuth2User.getAttributes());
      // get the name of the provider of the oauth service(expected value here is "kakao")
      String registrationId = userRequest.getClientRegistration().getRegistrationId();
      // get the unique user id from the provider of the oauth service
      String providerId = String.valueOf(kakaoResponse.id());
      // concatenate the aforementioned strings into a nice looking format
      String username = registrationId + "_" + providerId;
      // passwords are not required for kakao oauth users, so pick a random password
      String dummyPassword = passwordEncoder.encode("{bcrypt}" + UUID.randomUUID());

      // first check whether this user already exists in the database
      return userAccountService.searchUser(username)
          .map(BoardPrincipal::from)
          // otherwise, create a new user from the user data given by oauth provider
          .orElseGet(() ->
              BoardPrincipal.from(
                  userAccountService.saveUser(
                      username,
                      dummyPassword,
                      kakaoResponse.email(),
                      kakaoResponse.nickname(),
                      null
                  )
              )
          );
    };
  }

  @Bean
  // implementation of this method is required when using spring security
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}
