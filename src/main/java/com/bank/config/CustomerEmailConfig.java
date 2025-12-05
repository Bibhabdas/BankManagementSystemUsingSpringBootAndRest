package com.bank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class CustomerEmailConfig {

	@Value("${spring.mail.username}")
	private String from;
	@Autowired
	private JavaMailSender javaMailSender;

	public void emailForCustomerRegistration(String to, String name, String otp) {
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
		simpleMailMessage.setFrom(from);
		simpleMailMessage.setTo(to);
		simpleMailMessage.setSubject("OTP For registration");
		simpleMailMessage
				.setText("Hi " + name + " we got a registration request from your side, To confirm registration"
						+ " please verify with otp: " + otp);
		/*
		 * send() is used the mail from the java based application It is non static
		 * method present in JavaMailSender interface For above properties we need to
		 * add JavaMailSender dependency
		 */
		javaMailSender.send(simpleMailMessage);
	}

	public void sendAccountActivationEmail(String toEmail, String name, long accountNumber, String pin, String ifscCode)
			throws MailException {

		String subject = "Your Account is Now Activated!";

		// ⭐ Body formatting done directly within the method
		String body = String.format("Dear %s,\n\n"
				+ "Congratulations! Your bank account has been successfully activated.\n\n" + "Account Details:\n"
				+ "  Account Number: %d\n" + "  Your Temporary PIN: %s\n" + "  IFSC Code: %s\n\n"
				+ "You can now log in and begin making transactions. **Please change your PIN immediately after your first login.**\n\n"
				+ "Thank you for choosing our service.", name, accountNumber, pin, ifscCode);

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(toEmail);
		message.setSubject(subject); // Set subject directly
		message.setText(body); // Set formatted body directly

		javaMailSender.send(message);
	}

	public void sendPinUpdateNotification(String toEmail, String name) throws MailException {
		String subject = "Security Alert: Your PIN Has Been Changed";
		String body = String.format("Dear %s,\n\n" + "Your account PIN was successfully updated just now.\n\n"
				+ "If you did not make this change, please contact us immediately:\n\n" + "  Phone Number: 9876543210\n"
				+ "  Email ID: Bank123@gmail.com\n\n" + "Thank you for choosing our service.", name);

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(toEmail);
		message.setSubject(subject);
		message.setText(body);

		javaMailSender.send(message);
	}
	
	public void sendAnnualChargeEmail(String toEmail, String name, double chargeAmount) throws MailException {
        String subject = "Annual Account Maintenance Charge Notification";
        String body = String.format(
            "Dear %s,\n\n" +
            "This is a notification regarding the annual account maintenance fee.\n" +
            "A charge of INR %.2f has been applied to your account for maintaining services over the last year.\n\n" +
            "Please ensure your account has sufficient funds. Thank you for your continued business.\n\n" +
            "Sincerely,\n" +
            "The Bank Management Team",
            name, chargeAmount
        );
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        
        javaMailSender.send(message);
    }
}
