package com.mainlineclean.app.config;

import com.mainlineclean.app.entity.AdminDetails;
import com.mainlineclean.app.repository.AdminDetailsRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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

            details.setRegularPrice          (new BigDecimal("0.12"));
            details.setMoveInOutPrice       (new BigDecimal("0.18"));
            details.setEnvironmentPrice     (new BigDecimal("0.20"));
            details.setFirePrice            (new BigDecimal("0.30"));
            details.setWaterPrice           (new BigDecimal("0.25"));
            details.setDeceasedPrice        (new BigDecimal("0.30"));
            details.setHazmatPrice          (new BigDecimal("0.40"));
            details.setExplosiveResiduePrice(new BigDecimal("0.50"));
            details.setMoldPrice            (new BigDecimal("0.20"));
            details.setConstructionPrice    (new BigDecimal("0.20"));
            details.setCommercialPrice      (new BigDecimal("0.20"));
            details.setDeepCleanPrice       (new BigDecimal("0.15"));

            details.setEmail(adminEmail);
            details.setCode("");
            adminDetailsRepo.save(details);
        }
    }
}
