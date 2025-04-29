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

            details.setRegularPrice("0.12");
            details.setMoveInOutPrice("0.18");
            details.setEnvironmentPrice("0.20");
            details.setFirePrice("0.30");
            details.setWaterPrice("0.25");
            details.setDeceasedPrice("0.30");
            details.setHazmat("0.40");
            details.setExplosiveResidue("0.50");
            details.setMoldPrice("0.20");
            details.setConstructionPrice("0.20");
            details.setCommercialPrice("0.20");
            details.setDeepCleanPrice("0.15");

            details.setEmail(adminEmail);
            details.setCode("");
            adminDetailsRepo.save(details);
        }
    }
}
