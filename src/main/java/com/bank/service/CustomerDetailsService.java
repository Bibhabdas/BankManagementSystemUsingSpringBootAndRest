package com.bank.service;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bank.dao.CustomerDAO;
import com.bank.entity.CustomerDetails;

@Service("customerDetailsService")
public class CustomerDetailsService implements UserDetailsService {

    @Autowired
    private CustomerDAO customerDAO;
    
    // ⭐ REMOVED ADMIN/DBMANAGER CONSTANTS - This class now handles only customers

    @Override
    public UserDetails loadUserByUsername(String emailid) throws UsernameNotFoundException {        
        CustomerDetails customer = customerDAO.getCustomerDetailsByEmailid(emailid);

        if (customer == null) {
            // Throwing UsernameNotFoundException allows Spring Security to try the next provider (AdminDetailsService)
            throw new UsernameNotFoundException("Customer not found with email: " + emailid);
        }
        
        String rawPinAsString = customer.getCustomerPIN(); 

        // Deny login for non-Active accounts in the security layer
        if (!customer.getCustomerStatus().equalsIgnoreCase("Active")) {
            throw new UsernameNotFoundException("Account is pending activation or closure.");
        }

        // --- Assign roles: Only 'ROLE_USER' for regular customers ---
        Collection<SimpleGrantedAuthority> authorities = 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        // --- END Role Assignment ---

        // Return Spring Security's UserDetails object
        return new User(
            customer.getCustomerEmailid(),
            rawPinAsString, 
            authorities 
        );
    }
}