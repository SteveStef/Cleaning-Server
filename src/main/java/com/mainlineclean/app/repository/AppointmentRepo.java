package com.mainlineclean.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mainlineclean.app.entity.Appointment;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {}
