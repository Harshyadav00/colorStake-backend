package com.example.demo.Entity.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Account {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String Id;
    private String name;
    private String email;
    private Double balance = 0.0;
    private Double totalAmountWon = 0.0;
    private Double totalAmountAdded = 0.0;
    private Double totalAmountWithdrawn = 0.0;
    private int totalBetPlayed = 0;
    private int totalBetLost = 0;
    private int totalBetWon = 0;


}
