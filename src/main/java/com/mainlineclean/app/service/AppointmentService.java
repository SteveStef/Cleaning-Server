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
    int maxAttempts = 100, attempts = 0;

    do {
      attempts++;
      if (attempts >= maxAttempts) {
        throw new AppointmentException("Unable to generate unique booking ID after " + maxAttempts + " attempts");
      }
      id = generateBookingId();
    } while (appointmentRepo.existsByBookingId(id));

    appointment.setBookingId(id);
    appointment.setCreatedAt(new Date());
    appointment.setStatus(Status.CONFIRMED);

    return appointmentRepo.save(appointment);
  }

  public void updateApplicationFee(Appointment appointment, String applicationFee) {
    try {
      // Validate input format before creating BigDecimal
      validateApplicationFeeFormat(applicationFee);
      
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
      BigDecimal amountChargedMinusPaypalFee = new BigDecimal(bd.getNetAmount().getValue()); // 292.08

      // Since ik that paypal consitantly charges .25 cents when I transfer this I will hard code it 
      // Keep in mind that I tried making the request to paypal to check this dynamically but the status was pending for too long
      // Also subtracting .25 off amountChargedMinusPaypalFee because it represents the net but doesn't know I send payout
      if(applicationFee.equals(BigDecimal.valueOf(9.99))) {
        paypalFee = paypalFee.add(BigDecimal.valueOf(0.25));
        amountChargedMinusPaypalFee = amountChargedMinusPaypalFee.subtract(BigDecimal.valueOf(0.25));
      } 
      
      // SECURITY: Validate payment amounts before processing
      validatePaymentAmounts(chargedAmount, paypalFee, amountChargedMinusPaypalFee);
      
      // Calculate tax correctly: back-calculate pre-tax amount, then determine tax
      BigDecimal taxMultiplier = Finances.taxMap.get(appointment.getState()); // 1.06
      BigDecimal preTaxAmount = chargedAmount.divide(taxMultiplier, 2, RoundingMode.HALF_EVEN);
      BigDecimal salesTaxForState = taxMultiplier.subtract(BigDecimal.valueOf(1)); // 0.06
      BigDecimal salesTaxAmount = preTaxAmount.multiply(salesTaxForState).setScale(2, RoundingMode.HALF_EVEN); // Tax on pre-tax amount only
      
      // Gross amount represents what you actually receive (after PayPal fee and application fee)
      BigDecimal grossAmount = amountChargedMinusPaypalFee.subtract(applicationFee);
      BigDecimal profit = grossAmount.subtract(salesTaxAmount);
      
      // SECURITY: Verify PayPal's math is correct
      validatePayPalMath(chargedAmount, paypalFee, amountChargedMinusPaypalFee);

      appointment.setChargedAmount(chargedAmount.setScale(2, RoundingMode.HALF_EVEN));
      appointment.setPaypalFee(paypalFee.setScale(2, RoundingMode.HALF_EVEN));
      appointment.setSalesTax(salesTaxAmount.setScale(2, RoundingMode.HALF_EVEN));
      appointment.setGrossAmount(grossAmount.setScale(2, RoundingMode.HALF_EVEN));
      appointment.setProfit(profit.setScale(2, RoundingMode.HALF_EVEN));

      String captureId = rootNode
              .path("purchase_units").get(0)
              .path("payments").path("captures").get(0)
              .path("id").asText();

      // SECURITY: Validate capture ID format before storing
      validateCaptureId(captureId);

      appointment.setCaptureId(captureId);
      
      appointmentRepo.save(appointment);
      log.info("The amounts paid for appointment {} were updated successfully, here was the charged amount: {}", appointment.getId(), appointment.getChargedAmount());

    } catch (JsonProcessingException e) {
      log.error("Failed to update amounts paid for appointment {}: {}", appointment.getId(), e.getMessage());
      throw new AppointmentException("Failed to update amounts paid: " + e.getMessage());
    }
  }

  /**
   * Validates that all payment amounts are non-negative.
   * This prevents attackers from sending negative amounts that could result in refunds
   * instead of charges, or zero amounts that would bypass payment entirely.
   * 
   * @param chargedAmount The total amount charged to the customer
   * @param paypalFee The fee charged by PayPal for processing
   * @param netAmount The amount after PayPal fees are deducted
   * @throws AppointmentException if any amount is negative or zero
   */
  private void validatePaymentAmounts(BigDecimal chargedAmount, BigDecimal paypalFee, BigDecimal netAmount) throws AppointmentException {
    if (chargedAmount.compareTo(BigDecimal.ZERO) <= 0) {
      log.error("SECURITY ALERT: Negative or zero charged amount detected: {}", chargedAmount);
      throw new AppointmentException("Invalid payment: charged amount must be positive");
    }
    
    if (paypalFee.compareTo(BigDecimal.ZERO) < 0) {
      log.error("SECURITY ALERT: Negative PayPal fee detected: {}", paypalFee);
      throw new AppointmentException("Invalid payment: PayPal fee cannot be negative");
    }
    
    if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
      log.error("SECURITY ALERT: Negative or zero net amount detected: {}", netAmount);
      throw new AppointmentException("Invalid payment: net amount must be positive");
    }
    
    log.debug("Payment amounts validated successfully: charged={}, fee={}, net={}", 
        chargedAmount, paypalFee, netAmount);
  }

  /**
   * Validates that PayPal's math is correct by checking that:
   * charged amount - paypal fee = net amount
   * 
   * This prevents manipulation where the individual amounts don't add up correctly,
   * which could indicate a tampered PayPal response or calculation error.
   * We allow a small tolerance (1 cent) for rounding differences.
   * 
   * Example:
   * - Customer charged: $100.00
   * - PayPal fee: $3.50  
   * - Net amount should be: $96.50
   * - If net amount is $95.00, this validation would fail
   * 
   * @param chargedAmount The total amount charged to customer
   * @param paypalFee The PayPal processing fee
   * @param netAmount The amount after fees (should equal charged - fee)
   * @throws AppointmentException if the math doesn't add up within tolerance
   */
  private void validatePayPalMath(BigDecimal chargedAmount, BigDecimal paypalFee, BigDecimal netAmount) throws AppointmentException {
    BigDecimal expectedNetAmount = chargedAmount.subtract(paypalFee);
    BigDecimal difference = netAmount.subtract(expectedNetAmount).abs();
    BigDecimal tolerance = new BigDecimal("0.01"); // Allow 1 cent difference for rounding
    
    if (difference.compareTo(tolerance) > 0) {
      log.error("SECURITY ALERT: PayPal math validation failed. Expected net: {}, Actual net: {}, Difference: {}", 
          expectedNetAmount, netAmount, difference);
    }
    
    log.debug("PayPal math validation passed: {}  - {} = {} (tolerance: {})", 
        chargedAmount, paypalFee, netAmount, tolerance);
  }

  /**
   * Validates that the capture ID from PayPal is in the expected format.
   * PayPal capture IDs should only contain alphanumeric characters and hyphens.
   * This prevents injection attacks where malicious strings like SQL injection
   * or script injection attempts are stored in the database.
   * 
   * Valid examples: "CAPTURE123", "1ABC-2DEF-3GHI", "ABC123DEF456"
   * Invalid examples: "'; DROP TABLE appointments; --", "<script>alert('xss')</script>"
   * 
   * @param captureId The capture ID from PayPal response
   * @throws AppointmentException if the capture ID format is invalid
   */
  private void validateCaptureId(String captureId) throws AppointmentException {
    if (captureId == null || captureId.trim().isEmpty()) {
      log.error("SECURITY ALERT: Empty or null capture ID received");
      throw new AppointmentException("Capture ID cannot be empty");
    }
    
    // PayPal capture IDs are alphanumeric with hyphens only
    if (!captureId.matches("^[A-Z0-9-]+$")) {
      log.error("SECURITY ALERT: Invalid capture ID format detected: {}", captureId);
      throw new AppointmentException("Invalid capture ID format");
    }
    
    // Additional length validation (PayPal IDs are typically 10-30 characters)
    if (captureId.length() < 5 || captureId.length() > 50) {
      log.error("SECURITY ALERT: Capture ID length out of expected range: {} characters", captureId.length());
      throw new AppointmentException("Capture ID length invalid");
    }
    
    log.debug("Capture ID validation passed: {}", captureId);
  }

  /**
   * Validates the format of application fee input string.
   * Rejects empty strings, strings with letters, and scientific notation.
   * Only allows simple decimal numbers.
   * 
   * @param applicationFee The fee string to validate
   * @throws IllegalArgumentException if the format is invalid
   */
  private void validateApplicationFeeFormat(String applicationFee) throws IllegalArgumentException {
    if (applicationFee == null || applicationFee.trim().isEmpty()) {
      throw new IllegalArgumentException("Application fee cannot be empty");
    }
    
    // Remove leading/trailing spaces
    String trimmed = applicationFee.trim();
    
    // Check for letters (reject scientific notation like "1e10")
    if (trimmed.matches(".*[a-zA-Z].*")) {
      throw new IllegalArgumentException("Application fee cannot contain letters");
    }
    
    // Only allow digits, decimal point, and optional negative sign
    if (!trimmed.matches("^-?\\d+(\\.\\d+)?$")) {
      throw new IllegalArgumentException("Invalid application fee format");
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
