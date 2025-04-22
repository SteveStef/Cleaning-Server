package com.mainlineclean.app.service;

import com.mainlineclean.app.dto.LoginForm;
import com.mainlineclean.app.entity.AdminDetails;
import com.mainlineclean.app.repository.AdminDetailsRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminDetailsService {
    AdminDetailsRepo adminDetailsRepo;

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    public AdminDetailsService(AdminDetailsRepo adminDetailsRepo){
        this.adminDetailsRepo = adminDetailsRepo;
    }

    public AdminDetails getAdminDetails() {
        return adminDetailsRepo.findById((long) 1).orElseThrow(() -> new EntityNotFoundException("Admin details not found"));
    }

    public String getAdminEmail() {
        Optional<AdminDetails> details = adminDetailsRepo.findById(1L);
        if(details.isPresent()) {
            return details.get().getEmail();
        } else {
            throw new EntityNotFoundException("No admin email found in the database");
        }
    }

    public AdminDetails updatePricing(AdminDetails details) {
        AdminDetails existing = this.getAdminDetails();

        existing.setDeepCleanPrice(details.getDeepCleanPrice());
        existing.setRegularPrice(details.getRegularPrice());
        existing.setMoveInOutPrice(details.getMoveInOutPrice());
        existing.setCommercialPrice(details.getCommercialPrice());
        existing.setDeceasedPrice(details.getDeceasedPrice());
        existing.setFirePrice(details.getFirePrice());
        existing.setExplosiveResidue(details.getExplosiveResidue());
        existing.setEnvironmentPrice(details.getEnvironmentPrice());
        existing.setWaterPrice(details.getWaterPrice());
        existing.setMoldPrice(details.getMoldPrice());
        existing.setHazmat(details.getHazmat());
        existing.setConstructionPrice(details.getConstructionPrice());

        return adminDetailsRepo.save(existing);
    }

    public AdminDetails updateEmail(String email) {
        AdminDetails existing = this.getAdminDetails();
        existing.setEmail(email);
        return adminDetailsRepo.save(existing);
    }

    public boolean verifyCredentials(LoginForm form) {
        return form.getUsername().equals(username) && form.getPassword().equals(password);
    }

    public void setVerificationCode(String code) {
        AdminDetails existing = this.getAdminDetails();
        existing.setCode(code);
        adminDetailsRepo.save(existing);
    }

    public boolean verifyCode(String code) {
        AdminDetails existing = this.getAdminDetails();
        if(!existing.getCode().equals(code)) return false;
        existing.setCode("");
        adminDetailsRepo.save(existing);
        return true;
    }
}
