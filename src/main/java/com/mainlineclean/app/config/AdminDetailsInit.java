package com.mainlineclean.app.config;

import com.mainlineclean.app.entity.AdminDetails;
import com.mainlineclean.app.repository.AdminDetailsRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminDetailsInit implements CommandLineRunner {

    @Value("${mailgun.sender-email}")
    private String adminEmail;

    final private AdminDetailsRepo adminDetailsRepo;

    public AdminDetailsInit(AdminDetailsRepo repository) {
        this.adminDetailsRepo = repository;
    }

    @Override
    public void run(String... args) {
        if(!adminDetailsRepo.existsById((long)1)) {
            AdminDetails details = new AdminDetails();
            details.setRegularPrice("150.00");
            details.setMoveInOutPrice("350.00");
            details.setDeepCleanPrice("250.00");
            details.setEmail(adminEmail);
            details.setCode("");
            adminDetailsRepo.save(details);
        }
    }
}
