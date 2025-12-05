package com.bank.dto;

import java.sql.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailsDTO {
	
	@Pattern(regexp = "^[A-Za-z ]+$", message = "Invalid Name")
	private String customerName;
	@Email
	private String customerEmailid;
	@Min(value = 6000000000l,message = "Invalid Mobile Number") @Max(value = 9999999999l,message = "Invalid Mobile Number")
	private long customerMobileNumber;
	@Min(value = 100000000000l,message = "Invalid Aadhar Number") @Max(value = 999999999999l,message = "Invalid Aadhar Number")
	private long customerAadharNumber;
	private String customerPANnumber;
	@Pattern(regexp = "^(Male|Female|Others)$",message = "Invalid Gender")
	private String customerGender;
	private String customerAddress;
	@Pattern(regexp = "^(Pending|Active|Closed)$",message = "Invalid Gender")
	private String customerStatus;
	private Date dateOfBirth;
	@PositiveOrZero
	private double customerAmount;

}
