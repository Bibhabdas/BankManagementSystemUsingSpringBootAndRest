package com.bank.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping; // ⭐ Added Import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.CustomerDetailsDTO;
import com.bank.service.AdminService;
import com.bank.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
public class AdminController {
	
	@Autowired
	AdminService adminService;
	
	// PUBLIC: Get all customers
	@GetMapping("/AllCustomerDetails")
	public ResponseEntity<List<CustomerDetailsDTO>> getAllCustomerDetails() {
		return adminService.getAllCustomerDetails();
	}
	
	// PUBLIC: Get all pending account openings
	@GetMapping("/PendingCustomerDetails")
	public ResponseEntity<List<CustomerDetailsDTO>> getAllPendingDetails() {
		return adminService.getAllPendingCustomerDetails();
	}
	
	// PUBLIC: Accept/Activate a pending account (requires emailid)
	@PutMapping("/acceptCustomerOpeningRequest")
	public ResponseEntity<String> acceptAllPendingDetails(@RequestParam("customeremailid") String emailid) {
		return adminService.updateCustomerDetailsByEmail(emailid);
	}
	
	@GetMapping("/DeactivatedCustomerDetails")
	public ResponseEntity<List<CustomerDetailsDTO>> getDeactivatedDetails() {
	    return adminService.getAllDeactivatedCustomers();
	}
	
	@DeleteMapping("/deleteCustomer") // Use DELETE method for removal
	public ResponseEntity<String> deleteSpecificCustomer(@RequestParam("emailid") String emailid) {
	    // The Admin must be authenticated with ROLE_ADMIN or ROLE_DBMANAGER
	    return adminService.deleteSpecificCustomerByEmail(emailid);
	}
	
	
	@PostMapping("/sendAnnualChargeMail")
    public ResponseEntity<String> sendAnnualChargeMail() {
        // This endpoint will be secured by SecurityConfig, requiring ROLE_ADMIN/DBMANAGER
        return adminService.sendAnnualChargeNotification();
    }
}