package Nomhala.Society.controller;

import Nomhala.Society.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService service;

    @PostMapping("/send")
    public String send(@RequestParam String email) {
        service.sendOtp(email);
        return "OTP sent";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String email,
                         @RequestParam String otp) {

        return service.verifyOtp(email, otp)
                ? "VERIFIED"
                : "INVALID";
    }
}