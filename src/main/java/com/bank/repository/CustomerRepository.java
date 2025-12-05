package com.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bank.entity.CustomerDetails;

public interface CustomerRepository extends JpaRepository<CustomerDetails, Integer>{
	CustomerDetails findByCustomerEmailidAndCustomerPIN(String customerEmailid, String customerPIN);
	
	List<CustomerDetails> findByCustomerStatus(String status);
	
	CustomerDetails findByCustomerAccountNumber(long accountNumber);
    
    CustomerDetails findByCustomerEmailid(String customerEmailid);
    
    @Modifying
    @Transactional // Required for update/delete operations
    @Query("UPDATE CustomerDetails c SET c.customerAmount = :newAmount WHERE c.customerEmailid = :emailid AND c.customerPIN = :pin")
    int updateCustomerAmountByEmailAndPin(@Param("emailid") String emailid, @Param("pin") String pin, @Param("newAmount") double newAmount);
    
    @Modifying
    @Transactional
    @Query("UPDATE CustomerDetails c SET c.customerPIN = :newPin WHERE c.customerEmailid = :emailid AND c.customerPIN = :oldPin")
    int updateCustomerPin(@Param("emailid") String emailid, @Param("oldPin") String oldPin, @Param("newPin") String newPin);
    
    CustomerDetails findByCustomerMobileNumberAndCustomerPIN(long customerMobileNumber, String customerPIN);
    
    CustomerDetails findByCustomerAccountNumberAndCustomerIFSCcode(long customerAccountNumber, String customerIFSCcode);
    
    @Modifying
    @Transactional
    @Query("UPDATE CustomerDetails c SET c.customerAmount = :newAmount WHERE c.customerAccountNumber = :accountNumber")
    int updateCustomerAmountByAccountNumber(@Param("accountNumber") long accountNumber, @Param("newAmount") double newAmount);
}
