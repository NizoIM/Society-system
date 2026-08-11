package Nomhala.Society.controller;

import Nomhala.Society.entity.Payment;
import Nomhala.Society.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    @GetMapping("/payment/{id}")
    public ResponseEntity<Resource> viewPayment(
            @PathVariable Long id
    ) throws Exception {

        Payment payment =
                paymentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Payment not found"));

        Path filePath =
                Paths.get(payment.getProofPath());

        Resource resource =
                new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("File not found");
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                payment.getOriginalFileName() +
                                "\""
                )
                .body(resource);
    }
}