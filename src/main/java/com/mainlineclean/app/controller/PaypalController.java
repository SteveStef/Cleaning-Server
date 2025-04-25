package com.mainlineclean.app.controller;
import com.mainlineclean.app.dto.Records;
import com.mainlineclean.app.dto.RevenueDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.entity.PaymentIntent;
import com.mainlineclean.app.exception.PaymentException;
import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.utils.Finances;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.mainlineclean.app.exception.EmailException;
import com.mainlineclean.app.service.*;
import org.springframework.web.bind.annotation.*;

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

    public PaypalController(PaymentIntentService paymentIntentService, AvailabilityService availabilityService, AppointmentService appointmentService, EmailService emailService, Finances financesUtil) {
        this.paymentIntentService = paymentIntentService;
        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
        this.emailService = emailService;
        this.financesUtil = financesUtil;
    }

    @PostMapping("/paypal/createOrder")
    public ResponseEntity<String> createOrder(@RequestParam(value="serviceType") ServiceType serviceType) throws PaymentException {
        PaymentIntent intent = paymentIntentService.createOrder(serviceType);
        return ResponseEntity.ok(intent.getOrderId());
    }

    @PostMapping("/paypal/captureOrder")
    public ResponseEntity<Appointment> captureOrder(@RequestBody Appointment appointment) throws PaymentException, EmailException {
        availabilityService.isAvailableAt(appointment.getAppointmentDate()); // throws if not available

        Appointment createdAppointment = appointmentService.createAppointment(appointment);
        PaymentIntent pi = paymentIntentService.findPaymentIntentByOrderId(appointment.getOrderId());
        try {
            String paymentCaptureResponse = paymentIntentService.capturePaymentIntent(pi);
            appointmentService.updateAmountsPaid(appointment, paymentCaptureResponse); // and saves the amounts to db
            availabilityService.updateAvailability(appointment, false);
            emailService.notifyAppointment(createdAppointment);
        } catch(PaymentException e) {
            appointmentService.deleteAppointment(createdAppointment);
            throw new PaymentException("The payment was not valid");
        }

        return ResponseEntity.ok(createdAppointment);
    }

    @PostMapping("/cancel-appointment")
    public ResponseEntity<String> cancelAppointment(@RequestBody Appointment appointment) {
        paymentIntentService.cancelPayment(appointment, 1); // the 1 is for full refund
        emailService.notifyCancellation(appointment);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/customer-cancel-appointment")
    public ResponseEntity<String> customerCancelAppointment(@RequestBody Records.CustomerCancelAppointmentBody data) {
        Appointment appt = appointmentService.findByBookingIdAndEmailAndStatusNotCancel(data.bookingId(), data.email());

        LocalDate apptDate = appt.getAppointmentDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate today = LocalDate.now();

        long daysUntil = ChronoUnit.DAYS.between(today, apptDate);
        if (daysUntil < 0) return ResponseEntity.badRequest().body("Cannot cancel an appointment that has already occurred");

        if (daysUntil >= 2)  paymentIntentService.cancelPayment(appt, Double.parseDouble(CANCELLATION_PERCENT));
        else appointmentService.updateStatus(appt, Status.CANCELED);

        availabilityService.updateAvailability(appt, true);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/paypal-info")
    public ResponseEntity<RevenueDetails> getPaypalStats() {
        return ResponseEntity.ok(financesUtil.financeDetails());
    }
}
