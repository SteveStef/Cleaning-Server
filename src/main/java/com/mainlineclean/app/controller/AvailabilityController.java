package com.mainlineclean.app.controller;

import com.mainlineclean.app.entity.Availability;
import com.mainlineclean.app.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/availability")
    public ResponseEntity<List<Availability>> updateAvailability(@RequestBody Availability[] timeSlots) {
        return ResponseEntity.ok(availabilityService.createAvailability(timeSlots));
    }

    @GetMapping("/availability")
    public ResponseEntity<List<Availability>> getAvailability() {
        return ResponseEntity.ok(availabilityService.getAllAvailability());
    }
}
