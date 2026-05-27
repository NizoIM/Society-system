package Nomhala.Society.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentDTO {

    private Long id;

    private String email;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private LocalDate paymentMonth;

    private String status;

    private String proofPath;

    private Integer monthsAhead = 0;

    private Integer monthsBehind = 0;

    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    private BigDecimal expectedAmount = BigDecimal.ZERO;

    private BigDecimal remainingBalance = BigDecimal.ZERO;

    public PaymentDTO() {}

    public PaymentDTO(
            Long id,
            String email,
            BigDecimal amount,
            LocalDate paymentDate,
            LocalDate paymentMonth,
            String status,
            String proofPath
    ) {
        this.id = id;
        this.email = email;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMonth = paymentMonth;
        this.status = status;
        this.proofPath = proofPath;
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDate getPaymentMonth() {
        return paymentMonth;
    }

    public void setPaymentMonth(LocalDate paymentMonth) {
        this.paymentMonth = paymentMonth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProofPath() {
        return proofPath;
    }

    public void setProofPath(String proofPath) {
        this.proofPath = proofPath;
    }

    public Integer getMonthsAhead() {
        return monthsAhead;
    }

    public void setMonthsAhead(Integer monthsAhead) {
        this.monthsAhead = monthsAhead;
    }

    public Integer getMonthsBehind() {
        return monthsBehind;
    }

    public void setMonthsBehind(Integer monthsBehind) {
        this.monthsBehind = monthsBehind;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public void setOriginalFileName(String originalFileName) {
    }
}