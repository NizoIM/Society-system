package Nomhala.Society.service;

import Nomhala.Society.entity.Payment;
import Nomhala.Society.entity.PaymentStatus;
import Nomhala.Society.entity.User;
import Nomhala.Society.repository.PaymentRepository;
import Nomhala.Society.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentMonitorService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    // =========================================
    // AUTO DETECT OVERDUE MEMBERS
    // Runs every day at 1AM
    // =========================================

    @Scheduled(cron = "0 0 1 * * *")
    public void detectOverdueMembers() {

        // Current month
        LocalDate currentMonth =
                LocalDate.now()
                        .withDayOfMonth(1);

        // Load all users
        List<User> users =
                userRepo.findAll();

        for (User user : users) {

            // Only monitor MEMBERS
            if (
                    user.getRole() == null ||
                            !user.getRole()
                                    .name()
                                    .equalsIgnoreCase("MEMBER")
            ) {
                continue;
            }

            // Check if payment already exists
            boolean paymentExists =
                    paymentRepo.existsByMemberIdAndPaymentMonth(
                            user.getId(),
                            currentMonth
                    );

            // Skip if already paid/record exists
            if (paymentExists) {
                continue;
            }

            // Create overdue payment record
            Payment overdue =
                    new Payment();

            overdue.setMember(user);

            overdue.setAmount(
                    BigDecimal.ZERO
            );

            overdue.setPaymentMonth(
                    currentMonth
            );

            overdue.setPaymentDate(
                    LocalDate.now()
            );

            overdue.setStatus(
                    PaymentStatus.OVERDUE
            );

            // Optional notes
            overdue.setNotes(
                    "Auto-generated overdue payment"
            );

            // Required field - auto-generated payments have no uploaded file
            overdue.setOriginalFileName(
                    "AUTO_GENERATED"
            );

            paymentRepo.save(overdue);

            System.out.println(
                    "OVERDUE PAYMENT CREATED FOR: "
                            + user.getEmail()
            );
        }
    }
}