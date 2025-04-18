package com.mainlineclean.app.service;

import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.exception.EmailException;
import com.mainlineclean.app.dto.RequestQuote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
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

    public String generateAuthCode() {
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
        String clientText = "Your verification code is: " + code;

        sendEmail(encodedAuth, from, senderEmail, clientSubject, clientText);

        adminDetailsService.setVerificationCode(code);
    }

    public void notifyAppointment(Appointment appointment) throws EmailException {
        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String senderEmail = adminDetailsService.getAdminEmail();

        String from = "Mainline Clean <" +  senderEmail + ">";
        String clientSubject = "Cleaning Appointment Confirmed!";

        sendTemplatedEmail(encodedAuth, from, appointment.getEmail(), clientSubject, getConfirmationJson(appointment), "booking confirmed");
        sendTemplatedEmail(encodedAuth, from, appointment.getEmail(), "Tienes una nueva cita!", getDetailsJson(appointment), "you have a new cleaning client");
    }

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

    public void notifyCancellation(Appointment appointment) {
        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String senderEmail = adminDetailsService.getAdminEmail();

        String from = "Mainline Clean <" +  senderEmail + ">";
        String subject = "Your cleaning appointment has been canceled";
        String text = "Your cleaning appointment with booking ID: " + appointment.getBookingId()
                + " has been canceled. You have been refunded $" + appointment.getNetAmount();

        sendEmail(encodedAuth, from, senderEmail, subject, text);
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

    public void sendTemplatedEmail(String encodedAuth, String from, String to, String subject, String allVars, String template) throws EmailException {
        String postData = "from="  + URLEncoder.encode(from,  StandardCharsets.UTF_8)
                + "&to="    + URLEncoder.encode(to, StandardCharsets.UTF_8)
                + "&subject="+ URLEncoder.encode(subject, StandardCharsets.UTF_8)
                + "&template=" + URLEncoder.encode(template, StandardCharsets.UTF_8)
                + "&h:X-Mailgun-Variables=" + URLEncoder.encode(allVars, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mailgunURL))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type",  "application/x-www-form-urlencoded")
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

    private String getConfirmationJson(Appointment appointment) {
        String adminEmail = adminDetailsService.getAdminEmail();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMMM d, yyyy", Locale.ENGLISH);
        String formattedDate = sdf.format(appointment.getAppointmentDate());
        return "{"
                + "\"address\":\""       + appointment.getAddress() + "\","
                + "\"amountCharged\":\"" + "$"+appointment.getChargedAmount() + "\","
                + "\"bookingId\":\""     + appointment.getBookingId() + "\","
                + "\"cleaningType\":\""  + appointment.getService() + "\","
                + "\"date\":\""          + formattedDate + "\","
                + "\"supportEmail\":\""  + adminEmail + "\","
                + "\"time\":\""          + appointment.getTime() + "\""
                + "}";
    }

    private String getDetailsJson(Appointment appointment) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEEE d 'de' MMMM 'de' yyyy",
                new Locale("es", "ES")
        );
        String formattedDate = sdf.format(appointment.getAppointmentDate());
        return "{"
                + "\"address\":\""       + appointment.getAddress()       + "\","
                + "\"amountCharged\":\"" + "$"+appointment.getChargedAmount() + "\","
                + "\"bookingId\":\""     + appointment.getBookingId()     + "\","
                + "\"cleaningType\":\""  + appointment.getService() + "\","
                + "\"clientContact\":\"" + appointment.getEmail() + " | " +  appointment.getPhone() + "\","
                + "\"dateTime\":\""      + formattedDate   + " | " +  appointment.getTime()         + "\","
                + "\"notes\":\""         + appointment.getNotes()         + "\""
                + "}";
    }
}
