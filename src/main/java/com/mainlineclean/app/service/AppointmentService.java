package com.mainlineclean.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.exception.AppointmentException;
import com.mainlineclean.app.repository.AppointmentRepo;
import com.mainlineclean.app.dto.CostBreakdown;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class AppointmentService {

  private final AppointmentRepo appointmentRepo;

  public AppointmentService(AppointmentRepo appointmentRepository) {
    this.appointmentRepo = appointmentRepository;
  }

  public Appointment createAppointment(Appointment appointment) {
    return appointmentRepo.save(appointment);
  }

  public List<Appointment> getAllAppointments() {
    return appointmentRepo.findAll();
  }

  public Appointment findById(long id) {
    return appointmentRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("No appointment found with ID of" + id));
  }

  public void updateStatus(Appointment appointment, String status) {
    appointment.setStatus(status);
    appointmentRepo.save(appointment);
  }

  public void updateAmountsPaid(Appointment appointment, String responseFromPaymentApi) throws AppointmentException {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      JsonNode rootNode = objectMapper.readTree(responseFromPaymentApi);
      JsonNode breakdownNode = rootNode
              .path("purchase_units")
              .get(0)
              .path("payments")
              .path("captures")
              .get(0)
              .path("seller_receivable_breakdown");
      CostBreakdown bd = objectMapper.treeToValue(breakdownNode, CostBreakdown.class);
      appointment.setChargedAmount(bd.getGrossAmount().toString());
      appointment.setPaypalFee(bd.getPaypalFee().toString());
      appointment.setNetAmount(bd.getNetAmount().toString());

      String captureId = rootNode
              .path("purchase_units")
              .get(0)
              .path("payments")
              .path("captures")
              .get(0)
              .path("id").asText();

      appointment.setCaptureId(captureId);

    } catch (JsonProcessingException e) {
      throw new AppointmentException("Failed to update amounts paid", e);
    }
  }
}
