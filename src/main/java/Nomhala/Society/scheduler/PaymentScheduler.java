package Nomhala.Society.scheduler;

import Nomhala.Society.entity.*;
import Nomhala.Society.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PaymentScheduler {

    private final UserRepository userRepo;
    private final PaymentRepository paymentRepo;

    public PaymentScheduler(
            UserRepository userRepo,
            PaymentRepository paymentRepo
    ) {
        this.userRepo = userRepo;
        this.paymentRepo = paymentRepo;
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void markOverdueMembers() {

        List<User> users =
                userRepo.findAll();

        for (User user : users) {

            boolean paid =
                    paymentRepo.existsByMemberAndPaymentMonth(
                            user,
                            LocalDate.now()
                                    .withDayOfMonth(1)
                    );

            if (!paid) {

                Payment overdue =
                        new Payment();

                overdue.setMember(user);

                overdue.setAmount(
                        java.math.BigDecimal.ZERO
                );

                overdue.setPaymentDate(LocalDate.now());

                overdue.setPaymentMonth(
                        LocalDate.now()
                                .withDayOfMonth(1)
                );

                overdue.setStatus(
                        PaymentStatus.OVERDUE
                );

                overdue.setMonthsBehind(1);

                overdue.setPenaltyAmount(
                        java.math.BigDecimal.valueOf(20)
                );

                overdue.setExpectedAmount(
                        java.math.BigDecimal.valueOf(170)
                );

                paymentRepo.save(overdue);
            }
        }
    }
}