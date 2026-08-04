package Nomhala.Society.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SendGridService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String htmlContent) {

        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(apiKey);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                System.err.println("SendGrid error: " + response.getStatusCode()
                        + " - " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("Failed to send email via SendGrid: " + e.getMessage());
        }
    }
}
