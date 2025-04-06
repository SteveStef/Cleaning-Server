package com.mainlineclean.app.service;

import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.exception.EmailException;
import com.mainlineclean.app.dto.RequestQuote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

@Service
public class EmailService {

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.http-endpoint}")
    private String mailgunURL;

    private final AdminDetailsService adminDetailsService;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public EmailService(AdminDetailsService adminDetailsService) {
        this.adminDetailsService = adminDetailsService;
    }

    private String generateAuthCode() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 6; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    public void sendVerificationCode() throws EmailException {
        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String senderEmail = adminDetailsService.getAdminEmail();
        String code = generateAuthCode();

        String from = "Mainline Clean <" +  senderEmail + ">";
        String clientSubject = "Here is your admin verification code";
        String clientText = "Your verification code is: " + code + " (Note if this is not you then someone knows your password and you should change it. (talk to the developer of the website)";

        sendEmail(encodedAuth, from, senderEmail, clientSubject, clientText);

        adminDetailsService.setVerificationCode(code);
    }

    /**
     * Sends two emails when an appointment is booked: one confirmation email to the client,
     * and a notification email to the sender email with the appointment details.
     *
     * @param appointment the booked appointment details
     */
    public void notifyAppointment(Appointment appointment) throws EmailException {
        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String senderEmail = adminDetailsService.getAdminEmail();

        // Email to the client
        String from = "Mainline Clean <" +  senderEmail + ">";
        String clientTo = appointment.getEmail();
        String clientSubject = "Appointment Confirmation for " + appointment.getService();
        String clientText = "Dear " + appointment.getClientName() + ",\n\n"
                + "Your appointment for " + appointment.getService() + " on " + appointment.getAppointmentDate()
                + " at " + appointment.getTime() + " is confirmed.\n"
                + "Your booking ID is: " + appointment.getBookingId() + "\n\n"
                + "Thank you for choosing our services.\n"
                + "Best regards,\n"
                + "Mainline Clean Team";

        sendEmail(encodedAuth, from, clientTo, clientSubject, clientText);

        String adminSubject = "New Appointment Booked: " + appointment.getService();
        String adminText = "A new appointment has been booked with the following details:\n\n"
                + "Client Name: " + appointment.getClientName() + "\n"
                + "Booking ID: " + appointment.getBookingId() + "\n"
                + "Email: " + appointment.getEmail() + "\n"
                + "Phone: " + appointment.getPhone() + "\n"
                + "Service: " + appointment.getService() + "\n"
                + "Appointment Date: " + appointment.getAppointmentDate() + "\n"
                + "Time: " + appointment.getTime() + "\n"
                + "Address: " + appointment.getAddress() + "\n"
                + "Notes: " + appointment.getNotes() + "\n"
                + "Booking ID: " + appointment.getBookingId() + "\n";

        sendEmail(encodedAuth, from, senderEmail, adminSubject, adminText);
    }

    /**
     * Sends an email using the Mailgun HTTP API.
     *
     * @param encodedAuth Base64-encoded authorization header value
     * @param from        sender email address (with friendly name)
     * @param to          recipient email address
     * @param subject     email subject
     * @param text        email body text
     */
    private void sendEmail(String encodedAuth, String from, String to, String subject, String text) throws EmailException {
        String postData = "from=" + URLEncoder.encode(from, StandardCharsets.UTF_8)
                + "&to=" + URLEncoder.encode(to, StandardCharsets.UTF_8)
                + "&subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mailgunURL))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new EmailException("Got a non-200 response: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new EmailException("HTTP request error for Mailgun: " + e.toString());
        }
    }

    public void sendQuote(RequestQuote userInfo) throws EmailException {
        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String senderEmail = adminDetailsService.getAdminEmail();

        String from = userInfo.getEmail();
        String subject = userInfo.getFirstName() + " " + userInfo.getLastName() +
                " sent you a message via Main Line Cleaners";
        String text = userInfo.getMessage() + "\n\nPhone: " + userInfo.getPhone() + "\n";

        sendEmail(encodedAuth, from, senderEmail, subject, text);
    }
}
