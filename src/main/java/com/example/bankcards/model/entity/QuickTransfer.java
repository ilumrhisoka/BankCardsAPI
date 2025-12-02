package com.example.bankcards.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_quick_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuickTransfer extends BasicEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String cardNumber;
}