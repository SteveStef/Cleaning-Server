package com.mainlineclean.app.controller;

import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }
}
