package com.bank.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bank.entity.CustomerDetails;
import com.bank.entity.DeletedCustomerDetails;
import com.bank.repository.CustomerRepository;
import com.bank.repository.DeletedCustomerRepository;

@Repository
public class CustomerDAO {

	
	@Autowired
	CustomerRepository customerRepository;
	
	@Autowired
    DeletedCustomerRepository deletedCustomerRepository;

	public CustomerDetails insertCustomerDetails(CustomerDetails customerDetails) {
		return customerRepository.save(customerDetails);
	}
	
	public CustomerDetails getCustomerDetails(String emailid,String pin){
		return customerRepository.findByCustomerEmailidAndCustomerPIN(emailid, pin);
	}
	
	public List<CustomerDetails> getAllCustomerDetails() {
		return customerRepository.findAll();
	}
	
	public List<CustomerDetails> getAllPendingCustomerDetails() {
		return customerRepository.findByCustomerStatus("Pending");
	}
	
	public CustomerDetails getCustomerByAccountNumber(long accountNumber) {
		return customerRepository.findByCustomerAccountNumber(accountNumber);
	}
	
	public CustomerDetails getCustomerDetailsByEmailid(String emailid){
		return customerRepository.findByCustomerEmailid(emailid);
	}
	
	public double getCustomerAmountByUsingEmailIdAndPin(String emailid, String pin) { 
        CustomerDetails details = customerRepository.findByCustomerEmailidAndCustomerPIN(emailid, pin);
        return (details != null) ? details.getCustomerAmount() : -1; 
	}
    
    public int updateCustomerAmount(String emailid, String pin, double newAmount) {
        return customerRepository.updateCustomerAmountByEmailAndPin(emailid, pin, newAmount);
    }
    
    public int updateCustomerPin(String emailid, String oldPin, String newPin) {
        return customerRepository.updateCustomerPin(emailid, oldPin, newPin);
    }
    
    public CustomerDetails getCustomerDetailsByMoblineNumber(long cMobileNumber, String pin) {
        return customerRepository.findByCustomerMobileNumberAndCustomerPIN(cMobileNumber, pin);
    }
    
    public CustomerDetails getReceiverDetailsByAccountAndIFSC(long rAccountNumber, String rIFSCcode) {
        return customerRepository.findByCustomerAccountNumberAndCustomerIFSCcode(rAccountNumber, rIFSCcode);
    }
    
    public int updateCustomerAmountByAccountNumber(long accountNumber, double newAmount) {
        return customerRepository.updateCustomerAmountByAccountNumber(accountNumber, newAmount);
    }
    
    @Transactional
    public String deleteSpecificCustomerByEmail(String emailid) {
        // Find the customer object using the existing lookup method
        CustomerDetails customer = customerRepository.findByCustomerEmailid(emailid);
        
        if (customer == null) {
            return "Customer not found with email: " + emailid;
        }

        // 1. Save to history (Trigger simulation)
        saveDeletedCustomer(customer);
        
        // 2. Delete from main table
        customerRepository.delete(customer); 

        return "Customer deleted successfully.";
    }
    
    public List<CustomerDetails> getAllDeactivatedAccount() {
        return customerRepository.findByCustomerStatus("Deactivated");
    }
    
 // Inside CustomerDAO.java

    public DeletedCustomerDetails saveDeletedCustomer(CustomerDetails details) {

        DeletedCustomerDetails deletedDetails = new DeletedCustomerDetails();
        
        // ⭐ COPY ALL REQUIRED FIELDS FROM CUSTOMERDETAILS TO DELETEDCUSTOMERDETAILS
        
        // Primary Identifiers
        deletedDetails.setCustomerid(details.getCustomerid());
        deletedDetails.setCustomerEmailid(details.getCustomerEmailid());
        // Assuming 'mailid' was a typo and you meant CustomerEmailid, 
        // but if you have a separate mailid field, you should include it here.

        // Personal Details
        deletedDetails.setCustomerName(details.getCustomerName());
        deletedDetails.setCustomerGender(details.getCustomerGender());
        deletedDetails.setDateOfBirth(details.getDateOfBirth());
        deletedDetails.setCustomerAddress(details.getCustomerAddress());
        
        // Financial/Security Details
        deletedDetails.setCustomerAmount(details.getCustomerAmount()); // Final balance
        deletedDetails.setCustomerAccountNumber(details.getCustomerAccountNumber());
        deletedDetails.setCustomerIFSCcode(details.getCustomerIFSCcode());
        deletedDetails.setCustomerPIN(details.getCustomerPIN());
        deletedDetails.setPANnumber(details.getPANnumber());
        deletedDetails.setCustomerAadharNumber(details.getCustomerAadharNumber());
        deletedDetails.setCustomerMobileNumber(details.getCustomerMobileNumber());
        
        // Status and Timestamp
        deletedDetails.setCustomerStatus(details.getCustomerStatus()); // Should be "Pending Closure" or similar
        deletedDetails.setDeletionTimestamp(LocalDateTime.now());
        
        return deletedCustomerRepository.save(deletedDetails);
    }
    
    @Transactional
    public void deleteCustomer(CustomerDetails customer) {
        customerRepository.delete(customer);
    }
    
    public List<CustomerDetails> getAllActiveCustomers() {
        return customerRepository.findByCustomerStatus("Active");
    }
    
}
