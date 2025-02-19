package com.example.demo.Repository;

import com.example.demo.Entity.Entity.Account;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByEmail(@NotBlank @Email String email);

    // This method retrieves the balance directly by email
    @Query("Select a.balance from Account a where a.email = :email")
    double getBalanceByEmail(@Param("email") String userEmail);

    // This method retrieves balance by user ID, if needed
    @Query("SELECT a.balance FROM Account a WHERE a.id = :id")
    double getBalanceById(@Param("id") String id);
}

