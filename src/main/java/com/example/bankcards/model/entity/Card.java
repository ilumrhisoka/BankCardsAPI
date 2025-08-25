package com.example.bankcards.model.entity;

import com.example.bankcards.model.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_cards")
public class Card extends BasicEntity {

    @Column(nullable = false, length = 100)
    private String cardNumber;

    @Column(nullable = false, length = 100)
    private String cardHolder;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus cardStatus = CardStatus.ACTIVE;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy="toCard", fetch = FetchType.LAZY)
    private List<Transfer> incomingTransfers = new ArrayList<>();

    @OneToMany(mappedBy="fromCard", fetch = FetchType.LAZY)
    private List<Transfer> outgoingTransfers = new ArrayList<>();
}
