package Nomhala.Society.repository;

import Nomhala.Society.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // 🔥 PAGINATION (ADMIN TABLE)
    Page<User> findAll(Pageable pageable);

    // 🔍 SEARCH FEATURE
    List<User> findByEmailContaining(String email);
}