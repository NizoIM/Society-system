package Nomhala.Society.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ================= OTP EMAIL =================
    public void sendOtp(String to, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("MySociety OTP Verification");
        message.setText("Your OTP is: " + otp);

        mailSender.send(message);
    }

    // ================= GENERIC EMAIL (FIXED) =================
    public void send(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    // ================= PAYMENT EMAIL HELPERS =================
    public void sendPaymentApproved(String to, String month) {

        send(
                to,
                "Payment Approved",
                "Your payment for " + month + " has been approved."
        );
    }

    public void sendPaymentRejected(String to, String month) {

        send(
                to,
                "Payment Rejected",
                "Your payment for " + month + " was rejected. Please contact admin."
        );
    }

    // ================= REMINDER SERVICE (FIXED SEPARATE CLASS) =================
}