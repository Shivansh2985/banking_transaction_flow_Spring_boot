package com.banking.transactionservice.entity;


/*
* Transaction LifeCycle Flow
* PENDING -> PROCESSING -> COMPLETED (Clean Transaction)
*  - - -- - - --        -> PENDING_VERIFICATION (suspicious found)
*                           -> COMPLETED (verified)
*                           -> FLAGGED (SAGA REfUND)
*  PENDING -> PROCESSING -> FAILED -> FLAGGED
* */
public enum TransactionStatus {
    PENDING,
    PROCESSING,
    PENDING_VERIFICATION,
    COMPLETED,
    FAILED,
    FLAGGED
}
