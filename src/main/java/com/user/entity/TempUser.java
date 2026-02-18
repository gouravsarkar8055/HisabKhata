package com.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name="TempUserTable")

@Data
public class TempUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String userName;
	
	private String password;
	
	private String email;
	
	private Long phone;
	
	private String address;
	
	private String shopName;
	
	private String otp;
	
	private LocalDateTime expiryDate; // Added field for OTP expiration
}
