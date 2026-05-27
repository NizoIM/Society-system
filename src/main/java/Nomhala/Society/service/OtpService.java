package Nomhala.Society.service;

import Nomhala.Society.entity.OtpVerification;
import Nomhala.Society.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OtpRepository repo;
    @Autowired
    private OtpRepository otpRepository;

    public void sendOtp(String email) {

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        OtpVerification record = new OtpVerification();
        record.setEmail(email);
        record.setOtp(otp);
        record.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        repo.save(record);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("OTP Verification");
        msg.setText("Your OTP is: " + otp);

        mailSender.send(msg);
    }

    public boolean verifyOtp(String email, String otp) {

        OtpVerification record =
                repo.findTopByEmailOrderByIdDesc(email)
                        .orElse(null);

        if (record == null) return false;

        if (record.getExpiryTime().isBefore(LocalDateTime.now()))
            return false;

        return record.getOtp().equals(otp);
    }
    public void clearOtp(String email) {
        otpRepository.deleteByEmail(email);
    }
}