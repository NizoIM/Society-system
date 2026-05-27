package Nomhala.Society.repository;

import Nomhala.Society.entity.MemberQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberQueryRepository
        extends JpaRepository<MemberQuery, Long> {

    List<MemberQuery> findByEmail(String email);
}