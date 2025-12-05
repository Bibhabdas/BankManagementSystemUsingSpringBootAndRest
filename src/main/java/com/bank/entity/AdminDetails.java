package com.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Admin_Details")
public class AdminDetails {
	@Id @Column(name = "Admin_Id") @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int adminId;
	@Column(name = "Admin_Email_id",nullable = false, unique = true)
	private String adminEmailid;
	@Column(name="Admin_Password",nullable = false)
	private String adminPassword;
	@Column(name="Admin_Role",nullable = false)
	private String adminRole;
}
