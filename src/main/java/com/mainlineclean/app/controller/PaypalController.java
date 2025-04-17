package com.mainlineclean.app.controller;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.entity.PaymentIntent;
import com.mainlineclean.app.exception.PaymentException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.mainlineclean.app.exception.EmailException;
import com.mainlineclean.app.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaypalController {
    private final PaymentIntentService paymentIntentService;
    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;
    private final EmailService emailService;

    public PaypalController(PaymentIntentService paymentIntentService, AvailabilityService availabilityService, AppointmentService appointmentService, EmailService emailService) {
        this.paymentIntentService = paymentIntentService;
        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
        this.emailService = emailService;
    }

    @PostMapping("/paypal/createOrder")
    public ResponseEntity<String> createOrder(@RequestParam(value="serviceType") String serviceType) throws PaymentException {
        PaymentIntent intent = paymentIntentService.createOrder(serviceType);
        return ResponseEntity.ok(intent.getOrderId());
    }

    @PostMapping("/paypal/captureOrder")
    public ResponseEntity<Appointment> captureOrder(@RequestBody Appointment appointment) throws PaymentException, EmailException {
        availabilityService.isAvailableAt(appointment.getAppointmentDate()); // throws exception if not available
        PaymentIntent pi = paymentIntentService.findPaymentIntentByOrderId(appointment.getOrderId());
        String paymentCaptureResponse = paymentIntentService.capturePaymentIntent(pi);
        appointmentService.updateAmountsPaid(appointment, paymentCaptureResponse);
        availabilityService.updateAvailability(appointment);
        Appointment createdAppointment = appointmentService.createAppointment(appointment);
        emailService.notifyAppointment(createdAppointment);
        return ResponseEntity.ok(createdAppointment);
    }

    @PostMapping("/cancel-appointment")
    public ResponseEntity<String> cancelAppointment(@RequestBody Appointment appointment) {
        paymentIntentService.cancelPayment(appointment);
        emailService.notifyCancellation(appointment);
        return ResponseEntity.ok("OK");
    }

//    @GetMapping("/paypal-info")
//    public ResponseEntity<PaypalStats> getPaypalStats() {
//        return ResponseEntity.ok(paypalUtil.getDetails());
//    }
}
