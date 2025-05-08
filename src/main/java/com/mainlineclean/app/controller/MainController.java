package com.mainlineclean.app.controller;

import com.mainlineclean.app.dto.RequestQuote;
import com.mainlineclean.app.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@Slf4j
public class MainController {
  private final EmailService emailService;
  public MainController(EmailService emailService) {
    this.emailService = emailService;
  }

  @PostMapping("/requestQuote")
  public ResponseEntity<String> requestQuote(@RequestBody RequestQuote userInfo) {
    emailService.sendQuote(userInfo);
    return ResponseEntity.ok("OK");
  }
}