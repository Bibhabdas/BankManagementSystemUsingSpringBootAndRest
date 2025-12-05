package com.bank.service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.config.CustomerEmailConfig;
import com.bank.dao.CustomerDAO;
import com.bank.dto.CustomerDetailsDTO;
import com.bank.entity.CustomerDetails;

@Service
public class AdminService {
	
	@Autowired
	ModelMapper mapper;
	
	@Autowired
	CustomerDAO customerDAO;
	
	@Autowired
    CustomerEmailConfig emailConfig;
	
	public ResponseEntity<List<CustomerDetailsDTO>> getAllCustomerDetails() { 
		List<CustomerDetails> allCustomerDetails = customerDAO.getAllCustomerDetails();
		
		List<CustomerDetailsDTO> allCustomersDTO = allCustomerDetails.stream()
				// Applying the fix for the previous type inference error (added cast)
				.map(customer -> (CustomerDetailsDTO) mapper.map(customer, CustomerDetailsDTO.class)) 
				.collect(Collectors.toList());
        
        // ⭐ Check for empty list and create ResponseEntity
        if (allCustomersDTO.isEmpty()) {
            // Return 404 Not Found if no customers are present
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        }
        
        // Return list with 200 OK status
        return new ResponseEntity<>(allCustomersDTO, HttpStatus.OK);
	}
	
	public ResponseEntity<List<CustomerDetailsDTO>> getAllPendingCustomerDetails() { 
        List<CustomerDetails> pendingCustomers = customerDAO.getAllPendingCustomerDetails();
        
        List<CustomerDetailsDTO> pendingCustomersDTO = pendingCustomers.stream()
                // Map the Entity to the DTO
                .map(customer -> mapper.map(customer, CustomerDetailsDTO.class)) 
                .collect(Collectors.toList());
        
        if (pendingCustomersDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        }
        
        return new ResponseEntity<>(pendingCustomersDTO, HttpStatus.OK);
    }
	
	private int generatePIN() {
        Random random = new Random();
        // Generates a 6-digit number (100000 to 999999)
        return 100000 + random.nextInt(900000);
    }
	
	private long generateUniqueAccountNumber() {
        Random random = new Random();
        // 8-digit range: 10,000,000 to 99,999,999
        long min = 10000000L; 
        long max = 99999999L; 
        long accountNumber;
        
        do {
            // Generate a random number within the 8-digit range
            // nextLong(n) returns a value between 0 (inclusive) and n (exclusive)
            // (max - min + 1) is the size of the range
            accountNumber = min + (long) (random.nextDouble() * (max - min + 1));
            
            // Check if the number already exists using the DAO method
            CustomerDetails existingCustomer = customerDAO.getCustomerByAccountNumber(accountNumber);
            if (existingCustomer == null) {
                return accountNumber; // Return the unique number
            }
        } while (true); // Loop until a unique number is found
    }
	
	
	
	public ResponseEntity<String> updateCustomerDetailsByEmail(String emailid) {
        
        // 1. Get the customer by email ID
        CustomerDetails customerToUpdate = customerDAO.getCustomerDetailsByEmailid(emailid);
        
        // 2. Check if customer exists
        if (customerToUpdate == null) {
            return new ResponseEntity<>("Customer not found with email: " + emailid, HttpStatus.NOT_FOUND);
        }
        
        // 3. Check current status
        if (!customerToUpdate.getCustomerStatus().equalsIgnoreCase("Pending")) {
            return new ResponseEntity<>("Customer status is already: " + customerToUpdate.getCustomerStatus(), HttpStatus.CONFLICT);
        }

        long newAccountNumber = generateUniqueAccountNumber();
        int nPIN = generatePIN();
        String newPIN=nPIN+"";
        String ifscCode = "BANK1234JSP"; 

        // 5. Update all fields
        customerToUpdate.setCustomerStatus("Active"); 
        customerToUpdate.setCustomerAccountNumber(newAccountNumber);
        customerToUpdate.setCustomerPIN(newPIN);
        customerToUpdate.setCustomerIFSCcode(ifscCode);
        
        // 6. Save the updated customer details
        customerDAO.insertCustomerDetails(customerToUpdate);
        
        try {
            emailConfig.sendAccountActivationEmail(
                customerToUpdate.getCustomerEmailid(), 
                customerToUpdate.getCustomerName(), // Need to pass name for personalized body
                newAccountNumber, 
                newPIN, 
                ifscCode
            );
        
        } catch (Exception e) {
            System.err.println("Failed to send activation email: " + e.getMessage());
        }
        
        return new ResponseEntity<>("Account for " + emailid + " activated. Account No: " + newAccountNumber + ", PIN: " + newPIN + ", IFSC: " + ifscCode, HttpStatus.OK);
	}
	
	@Transactional
	public ResponseEntity<String> deleteSpecificCustomerByEmail(String emailid) {
	    // 1. Find the customer object
	    CustomerDetails customer = customerDAO.getCustomerDetailsByEmailid(emailid);
	    
	    // ⭐ FIX: Check if the customer exists before proceeding
	    if (customer == null) {
	        return new ResponseEntity<>("Error: Customer not found with email " + emailid, HttpStatus.NOT_FOUND);
	    }
	    
	    // 2. Perform History Save (Audit Trail)
	    customerDAO.saveDeletedCustomer(customer);
	    
	    // 3. Delete from main table (This is handled by JpaRepository's delete method)
	    customerDAO.deleteCustomer(customer); // Assumes this calls repository.delete(entity)
	    
	    // According to REST principles, a successful deletion should return 200 OK
	    // or 204 No Content. We'll stick to 200 OK with a message.
	    return new ResponseEntity<>("Customer " + emailid + " permanently deleted and moved to history.", HttpStatus.OK);
	}
	
	public ResponseEntity<List<CustomerDetailsDTO>> getAllDeactivatedCustomers() { 
	    
	    // Assuming 'getAllDeactivatedAccount()' fetches entities with status "Pending Closure"
	    List<CustomerDetails> deactivatedCustomers = customerDAO.getAllDeactivatedAccount(); 
	    
	    // ⭐ FIX: Declare and map the list of DTOs here
	    List<CustomerDetailsDTO> deactivatedCustomersDTO = deactivatedCustomers.stream()
	            // Map the Entity to the DTO using your ModelMapper instance
	            .map(customer -> mapper.map(customer, CustomerDetailsDTO.class)) 
	            .collect(Collectors.toList());
	    
	    // Check for an empty list and return 404
	    if (deactivatedCustomersDTO.isEmpty()) {
	        return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
	    }
	    
	    // Return the correctly mapped and populated list of DTOs
	    return new ResponseEntity<>(deactivatedCustomersDTO, HttpStatus.OK);
	}
	
	public ResponseEntity<String> sendAnnualChargeNotification() {
        final double ANNUAL_CHARGE = 285.00; 

        List<CustomerDetails> activeCustomers = customerDAO.getAllActiveCustomers();

        if (activeCustomers.isEmpty()) {
            return new ResponseEntity<>("No active customers found to notify.", HttpStatus.NOT_FOUND);
        }

        int successCount = 0;
        for (CustomerDetails customer : activeCustomers) {
            try {
                // Call the new email method
                emailConfig.sendAnnualChargeEmail(
                    customer.getCustomerEmailid(),
                    customer.getCustomerName(),
                    ANNUAL_CHARGE
                );
                successCount++;
            } catch (Exception e) {
                // Log failure for auditing purposes
                System.err.println("Failed to send annual charge email to " + customer.getCustomerEmailid() + ": " + e.getMessage());
            }
        }

        if (successCount > 0) {
            return new ResponseEntity<>(successCount + " annual charge emails successfully sent.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to send any annual charge emails.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
