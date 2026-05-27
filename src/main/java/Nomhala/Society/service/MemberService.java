package Nomhala.Society.service;

import Nomhala.Society.entity.MemberQuery;
import Nomhala.Society.entity.Payment;
import Nomhala.Society.entity.PaymentStatus;
import Nomhala.Society.entity.User;
import Nomhala.Society.repository.MemberQueryRepository;
import Nomhala.Society.repository.MemberRepository;
import Nomhala.Society.repository.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import Nomhala.Society.dto.PaymentDTO;
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepo;
    private final MemberQueryRepository queryRepo;
    private final PaymentRepository paymentRepo;

    // ================= PROFILE =================

    public User getProfile(String email) {
        return memberRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ================= MEMBER QUERIES =================

    public void saveQuery(MemberQuery query) {
        query.setStatus("PENDING");
        queryRepo.save(query);
    }

    public List<MemberQuery> getQueries(String email) {
        return queryRepo.findByEmail(email);
    }

    // ================= PAYMENTS =================

    public PaymentDTO uploadPayment(String email, MultipartFile file) {

        User member = memberRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateFile(file);

        try {
            // ✅ safer base directory (no Tomcat temp issues)
            String uploadDir = System.getProperty("user.home")
                    + File.separator + "society_uploads"
                    + File.separator + "payments"
                    + File.separator;

            File dir = new File(uploadDir);

            if (!dir.exists() && !dir.mkdirs()) {
                throw new RuntimeException("Could not create upload directory");
            }

            // ✅ SAFE FILE NAME HANDLING
            String original = file.getOriginalFilename();

            if (original == null || original.isBlank()) {
                throw new RuntimeException("Invalid file name");
            }

            String cleanFileName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String filename = UUID.randomUUID() + "_" + cleanFileName;

            // ✅ Use NIO instead of File.transferTo (more stable)
            Path path = Paths.get(uploadDir + filename);
            Files.copy(file.getInputStream(), path);

            // ✅ Save DB record
            Payment payment = new Payment();
            payment.setMember(member);
            payment.setAmount(BigDecimal.valueOf(500));
            payment.setPaymentDate(LocalDate.now());
            payment.setPaymentMonth(LocalDate.now().withDayOfMonth(1));
            payment.setProofPath(path.toString());
            payment.setOriginalFileName(original);
            payment.setStatus(PaymentStatus.PENDING);

            paymentRepo.save(payment);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload payment proof", e);
        }
        return null;
    }

    // ================= PAYMENT HISTORY =================



    public List<PaymentDTO> getPayments(String email) {

        return paymentRepo.findByMember_Email(email)
                .stream()
                .map(p -> {
                    PaymentDTO dto = new PaymentDTO();

                    dto.setEmail(p.getMember().getEmail());
                    dto.setAmount(p.getAmount());
                    dto.setPaymentDate(p.getPaymentDate());
                    dto.setStatus(p.getStatus().name());
                    dto.setProofPath(p.getProofPath());

                    return dto;
                })
                .toList();
    }
    // ================= FILE VALIDATION =================

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        long maxSize = 5 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds 5MB");
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new RuntimeException("Invalid file type");
        }

        if (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("application/pdf")) {

            throw new RuntimeException("Only JPG, PNG or PDF files allowed");
        }
    }
}