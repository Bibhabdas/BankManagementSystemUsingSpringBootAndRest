package com.bank.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.bank.entity.AdminDetails;
import com.bank.repository.AdminRepository;

@Repository
public class AdminDAO {

    @Autowired
    private AdminRepository adminRepository;

    public AdminDetails getAdminDetailsByEmailid(String emailid) {
        return adminRepository.findByAdminEmailid(emailid);
    }
}