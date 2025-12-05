package com.bank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // ⭐ NEW IMPORT
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // Re-added for standard path matching

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    @Qualifier("customerDetailsService")
    private UserDetailsService customerDetailsService;

    @Autowired
    @Qualifier("adminDetailsService") 
    private UserDetailsService adminDetailsService; 

    // 1. Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
    
    // ----------------------------------------------------
    // 2. Authentication Providers and Manager
    // ----------------------------------------------------
    
    @Bean
    public DaoAuthenticationProvider customerDaoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customerDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public DaoAuthenticationProvider adminDaoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(adminDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(
            adminDaoAuthenticationProvider(),       
            customerDaoAuthenticationProvider()     
        );
    }
    
    // ----------------------------------------------------
    // 3. Web Security Customizer (CRITICAL FIX)
    // ----------------------------------------------------

    // ⭐ FIX: Completely bypasses security filters for OTP verification.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // We must use AntPathRequestMatcher here if we want to ignore the POST method specifically.
        return (web) -> web.ignoring()
                           .requestMatchers(AntPathRequestMatcher.antMatcher("/verifyRegistration"));
    }


    // ----------------------------------------------------
    // 4. Security Filter Chain (Authorization Rules)
    // ----------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            
            .authorizeHttpRequests((requests) -> requests
                
                // ⭐ ADMIN/DBMANAGER ENDPOINTS (Same as before)
                .requestMatchers(
                    "/PendingCustomerDetails",
                    "/acceptCustomerOpeningRequest",
                    "/sendAnnualChargeMail"
                ).hasRole("ADMIN") 
                
                .requestMatchers(
                    "/DeactivatedCustomerDetails",
                    "/deleteCustomer",
                    "/processClosingAccountRequests",
                    "/addNewFDScheme", 
                    "/addNewInsurence"
                ).hasAnyRole("ADMIN", "DBMANAGER") 

                // PUBLIC ENDPOINTS (Only those remaining that don't bypass the filter chain)
                .requestMatchers(
                    "/customerRegisterPage",
                    "/customerLoginPage" ,
                    "/verifyRegistration"
                    // ⭐ NOTE: "/verifyRegistration" is REMOVED from here, as it's ignored above.
                ).permitAll()
                
                // CUSTOMER ENDPOINTS
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {}); 

        return http.build();
    }
}