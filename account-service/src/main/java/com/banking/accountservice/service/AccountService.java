package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private static SecureRandom random = new SecureRandom();
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating Account For: {}", request.getEmail());

        if(accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Account already exists for email: " + request.getEmail());
        }

        Account account = new Account();
        account.setEmail(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountType(request.getAccountType());
        account.setAccountStatus(AccountStatus.ACTIVE);;
        account.setBalance(request.getInitialDeposit());
        account.setAccountNumber(generateAccountNumber());
        account.setDailyTransactionLimit(
                request.getAccountType() == AccountType.SAVING ? new BigDecimal("100000")
                        : new BigDecimal("500000")
        );
        Account savedAccount = accountRepository.save(account);
        log.info("Account Created: {}", savedAccount.getAccountNumber());

        return mapToResponse(savedAccount);
    }

    public AccountResponse getAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found"));

        return mapToResponse(account);
    }

    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found"));

        return account.getBalance();
    }

    /*
    * Block Account - Called by Fraud detection Service via Kafka
    * @Param accountNumber
    * */

    public void blockAccount(String accountNumber) {
        log.info("Blocking Account for Account Number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found"));

        account.setAccountStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
        log.info("Account Blocked for Account Number: {}", accountNumber);
    }

    /*
     * Deduct balance
     * Called by Transaction Service
     * */
    public void deductBalance(String accountNumber, BigDecimal amount) {
        log.info("Deducting Balance {} for Account Number: {}", amount, accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found"));

        if(account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account Status Not Active"+accountNumber);
        }

        if(account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Balance Not Enough"+accountNumber);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        log.info("Balance Deducted and New Balance is: {}", account.getBalance());
    }

    /*
    * Credit balance
    * Called by Transaction Service via Kafka
    * */
    public void creditBalance(String accountNumber, BigDecimal amount) {
        log.info("Crediting Balance {} for Account Number: {}", amount, accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found"));

        if(account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account Status Not Active"+accountNumber);
        }
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        log.info("Balance Credited and New Balance is: {}", account.getBalance());
    }


    private String generateAccountNumber() {
        String accountNum;
        do{
            long number = random.nextLong(1_000_000_000_000L);
            accountNum = String.format("%012d", number);
        }while(accountRepository.existsByAccountNumber(accountNum));
        return accountNum;
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setEmail(account.getEmail());
        response.setPhone(account.getPhone());
        response.setAccountType(account.getAccountType());
        response.setAccountStatus(account.getAccountStatus());
        response.setBalance(account.getBalance());
        response.setCreatedAt(account.getCreatedAt());
        response.setDailyTransactionLimit(account.getDailyTransactionLimit());

        return response;
    }
}
