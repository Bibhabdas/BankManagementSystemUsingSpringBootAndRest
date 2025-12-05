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

import java.sql.Date;
import java.time.LocalDateTime; // ⭐ New Import for timestamp

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Deleted_Customer_Details")
public class DeletedCustomerDetails {
	@Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id; // Unique ID for the deleted record history

	// --- Copied fields from CustomerDetails (Ensure types match) ---
    @Column(name = "Customer_id")
	private Integer customerid;
	@Column(name="Customer_Name")
	private String customerName;
	@Column(name="customer_Email_Id")
	private String customerEmailid;
	@Column(name="customer_Mobile_Number")
	private Long customerMobileNumber;
	@Column(name="customer_Aadhar_Number")
	private Long customerAadharNumber;
	@Column(name="customer_PAN_Number")
	private String PANnumber;
	@Column(name="customer_Gender")
	private String customerGender;
	@Column(name="customer_Address")
	private String customerAddress;
	@Column(name="customer_Status")
	private String customerStatus;
	@Column(name="customer_Date_of_Birth")
	private Date dateOfBirth;
	@Column(name="customer_Amount")
	private double customerAmount;
	@Column(name="customer_Account_Number")
	private Long customerAccountNumber;
	@Column(name="customer_PIN")
	private String customerPIN;
	@Column(name="customer_IFSC_Code")
	private String customerIFSCcode;
    
    // --- New field for the deletion timestamp ---
    @Column(name="Deletion_Timestamp", nullable = false)
    private LocalDateTime deletionTimestamp;
}
