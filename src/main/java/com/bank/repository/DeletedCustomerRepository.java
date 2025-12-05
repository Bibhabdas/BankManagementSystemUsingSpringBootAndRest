package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.entity.DeletedCustomerDetails;

public interface DeletedCustomerRepository extends JpaRepository<DeletedCustomerDetails, Integer>{
    // Repository for the deleted customer history
}
