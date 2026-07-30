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

    // ================= OTP =================

    public void sendOtp(String to, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Nomhala Society - OTP Verification");

        message.setText("""
                Your OTP is:

                %s

                This OTP expires in 5 minutes.

                Do not share this code with anyone.
                """.formatted(otp));

        mailSender.send(message);
    }

    // ================= VERIFY EMAIL =================

    public void sendVerificationEmail(User user) {

        try {

            String link =
                    "https://society-kwgy.onrender.com/api/auth/verify?token="
                            + user.getVerificationToken();

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());

            helper.setSubject("Verify your Nomhala Society account");

            helper.setText("""
                    <html>

                    <body style="font-family:Arial;background:#f5f5f5;padding:30px;">

                        <div style="
                            max-width:600px;
                            margin:auto;
                            background:white;
                            padding:30px;
                            border-radius:10px;
                            box-shadow:0 0 10px rgba(0,0,0,.1);">

                            <h2 style="color:#0d6efd;">
                                Welcome to Nomhala Society
                            </h2>

                            <p>
                                Thank you for registering.
                            </p>

                            <p>
                                Please verify your email before logging in.
                            </p>

                            <p style="text-align:center;margin:40px 0;">

                                <a href="%s"
                                   style="
                                       background:#0d6efd;
                                       color:white;
                                       padding:14px 28px;
                                       text-decoration:none;
                                       border-radius:6px;
                                       font-weight:bold;">
                                    Verify Email
                                </a>

                            </p>

                            <p>
                                If the button doesn't work, copy and paste this
                                link into your browser:
                            </p>

                            <p>%s</p>

                            <hr>

                            <small>
                                If you didn't create this account,
                                simply ignore this email.
                            </small>

                        </div>

                    </body>

                    </html>
                    """.formatted(link, link), true);

            mailSender.send(message);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to send verification email",
                    e
            );
        }
    }

    // ================= PAYMENT STATUS =================

    public void sendPaymentStatus(String email,
                                  boolean approved,
                                  String message) {

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(email);

        if (approved) {

            mail.setSubject("Payment Approved");

            mail.setText("""
                    Congratulations!

                    Your payment has been approved.

                    %s

                    Thank you.
                    """.formatted(message));

        } else {

            mail.setSubject("Payment Rejected");

            mail.setText("""
                    Unfortunately your payment was rejected.

                    Reason:

                    %s

                    Please upload a new proof of payment.
                    """.formatted(message));
        }

        mailSender.send(mail);
    }

    // ================= PAYMENT REMINDER =================

    public void sendReminder(String to, String month) {

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(to);

        mail.setSubject("Nomhala Society Payment Reminder");

        mail.setText("""
                This is a reminder that your payment for %s
                is overdue.

                Please make payment as soon as possible.

                Thank you.
                """.formatted(month));

        mailSender.send(mail);
    }
}