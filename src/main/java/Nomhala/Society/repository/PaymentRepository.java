package Nomhala.Society.repository;

import Nomhala.Society.entity.Payment;
import Nomhala.Society.entity.PaymentStatus;
import Nomhala.Society.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    // =========================================
    // FIND MEMBER PAYMENTS BY EMAIL
    // =========================================

    List<Payment> findByMember_Email(
            String email
    );

    // =========================================
    // FIND PAYMENTS BY STATUS
    // =========================================

    List<Payment> findByStatus(
            PaymentStatus status
    );

    // =========================================
    // FIND MEMBER PAYMENTS BY MEMBER ID
    // =========================================

    List<Payment> findByMemberId(
            Long memberId
    );

    // =========================================
    // FIND MEMBER PAYMENTS BY USER
    // =========================================

    List<Payment> findByMember(
            User member
    );

    // =========================================
    // CHECK IF PAYMENT EXISTS FOR MONTH
    // =========================================

    boolean existsByMemberIdAndPaymentMonth(
            Long memberId,
            LocalDate paymentMonth
    );

    // =========================================
    // FIND PAYMENT FOR MEMBER + MONTH
    // =========================================

    Optional<Payment> findByMemberIdAndPaymentMonth(
            Long memberId,
            LocalDate paymentMonth
    );

    // =========================================
    // FIND LATEST PAYMENT
    // =========================================

    Optional<Payment> findTopByMemberIdOrderByPaymentMonthDesc(
            Long memberId
    );

    // =========================================
    // FIND OVERDUE PAYMENTS
    // =========================================

    List<Payment> findByStatusAndPaymentMonthBefore(
            PaymentStatus status,
            LocalDate date
    );

    // =========================================
    // FIND APPROVED PAYMENTS
    // =========================================

    List<Payment> findByMemberIdAndStatus(
            Long memberId,
            PaymentStatus status
    );

    // =========================================
    // FIND PAYMENTS BETWEEN DATES
    // =========================================

    List<Payment> findByPaymentDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByMemberAndPaymentMonth(
            User user, LocalDate localDate);
}