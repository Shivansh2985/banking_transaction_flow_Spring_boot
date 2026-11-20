package com.banking.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor

/*
* Consume transaction.completed Event From Kafka
* Credits Receiver Amount
* */

@KafkaListener( topics = "transaction.completed")
public class AccountEventConsumer {
    private final AccountService accountService;

    public void consumeTransactionCompleted(
            @Payload Map<String,Object> payload){

        try{
            String receiverAccount = (String) payload.get("receiverAccountNumber");
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            log.info("Credit Balance {} for Account Number: {}", amount, receiverAccount);
            accountService.creditBalance(receiverAccount, amount);
        }catch (Exception e){
            log.error("Error while credit balance {}", e.getMessage());
        }
    }

    /*
     * Consume fraud.detected Event From Kafka
     * Block flagged account
     * */
    @KafkaListener(topics = "fraud.detected")
    public void consumeFraudDetected(@Payload Map<String,Object> payload){
        try{
            String accountNumber = (String) payload.get("accountNumber");
            log.info("Fraud Detected - Blocking the Fraud Account : {}", accountNumber);
            accountService.blockAccount(accountNumber);
        }
        catch (Exception e){
            log.error("Error while blocking the Account {}", e.getMessage());
        }
    }
}

