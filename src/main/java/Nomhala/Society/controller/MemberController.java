package Nomhala.Society.controller;

import Nomhala.Society.dto.PaymentDTO;
import Nomhala.Society.dto.UserDTO;
import Nomhala.Society.entity.MemberQuery;
import Nomhala.Society.entity.User;
import Nomhala.Society.service.MemberService;
import Nomhala.Society.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
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
    public ResponseEntity<UserDTO> profile(@PathVariable String email) {

        User member = service.getProfile(email);

        if (member == null) {
            return ResponseEntity.notFound().build();
        }

        UserDTO dto = new UserDTO();
        dto.setId(member.getId());
        dto.setFirstName(member.getFirstName());
        dto.setLastName(member.getLastName());
        dto.setEmail(member.getEmail());
        dto.setPhone(member.getPhone());
        dto.setRole(String.valueOf(member.getRole()));
        dto.setEnabled(member.isEnabled());

        return ResponseEntity.ok(dto);
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

    @Transactional
    @PostMapping("/payment/{email}")
    public ResponseEntity<PaymentDTO> uploadPayment(

            @PathVariable String email,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) {

        // Verify the authenticated user is uploading for themselves (unless admin)
        String userEmail = auth.getName();
        if (!userEmail.equals(email) && !auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN"))) {
            return ResponseEntity.status(403).build();
        }

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