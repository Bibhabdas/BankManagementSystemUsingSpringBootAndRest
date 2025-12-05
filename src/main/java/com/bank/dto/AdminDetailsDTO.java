package com.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDetailsDTO {
	@Email(message = "Not a valid mail id")
	private String adminEmailid;
	@NotNull @Size(min = 6,message = "Invalid Password")
	private String adminPassword;
	@NotNull @Pattern(regexp = "^[A-Za-z0-9]+$",message = "Invalid Password")
	private String adminRole;
}
