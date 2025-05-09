package com.mainlineclean.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mainlineclean.app.dto.Records;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.exception.AppointmentException;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.repository.AppointmentRepo;
import com.mainlineclean.app.dto.CostBreakdown;

import com.mainlineclean.app.utils.Finances;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AppointmentService {

  private final AppointmentRepo appointmentRepo;
  private final AvailabilityService availabilityService;

  public AppointmentService(AppointmentRepo appointmentRepository, AvailabilityService availabilityService) {
    this.appointmentRepo = appointmentRepository;
    this.availabilityService = availabilityService;
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
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  public void rescheduleAppointment(Records.RescheduleAppointmentBody scheduleData) {
    Appointment appointment = this.findByBookingIdAndEmailAndStatusNotCancelAndInFuture(scheduleData.bookingId(), scheduleData.email());

    // making the old appointment available
    availabilityService.updateAvailability(appointment, true);

    appointment.setAppointmentDate(scheduleData.newAppointmentDate());
    appointment.setTime(scheduleData.newTime());

    // making the new appointment not an available time anymore
    availabilityService.updateAvailability(appointment, false);

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

      // KEEP IN MIND: Gross amount in this case represents the amount of money that the cleaning lady gets before tax

      BigDecimal applicationFee = appointment.getApplicationFee(); // 9.99
      BigDecimal chargedAmount = new BigDecimal(bd.getGrossAmount().getValue()); // 303.15
      BigDecimal paypalFee = new BigDecimal(bd.getPaypalFee().getValue()); // 11.07
      BigDecimal salesTaxForState = Finances.taxMap.get(appointment.getState()).subtract(BigDecimal.valueOf(1)); // 1.06 - 1 = 0.06
      BigDecimal salesTaxAmount = chargedAmount.multiply(salesTaxForState); // 18.189
      BigDecimal amountChargedMinusPaypalFee = new BigDecimal(bd.getNetAmount().getValue()); // 292.08
      BigDecimal grossAmount = amountChargedMinusPaypalFee.subtract(applicationFee); // 282.09
      BigDecimal profit = grossAmount.subtract(salesTaxAmount); // 263.901

      appointment.setChargedAmount(chargedAmount.setScale(2, RoundingMode.HALF_EVEN));
      appointment.setPaypalFee(paypalFee.setScale(2, RoundingMode.HALF_EVEN));
      appointment.setSalesTax(salesTaxAmount.setScale(2, RoundingMode.HALF_EVEN));
      appointment.setGrossAmount(grossAmount.setScale(2, RoundingMode.HALF_EVEN));
      appointment.setProfit(profit.setScale(2, RoundingMode.HALF_EVEN));

      String captureId = rootNode
              .path("purchase_units").get(0)
              .path("payments").path("captures").get(0)
              .path("id").asText();

      appointment.setCaptureId(captureId);
      appointmentRepo.save(appointment);
      log.info("The amounts paid for appointment {} were updated successfully, here was the charged amount: {}", appointment.getId(), appointment.getChargedAmount());

    } catch (JsonProcessingException e) {
      log.error("Failed to update amounts paid for appointment {}: {}", appointment.getId(), e.getMessage());
      throw new AppointmentException("Failed to update amounts paid: " + e.getMessage());
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
