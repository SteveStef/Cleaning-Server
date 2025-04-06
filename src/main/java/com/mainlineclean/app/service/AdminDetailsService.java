package com.mainlineclean.app.service;

import com.mainlineclean.app.entity.AdminDetails;
import com.mainlineclean.app.repository.AdminDetailsRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminDetailsService {
    AdminDetailsRepo adminDetailsRepo;

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

        return adminDetailsRepo.save(existing);
    }

    public AdminDetails updateEmail(String email) {
        AdminDetails existing = this.getAdminDetails();
        existing.setEmail(email);
        return adminDetailsRepo.save(existing);
    }
}
