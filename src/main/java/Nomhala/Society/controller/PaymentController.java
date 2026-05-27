package Nomhala.Society.controller;

import Nomhala.Society.dto.PaymentDTO;
import Nomhala.Society.entity.Payment;
import Nomhala.Society.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService service;

    // ================= UPLOAD PAYMENT PROOF =================

    @PostMapping("/upload/{email}")
    public ResponseEntity<PaymentDTO> upload(
            @PathVariable String email,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("file") MultipartFile file
    ) {

        PaymentDTO result =
                service.uploadPayment(email, amount, file);

        return ResponseEntity.ok(result);
    }

    // ================= APPROVE PAYMENT =================

    @PutMapping("/approve/{id}")
    public Payment approve(@PathVariable Long id) {
        return service.approve(id);
    }

    // ================= REJECT PAYMENT =================

    @PutMapping("/reject/{id}")
    public Payment reject(@PathVariable Long id) {
        return service.reject(id);
    }
}