package Nomhala.Society.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentAdminDTO {

    private Long id;
    private String memberEmail;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private LocalDate paymentMonth;
    private String status;
    private String originalFileName;

    // getters + setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getPaymentMonth() {
        return paymentMonth;
    }

    public void setPaymentMonth(LocalDate paymentMonth) {
        this.paymentMonth = paymentMonth;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public void setMonthsBehind(Integer monthsBehind) {
    }

    public void setMonthsAhead(Integer monthsAhead) {
    }
}