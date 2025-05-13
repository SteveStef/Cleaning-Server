package com.mainlineclean.app.controller;
import com.mainlineclean.app.dto.LoginForm;
import com.mainlineclean.app.entity.AdminDetails;
import com.mainlineclean.app.service.AdminDetailsService;
import com.mainlineclean.app.service.EmailService;
import com.mainlineclean.app.utils.HMacSigner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminController {
    private final AdminDetailsService adminDetailsService;
    private final EmailService emailService;
    private final HMacSigner hMacSigner;

    public AdminController(AdminDetailsService adminDetailsService, EmailService emailService, HMacSigner hMacSigner) {
        this.adminDetailsService = adminDetailsService;
        this.emailService = emailService;
        this.hMacSigner = hMacSigner;
    }

    @GetMapping("/service-details")
    public ResponseEntity<AdminDetails> serviceDetails() {
        AdminDetails details = adminDetailsService.getAdminDetails();
        return ResponseEntity.ok(details);
    }

    @PutMapping("/update-admin-pricing")
    public ResponseEntity<AdminDetails> updateAdminPricing(@RequestBody AdminDetails details) {
        return ResponseEntity.ok(adminDetailsService.updatePricing(details));
    }

    @PutMapping("/update-admin-email")
    public ResponseEntity<AdminDetails> updateAdminEmail(@RequestBody String email) {
        return ResponseEntity.ok(adminDetailsService.updateEmail(email));
    }

    @PostMapping("/verify-credentials")
    public ResponseEntity<String> verifyCredentials(@RequestBody LoginForm form) {
        if(!adminDetailsService.verifyCredentials(form)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        emailService.sendVerificationCode();
        return ResponseEntity.ok("Verification code has been send to your admin email");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody String code) {
        if(!adminDetailsService.verifyCode(code)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        String token = hMacSigner.getSignature(true);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/authenticate")
    public ResponseEntity<String> authenticate() {
        return ResponseEntity.ok("OK");
    }
}
