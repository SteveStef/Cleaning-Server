package com.mainlineclean.app.controller;

import com.mainlineclean.app.dto.RequestQuote;
import com.mainlineclean.app.service.*;
import com.mainlineclean.app.utils.HMacSigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@Slf4j
public class MainController {
  private final EmailService emailService;
  private final HMacSigner HMacSigner;

  public MainController(EmailService emailService, HMacSigner HMacSigner) {
    this.emailService = emailService;
    this.HMacSigner = HMacSigner;
  }

  @PostMapping("/requestQuote")
  public ResponseEntity<String> requestQuote(@RequestBody RequestQuote userInfo) {
    emailService.sendQuote(userInfo);
    return ResponseEntity.ok("OK");
  }

  @GetMapping("/token")
  public ResponseEntity<String> getToken() {
    String token = HMacSigner.getSignature(false); // false means user not admin
    return ResponseEntity.ok(token);
  }

}