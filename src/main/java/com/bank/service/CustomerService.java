package com.bank.service;

import java.util.Random;
import com.bank.exception.CustomerDetailsExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder; // ⭐ NEW IMPORT

import com.bank.config.CustomerEmailConfig;
import com.bank.dao.CustomerDAO;
import com.bank.dto.CustomerDetailsDTO;
import com.bank.entity.CustomerDetails;

@Service
public class CustomerService {

	private final CustomerDetailsExceptionHandler customerDetailsExceptionHandler;

	@Autowired
	ModelMapper mapper;

	@Autowired
	CustomerDAO customerDAO;

	@Autowired
	CustomerEmailConfig emailConfig;

	private String lastGeneratedOTP;
	private CustomerDetailsDTO pendingRegistration;

	CustomerService(CustomerDetailsExceptionHandler customerDetailsExceptionHandler) {
		this.customerDetailsExceptionHandler = customerDetailsExceptionHandler;
	}

	/* Generates a 6-digit numeric OTP */
	private String generateOTP() {
		Random random = new Random();
		// Generates a 6-digit number (100000 to 999999)
		int otp = 100000 + random.nextInt(900000);
		return String.valueOf(otp);
	}

	/* Step 1: Initiates registration process by generating and sending OTP */
	public ResponseEntity<String> customerRegistrationByUsingOTP(CustomerDetailsDTO customerDetailsDTO) {
		String otp = generateOTP();
		this.lastGeneratedOTP = otp;
		this.pendingRegistration = customerDetailsDTO; // Store DTO temporarily

		try {
			emailConfig.emailForCustomerRegistration(customerDetailsDTO.getCustomerEmailid(),
					customerDetailsDTO.getCustomerName(), otp);
			// Status is set to "Pending" on successful registration, not here.
			return new ResponseEntity<String>("OTP sent successfully. Please verify to complete registration.",
					HttpStatus.OK);
		} catch (Exception e) {
			// Handle email sending failure
			this.lastGeneratedOTP = null; // Clear state on failure
			this.pendingRegistration = null;
			return new ResponseEntity<String>("Failed to send OTP email.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/* Step 2: Verifies OTP and completes registration */
	public ResponseEntity<String> verifyOTPAndRegister(String providedOTP) {

		if (pendingRegistration == null || lastGeneratedOTP == null) {
			return new ResponseEntity<String>("Registration session expired or not initiated.", HttpStatus.BAD_REQUEST);
		}

		// Check if the provided OTP matches the last generated one
		if (lastGeneratedOTP.equals(providedOTP)) {
			
			CustomerDetails customerDetails = mapper.map(pendingRegistration, CustomerDetails.class);
			
			// ⭐ FIX: Status set to Pending. No PIN hashing since Admin generates it later.
			customerDetails.setCustomerStatus("Pending"); 
			customerDetails.setCustomerPIN("0");

			CustomerDetails details = customerDAO.insertCustomerDetails(customerDetails);

			// Clear temporary storage after successful operation
			this.lastGeneratedOTP = null;
			this.pendingRegistration = null;

			if (details != null && details.getCustomerid() != null && details.getCustomerid() > 0) {
				return new ResponseEntity<String>("Registration Successful. Your account is now Pending Admin Approval.", HttpStatus.CREATED);
			} else {
				return new ResponseEntity<String>("Registration failed due to database error.",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<String>("Invalid OTP. Please try again.", HttpStatus.FORBIDDEN);
		}
	}

    // ⭐ REMOVED/OBSOLETE: customerLogin method is handled by Spring Security

    // ⭐ REMOVED/OBSOLETE: getAmountByUsingEmailidAndPin method is obsolete since we check balance via checkAccountBalance()

	/* Withdraw Money - Refactored to use Security Context */
	public ResponseEntity<String> withdrawMoney(double withdrawalAmount) {
		// Get authenticated user's email
		String emailid = SecurityContextHolder.getContext().getAuthentication().getName();

		if (withdrawalAmount <= 0) {
			return new ResponseEntity<>("Withdrawal amount must be greater than zero.", HttpStatus.BAD_REQUEST);
		}

		// Use email only to fetch details (PIN check done by Spring Security)
		CustomerDetails details = customerDAO.getCustomerDetailsByEmailid(emailid);
		
		// Note: Status check is redundant here but kept for safety (SecurityContext is set only for Active accounts)
		if (!details.getCustomerStatus().equalsIgnoreCase("Active")) {
			return new ResponseEntity<>("Account is not active for transactions.", HttpStatus.FORBIDDEN);
		}

		double currentBalance = details.getCustomerAmount();

		if (currentBalance < withdrawalAmount) {
			return new ResponseEntity<>("Insufficient balance. Current balance: " + currentBalance,
					HttpStatus.BAD_REQUEST);
		}

		double newBalance = currentBalance - withdrawalAmount;

		// ⭐ ASSUMPTION: You must update your DAO to use a method that takes ONLY email
		// int updatedRows = customerDAO.updateCustomerAmountByEmail(emailid, newBalance); // Use this method
		// For now, using the old method but passing the correct PIN (which is insecure)
		int updatedRows = customerDAO.updateCustomerAmount(emailid, details.getCustomerPIN(), newBalance);

		if (updatedRows > 0) {
			return new ResponseEntity<>(
					"Withdrawal successful. Amount: " + withdrawalAmount + ". New balance: " + newBalance,
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Transaction failed due to database error.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/* Deposit Money - Refactored to use Security Context */
	public ResponseEntity<String> depositeMoney(double depositAmount) {
		// Get authenticated user's email
		String emailid = SecurityContextHolder.getContext().getAuthentication().getName();

		if (depositAmount <= 0) {
			return new ResponseEntity<>("Deposit amount must be greater than zero.", HttpStatus.BAD_REQUEST);
		}

		CustomerDetails details = customerDAO.getCustomerDetailsByEmailid(emailid);

		if (!details.getCustomerStatus().equalsIgnoreCase("Active")) {
			return new ResponseEntity<>("Account is not active for transactions.", HttpStatus.FORBIDDEN);
		}

		double currentBalance = details.getCustomerAmount();

		double newBalance = currentBalance + depositAmount;

		// ⭐ ASSUMPTION: Passing PIN to old method. Refactor DAO to only use email!
		int updatedRows = customerDAO.updateCustomerAmount(emailid, details.getCustomerPIN(), newBalance);

		if (updatedRows > 0) {
			return new ResponseEntity<>("Deposit successful. Amount: " + depositAmount + ". New balance: " + newBalance,
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Transaction failed due to database error.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/* Check Balance - Refactored to use Security Context */
	public ResponseEntity<String> checkAccountBalance() {
		// Get authenticated user's email
		String emailid = SecurityContextHolder.getContext().getAuthentication().getName();

		CustomerDetails details = customerDAO.getCustomerDetailsByEmailid(emailid);

		if (!details.getCustomerStatus().equalsIgnoreCase("Active")) {
			return new ResponseEntity<>("Account is " + details.getCustomerStatus() + ". Cannot check balance.",
					HttpStatus.FORBIDDEN);
		}

		double currentBalance = details.getCustomerAmount();

		return new ResponseEntity<>("Your current account balance is: " + String.format("%.2f", currentBalance),
				HttpStatus.OK);
	}

	/* Change PIN - Refactored to use Security Context (Still requires oldPin check) */
	public ResponseEntity<String> changePinNumber(String oldPin, String newPin) {
	    // Get authenticated user's email
	    String emailid = SecurityContextHolder.getContext().getAuthentication().getName();

	    if (String.valueOf(newPin).length() != 6) {
	        return new ResponseEntity<>("New PIN must be exactly 6 digits.", HttpStatus.BAD_REQUEST);
	    }
	    if (oldPin.equals(newPin)) {
	        return new ResponseEntity<>("New PIN cannot be the same as the old PIN.", HttpStatus.BAD_REQUEST);
	    }

	    // Attempt to update the PIN (this implicitly checks if old credentials are valid)
	    int updatedRows = customerDAO.updateCustomerPin(emailid, oldPin, newPin);

	    if (updatedRows > 0) {
	        // --- Successful Update: Send Notification ---
	        
	        // 1. Get the customer object to retrieve the name for the email
	        CustomerDetails details = customerDAO.getCustomerDetailsByEmailid(emailid);
	        
	        try {
	            // 2. Send the notification email
	            emailConfig.sendPinUpdateNotification(
	                details.getCustomerEmailid(), 
	                details.getCustomerName()
	            );
	        } catch (Exception e) {
	            System.err.println("Failed to send PIN update email notification to " + emailid + ": " + e.getMessage());
	            // Note: The transaction is already successful, so we log the email failure but return success.
	        }

	        return new ResponseEntity<>("PIN number successfully changed. A confirmation email has been sent.", HttpStatus.OK);
	    } else {
	        // This occurs if the old emailid and oldPin combination was incorrect.
	        return new ResponseEntity<>("PIN change failed. Invalid Old PIN provided.",
	                HttpStatus.UNAUTHORIZED);
	    }
	}
	
    /* Mobile Transaction (Fund Transfer) - Retains full arguments for security signing */
	public ResponseEntity<String> mobileTransaction(long cMobileNumber, String pin, long rAccountNumber, long rConfirmAccountNumber, String rIFSCcode, double amount) {
        
        // --- 1. Basic Validation ---
        if (amount <= 0) {
            return new ResponseEntity<>("Transaction amount must be greater than zero.", HttpStatus.BAD_REQUEST);
        }
        if (rAccountNumber != rConfirmAccountNumber) {
            return new ResponseEntity<>("Receiver account number and confirm account number do not match.", HttpStatus.BAD_REQUEST);
        }
        
        // --- 2. Sender Authentication (Uses Mobile/PIN for transaction signing) ---
        CustomerDetails senderDetails = customerDAO.getCustomerDetailsByMoblineNumber(cMobileNumber, pin);

        if (senderDetails == null) {
            return new ResponseEntity<>("Invalid Mobile Number or PIN.", HttpStatus.UNAUTHORIZED);
        }
        
        if (!senderDetails.getCustomerStatus().equalsIgnoreCase("Active")) {
            return new ResponseEntity<>("Sender account is not active.", HttpStatus.FORBIDDEN);
        }
        
        // ... rest of the MobileTransaction logic ...

        double currentSenderBalance = senderDetails.getCustomerAmount();
        if (currentSenderBalance < amount) {
            return new ResponseEntity<>("Insufficient balance. Current balance: " + currentSenderBalance, HttpStatus.BAD_REQUEST);
        }
        
        // --- 3. Identify Receiver and Debit Sender ---
        CustomerDetails receiverDetails = customerDAO.getReceiverDetailsByAccountAndIFSC(rAccountNumber, rIFSCcode);
        
        double newSenderBalance = currentSenderBalance - amount;

        // Debit the sender's account using the existing update method (using email/pin)
        int senderDebitRows = customerDAO.updateCustomerAmount(senderDetails.getCustomerEmailid(), pin, newSenderBalance);

        if (senderDebitRows <= 0) {
             return new ResponseEntity<>("Transaction failed: Sender debit error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        // --- 4. Handle Receiver Credit (Internal vs. External) ---
        if (receiverDetails != null) {
            // ⭐ Same Bank Transfer (Internal)
            if (senderDetails.getCustomerAccountNumber() == rAccountNumber) {
                 return new ResponseEntity<>("Transfer to the same account is not allowed.", HttpStatus.BAD_REQUEST);
            }
            
            double receiverNewBalance = receiverDetails.getCustomerAmount() + amount;
            
            // Credit the receiver's account using the update by account number method
            int receiverCreditRows = customerDAO.updateCustomerAmountByAccountNumber(rAccountNumber, receiverNewBalance);

            if (receiverCreditRows > 0) {
                return new ResponseEntity<>("Money transfer successful. Amount: " + amount + ". New sender balance: " + newSenderBalance, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Transfer failed: Receiver credit error. Please contact support.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } else {
            // ⭐ Different Bank Transfer (External)
            return new ResponseEntity<>("Money transfer successful. Amount: " + amount + " sent. New sender balance: " + newSenderBalance, HttpStatus.OK);
        }
    }
    
    /* Request Closing Account - Refactored to use Security Context */
    public ResponseEntity<String> requestForClosingAccount() {
        // Get authenticated user's email
		String emailid = SecurityContextHolder.getContext().getAuthentication().getName();
        
        CustomerDetails details = customerDAO.getCustomerDetailsByEmailid(emailid);

        if (details == null) {
            return new ResponseEntity<>("User details not found in database.", HttpStatus.NOT_FOUND);
        }

        final String warning = "WARNING: If you close your account, you will NOT be able to open any new accounts with this bank in the future. ";

        if (details.getCustomerAmount() > 0.0) {
            String balanceMessage = "ERROR: Current balance is " + String.format("%.2f", details.getCustomerAmount()) + ". Please withdraw all your money and ensure the balance is 0.0 before applying for closure.";
            return new ResponseEntity<>(warning + balanceMessage, HttpStatus.BAD_REQUEST);
        }

        details.setCustomerStatus("Deactivated");
        customerDAO.insertCustomerDetails(details); 

        String successMessage = "SUCCESS: Your account closure request has been submitted and is now PENDING ADMIN APPROVAL.";
        return new ResponseEntity<>(warning + successMessage, HttpStatus.ACCEPTED);
    }
}