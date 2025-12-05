package com.bank.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.CustomerDetailsDTO;
import com.bank.entity.CustomerDetails;
import com.bank.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
public class CustomerController {
	
	@Autowired
	ModelMapper modelMapper;
	@Autowired
	CustomerService customerService;
	
	// PUBLIC: Initiates registration (Step 1 of 2)
	@PostMapping("/customerRegisterPage")
	public ResponseEntity<String> customerRegistration(@RequestBody @Valid CustomerDetailsDTO customerDetailsDTO) {
		return customerService.customerRegistrationByUsingOTP(customerDetailsDTO);
	}
	
	// PUBLIC: Verifies OTP and saves account as Pending (Step 2 of 2)
	@PostMapping("/verifyRegistration")
	public ResponseEntity<String> verifyOTPAndRegister(@RequestParam("otp") String otp) {
	    return customerService.verifyOTPAndRegister(otp);
	}
	
	// PUBLIC: Endpoint hit by the user during HTTP Basic authentication challenge.
	// No code needed here; Spring Security handles the actual login.
	@GetMapping("/customerLoginPage")
	public ResponseEntity<String> customerLogin() {
		// If a user reaches this GET mapping, authentication was successful.
        return new ResponseEntity<>("Authentication Successful", HttpStatus.OK);
	}
	
	// PROTECTED: Withdrawal (Email/PIN are taken from Security Context)
	@PostMapping("/withdrawMoney")
	public ResponseEntity<String> withdrawMoney(@RequestParam("amount") double amount) {
		return customerService.withdrawMoney(amount);
	}
	
	// PROTECTED: Deposit (Email/PIN are taken from Security Context)
	@PostMapping("/DepositeMoney")
	public ResponseEntity<String> depositeMoney(@RequestParam("amount") double amount) {
		return customerService.depositeMoney(amount);
	}
	
	// PROTECTED: Check Balance (Email/PIN are taken from Security Context)
	@GetMapping("/checkAccountBalance")
	public ResponseEntity<String> checkBalance() {
		return customerService.checkAccountBalance();
	}
	
	// PROTECTED: Change PIN (Relies on Security Context for email, oldPin for validation)
	@PostMapping("/changePin")
	public ResponseEntity<String> ChangePinNumber(
	        @RequestParam("oldpin") String oldPin, 
	        @RequestParam("newpin") String newPin) {
	    
		return customerService.changePinNumber(oldPin, newPin);
	}
	
	// PROTECTED: Mobile Transaction (Retains full arguments for transaction signing)
	@PostMapping("/MobileTransaction") 
	public ResponseEntity<String> mobileTransaction(
	        @RequestParam("customerMobileNumber") long cMobileNumber,
	        @RequestParam("customerpin") String pin,
	        @RequestParam("receiverAccountNumber") long rAccountNumber,
	        @RequestParam("confirmAccountNumber") long rConfirmAccountNumber,
	        @RequestParam("receiverIFSCCode") String rIFSCcode,
	        @RequestParam("amount") double amount) {
	    
		return customerService.mobileTransaction(cMobileNumber, pin, rAccountNumber, rConfirmAccountNumber, rIFSCcode, amount);
	}
	
	// PROTECTED: Request Account Closure (Email/PIN are taken from Security Context)
	@PostMapping("/requestClosingAccount")
	public ResponseEntity<String> requestForClosingAccount() {
		return customerService.requestForClosingAccount();
	}
	
}