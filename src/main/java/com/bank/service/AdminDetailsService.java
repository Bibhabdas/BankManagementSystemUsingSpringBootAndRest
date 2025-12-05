package com.bank.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bank.dao.AdminDAO;
import com.bank.entity.AdminDetails;

@Service("adminDetailsService")
public class AdminDetailsService implements UserDetailsService {

    @Autowired
    private AdminDAO adminDAO;
    
    // Constants for defining Admin roles
    private static final String DBA_EMAIL = "dba@bank.com"; 
    private static final String DBMANAGER_EMAIL = "dbmanager@bank.com"; 

    /**
     * Locates the admin user based on the emailid (username).
     * @param emailid The emailid of the admin user.
     * @return A fully populated UserDetails object (Spring Security User).
     * @throws UsernameNotFoundException if the user could not be found.
     */
    @Override
    public UserDetails loadUserByUsername(String emailid) throws UsernameNotFoundException {
        // 1. Fetch Admin details from the database
        AdminDetails admin = adminDAO.getAdminDetailsByEmailid(emailid);

        if (admin == null) {
            // Spring Security will try the next provider if this one throws NotFoundException
            throw new UsernameNotFoundException("Admin not found with email: " + emailid);
        }
        
        // 2. Determine and assign roles based on the email (or a role field in AdminDetails if available)
        Collection<SimpleGrantedAuthority> authorities;
        
        if (DBA_EMAIL.equalsIgnoreCase(emailid)) {
            // DBA (Highest permissions - for the specified admin email)
            authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
            );
        } else if (DBMANAGER_EMAIL.equalsIgnoreCase(emailid)) {
            // DB Manager 
            authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_DBMANAGER")
            );
        } else {
            // Fallback for any other email found in AdminDetails table
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // 3. Return Spring Security's UserDetails object
        // NOTE: The AdminDetails entity must have an 'adminPassword' field matching the user's input (e.g., "1234").
        return new User(
            admin.getAdminEmailid(),
            admin.getAdminPassword(), 
            authorities
        );
    }
}