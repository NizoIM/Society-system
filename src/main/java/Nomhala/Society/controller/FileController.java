package Nomhala.Society.controller;

import Nomhala.Society.entity.Payment;
import Nomhala.Society.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private PaymentRepository paymentRepo;

    // ================= VIEW PAYMENT FILE =================

    @GetMapping("/payment/{id}")
    public ResponseEntity<Resource> getPaymentFile(
            @PathVariable Long id
    ) {

        try {

            Payment payment = paymentRepo.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Payment not found"));

            File file = new File(payment.getProofPath());

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource =
                    new UrlResource(file.toURI().toURL());

            String contentType = "application/octet-stream";

            String filename =
                    payment.getOriginalFileName();

            if (filename != null) {

                if (filename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                }

                else if (
                        filename.endsWith(".png")) {

                    contentType = "image/png";
                }

                else if (
                        filename.endsWith(".jpg")
                                || filename.endsWith(".jpeg")) {

                    contentType = "image/jpeg";
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\""
                    )
                    .body(resource);

        }

        catch (MalformedURLException e) {

            throw new RuntimeException(
                    "File error",
                    e
            );
        }
    }
}