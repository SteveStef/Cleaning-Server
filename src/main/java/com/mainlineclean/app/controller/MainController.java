package com.mainlineclean.app.controller;

import com.mainlineclean.app.dto.LoginForm;
import com.mainlineclean.app.entity.*;
import com.mainlineclean.app.exception.EmailException;
import com.mainlineclean.app.dto.RequestQuote;
import com.mainlineclean.app.service.*;
import com.mainlineclean.app.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import com.mainlineclean.app.exception.PaymentException;

import java.util.List;

@RestController
public class MainController {

  private final AppointmentService appointmentService;
  private final ReviewService reviewService;
  private final PaymentIntentService paymentIntentService;
  private final AvailabilityService availabilityService;
  private final EmailService emailService;
  private final AdminDetailsService adminDetailsService;
  private final JwtUtil jwtUtil;

  public MainController(AppointmentService appointmentService, ReviewService reviewService,
                        PaymentIntentService paymentIntentService, AvailabilityService availabilityService,
                        EmailService emailService, AdminDetailsService adminDetailsService, JwtUtil jwtUtil) {
    this.appointmentService = appointmentService;
    this.reviewService = reviewService;
    this.paymentIntentService = paymentIntentService;
    this.availabilityService = availabilityService;
    this.emailService = emailService;
    this.adminDetailsService = adminDetailsService;
    this.jwtUtil = jwtUtil;
  }

  @GetMapping("/reviews")
  public ResponseEntity<List<Review>> getReviews() {
    return ResponseEntity.ok(reviewService.getAllReviews());
  }

  @PostMapping("/review")
  public ResponseEntity<Review> createReview(@RequestBody Review review) {
    return ResponseEntity.ok(reviewService.createReview(review));
  }

  @GetMapping("/appointments")
  public ResponseEntity<List<Appointment>> getAppointments() {
    return ResponseEntity.ok(appointmentService.getAllAppointments());
  }

  @PostMapping("/paypal/createOrder")
  public ResponseEntity<String> createOrder(@RequestParam(value="serviceType") String serviceType) throws PaymentException {
    PaymentIntent intent = paymentIntentService.createOrder(serviceType);
    return ResponseEntity.ok(intent.getOrderId());
  }

  @PostMapping("/paypal/captureOrder")
  public ResponseEntity<Appointment> captureOrder(@RequestBody Appointment appointment) throws PaymentException, EmailException {
    // checks if the date is available before paying
    availabilityService.isAvailableAt(appointment.getAppointmentDate()); // throws exception if not available
    // finds the payment from the appointment
    PaymentIntent pi = paymentIntentService.findPaymentIntentByOrderId(appointment.getOrderId());
    // charges the card
    String paymentCaptureResponse = paymentIntentService.capturePaymentIntent(pi);
    // Updates the appointment objected based off what was charged
    appointmentService.updateAmountsPaid(appointment, paymentCaptureResponse);
    // makes this date/time no longer available
    availabilityService.updateAvailability(appointment);
    // saves the appointment into database
    Appointment createdAppointment = appointmentService.createAppointment(appointment);
    // Send notifications
    emailService.notifyAppointment(createdAppointment);
    return ResponseEntity.ok(createdAppointment);
  }

  // This needs to be a protected route
  @PostMapping("/availability")
  public ResponseEntity<List<Availability>> updateAvailability(@RequestBody Availability[] timeSlots) {
    return ResponseEntity.ok(availabilityService.createAvailability(timeSlots));
  }

  @GetMapping("/availability")
  public ResponseEntity<List<Availability>> getAvailability() {
    return ResponseEntity.ok(availabilityService.getAllAvailability());
  }

  @PostMapping("/requestQuote")
  public ResponseEntity<String> requestQuote(@RequestBody RequestQuote userInfo) throws EmailException {
    emailService.sendQuote(userInfo);
    return ResponseEntity.ok("OK");
  }

  @GetMapping("/service-details")
  public ResponseEntity<AdminDetails> serviceDetails() {
    AdminDetails details = adminDetailsService.getAdminDetails();
    return ResponseEntity.ok(details);
  }

  @PutMapping("/update-admin-pricing")
  public ResponseEntity<AdminDetails> updateAdminPricing(@RequestBody AdminDetails details) {
    return ResponseEntity.ok(adminDetailsService.updatePricing(details));
  }

  @PutMapping("/update-admin-email")
  public ResponseEntity<AdminDetails> updateAdminEmail(@RequestBody String email) {
    return ResponseEntity.ok(adminDetailsService.updateEmail(email));
  }

  @PostMapping("/verify-credentials")
  public ResponseEntity<String> verifyCredentials(@RequestBody LoginForm form) {
    if(!adminDetailsService.verifyCredentials(form)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    emailService.sendVerificationCode();
    return ResponseEntity.ok("Verification code has been send to your admin email");
  }

  @PostMapping("/verify-code")
  public ResponseEntity<String> verifyCode(@RequestBody String code) {
    if(!adminDetailsService.verifyCode(code)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    String token = jwtUtil.generateToken();
    return ResponseEntity.ok(token);
  }

  @GetMapping("/authenticate")
  public ResponseEntity<String> authenticate() {
    return ResponseEntity.ok("OK");
  }

  @PostMapping("/cancel-appointment")
  public ResponseEntity<String> cancelAppointment(@RequestBody Appointment appointment) {
    paymentIntentService.cancelPayment(appointment);
    emailService.notifyCancellation(appointment);
    return ResponseEntity.ok("OK");
  }
}