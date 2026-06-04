package Nomhala.Society.controller;

import Nomhala.Society.dto.PaymentAdminDTO;
import Nomhala.Society.dto.PaymentDTO;
import Nomhala.Society.dto.UserDTO;
import Nomhala.Society.dto.QueryDTO;
import Nomhala.Society.entity.*;
import Nomhala.Society.repository.*;

import Nomhala.Society.service.EmailService;
import Nomhala.Society.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MemberQueryRepository queryRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    // =========================================
    // USERS
    // =========================================

    @GetMapping("/users")
    public List<UserDTO> getUsers() {

        return userRepo.findAll().stream().map(u -> {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
            dto.setEmail(u.getEmail());
            dto.setPhone(u.getPhone());
            dto.setRole(String.valueOf(u.getRole()));
            dto.setEnabled(u.isEnabled());
            return dto;
        }).toList();
    }

    @PostMapping("/users")
    public User add(@RequestBody User u) {

        u.setPassword(
                passwordEncoder.encode(u.getPassword())
        );

        u.setEnabled(true);

        return userRepo.save(u);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @RequestBody User u
    ) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(u.getFirstName());
        user.setLastName(u.getLastName());
        user.setEmail(u.getEmail());
        user.setPhone(u.getPhone());
        user.setRole(u.getRole());
        user.setEnabled(u.isEnabled());

        return ResponseEntity.ok(userRepo.save(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        userRepo.deleteById(id);

        return ResponseEntity.ok("User deleted");
    }

    // =========================================
    // QUERIES
    // =========================================

    @GetMapping("/queries")
    public List<QueryDTO> getQueries() {

        return queryRepo.findAll().stream().map(q -> {
            QueryDTO dto = new QueryDTO();
            dto.setId(q.getId());
            dto.setEmail(q.getEmail());
            dto.setSubject(q.getSubject());
            dto.setMessage(q.getMessage());
            dto.setStatus(q.getStatus());
            dto.setResponse(q.getResponse());
            return dto;
        }).toList();
    }

    @PutMapping("/queries/{id}")
    public ResponseEntity<MemberQuery> respondQuery(
            @PathVariable Long id,
            @RequestBody MemberQuery updated
    ) {

        MemberQuery q = queryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found"));

        q.setStatus(updated.getStatus());
        q.setResponse(updated.getResponse());

        return ResponseEntity.ok(queryRepo.save(q));
    }

    // =========================================
    // ADMIN PROFILE (FIXED JWT USAGE)
    // =========================================

    @GetMapping("/profile")
    public ResponseEntity<User> getAdminProfile(Authentication auth) {

        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();

        User admin = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return ResponseEntity.ok(admin);
    }

    // =========================================
    // PAYMENTS
    // =========================================

    @GetMapping("/payments")
    public List<PaymentAdminDTO> getPayments() {

        return paymentRepo.findAll().stream().map(p -> {

            PaymentAdminDTO dto =
                    new PaymentAdminDTO();

            dto.setId(p.getId());

            dto.setMemberEmail(

                    p.getMember() != null

                            ? p.getMember().getEmail()

                            : "Unknown"
            );

            dto.setAmount(p.getAmount());

            dto.setPaymentDate(p.getPaymentDate());

            dto.setPaymentMonth(p.getPaymentMonth());

            dto.setStatus(

                    p.getStatus() != null

                            ? p.getStatus().name()

                            : "PENDING"
            );

            dto.setOriginalFileName(
                    p.getOriginalFileName()
            );

            return dto;

        }).toList();
    }
    // =========================================
    // APPROVE PAYMENT
    // =========================================

    @PutMapping("/payments/{id}/approve")
    public ResponseEntity<?> approvePayment(@PathVariable Long id) {

        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.APPROVED);
        paymentRepo.save(payment);

        return ResponseEntity.ok(
                new ApiResponse("Payment approved successfully")
        );
    }

    // =========================================
    // REJECT PAYMENT
    // =========================================

    @PutMapping("/payments/{id}/reject")
    public ResponseEntity<?> rejectPayment(@PathVariable Long id) {

        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.REJECTED);

        paymentRepo.save(payment);

        return ResponseEntity.ok(
                new ApiResponse("Payment rejected successfully")
        );
    }
    @GetMapping("/stats")
    public Map<String, Object> getStats() {

        Map<String, Object> stats =
                new HashMap<>();

        List<Payment> payments =
                paymentRepo.findAll();

        long overdue =
                payments.stream()
                        .filter(p ->
                                p.getStatus() ==
                                        PaymentStatus.OVERDUE
                        )
                        .count();

        long approved =
                payments.stream()
                        .filter(p ->
                                p.getStatus() ==
                                        PaymentStatus.APPROVED
                        )
                        .count();

        long severeOverdue =
                payments.stream()
                        .filter(p ->
                                p.getStatus() ==
                                        PaymentStatus.OVERDUE
                        )
                        .filter(p -> {

                            if (p.getPaymentMonth() == null) {
                                return false;
                            }

                            return p.getPaymentMonth()
                                    .isBefore(
                                            LocalDate.now()
                                                    .minusMonths(2)
                                    );
                        })
                        .count();

        stats.put("approvedPayments", approved);

        stats.put("overduePayments", overdue);

        stats.put("severeOverdue", severeOverdue);

        stats.put(
                "totalMembers",
                userRepo.count()
        );

        return stats;
    }
    @GetMapping("/stats/download")
    public void downloadStats(
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/pdf");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=stats.pdf"
        );

        PrintWriter writer =
                response.getWriter();

        writer.println(
                "Total Members,Approved Payments,Overdue Payments"
        );

        long totalMembers =
                userRepo.count();

        long approvedPayments =
                paymentRepo.findAll().stream()
                        .filter(p ->
                                p.getStatus() ==
                                        PaymentStatus.APPROVED
                        )
                        .count();

        long overduePayments =
                paymentRepo.findAll().stream()
                        .filter(p ->
                                p.getStatus() ==
                                        PaymentStatus.OVERDUE
                        )
                        .count();

        writer.println(
                totalMembers + "," +
                        approvedPayments + "," +
                        overduePayments
        );

        writer.flush();
    }

    @GetMapping("/stats/monthly")
    public List<Map<String, Object>> monthlyStats() {

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (int i = 5; i >= 0; i--) {

            YearMonth month =
                    YearMonth.now().minusMonths(i);

            long total =
                    paymentRepo.findAll().stream()
                            .filter(p -> p.getPaymentMonth() != null)
                            .filter(p ->

                                    YearMonth.from(
                                            p.getPaymentMonth()
                                    ).equals(month)
                            )
                            .count();

            Map<String, Object> row =
                    new HashMap<>();

            row.put(
                    "month",
                    month.toString()
            );

            row.put(
                    "payments",
                    total
            );

            result.add(row);
        }

        return result;
    }
    @GetMapping("/payments/export")
    public void exportPdf(
            HttpServletResponse response
    ) throws Exception {

        response.setContentType(
                "application/pdf"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=payments.pdf"
        );

        Document document =
                new Document();

        PdfWriter.getInstance(
                document,
                response.getOutputStream()
        );

        document.open();

        document.add(
                new Paragraph(
                        "Payment Report"
                )
        );

        document.add(
                new Paragraph(" ")
        );

        PdfPTable table =
                new PdfPTable(5);

        table.addCell("ID");
        table.addCell("Member");
        table.addCell("Amount");
        table.addCell("Month");
        table.addCell("Status");

        List<Payment> payments =
                paymentRepo.findAll();

        for (Payment p : payments) {

            table.addCell(
                    String.valueOf(
                            p.getId()
                    )
            );

            table.addCell(
                    p.getMember().getEmail()
            );

            table.addCell(
                    p.getAmount().toString()
            );

            table.addCell(
                    String.valueOf(
                            p.getPaymentMonth()
                    )
            );

            table.addCell(
                    p.getStatus().name()
            );
        }

        document.add(table);

        document.close();
    }
    // =========================================
// ADMIN PAYMENT UPLOAD
// =========================================

    @PostMapping("/payments/upload")
    public ResponseEntity<PaymentDTO> uploadAdminPayment(

            Authentication authentication,

            @RequestParam("amount") BigDecimal amount,

            @RequestParam("file") MultipartFile file
    ) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(

                paymentService.uploadPayment(
                        email,
                        amount,
                        file
                )
        );
    }
    // =========================================
// ADMIN PAYMENT HISTORY
// =========================================

    @GetMapping("/payments/history")
    public ResponseEntity<List<PaymentDTO>> getAdminPayments(

            Authentication authentication
    ) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(

                paymentService.getPayments(email)
        );
    }
    @GetMapping("/ai-summary")
    public Map<String, Object> getAiSummary() {

        Map<String, Object> data = new HashMap<>();

        List<Payment> payments = paymentRepo.findAll();

        long approved =
                payments.stream()
                        .filter(p -> p.getStatus() == PaymentStatus.APPROVED)
                        .count();

        long overdue =
                payments.stream()
                        .filter(p -> p.getStatus() == PaymentStatus.OVERDUE)
                        .count();

        long rejected =
                payments.stream()
                        .filter(p -> p.getStatus() == PaymentStatus.REJECTED)
                        .count();

        long pendingQueries =
                queryRepo.findAll().stream()
                        .filter(q -> !"RESOLVED".equalsIgnoreCase(q.getStatus()))
                        .count();

        BigDecimal totalIncome =
                payments.stream()
                        .filter(p -> p.getStatus() == PaymentStatus.APPROVED)
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        String summary;

        if (overdue > 10) {

            summary =
                    "High overdue payments detected. Immediate follow-up recommended.";

        } else if (pendingQueries > 5) {

            summary =
                    "Several member queries are still unresolved.";

        } else if (approved > overdue) {

            summary =
                    "Payment collection is healthy this month.";

        } else {

            summary =
                    "System operating normally.";
        }

        data.put("approvedPayments", approved);
        data.put("overduePayments", overdue);
        data.put("rejectedPayments", rejected);
        data.put("pendingQueries", pendingQueries);
        data.put("totalIncome", totalIncome);
        data.put("summary", summary);

        return data;
    }

    // =========================================
    // SIMPLE RESPONSE CLASS
    // =========================================

    static class ApiResponse {
        public String message;

        public ApiResponse(String message) {
            this.message = message;
        }
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