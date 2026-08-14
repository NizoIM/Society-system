package Nomhala.Society.controller;

import Nomhala.Society.dto.PaymentAdminDTO;
import Nomhala.Society.dto.PaymentDTO;
import Nomhala.Society.dto.UserDTO;
import Nomhala.Society.entity.MemberQuery;
import Nomhala.Society.entity.User;
import Nomhala.Society.repository.MemberQueryRepository;
import Nomhala.Society.repository.PaymentRepository;
import Nomhala.Society.repository.UserRepository;

import Nomhala.Society.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private MemberQueryRepository repo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepo;

    // ================= STAFF PROFILE =================

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getStaffProfile(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User staff = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Staff user not found"));

        UserDTO dto = new UserDTO();
        dto.setId(staff.getId());
        dto.setFirstName(staff.getFirstName());
        dto.setLastName(staff.getLastName());
        dto.setEmail(staff.getEmail());
        dto.setPhone(staff.getPhone());
        dto.setRole(String.valueOf(staff.getRole()));
        dto.setEnabled(staff.isEnabled());

        return ResponseEntity.ok(dto);
    }

    // ================= GET ALL QUERIES =================

    @GetMapping("/queries")
    public ResponseEntity<List<MemberQuery>> getAll() {

        return ResponseEntity.ok(
                repo.findAll()
        );
    }

    // ================= RESPOND TO QUERY =================

    @PutMapping("/queries/{id}")
    public ResponseEntity<MemberQuery> respondQuery(
            @PathVariable Long id,
            @RequestBody MemberQuery request
    ) {

        MemberQuery q = repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Query not found"));

        // staff response
        q.setResponse(request.getResponse());

        // update status
        q.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : "RESOLVED"
        );

        MemberQuery updated = repo.save(q);

        return ResponseEntity.ok(updated);
    }
    // ================= PAYMENT UPLOAD =================

    @PostMapping("/payments/upload")
    public ResponseEntity<PaymentDTO> uploadPayment(
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) {
        String email = auth.getName();

        return ResponseEntity.ok(
                paymentService.uploadPayment(email, amount, file)
        );
    }

    // ================= PAYMENT HISTORY =================

    @GetMapping("/payments/history")
    public ResponseEntity<List<PaymentDTO>> getPayments(

            Authentication authentication
    ) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(

                paymentService.getPayments(email)
        );
    }
    // ================= PAYMENTS (READ ONLY) =================

    @GetMapping("/payments")
    public List<PaymentAdminDTO> getPayments() {

        return paymentRepo.findAll().stream().map(p -> {
            PaymentAdminDTO dto = new PaymentAdminDTO();
            dto.setId(p.getId());
            dto.setMemberEmail(p.getMember().getEmail());
            dto.setAmount(p.getAmount());
            dto.setPaymentDate(p.getPaymentDate());
            dto.setPaymentMonth(p.getPaymentMonth());
            dto.setStatus(p.getStatus().name());
            dto.setOriginalFileName(p.getOriginalFileName());
            return dto;
        }).toList();
    }
}