package com.mainlineclean.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.exception.AppointmentException;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.repository.AppointmentRepo;
import com.mainlineclean.app.dto.CostBreakdown;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class AppointmentService {

  private final AppointmentRepo appointmentRepo;

  public AppointmentService(AppointmentRepo appointmentRepository) {
    this.appointmentRepo = appointmentRepository;
  }

  public void deleteAppointment(Appointment appointment) {
    this.appointmentRepo.delete(appointment);
  }

  public Appointment createAppointment(Appointment appointment) {
    String id;
    do {
      id = generateBookingId();
    } while (appointmentRepo.existsByBookingId(id));

    appointment.setBookingId(id);
    appointment.setCreatedAt(new Date());
    appointment.setStatus(Status.CONFIRMED);

    return appointmentRepo.save(appointment);
  }

  public List<Appointment> getAllAppointments() {
    return appointmentRepo.findAll();
  }

  public Appointment findById(long id) {
    return appointmentRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("No appointment found with ID of" + id));
  }

  public Appointment findByBookingIdAndEmail(String bookingId, String email) {
    return appointmentRepo.findByBookingIdAndEmail(bookingId, email).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment not found"));
  }

  public void updateStatus(Appointment appointment, Status status) {
    appointment.setStatus(status);
    appointmentRepo.save(appointment);
  }

  public List<Appointment> getAllSuccessfulAppointments() {
    return appointmentRepo.findAll();
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
      appointmentRepo.save(appointment);

    } catch (JsonProcessingException e) {
      throw new AppointmentException("Failed to update amounts paid", e);
    }
  }
  private String generateBookingId() {
    StringBuilder b = new StringBuilder("BK-");
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    Random rnd = new Random();
    for (int i = 0; i < 4; i++) b.append(chars.charAt(rnd.nextInt(chars.length())));
    b.append("-");
    for (int i = 0; i < 3; i++) b.append(chars.charAt(rnd.nextInt(chars.length())));
    return b.toString();
  }
}
