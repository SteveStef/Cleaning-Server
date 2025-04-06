package com.mainlineclean.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.exception.AppointmentException;
import com.mainlineclean.app.repository.AppointmentRepo;
import com.mainlineclean.app.dto.CostBreakdown;

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

  public void updateAmountsPaid(Appointment appointment, String responseFromPaymentApi) throws AppointmentException{
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

    } catch(JsonProcessingException e) {
      throw new AppointmentException("Failed to update amounts paid", e);
    }
  }

}
