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
            details.setRegularPrice("200.00");
            details.setMoveInOutPrice("200.00");
            details.setEnvironmentPrice("200.00");
            details.setFirePrice("200.00");
            details.setWaterPrice("200.00");
            details.setDeceasedPrice("200.00");
            details.setHazmat("200.00");
            details.setExplosiveResidue("200.00");
            details.setMoldPrice("200.00");
            details.setConstructionPrice("200.00");
            details.setCommercialPrice("200.00");
            details.setDeepCleanPrice("200.00");

            details.setEmail(adminEmail);
            details.setCode("");
            adminDetailsRepo.save(details);
        }
    }
}
