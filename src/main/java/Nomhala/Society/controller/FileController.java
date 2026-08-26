package Nomhala.Society.controller;

import Nomhala.Society.entity.Payment;
import Nomhala.Society.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            @PathVariable Long id,
            Authentication auth
    ) throws Exception {

        Payment payment =
                paymentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Payment not found"));

        // Verify the authenticated user owns this payment
        String userEmail = auth.getName();
        boolean isAdminOrStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN")
                        || a.getAuthority().contains("STAFF"));

        if (!isAdminOrStaff
                && (payment.getMember() == null
                        || !payment.getMember().getEmail().equals(userEmail))) {
            return ResponseEntity.status(403).build();
        }

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