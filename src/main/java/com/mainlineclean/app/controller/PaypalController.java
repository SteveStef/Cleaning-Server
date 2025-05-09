package com.mainlineclean.app.controller;
import com.mainlineclean.app.dto.Records;
import com.mainlineclean.app.dto.RevenueDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.entity.PaymentIntent;
import com.mainlineclean.app.exception.PaymentException;
import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.State;
import com.mainlineclean.app.utils.Finances;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.mainlineclean.app.service.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@RestController
@Slf4j
public class PaypalController {
    private final PaymentIntentService paymentIntentService;
    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;
    private final EmailService emailService;
    private final Finances financesUtil;
    private final ClientService clientService;

    @Value("${full.cancellation.percent}")
    private String FULL_CANCELLATION_PERCENT;

    @Value("${partial.cancellation.percent}")
    private String PARTIAL_CANCELLATION_PERCENT;

    @Value("${application.fee}")
    private String APPLICATION_FEE;

    public PaypalController(PaymentIntentService paymentIntentService, AvailabilityService availabilityService, AppointmentService appointmentService, EmailService emailService, Finances financesUtil, ClientService clientService) {
        this.paymentIntentService = paymentIntentService;
        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
        this.emailService = emailService;
        this.financesUtil = financesUtil;
        this.clientService = clientService;
    }

    @PostMapping("/paypal/createOrder")
    public ResponseEntity<String> createOrder(
            @RequestParam(value="serviceType") ServiceType serviceType,
            @RequestParam(value="state") State state,
            @RequestBody int squareFeet) throws PaymentException {
        PaymentIntent intent = paymentIntentService.createOrder(serviceType, squareFeet, state);
        return ResponseEntity.ok(intent.getOrderId());
    }

    @PostMapping("/paypal/captureOrder")
    public ResponseEntity<Appointment> captureOrder(@RequestBody Appointment appointment) throws PaymentException {
        availabilityService.isAvailableAt(appointment.getAppointmentDate()); // throws if not available
        Appointment createdAppointment = appointmentService.createAppointment(appointment);
        clientService.createClient(createdAppointment);

        PaymentIntent pi = paymentIntentService.findPaymentIntentByOrderId(createdAppointment.getOrderId());
        try {
            String accessToken = paymentIntentService.getAccessToken();
            String paymentCaptureResponse = paymentIntentService.capturePaymentIntent(pi, accessToken);

            try {
                paymentIntentService.sendPayout(accessToken); // add more params to this
                appointmentService.updateApplicationFee(createdAppointment, APPLICATION_FEE);
            } catch(Exception e) {
                appointmentService.updateApplicationFee(createdAppointment, "0.00");
                log.error("Error sending payout for appointment {} you were supposed to get paid: {}", createdAppointment.getId(), APPLICATION_FEE, e);
            }

            availabilityService.updateAvailability(appointment, false); // false meaning that we are not available
            appointmentService.updateAmountsPaid(createdAppointment, paymentCaptureResponse); // Updating the appointments gross/profit/paypal_fee
            emailService.notifyAppointment(createdAppointment);

        } catch(PaymentException e) {
            appointmentService.deleteAppointment(createdAppointment);
            log.warn("The payment capture failed so I am deleting the created appointment with bookingID: {}", createdAppointment.getBookingId());
            throw new PaymentException("The payment was not valid " + e.getMessage());
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

        LocalDate apptDate = appt.getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        long daysUntil = ChronoUnit.DAYS.between(today, apptDate);

        if(daysUntil >= 2)  {
            log.info("Appointment {} is more than 2 days away from today. Cancelling with full refund.", appt.getId());
            paymentIntentService.customerCancelPayment(appt, new BigDecimal(FULL_CANCELLATION_PERCENT));
        } else {
            log.info("Appointment {} is less than 2 days away from today. Cancelling with partial refund.", appt.getId());
            paymentIntentService.customerCancelPayment(appt, new BigDecimal(PARTIAL_CANCELLATION_PERCENT));
        }

        availabilityService.updateAvailability(appt, true);
        emailService.notifyCancellation(appt);

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/paypal-info")
    public ResponseEntity<RevenueDetails> getPaypalStats() {
        RevenueDetails details = financesUtil.financeDetails();
        return ResponseEntity.ok(details);
    }
}
