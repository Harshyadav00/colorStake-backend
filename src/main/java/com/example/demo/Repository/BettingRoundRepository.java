package com.example.demo.Repository;

import com.example.demo.Entity.Entity.BettingRound;
import com.example.demo.Entity.Enum.RoundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BettingRoundRepository extends JpaRepository<BettingRound, String> {

    @Query("SELECT b FROM BettingRound b WHERE b.status = :status")
    Page<BettingRound> findAllByStatus(@Param("status") RoundStatus status, Pageable pageable);


}
