package com.example.bankcards.service.fee;

import com.example.bankcards.exception.account.AccountNotFoundException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.model.dto.fee.ServiceFeeResponseDto;
import com.example.bankcards.model.entity.Account;
import com.example.bankcards.model.entity.ServiceFee;
import com.example.bankcards.model.entity.Transaction;
import com.example.bankcards.model.entity.enums.FeeType;
import com.example.bankcards.model.entity.enums.NotificationType;
import com.example.bankcards.model.entity.enums.TransferStatus;
import com.example.bankcards.model.entity.enums.TransactionType;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.ServiceFeeRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.notification.NotificationService;
import com.example.bankcards.util.mapper.ServiceFeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing service fees and charging them to accounts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceFeeService {

    private final ServiceFeeRepository serviceFeeRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ServiceFeeMapper serviceFeeMapper;
    private final NotificationService notificationService;

    /**
     * Retrieves all fees associated with the authenticated user's accounts.
     *
     * @param username The username of the user.
     * @return List of fee DTOs.
     */
    public List<ServiceFeeResponseDto> getMyFees(String username) {
        return serviceFeeRepository.findByAccount_User_Username(username).stream()
                .map(serviceFeeMapper::toServiceFeeResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Charges a specific fee to an account. This is typically an internal process.
     *
     * @param accountId The ID of the account to charge.
     * @param amount The amount of the fee.
     * @param feeType The type of fee.
     * @return The created ServiceFee entity.
     */
    @Transactional
    public ServiceFee chargeFee(Long accountId, BigDecimal amount, FeeType feeType) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        ServiceFee fee = new ServiceFee();
        fee.setAccount(account);
        fee.setAmount(amount);
        fee.setFeeType(feeType);
        fee.setDateCharged(LocalDateTime.now());
        fee.setIsPaid(false); // Изначально не оплачено

        ServiceFee savedFee = serviceFeeRepository.save(fee);
        log.info("Fee of {} charged to account {} (Type: {})", amount, accountId, feeType);

        // Попытка немедленного списания
        try {
            processFeePayment(savedFee);
        } catch (Exception e) {
            log.warn("Immediate payment failed for fee {}. Will remain unpaid.", savedFee.getId());
            // Уведомление пользователя о начислении (даже если не списано)
            notificationService.createNotification(
                    account.getUser().getUsername(),
                    String.format("Fee charged: %s of %.2f. Status: Pending payment.", feeType.name(), amount),
                    NotificationType.ALERT
            );
        }

        return savedFee;
    }

    /**
     * Processes the payment for a specific fee, debiting the account balance.
     *
     * @param fee The ServiceFee entity to process.
     */
    @Transactional
    public void processFeePayment(ServiceFee fee) {
        if (fee.getIsPaid()) {
            return; // Уже оплачено
        }

        Account account = fee.getAccount();
        BigDecimal amount = fee.getAmount();

        if (account.getBalance().compareTo(amount) >= 0) {
            // 1. Списание с баланса счета
            account.setBalance(account.getBalance().subtract(amount));
            accountRepository.save(account);

            // 2. Запись детальной транзакции
            recordFeeTransaction(account, amount, fee.getFeeType());

            // 3. Обновление статуса комиссии
            fee.setIsPaid(true);
            serviceFeeRepository.save(fee);

            // 4. Уведомление пользователя
            notificationService.createNotification(
                    account.getUser().getUsername(),
                    String.format("Fee paid: %s of %.2f successfully debited from account %s.",
                            fee.getFeeType().name(), amount, account.getAccountNumber()),
                    NotificationType.INFO
            );
            log.info("Fee {} successfully paid by account {}", fee.getId(), account.getId());
        } else {
            // Уведомление о недостатке средств
            notificationService.createNotification(
                    account.getUser().getUsername(),
                    String.format("Fee payment failed: Insufficient funds (%.2f) to cover %s fee of %.2f.",
                            account.getBalance(), fee.getFeeType().name(), amount),
                    NotificationType.ALERT
            );
            log.warn("Fee payment failed for account {}: Insufficient funds.", account.getId());
            throw new RuntimeException("Insufficient funds to pay the fee.");
        }
    }

    /**
     * Helper method to record the fee transaction.
     */
    private void recordFeeTransaction(Account account, BigDecimal amount, FeeType feeType) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setCard(null); // Комиссия обычно не привязана к конкретной карте
        transaction.setAmount(amount);
        transaction.setType(TransactionType.FEE);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription("Service Fee: " + feeType.name());
        transaction.setStatus(TransferStatus.SUCCESS);
        transactionRepository.save(transaction);
    }

    /**
     * Retrieves all unpaid fees for a specific account (for internal use or user's dashboard).
     */
    public List<ServiceFeeResponseDto> getUnpaidFeesForAccount(Long accountId, String username) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied to this account.");
        }

        return serviceFeeRepository.findByAccount_IdAndIsPaidFalse(accountId).stream()
                .map(serviceFeeMapper::toServiceFeeResponseDto)
                .collect(Collectors.toList());
    }
}