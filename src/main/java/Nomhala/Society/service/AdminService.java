package Nomhala.Society.service;

import Nomhala.Society.dto.PaymentAdminDTO;
import Nomhala.Society.dto.QueryDTO;
import Nomhala.Society.dto.UserDTO;
import Nomhala.Society.entity.MemberQuery;
import Nomhala.Society.entity.Payment;
import Nomhala.Society.entity.PaymentStatus;
import Nomhala.Society.entity.User;
import Nomhala.Society.repository.MemberQueryRepository;
import Nomhala.Society.repository.PaymentRepository;
import Nomhala.Society.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepo;
    private final MemberQueryRepository queryRepo;
    private final PaymentRepository paymentRepo;

    // =========================================
    // PROFILE
    // =========================================

    public User getAdminProfile(Authentication auth) {

        String email = auth.getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Admin not found"));
    }

    // =========================================
    // USERS
    // =========================================

    public List<UserDTO> getUsers() {

        return userRepo.findAll()
                .stream()
                .map(u -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(u.getId());
                    dto.setFirstName(u.getFirstName());
                    dto.setLastName(u.getLastName());
                    dto.setEmail(u.getEmail());
                    dto.setPhone(u.getPhone());
                    dto.setRole(String.valueOf(u.getRole()));
                    dto.setEnabled(u.isEnabled());
                    return dto;
                })
                .toList();
    }

    public User updateUser(Long id, User updated) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(updated.getFirstName());
        user.setLastName(updated.getLastName());
        user.setEmail(updated.getEmail());
        user.setPhone(updated.getPhone());
        user.setRole(updated.getRole());
        user.setEnabled(updated.isEnabled());

        return userRepo.save(user);
    }

    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }

    // =========================================
    // QUERIES
    // =========================================

    public List<QueryDTO> getQueries() {

        return queryRepo.findAll()
                .stream()
                .map(q -> {
                    QueryDTO dto = new QueryDTO();
                    dto.setId(q.getId());
                    dto.setEmail(q.getEmail());
                    dto.setSubject(q.getSubject());
                    dto.setMessage(q.getMessage());
                    dto.setStatus(q.getStatus());
                    dto.setResponse(q.getResponse());
                    return dto;
                })
                .toList();
    }

    public MemberQuery respondToQuery(Long id, MemberQuery updated) {

        MemberQuery q = queryRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Query not found"));

        q.setStatus(updated.getStatus());
        q.setResponse(updated.getResponse());

        return queryRepo.save(q);
    }

    // =========================================
    // PAYMENTS
    // =========================================

    public List<PaymentAdminDTO> getPayments() {

        return paymentRepo.findAll()
                .stream()
                .map(p -> {
                    PaymentAdminDTO dto = new PaymentAdminDTO();
                    dto.setId(p.getId());
                    dto.setMemberEmail(p.getMember().getEmail());
                    dto.setAmount(p.getAmount());
                    dto.setPaymentDate(p.getPaymentDate());
                    dto.setPaymentMonth(p.getPaymentMonth());
                    dto.setStatus(p.getStatus().name());
                    dto.setOriginalFileName(p.getOriginalFileName());
                    return dto;
                })
                .toList();
    }

    public String approvePayment(Long id) {

        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.APPROVED);
        paymentRepo.save(payment);

        return "Payment approved successfully";
    }

    public String rejectPayment(Long id) {

        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.REJECTED);
        paymentRepo.save(payment);

        return "Payment rejected successfully";
    }
}