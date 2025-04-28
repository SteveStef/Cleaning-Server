package com.mainlineclean.app.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;

public class Messages {

    @Value("twilio.account-sid")
    public String ACCOUNT_SID;

    @Value("twilio.auth-token")
    public String AUTH_TOKEN;

    @Value("twilio.to-number")
    public String TO_PHONE_NUMBER;

    @Value("twilio.message-number")
    public String MESSAGE_NUMBER;

    public void sendSMS() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                        new com.twilio.type.PhoneNumber(TO_PHONE_NUMBER),
                        MESSAGE_NUMBER,
                        "Hello There!")
                .create();
        System.out.println(message.getSid());
    }

    public static void main(String[] args) {}
}
