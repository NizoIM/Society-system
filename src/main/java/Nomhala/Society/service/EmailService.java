package Nomhala.Society.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(
            String to,
            String otp
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(to);

        message.setSubject(
                "MySociety OTP Verification"
        );

        message.setText(
                "Your OTP is: " + otp
        );

        mailSender.send(message);
    }
    @Service
    public class ReminderService {

        @Autowired
        private JavaMailSender mailSender;

        public void sendReminder(
                String to,
                String month
        ) {

            SimpleMailMessage mail =
                    new SimpleMailMessage();

            mail.setTo(to);

            mail.setSubject(
                    "Society Payment Reminder"
            );

            mail.setText(

                    "Your payment for "
                            + month +
                            " is overdue."
            );

            mailSender.send(mail);
        }
    }
}