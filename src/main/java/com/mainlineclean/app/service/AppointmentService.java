package com.mainlineclean.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mainlineclean.app.dto.Records;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
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

  public void updateApplicationFee(Appointment appointment, String applicationFee) {
    try {
      BigDecimal fee = new BigDecimal(applicationFee).setScale(2, RoundingMode.HALF_EVEN);
      appointment.setApplicationFee(fee);
      appointmentRepo.save(appointment);
    } catch(Exception e) {
      throw new IllegalArgumentException("invalid application fee, check env");
    }
  }

  public void rescheduleAppointment(Records.RescheduleAppointmentBody scheduleData) {
    Appointment appointment = this.findByBookingIdAndEmailAndStatusNotCancelAndInFuture(scheduleData.bookingId(), scheduleData.email());

    appointment.setAppointmentDate(scheduleData.newAppointmentDate());
    appointment.setTime(scheduleData.newTime());

    appointmentRepo.save(appointment);
  }

  public List<Appointment> getAllAppointments() {
    return appointmentRepo.findAll();
  }

  public Appointment findById(long id) {
    return appointmentRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("No appointment found with ID of" + id));
  }

  public Appointment findByBookingIdAndEmailAndStatusNotCancelAndInFuture(String bookingId, String email) {
    return appointmentRepo.findByBookingIdAndEmailAndStatusNotAndAppointmentDateAfter(bookingId, email, Status.CANCELED, new Date())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment not found"));
  }

  public void updateStatus(Appointment appointment, Status status) {
    appointment.setStatus(status);
    appointmentRepo.save(appointment);
  }

  public void updateAmountsPaid(Appointment appointment, String responseFromPaymentApi) throws AppointmentException {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      JsonNode rootNode = objectMapper.readTree(responseFromPaymentApi);
      JsonNode breakdownNode = rootNode
              .path("purchase_units").get(0).path("payments").path("captures").get(0)
              .path("seller_receivable_breakdown");
      CostBreakdown bd = objectMapper.treeToValue(breakdownNode, CostBreakdown.class);

      appointment.setChargedAmount(new BigDecimal(bd.getGrossAmount().getValue()));
      appointment.setPaypalFee(new BigDecimal(bd.getPaypalFee().getValue()));
      appointment.setGrossAmount(new BigDecimal(bd.getNetAmount().getValue()));

      String captureId = rootNode
              .path("purchase_units").get(0)
              .path("payments").path("captures").get(0)
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
