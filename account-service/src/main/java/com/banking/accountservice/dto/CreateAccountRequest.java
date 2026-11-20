package com.banking.accountservice.dto;


import com.banking.accountservice.entity.AccountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "AccountHolderName must not be empty")
    private String accountHolderName;

    @NotBlank(message = "Email must be required")
    @Email(message = "Please Enter correct Email Address")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotNull(message = "AccountType should not be null")
    private AccountType accountType;

    @NotNull(message = "Initial Deposit Amount should not be null")
    @Positive(message = "Amount should not be negative")
    private BigDecimal initialDeposit;

}
