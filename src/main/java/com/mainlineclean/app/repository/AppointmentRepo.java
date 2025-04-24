package com.mainlineclean.app.repository;

import com.mainlineclean.app.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mainlineclean.app.entity.Appointment;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    boolean existsByBookingId(String bookingId);
    List<Appointment> findByStatusNot(Status status);
    Optional<Appointment> findByBookingIdAndEmail(String bookingId, String email);
}
