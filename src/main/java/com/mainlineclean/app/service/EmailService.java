package com.mainlineclean.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.dto.RequestQuote;
import com.mainlineclean.app.exception.EmailException;
import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.Time;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.mainlineclean.app.model.EmailTemplates;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class EmailService {

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.http-endpoint}")
    private String mailgunURL;

    private final AdminDetailsService adminDetailsService;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Map<ServiceType, String> SERVICE_DESCRIPTIONS = new EnumMap<>(ServiceType.class);
    static {
        SERVICE_DESCRIPTIONS.put(ServiceType.REGULAR,           "Regular Cleaning");
        SERVICE_DESCRIPTIONS.put(ServiceType.ENVIRONMENT,       "Eco-Friendly Cleaning");
        SERVICE_DESCRIPTIONS.put(ServiceType.DEEP,              "Deep Cleaning");
        SERVICE_DESCRIPTIONS.put(ServiceType.HAZMAT,            "Hazardous Materials Cleanup");
        SERVICE_DESCRIPTIONS.put(ServiceType.FIRE,              "Fire Damage Restoration");
        SERVICE_DESCRIPTIONS.put(ServiceType.WATER,             "Water Damage Restoration");
        SERVICE_DESCRIPTIONS.put(ServiceType.MOVE_IN_OUT,       "Move-In/Out Cleaning");
        SERVICE_DESCRIPTIONS.put(ServiceType.DECEASED,          "Deceased Estate Cleaning");
        SERVICE_DESCRIPTIONS.put(ServiceType.EXPLOSIVE_RESIDUE, "Explosive Residue Cleanup");
        SERVICE_DESCRIPTIONS.put(ServiceType.MOLD,              "Mold Remediation");
        SERVICE_DESCRIPTIONS.put(ServiceType.CONSTRUCTION,      "Construction Cleaning");
        SERVICE_DESCRIPTIONS.put(ServiceType.COMMERCIAL,        "Commercial Cleaning");
    }
    private static final Map<ServiceType, String> SERVICE_DESCRIPTIONS_ES = new EnumMap<>(ServiceType.class);
    static {
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.REGULAR,           "Limpieza Regular");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.ENVIRONMENT,       "Limpieza Ecológica");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.DEEP,              "Limpieza Profunda");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.HAZMAT,            "Limpieza de Materiales Peligrosos");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.FIRE,              "Restauración por Daños de Incendio");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.WATER,             "Restauración por Daños de Agua");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.MOVE_IN_OUT,       "Limpieza de Entrada/Salida");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.DECEASED,          "Limpieza de Bienes de Difuntos");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.EXPLOSIVE_RESIDUE, "Limpieza de Residuos Explosivos");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.MOLD,              "Remediación de Moho");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.CONSTRUCTION,      "Limpieza de Construcción");
        SERVICE_DESCRIPTIONS_ES.put(ServiceType.COMMERCIAL,        "Limpieza Comercial");
    }
    private static final Map<Time, String> TIME_DESCRIPTION_ES = new EnumMap<>(Time.class);
    static {
        TIME_DESCRIPTION_ES .put(Time.MORNING,   "Mañana");
        TIME_DESCRIPTION_ES .put(Time.AFTERNOON, "Tarde");
        TIME_DESCRIPTION_ES  .put(Time.NIGHT,     "Noche");
    }

    public EmailService(AdminDetailsService adminDetailsService) {
        this.adminDetailsService = adminDetailsService;
    }

    public String generateAuthCode() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 6; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }
    public void sendEmailToClients(List<String> emails, String message) {

    }

    public void notifyCancellation(Appointment appointment) {
        String senderEmail = adminDetailsService.getAdminEmail();
        String from = "Dos Chicas <" + senderEmail + ">";
        String subject = "Your cleaning appointment has been canceled";

        String allVars = getCancellationJson(appointment, true);
        sendTemplatedEmail(appointment.getEmail(), from, subject, allVars, EmailTemplates.CANCELLED_APPOINTMENT_ENGLISH);

        allVars = getCancellationJson(appointment, false);
        sendTemplatedEmail(senderEmail, from, "Una cita ha sido cancelada", allVars, EmailTemplates.CANCELLED_APPOINTMENT_SPANISH);
    }

    public void notifyAppointment(Appointment appointment) {
        String senderEmail = adminDetailsService.getAdminEmail();
        String from = "Dos Chicas <" + senderEmail + ">";

        String clientSubject = "Your Cleaning Appointment is Scheduled!";
        String allVars = getConfirmationJson(appointment);
        sendTemplatedEmail(appointment.getEmail(), from, clientSubject, allVars, EmailTemplates.APPOINTMENT_CONFIRMED);

        String cleaningLadySubject = "Tienes una nueva cita de limpieza";
        allVars = getDetailsJson(appointment);
        sendTemplatedEmail(senderEmail, from, cleaningLadySubject, allVars, EmailTemplates.NEW_APPOINTMENT);
    }

    public void sendVerificationCode() {
        String senderEmail = adminDetailsService.getAdminEmail();
        String code = generateAuthCode();
        String clientSubject = "Dos Chicas Verification Code";
        String from = "Dos Chicas <" + senderEmail + ">";

        adminDetailsService.setVerificationCode(code);
        String body = "{"+"\"code\":\"" + code + "\"" + "}";
        sendTemplatedEmail(senderEmail, from, clientSubject, body, EmailTemplates.VERIFICATION_CODE);
    }

    public void sendQuote(RequestQuote userInfo) {
        String senderEmail = adminDetailsService.getAdminEmail();

        String from = "Dos Chicas <" + userInfo.getEmail() + ">";
        String subject = userInfo.getFirstName() + " " + userInfo.getLastName() + " te ha enviado un mensaje a través de Dos Chicas";

        String allVars = getRequestQuoteJson(userInfo);
        sendTemplatedEmail(senderEmail, from, subject, allVars, EmailTemplates.REQUEST_QUOTE);
    }

    public void sendTemplatedEmail(String to, String from, String subject, String allVars, EmailTemplates template) {
        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        String postData = "from="  + URLEncoder.encode(from,  StandardCharsets.UTF_8)
                + "&to="    + URLEncoder.encode(to, StandardCharsets.UTF_8)
                + "&subject="+ URLEncoder.encode(subject, StandardCharsets.UTF_8)
                + "&template=" + URLEncoder.encode(template.toString().toLowerCase(), StandardCharsets.UTF_8)
                + "&h:X-Mailgun-Variables=" + URLEncoder.encode(allVars, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mailgunURL))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type",  "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();

        final long BASE_DELAY = 1_000L;
        final int MAX_RETRIES = 3;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    log.info("Email with template of {} sent successfully to {}", template.toString().toLowerCase(), to);
                    return;
                }
                // retryable HTTP statuses
                if (status == 429 || (status >= 500 && status < 600)) {
                    long backoff = BASE_DELAY * (1L << (attempt - 1)) + ThreadLocalRandom.current().nextLong(0, 500);
                    log.warn("Retrying from non-200 status code in {}ms (attempt {})", backoff, attempt);
                    Thread.sleep(backoff);
                    continue;
                }
                throw new EmailException("Permanent HTTP error: " + status + " – " + response.body());
            } catch (IOException e) {
                if (attempt == MAX_RETRIES) {
                    log.error("Network failure after {} attempts {}", MAX_RETRIES, e.getMessage());
                    throw new EmailException("Network failure after " + MAX_RETRIES + " attempts\n" + e.toString());
                }
                long backoff = BASE_DELAY * (1L << (attempt - 1)) + ThreadLocalRandom.current().nextLong(0, 500);
                log.warn("Retrying from exception in {}ms (attempt {})", backoff, attempt);
                try { Thread.sleep(backoff); }
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new EmailException("Interrupted during backoff" + ie.getMessage());
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted during email send");
                throw new EmailException("Send interrupted" + ie.getMessage());
            }
        }
        throw new EmailException("Exceeded max retries without success");
    }

    private record ConfirmationEmailBody(String address, String amountCharged, String bookingId, String cleaningType, String date, String supportEmail, String time){};
    private String getConfirmationJson(Appointment appointment) {
        String adminEmail = adminDetailsService.getAdminEmail();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMMM d, yyyy", Locale.ENGLISH);
        String formattedDate = sdf.format(appointment.getAppointmentDate());
        ObjectMapper mapper = new ObjectMapper();
        String jsonPayload;
        try {
            jsonPayload = mapper.writeValueAsString(new ConfirmationEmailBody(
                    appointment.getAddress(),
                    "$"+appointment.getChargedAmount(),
                    appointment.getBookingId(),
                    SERVICE_DESCRIPTIONS.get(appointment.getService()),
                    formattedDate,
                    adminEmail,
                    appointment.getTime().toString().toLowerCase()
            ));
        } catch(Exception e) {
            log.error("Error serializing confirmation email: {}", e.getMessage());
            throw new EmailException("Error serializing appointment to JSON: " + e.getMessage());
        }
        return jsonPayload;
    }

    private record AppointmentEmailBody(String address, String chargedAmount, String bookingId, String cleaningType, String clientContact, String dateTime){};
    private String getDetailsJson(Appointment appointment) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String formattedDate = sdf.format(appointment.getAppointmentDate());
        String jsonPayload;
        try {
            jsonPayload = mapper.writeValueAsString(new AppointmentEmailBody(
                    appointment.getAddress(),
                    "$"+appointment.getChargedAmount(),
                    appointment.getBookingId(),
                    SERVICE_DESCRIPTIONS_ES.get(appointment.getService()),
                    appointment.getEmail() + " | " +  appointment.getPhone(),
                    formattedDate + " | " +  TIME_DESCRIPTION_ES.get(appointment.getTime())
            ));
        } catch(Exception e) {
            log.error("Error when creating the appointment into the record: {}", e.getMessage());
            throw new EmailException("Error serializing appointment to JSON: " + e.getMessage());
        }
        return jsonPayload;
    }

    private record CancellationEmailBody(String address, String bookingId, String cleaningType, String dateTime){};
    private String getCancellationJson(Appointment appointment, boolean inEnglish) {
        String cleaningType = inEnglish ? SERVICE_DESCRIPTIONS.get(appointment.getService()) : SERVICE_DESCRIPTIONS_ES.get(appointment.getService());
        SimpleDateFormat sdf = inEnglish ? new SimpleDateFormat("EEEE MMMM d, yyyy", Locale.ENGLISH) :
                new SimpleDateFormat("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String time = inEnglish ? appointment.getTime().toString().toLowerCase() : TIME_DESCRIPTION_ES.get(appointment.getTime());
        String formattedDate = sdf.format(appointment.getAppointmentDate());

        String jsonPayload;
        try {
            jsonPayload = mapper.writeValueAsString(new CancellationEmailBody(
                    appointment.getAddress(),
                    appointment.getBookingId(),
                    cleaningType,
                    formattedDate + " | " + time
            ));
        } catch(Exception e) {
            log.error("Error when creating the cancellation into the record: {}", e.getMessage());
            throw new EmailException("Error serializing appointment to JSON: " + e.getMessage());
        }
        return jsonPayload;
    }


    private record QuoteEmailBody(String name, String email, String serviceType, String phone, String message){};
    private String getRequestQuoteJson(RequestQuote quote) {
        String jsonPayload;
        try {
            jsonPayload = mapper.writeValueAsString(new QuoteEmailBody(
                    quote.getFirstName() + " " + quote.getLastName(),
                    quote.getEmail(), SERVICE_DESCRIPTIONS_ES.get(quote.getService()),
                    quote.getPhone(), quote.getMessage()
            ));
        } catch(Exception e) {
            log.error("Error when creating the quote into the record: {}", e.getMessage());
            throw new EmailException("Error serializing appointment to JSON: " + e.getMessage());
        }
        return jsonPayload;
    }
}
