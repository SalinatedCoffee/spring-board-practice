package com.example.boardservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Configuration
public class FaviconConfig {
  @Controller
  static class FaviconController {
    @RequestMapping(value = "favicon.ico", method = RequestMethod.GET)
    @ResponseBody
    void favicon() {
    }
  }
}
