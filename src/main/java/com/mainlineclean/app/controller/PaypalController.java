package com.mainlineclean.app.controller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainlineclean.app.dto.Records;
import com.mainlineclean.app.dto.RevenueDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.entity.PaymentIntent;
import com.mainlineclean.app.exception.PaymentException;
import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.utils.Finances;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.mainlineclean.app.exception.EmailException;
import com.mainlineclean.app.service.*;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@RestController
public class PaypalController {
    private final PaymentIntentService paymentIntentService;
    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;
    private final EmailService emailService;
    private final Finances financesUtil;

    @Value("${cancellation.percent}")
    private String CANCELLATION_PERCENT;

    @Value("${application.fee}")
    private String APPLICATION_FEE;

    public PaypalController(PaymentIntentService paymentIntentService, AvailabilityService availabilityService, AppointmentService appointmentService, EmailService emailService, Finances financesUtil) {
        this.paymentIntentService = paymentIntentService;
        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
        this.emailService = emailService;
        this.financesUtil = financesUtil;
    }

    @PostMapping("/paypal/createOrder")
    public ResponseEntity<String> createOrder(@RequestParam(value="serviceType") ServiceType serviceType, @RequestBody int squareFeet) throws PaymentException {
        PaymentIntent intent = paymentIntentService.createOrder(serviceType, squareFeet);
        return ResponseEntity.ok(intent.getOrderId());
    }

    @PostMapping("/paypal/captureOrder")
    public ResponseEntity<Appointment> captureOrder(@RequestBody Appointment appointment) throws PaymentException, EmailException {
        availabilityService.isAvailableAt(appointment.getAppointmentDate()); // throws if not available

        Appointment createdAppointment = appointmentService.createAppointment(appointment);
        PaymentIntent pi = paymentIntentService.findPaymentIntentByOrderId(appointment.getOrderId());
        try {

            String accessToken = paymentIntentService.getAccessToken();
            String paymentCaptureResponse = paymentIntentService.capturePaymentIntent(pi, accessToken);
            appointmentService.updateAmountsPaid(appointment, paymentCaptureResponse); // and saves the amounts to db
            availabilityService.updateAvailability(appointment, false); // false meaning that we are not available
            emailService.notifyAppointment(createdAppointment);

            try {
                paymentIntentService.sendPayout(pi.getOrderId(), accessToken);
                appointmentService.updateApplicationFee(appointment, APPLICATION_FEE);
            } catch(Exception e) {
                System.out.println("There was a problem sending out the payout to steve!!");
                System.out.println(e.toString());
                appointmentService.updateApplicationFee(appointment, "0.00");
            }

        } catch(PaymentException e) {
            appointmentService.deleteAppointment(createdAppointment);
            throw new PaymentException("The payment was not valid");
        }

        return ResponseEntity.ok(createdAppointment);
    }

    // this is for admin cancelling
    @PostMapping("/cancel-appointment")
    public ResponseEntity<String> cancelAppointment(@RequestBody Records.AdminCancelAppointmentBody data) {
        Appointment appt = appointmentService.findByBookingIdAndEmailAndStatusNotCancelAndInFuture(data.appointment().getBookingId(), data.appointment().getEmail());
        paymentIntentService.refundPayment(appt, data.refundAmount());
        emailService.notifyCancellation(appt);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/customer-cancel-appointment")
    public ResponseEntity<String> customerCancelAppointment(@RequestBody Records.CustomerCancelAppointmentBody data) {
        Appointment appt = appointmentService.findByBookingIdAndEmailAndStatusNotCancelAndInFuture(data.bookingId(), data.email());

        LocalDate apptDate = appt.getAppointmentDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate today = LocalDate.now();

        long daysUntil = ChronoUnit.DAYS.between(today, apptDate);

        if (daysUntil >= 2)  paymentIntentService.customerCancelPayment(appt, Double.parseDouble(CANCELLATION_PERCENT));
        else appointmentService.updateStatus(appt, Status.CANCELED);

        availabilityService.updateAvailability(appt, true);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/paypal-info")
    public ResponseEntity<RevenueDetails> getPaypalStats() {
        return ResponseEntity.ok(financesUtil.financeDetails());
    }

    @PostMapping("/paypal/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String body,
            @RequestHeader("PayPal-Transmission-Id") String transmissionId,
            @RequestHeader("PayPal-Transmission-Time") String transmissionTime,
            @RequestHeader("PayPal-Transmission-Sig") String transmissionSig,
            @RequestHeader("PayPal-Cert-Url") String certUrl,
            @RequestHeader("PayPal-Auth-Algo") String authAlgo) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode event = objectMapper.readTree(body);
        String eventType = event.get("event_type").asText();

        System.out.println(eventType);

        return ResponseEntity.ok("OK");
    }
}
