package com.mainlineclean.app.config;

import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.model.Time;
import com.mainlineclean.app.repository.AppointmentRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TestAppointmentDataLoader implements CommandLineRunner {

    private final AppointmentRepo appointmentRepository;

    public TestAppointmentDataLoader(AppointmentRepo appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Appointment> appointments = new ArrayList<>();

        ServiceType[] services = ServiceType.values();
        Time[] times = Time.values();
        Status[] statuses = Status.values();

        // Generate 20 test appointments with both past and future dates
        for (int i = 1; i <= 20; i++) {
            Appointment appt = new Appointment();
            appt.setClientName("Test Client " + i);
            appt.setEmail("test" + i + "@example.com");
            appt.setPhone("555-010" + String.format("%02d", i));
            appt.setZipcode("1910" + (10 + i));

            // Random enum values
            appt.setService(services[ThreadLocalRandom.current().nextInt(services.length)]);
            appt.setTime(times[ThreadLocalRandom.current().nextInt(times.length)]);
            appt.setStatus(Status.CONFIRMED);

            // Random appointment date offset between -10 (past) and +10 (future) days
            int offsetDays = ThreadLocalRandom.current().nextInt(-10, 11);
            LocalDateTime apptDateTime = LocalDateTime.now().plusDays(offsetDays);
            appt.setAppointmentDate(Date.from(apptDateTime.atZone(ZoneId.systemDefault()).toInstant()));

            // Random createdAt date in March, April, or May 2025
            int[] months = {3, 4, 5};
            int month = months[ThreadLocalRandom.current().nextInt(months.length)];
            int maxDay = (month == 4 ? 30 : 31);
            int day = ThreadLocalRandom.current().nextInt(1, maxDay + 1);
            int hour = ThreadLocalRandom.current().nextInt(8, 18);
            int minute = ThreadLocalRandom.current().nextInt(0, 60);
            LocalDateTime createdDateTime = LocalDateTime.of(2025, month, day, hour, minute);
            appt.setCreatedAt(Date.from(createdDateTime.atZone(ZoneId.systemDefault()).toInstant()));

            appt.setAddress(i + " Test St, Philadelphia, PA");
            appt.setNotes("This is a test appointment number " + i);
            appt.setOrderId("ORDER-" + UUID.randomUUID());
            appt.setCaptureId("CAPTURE-" + UUID.randomUUID());

            // Financials
            BigDecimal charged = BigDecimal.valueOf(100 + i);
            BigDecimal fee = charged.multiply(BigDecimal.valueOf(0.029)).add(BigDecimal.valueOf(0.30));
            appt.setChargedAmount(charged);
            appt.setPaypalFee(fee.setScale(2, BigDecimal.ROUND_HALF_UP));
            appt.setGrossAmount(charged.add(fee).setScale(2, BigDecimal.ROUND_HALF_UP));

            appt.setBookingId("BK-" + UUID.randomUUID().toString().substring(0,5).toUpperCase());
            appt.setSmsConsent(i % 2 == 0);
            appt.setSquareFeet(ThreadLocalRandom.current().nextInt(500, 3000));
            appt.setApplicationFee(BigDecimal.valueOf(15));

            appointments.add(appt);
        }

        appointmentRepository.saveAll(appointments);
        System.out.println("Loaded " + appointments.size() + " test appointments.");
    }
}
