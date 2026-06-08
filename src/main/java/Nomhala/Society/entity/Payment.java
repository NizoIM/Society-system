package Nomhala.Society.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private User member;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private LocalDate paymentMonth;

    @Column(nullable = false)
    private String proofPath;

    @Column(nullable = false)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    @Column(length = 500)
    private String notes;
    // ✅ KEEP ONLY THIS (NO PrePersist)
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ================= SMART FIELDS =================

    private Integer monthsAhead = 0;
    private Integer monthsBehind = 0;

    private BigDecimal penaltyAmount = BigDecimal.ZERO;
    private BigDecimal expectedAmount = BigDecimal.ZERO;
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    // ================= GETTERS & SETTERS =================

    public void setMember(User member) { this.member = member; }

    public void setId(Long id) { this.id = id; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public void setPaymentMonth(LocalDate paymentMonth) { this.paymentMonth = paymentMonth; }

    public void setProofPath(String proofPath) { this.proofPath = proofPath; }

    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public void setStatus(PaymentStatus status) { this.status = status; }

    public void setMonthsAhead(Integer monthsAhead) { this.monthsAhead = monthsAhead; }

    public void setMonthsBehind(Integer monthsBehind) { this.monthsBehind = monthsBehind; }

    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }

    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }

    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }


}