package com.appointments.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class NotificationService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.from.number}")
    private String twilioFromNumber;

    @PostConstruct
    public void init() {
        if (twilioAccountSid != null && !twilioAccountSid.isEmpty()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            log.info("Twilio initialized");
        }
    }

    public boolean sendEmail(String to, String subject, String body) {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            log.warn("SendGrid API key not configured, skipping email");
            return false;
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", body);
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent successfully to {}", to);
                return true;
            } else {
                log.error("Failed to send email. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("Error sending email to {}", to, e);
            return false;
        }
    }

    public boolean sendSMS(String to, String body) {
        if (twilioAccountSid == null || twilioAccountSid.isEmpty()) {
            log.warn("Twilio credentials not configured, skipping SMS");
            return false;
        }

        if (to == null || to.isEmpty()) {
            log.warn("Recipient phone number is empty, skipping SMS");
            return false;
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioFromNumber),
                    body
            ).create();

            log.info("SMS sent successfully to {}. SID: {}", to, message.getSid());
            return true;
        } catch (Exception e) {
            log.error("Error sending SMS to {}", to, e);
            return false;
        }
    }
}
