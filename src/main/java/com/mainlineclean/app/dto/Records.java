package com.mainlineclean.app.dto;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.model.Time;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Records {
    public record FindAppointmentBody(String bookingId, String email){};
    public record CustomerCancelAppointmentBody(String bookingId, String email, String reason){};
    public record RescheduleAppointmentBody(String bookingId, String email, Date newAppointmentDate, Time newTime){};
    public record AdminCancelAppointmentBody(Appointment appointment, BigDecimal refundAmount){};
    public record ClientEmailBody(List<String> clientEmails, String subject, String message){};
}
