package Nomhala.Society.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
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

    public Long getId() { return id; }

    public User getMember() { return member; }

    public void setMember(User member) { this.member = member; }

    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public LocalDate getPaymentDate() { return paymentDate; }

    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public LocalDate getPaymentMonth() { return paymentMonth; }

    public void setPaymentMonth(LocalDate paymentMonth) { this.paymentMonth = paymentMonth; }

    public String getProofPath() { return proofPath; }

    public void setProofPath(String proofPath) { this.proofPath = proofPath; }

    public String getOriginalFileName() { return originalFileName; }

    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public PaymentStatus getStatus() { return status; }

    public void setStatus(PaymentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Integer getMonthsAhead() { return monthsAhead; }

    public void setMonthsAhead(Integer monthsAhead) { this.monthsAhead = monthsAhead; }

    public Integer getMonthsBehind() { return monthsBehind; }

    public void setMonthsBehind(Integer monthsBehind) { this.monthsBehind = monthsBehind; }

    public BigDecimal getPenaltyAmount() { return penaltyAmount; }

    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }

    public BigDecimal getExpectedAmount() { return expectedAmount; }

    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }

    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
}