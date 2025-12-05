package com.bank.entity;

import java.sql.Date;

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
@Table(name = "Customer_Details")
public class CustomerDetails {
	@Id 
	@Column(name = "Customer_id") 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer customerid;
	@Column(name="Customer_Name",nullable = false)
	private String customerName;
	@Column(name="customer_Email_Id",unique = true,nullable = false)
	private String customerEmailid;
	@Column(name="customer_Mobile_Number",unique = true,nullable = false)
	private long customerMobileNumber;
	@Column(name="customer_Aadhar_Number",unique = true,nullable = false)
	private long customerAadharNumber;
	@Column(name="customer_PAN_Number",unique = true,nullable = false)
	private String PANnumber;
	@Column(name="customer_Gender",nullable = false)
	private String customerGender;
	@Column(name="customer_Address",nullable = false)
	private String customerAddress;
	@Column(name="customer_Status",nullable = false)
	private String customerStatus;
	@Column(name="customer_Date_of_Birth",nullable = false)
	private Date dateOfBirth;
	@Column(name="customer_Amount",nullable = false)
	private double customerAmount;
	@Column(name="customer_Account_Number",nullable = false)
	private long customerAccountNumber;
	@Column(name="customer_PIN",nullable = true)
	private String customerPIN;
	@Column(name="customer_IFSC_Code")
	private String customerIFSCcode;
}
