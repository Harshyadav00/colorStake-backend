package com.example.demo.Repository;

import com.example.demo.Entity.Entity.Bet;
import com.example.demo.Entity.Enum.Colour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, String> {
    List<Bet> findByRoundIdAndChoice(String id, Colour winningColor);

    Page<Bet> findAllByUserId(String userId, Pageable pageable);

}
