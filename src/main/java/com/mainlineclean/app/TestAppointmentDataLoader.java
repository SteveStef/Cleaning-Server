package com.mainlineclean.app;

import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.entity.Client;
import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.State;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.model.Time;
import com.mainlineclean.app.repository.AppointmentRepo;
import com.mainlineclean.app.repository.ClientRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This component loads realistic test data for appointments.
 * It is only active in the "dev" profile to prevent accidental data loading in production.
 */
public class TestAppointmentDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestAppointmentDataLoader.class);
    private final AppointmentRepo appointmentRepository;
    private final ClientRepo clientRepo;

    // Constants for more realistic test data
    private static final String[] FIRST_NAMES = {
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
            "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
            "Thomas", "Sarah", "Charles", "Karen", "Daniel", "Nancy", "Matthew", "Lisa"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson",
            "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin",
            "Thompson", "Garcia", "Martinez", "Robinson", "Clark", "Rodriguez", "Lewis", "Lee"
    };

    private static final String[] STREET_NAMES = {
            "Main St", "Oak Ave", "Maple Dr", "Cedar Ln", "Pine St", "Washington Ave",
            "Park Rd", "Lake Dr", "River Rd", "Church St", "Broad St", "Market St",
            "Highland Ave", "Green St", "Walnut St", "Franklin St", "Elm St", "Front St"
    };

    private static final String[] CITIES = {
            "Philadelphia", "Bryn Mawr", "Devon", "Villanova", "Wayne", "Paoli",
            "Ardmore", "Haverford", "Gladwyne", "Narberth", "King of Prussia"
    };

    private static final String[] PA_ZIPS = {
            "19001", "19003", "19010", "19035", "19041", "19072", "19085",
            "19087", "19096", "19301", "19333", "19355", "19406"
    };

    private static final String[] NOTES_TEMPLATES = {
            "Please clean {} thoroughly",
            "Special attention to the {} area",
            "Have pets, please be careful with the door",
            "Don't use strong chemicals due to allergies",
            "Extra attention to {} please",
            "Also need help with {}",
            "Entry code: {}",
            "Please call when you arrive",
            "Ring doorbell, I work from home",
            "Have security system, will disable before your arrival",
            "Need service by noon if possible",
            "Leave key under mat when finished"
    };

    private static final String[] NOTE_FILL_INS = {
            "bathrooms", "kitchen", "living room", "basement", "windows",
            "bedrooms", "carpets", "floors", "furniture", "appliances"
    };

    private static final String[] EMAIL_DOMAINS = {
            "gmail.com", "yahoo.com", "outlook.com", "hotmail.com",
            "aol.com", "icloud.com", "comcast.net", "verizon.net"
    };

    public TestAppointmentDataLoader(AppointmentRepo appointmentRepository, ClientRepo clientRepo) {
        this.appointmentRepository = appointmentRepository;
        this.clientRepo = clientRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists to prevent duplicate loading
        if (appointmentRepository.count() > 0) {
            logger.info("Test appointments already exist. Skipping data load.");
            return;
        }

        createTestClients();

        logger.info("Loading test appointment data...");
        List<Appointment> appointments = new ArrayList<>();

        // Create a mix of past, current, and future appointments with realistic statuses
        createPastAppointments(appointments, 4);      // Past appointments (completed or canceled)
        createCurrentAppointments(appointments, 3);   // Appointments for today and tomorrow
        createFutureAppointments(appointments, 5);    // Future appointments

        appointmentRepository.saveAll(appointments);
        logger.info("Successfully loaded {} test appointments.", appointments.size());
    }

    private void createTestClients() {
        for (int i = 1; i <= 10; i++) {
            Client client = new Client();
            client.setName("Client " + i);
            client.setEmail("client" + i + "@example.com");
            client.setPhone("555-555-5555");
            client.setAddress("123 Main St, Philadelphia, PA 19101");
            client.setZipcode("19095");
            clientRepo.save(client);
        }
    }

    private void createPastAppointments(List<Appointment> appointments, int count) {
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= count; i++) {
            Appointment appt = createBaseAppointment();

            // Set past date (between 1-60 days ago)
            int daysAgo = ThreadLocalRandom.current().nextInt(1, 61);
            LocalDateTime pastDate = today.minusDays(daysAgo).atTime(9, 0);
            appt.setAppointmentDate(Date.from(pastDate.atZone(ZoneId.systemDefault()).toInstant()));

            // Create date before appointment date
            LocalDateTime createdDate = pastDate.minusDays(ThreadLocalRandom.current().nextInt(1, 14));
            appt.setCreatedAt(Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant()));

            // 80% completed, 20% canceled
            if (ThreadLocalRandom.current().nextInt(100) < 80) {
                appt.setStatus(Status.COMPLETED);
            } else {
                appt.setStatus(Status.CANCELED);
            }

            appointments.add(appt);
        }
    }

    private void createCurrentAppointments(List<Appointment> appointments, int count) {
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= count; i++) {
            Appointment appt = createBaseAppointment();

            // Set date to today or tomorrow
            int dayOffset = ThreadLocalRandom.current().nextInt(0, 2); // 0 = today, 1 = tomorrow
            LocalDateTime appointmentDate = today.plusDays(dayOffset).atTime(
                    ThreadLocalRandom.current().nextInt(9, 17),
                    ThreadLocalRandom.current().nextInt(0, 4) * 15); // Time between 9am-5pm, 15-min intervals

            appt.setAppointmentDate(Date.from(appointmentDate.atZone(ZoneId.systemDefault()).toInstant()));

            // Created 1-5 days ago
            LocalDateTime createdDate = today.minusDays(ThreadLocalRandom.current().nextInt(1, 6))
                    .atTime(ThreadLocalRandom.current().nextInt(8, 20),
                            ThreadLocalRandom.current().nextInt(0, 60));

            appt.setCreatedAt(Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant()));
            appt.setStatus(Status.CONFIRMED);

            appointments.add(appt);
        }
    }

    private void createFutureAppointments(List<Appointment> appointments, int count) {
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= count; i++) {
            Appointment appt = createBaseAppointment();

            // Set future date (2-30 days from now)
            int daysAhead = ThreadLocalRandom.current().nextInt(2, 31);
            LocalDateTime futureDate = today.plusDays(daysAhead).atTime(
                    ThreadLocalRandom.current().nextInt(9, 17),
                    ThreadLocalRandom.current().nextInt(0, 4) * 15);

            appt.setAppointmentDate(Date.from(futureDate.atZone(ZoneId.systemDefault()).toInstant()));

            // Created within the last 14 days
            LocalDateTime createdDate = today.minusDays(ThreadLocalRandom.current().nextInt(0, 15))
                    .atTime(ThreadLocalRandom.current().nextInt(8, 20),
                            ThreadLocalRandom.current().nextInt(0, 60));

            appt.setCreatedAt(Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant()));
            appt.setStatus(Status.CONFIRMED);

            appointments.add(appt);
        }
    }

    private Appointment createBaseAppointment() {
        Appointment appt = new Appointment();

        // Generate realistic personal information
        String firstName = FIRST_NAMES[ThreadLocalRandom.current().nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[ThreadLocalRandom.current().nextInt(LAST_NAMES.length)];
        appt.setClientName(firstName + " " + lastName);

        String emailDomain = EMAIL_DOMAINS[ThreadLocalRandom.current().nextInt(EMAIL_DOMAINS.length)];
        appt.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + emailDomain);

        // Phone format: 610-555-XXXX or 484-555-XXXX
        String areaCode = ThreadLocalRandom.current().nextBoolean() ? "610" : "484";
        appt.setPhone(areaCode + "-555-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000)));

        // Address in Philadelphia area
        String streetNumber = String.valueOf(ThreadLocalRandom.current().nextInt(1, 9999));
        String street = STREET_NAMES[ThreadLocalRandom.current().nextInt(STREET_NAMES.length)];
        String city = CITIES[ThreadLocalRandom.current().nextInt(CITIES.length)];
        String zipcode = PA_ZIPS[ThreadLocalRandom.current().nextInt(PA_ZIPS.length)];

        appt.setAddress(streetNumber + " " + street + ", " + city + ", PA");
        appt.setZipcode(zipcode);
        appt.setState(State.PA);

        // Service details
        ServiceType[] services = ServiceType.values();
        Time[] times = Time.values();

        appt.setService(services[ThreadLocalRandom.current().nextInt(services.length)]);
        appt.setTime(times[ThreadLocalRandom.current().nextInt(times.length)]);

        // Generate realistic notes
        String noteTemplate = NOTES_TEMPLATES[ThreadLocalRandom.current().nextInt(NOTES_TEMPLATES.length)];
        String fillIn = NOTE_FILL_INS[ThreadLocalRandom.current().nextInt(NOTE_FILL_INS.length)];
        appt.setNotes(noteTemplate.replace("{}", fillIn));

        // Generate payment details
        int basePrice = determineBasePrice(appt.getService(), ThreadLocalRandom.current().nextInt(500, 3500));
        int priceVariation = ThreadLocalRandom.current().nextInt(-20, 51); // -$20 to +$50 variation

        BigDecimal charged = new BigDecimal(basePrice + priceVariation);
        BigDecimal applicationFee = new BigDecimal(15);
        BigDecimal paypalFee = charged.multiply(new BigDecimal("0.029")).add(new BigDecimal("0.30"))
                .setScale(2, RoundingMode.HALF_EVEN);

        appt.setChargedAmount(charged);
        appt.setApplicationFee(applicationFee);
        appt.setPaypalFee(paypalFee);
        appt.setGrossAmount(charged.add(paypalFee).setScale(2, RoundingMode.HALF_EVEN));

        // Generate IDs
        appt.setBookingId("BK-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        appt.setOrderId("ORDER-" + UUID.randomUUID().toString().substring(0, 8));
        appt.setCaptureId("CAPTURE-" + UUID.randomUUID().toString().substring(0, 8));

        // Other fields
        appt.setSquareFeet(ThreadLocalRandom.current().nextInt(500, 3500));

        return appt;
    }

    private int determineBasePrice(ServiceType serviceType, int squareFeet) {
        // Base pricing logic based on service type and square footage
        int basePrice = 100; // Minimum price

        // Adjust based on service type
        switch (serviceType) {
            case REGULAR:
                basePrice += squareFeet * 0.10;
                break;
            case DEEP:
                basePrice += squareFeet * 0.15;
                break;
            case ENVIRONMENT:
                basePrice += squareFeet * 0.12;
                break;
            case MOVE_IN_OUT:
                basePrice += squareFeet * 0.18;
                break;
            case CONSTRUCTION:
                basePrice += squareFeet * 0.20;
                break;
            case COMMERCIAL:
                basePrice += squareFeet * 0.08;
                break;
            case MOLD:
            case WATER:
            case FIRE:
                basePrice += squareFeet * 0.25;
                break;
            case HAZMAT:
            case EXPLOSIVE_RESIDUE:
            case DECEASED:
                basePrice += squareFeet * 0.35;
                break;
        }

        return (int) basePrice;
    }
}
