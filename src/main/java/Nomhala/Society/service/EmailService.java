package Nomhala.Society.service;

import Nomhala.Society.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    public void send(String email, String paymentApproved, String s) {

    }

    public void sendVerificationEmail(User user) {
        try {

            String link =
                    "https://society-kwgy.onrender.com/api/auth/verify?token="
                            + user.getVerificationToken();

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(user.getEmail());

            helper.setSubject("Verify your Nomhala Society account");

            helper.setText("""
                <h2>Welcome to Nomhala Society</h2>

                <p>Click the button below to verify your email.</p>

                <a href="%s"
                   style="
                     background:#0d6efd;
                     color:white;
                     padding:12px 25px;
                     text-decoration:none;
                     border-radius:6px;">
                     Verify Email
                </a>

                <br><br>

                If you did not create this account, ignore this email.
                """.formatted(link), true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
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