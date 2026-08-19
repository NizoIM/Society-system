package Nomhala.Society.service;

import Nomhala.Society.dto.PaymentDTO;
import Nomhala.Society.entity.Payment;
import Nomhala.Society.entity.PaymentStatus;
import Nomhala.Society.entity.User;
import Nomhala.Society.repository.PaymentRepository;
import Nomhala.Society.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepo;

    private final UserRepository userRepo;

    // =========================================
    // CONSTANTS
    // =========================================

    public static final BigDecimal MONTHLY_FEE =
            BigDecimal.valueOf(150);

    public static final BigDecimal LATE_PENALTY =
            BigDecimal.valueOf(20);

    // =========================================
    // UPLOAD PAYMENT
    // =========================================

    public PaymentDTO uploadPayment(

            String email,

            BigDecimal amount,

            MultipartFile file
    ) {

        User member =
                userRepo.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException("User not found")
                        );

        validateFile(file);

        if (
                amount == null ||
                        amount.compareTo(BigDecimal.ZERO) <= 0
        ) {

            throw new RuntimeException(
                    "Invalid payment amount"
            );
        }

        try {

            // =========================================
            // CREATE DIRECTORY
            // =========================================

            String uploadDir =
                    System.getProperty("user.dir")
                            + "/uploads/payments/";

            File dir =
                    new File(uploadDir);

            if (!dir.exists()) {

                boolean created =
                        dir.mkdirs();

                if (!created) {

                    throw new RuntimeException(
                            "Could not create upload directory"
                    );
                }
            }

            // =========================================
            // SAVE FILE
            // =========================================

            String filename =
                    UUID.randomUUID()
                            + "_"
                            + file.getOriginalFilename();

            File destination =
                    new File(uploadDir, filename);

            file.transferTo(destination);

            // =========================================
            // CREATE PAYMENT
            // =========================================

            Payment payment =
                    new Payment();

            payment.setMember(member);

            payment.setAmount(amount);

            payment.setPaymentDate(
                    LocalDate.now()
            );

            payment.setPaymentMonth(
                    LocalDate.now()
                            .withDayOfMonth(1)
            );

            payment.setProofPath(
                    destination.getAbsolutePath()
            );

            payment.setOriginalFileName(
                    file.getOriginalFilename()
            );

            payment.setStatus(
                    PaymentStatus.PENDING
            );

            // =========================================
            // SMART CALCULATIONS
            // =========================================

            boolean overdue =
                    isOverdue(member);

            BigDecimal expected =
                    MONTHLY_FEE;

            if (overdue) {

                expected =
                        expected.add(LATE_PENALTY);

                payment.setPenaltyAmount(
                        LATE_PENALTY
                );

                payment.setMonthsBehind(1);

            } else {

                payment.setPenaltyAmount(
                        BigDecimal.ZERO
                );

                payment.setMonthsBehind(0);
            }

            payment.setExpectedAmount(expected);

            // =========================================
            // MONTHS AHEAD
            // =========================================

            int monthsCovered =
                    amount.divide(MONTHLY_FEE, 0, RoundingMode.HALF_UP)
                            .intValue();

            int ahead =
                    Math.max(monthsCovered - 1, 0);

            payment.setMonthsAhead(ahead);

            // =========================================
            // REMAINING BALANCE
            // =========================================

            BigDecimal remaining =
                    expected.subtract(amount);

            if (
                    remaining.compareTo(
                            BigDecimal.ZERO
                    ) < 0
            ) {

                remaining =
                        BigDecimal.ZERO;
            }

            payment.setRemainingBalance(
                    remaining
            );

            // =========================================
            // AUTO APPROVE LOGIC
            // =========================================

            if (
                    amount.compareTo(expected) >= 0
            ) {

                payment.setStatus(
                        PaymentStatus.APPROVED
                );

            } else {

                payment.setStatus(
                        PaymentStatus.OVERDUE
                );
            }

            // =========================================
            // SAVE
            // =========================================

            Payment saved =
                    paymentRepo.save(payment);

            return toDTO(saved);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to upload payment",
                    e
            );
        }
    }

    // =========================================
    // GET PAYMENTS
    // =========================================

    public List<PaymentDTO> getPayments(
            String email
    ) {

        return paymentRepo
                .findByMember_Email(email)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =========================================
    // APPROVE
    // =========================================

    public Payment approve(Long id) {

        Payment payment =
                paymentRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        payment.setStatus(
                PaymentStatus.APPROVED
        );

        return paymentRepo.save(payment);
    }

    // =========================================
    // REJECT
    // =========================================

    public Payment reject(Long id) {

        Payment payment =
                paymentRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        payment.setStatus(
                PaymentStatus.REJECTED
        );

        return paymentRepo.save(payment);
    }

    // =========================================
    // OVERDUE CHECK
    // =========================================

    public boolean isOverdue(User user) {

        List<Payment> payments =
                paymentRepo.findByMember(user);

        if (payments.isEmpty()) {
            return true;
        }

        Payment latest =
                payments.stream()
                        .max(
                                (a, b) ->
                                        a.getPaymentMonth()
                                                .compareTo(
                                                        b.getPaymentMonth()
                                                )
                        )
                        .orElse(null);

        if (latest == null) {
            return true;
        }

        return latest.getPaymentMonth()
                .isBefore(
                        LocalDate.now()
                                .withDayOfMonth(1)
                );
    }

    // =========================================
    // DTO MAPPER
    // =========================================

    private PaymentDTO toDTO(Payment p) {

        PaymentDTO dto =
                new PaymentDTO();

        dto.setId(p.getId());

        if (p.getMember() != null) {

            dto.setEmail(
                    p.getMember().getEmail()
            );
        }

        dto.setAmount(
                p.getAmount()
        );

        dto.setPaymentDate(
                p.getPaymentDate()
        );

        dto.setPaymentMonth(
                p.getPaymentMonth()
        );

        dto.setStatus(
                p.getStatus() != null
                        ? p.getStatus().name()
                        : "PENDING"
        );

        dto.setProofPath(
                p.getProofPath()
        );

        dto.setMonthsAhead(
                p.getMonthsAhead()
        );

        dto.setMonthsBehind(
                p.getMonthsBehind()
        );

        dto.setPenaltyAmount(
                p.getPenaltyAmount()
        );

        dto.setExpectedAmount(
                p.getExpectedAmount()
        );

        dto.setRemainingBalance(
                p.getRemainingBalance()
        );

        dto.setOriginalFileName(
                p.getOriginalFileName()
        );

        return dto;
    }

    // =========================================
    // FILE VALIDATION
    // =========================================

    private void validateFile(
            MultipartFile file
    ) {

        if (
                file == null ||
                        file.isEmpty()
        ) {

            throw new RuntimeException(
                    "File is required"
            );
        }

        if (
                file.getSize() >
                        5 * 1024 * 1024
        ) {

            throw new RuntimeException(
                    "File exceeds 5MB"
            );
        }

        String type =
                file.getContentType();

        if (
                type == null ||

                        (
                                !type.equals("image/jpeg")
                                        && !type.equals("image/png")
                                        && !type.equals("application/pdf")
                        )
        ) {

            throw new RuntimeException(
                    "Only JPG, PNG or PDF allowed"
            );
        }
    }
}
