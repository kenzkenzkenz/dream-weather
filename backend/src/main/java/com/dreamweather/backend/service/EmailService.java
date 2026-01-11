package com.dreamweather.backend.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final SendGrid sendGrid;
    private final String emailAddress;

    public EmailService(
            @Value("${contact.email}") String emailAddress,
            @Value("${sendgrid.api.key}") String apiKey) {

        this.emailAddress = emailAddress;

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("sendgrid.api.key not set. Emails will not be sent.");
            this.sendGrid = null;
        } else {
            this.sendGrid = new SendGrid(apiKey);
        }
    }

    @Async
    public void sendEmail(String subject, String body) {
        if (sendGrid == null) {
            log.warn("Email not sent: SendGrid not configured");
            return;
        }

        Email from = new Email(emailAddress);
        Email to = new Email(emailAddress);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                log.error(
                    "Failed to send email. Status: {}, Body: {}",
                    response.getStatusCode(),
                    response.getBody()
                );
            } else {
                log.info("Email sent successfully to self (status {})",
                    response.getStatusCode());
            }

        } catch (IOException ex) {
            log.error("Exception while sending email", ex);
        }
    }
}
