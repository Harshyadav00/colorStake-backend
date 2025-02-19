package com.example.demo.Repository;

import com.example.demo.Entity.Entity.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WithdrawalRepository extends JpaRepository<WithdrawalRequest, Long> {

    Optional<WithdrawalRequest> findByUserEmail(String userEmail);
}
