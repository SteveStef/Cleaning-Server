package com.mainlineclean.app.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public class Records {
    public record FindAppointmentBody(String bookingId, String email){};
    public record CustomerCancelAppointmentBody(String bookingId, String email, String reason){};

}
