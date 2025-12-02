package com.example.bankcards.model.entity.enums;

/**
 * Defines the type of financial transaction.
 */
public enum TransactionType {
    DEBIT,          // Списание (Общее)
    CREDIT,         // Зачисление (Общее)
    PURCHASE,       // Покупка
    ATM_WITHDRAWAL, // Снятие наличных в банкомате
    FEE,            // Комиссия
    TRANSFER_IN,    // Входящий перевод
    TRANSFER_OUT    // Исходящий перевод
}