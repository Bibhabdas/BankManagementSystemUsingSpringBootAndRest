package com.bank.dto;

import java.sql.Date;
import java.sql.Time;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailsDTO {
	@Positive(message = "Transaction Amount Should be positive")
	private double transactionAmount;
	@NotNull
	private String transactionType;
	@NotNull
	private Date transactionDate;
	@NotNull
	private Time transactionTime;
	@NotNull
	private int customerAccountNumber;
	@PositiveOrZero
	private double balanceAmount;
	@NotNull
	private int receiverAccountNumber;
}
