package com.marketplace.trade.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card_listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cardId; // Referência ao ID do card-service (MongoDB)

    @Column(nullable = false)
    private String sellerName;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String cardCondition; // Ex: "Near Mint", "Lightly Played", "Heavily Played"

    @Column(nullable = false)
    private String status; // Ex: "AVAILABLE", "SOLD"
}
