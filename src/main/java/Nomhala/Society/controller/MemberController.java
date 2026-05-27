package Nomhala.Society.controller;

import Nomhala.Society.dto.PaymentDTO;
import Nomhala.Society.entity.MemberQuery;
import Nomhala.Society.entity.User;
import Nomhala.Society.service.MemberService;
import Nomhala.Society.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = "*")
public class MemberController {

    @Autowired
    private MemberService service;

    @Autowired
    private PaymentService paymentService;

    // ================= PROFILE =================

    @GetMapping("/profile/{email}")
    public ResponseEntity<User> profile(@PathVariable String email) {

        User member = service.getProfile(email);

        if (member == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(member);
    }

    // ================= QUERY =================

    @PostMapping("/query")
    public ResponseEntity<String> sendQuery(@RequestBody MemberQuery query) {

        service.saveQuery(query);
        return ResponseEntity.ok("Query sent");
    }

    @GetMapping("/query/{email}")
    public ResponseEntity<List<MemberQuery>> getQueries(@PathVariable String email) {

        return ResponseEntity.ok(service.getQueries(email));
    }

    // ================= PAYMENT UPLOAD (FIXED) =================

    @PostMapping("/payment/{email}")
    public ResponseEntity<PaymentDTO> uploadPayment(

            @PathVariable String email,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("file") MultipartFile file
    ) {

        return ResponseEntity.ok(
                paymentService.uploadPayment(email, amount, file)
        );
    }

    // ================= PAYMENT HISTORY =================

    @GetMapping("/payments/{email}")
    public ResponseEntity<List<PaymentDTO>> getPayments(@PathVariable String email) {

        return ResponseEntity.ok(
                paymentService.getPayments(email)
        );
    }
}