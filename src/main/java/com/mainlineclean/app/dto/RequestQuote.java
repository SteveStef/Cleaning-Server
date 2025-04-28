package com.mainlineclean.app.dto;

public class RequestQuote {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String service;
    private String message;
    private boolean smsConsent;

    public RequestQuote() {}

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSmsConsent() {
        return smsConsent;
    }

    public void setSmsConsent(boolean smsConsent) {
        this.smsConsent = smsConsent;
    }

    @Override
    public String toString() {
        return "Quote from " + firstName + " " + lastName;
    }
}
