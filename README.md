# 🏦 Bank Management System

This is a comprehensive backend system for managing bank accounts, transactions, user authentication, and administrative tasks, built using **Spring Boot 3** and secured with **Spring Security**.

## ✨ Key Features

This project implements a robust set of features across Customer and Admin roles:

### 1. Security & Authentication
* **Dual Authentication:** Separate login providers for **Customers** (using PIN) and **Administrators** (using dedicated passwords).
* **Role-Based Authorization:** Access control using `ROLE_ADMIN` (DBA) and `ROLE_DBMANAGER` to restrict administrative endpoints (e.g., approvals, deletions).
* **Insecure PIN Handling:** Uses `NoOpPasswordEncoder` for compatibility with raw PINs stored in the database (NOTE: This is for testing/demo only; never use in production).

### 2. Customer Workflow
* **Two-Step Registration:** Account creation requires email verification via **OTP**.
* **Core Transactions:** Securely perform deposits and withdrawals based on authenticated user identity.
* **Account Management:** Check balance, change PIN number (with email notification).
* **Fund Transfer:** Perform mobile transactions, checking for **internal (same bank)** vs. **external (different bank)** transfers.
* **Account Closure:** Customer-initiated request process to flag accounts for admin review.

### 3. Administrative Workflow
* **Account Activation:** Admin approves pending registrations, automatically generating a unique **Account Number**, 6-digit **PIN**, and **IFSC Code**.
* **Email Notifications:** Sends activation confirmation emails to new customers.
* **Bulk Management:** View lists of customers whose status is "Pending" or "Deactivated" (ready for closure).
* **Deletion & Auditing:**
    * **Specific Deletion:** Admin can delete any single customer by email ID.
    * **Batch Deletion:** Admin can process and delete all accounts marked "Pending Closure."
    * **Audit Trail (Simulated Trigger):** Upon deletion, the system saves the full customer record to a `DeletedCustomerDetails` history table for compliance.
* **Bulk Communication:** Admin can trigger mass email notifications to all active customers (e.g., annual fee notifications).

## 🚀 Technologies Used

| Technology | Purpose |
| :--- | :--- |
| **Spring Boot 3.x** | Core framework for rapid application development. |
| **Spring Security 6.x** | Handles all authentication, authorization, and session management. |
| **Spring Data JPA & Hibernate** | ORM for database persistence and modeling. |
| **MySQL** | Relational database used for storing data. |
| **JavaMailSender** | Used for sending OTP and transaction/activation notifications. |
| **ModelMapper** | Used for mapping data between Entity and DTO layers. |
| **Lombok** | Boilerplate code reduction (`@Getter`, `@Setter`, etc.). |

## ⚙️ How to Run Locally

### Prerequisites

* Java 17 or newer (Current version used is **Java 21**).
* MySQL Server.
* Maven.

### 1. Database Setup

1.  Create a new schema in your MySQL server (e.g., `bank_db`).
2.  Update your `application.properties` file with your database credentials:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/bank_db
    spring.datasource.username=your_db_user
    spring.datasource.password=your_db_password
    spring.jpa.hibernate.ddl-auto=update
    ```

### 2. Email Configuration

Configure the JavaMailSender settings in `application.properties` (using Gmail as an example, requiring an App Password):

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_sending_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true