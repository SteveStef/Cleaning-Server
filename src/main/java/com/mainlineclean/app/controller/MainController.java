package com.mainlineclean.app.controller;

import com.mainlineclean.app.dto.RequestQuote;
import com.mainlineclean.app.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
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