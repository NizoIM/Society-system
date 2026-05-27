package Nomhala.Society.repository;

import Nomhala.Society.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByFirstNameAndPhone(
            String firstName,
            String phone
    );
}