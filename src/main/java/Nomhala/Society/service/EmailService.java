package Nomhala.Society.service;

import Nomhala.Society.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private SendGridService sendGridService;

    @Value("${app.base.url:https://society-system-production.up.railway.app}")
    private String baseUrl;

    // ================= OTP =================

    public void sendOtp(String to, String otp) {

        String subject = "Nomhala Society - OTP Verification";

        String content = """
                Your OTP is:

                %s

                This OTP expires in 5 minutes.

                Do not share this code with anyone.
                """.formatted(otp);

        sendGridService.sendEmail(to, subject, content);
    }

    // ================= VERIFY EMAIL =================

    public void sendVerificationEmail(User user) {

        String link =
                baseUrl + "/api/auth/verify?token="
                        + user.getVerificationToken();

        String subject = "Verify your Nomhala Society account";

        String htmlContent = """
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
                """.formatted(link, link);

        sendGridService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    // ================= PAYMENT STATUS =================

    public void sendPaymentStatus(String email,
                                  boolean approved,
                                  String message) {

        String subject;
        String content;

        if (approved) {

            subject = "Payment Approved";

            content = """
                    Congratulations!

                    Your payment has been approved.

                    %s

                    Thank you.
                    """.formatted(message);

        } else {

            subject = "Payment Rejected";

            content = """
                    Unfortunately your payment was rejected.

                    Reason:

                    %s

                    Please upload a new proof of payment.
                    """.formatted(message);
        }

        sendGridService.sendEmail(email, subject, content);
    }

    // ================= PAYMENT REMINDER =================

    public void sendReminder(String to, String month) {

        String subject = "Nomhala Society Payment Reminder";

        String content = """
                This is a reminder that your payment for %s
                is overdue.

                Please make payment as soon as possible.

                Thank you.
                """.formatted(month);

        sendGridService.sendEmail(to, subject, content);
    }
}
