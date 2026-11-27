package com.user.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="Customer_Transaction")
@Data
public class CustomerTransaction {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private Long custId;
	
	private String transaction; //debit or credit
	
	private Long amount;
	
	private String mode;  //online, cash, cheque, product
	
	private Long discount;
	
	private Long createdBy;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dateOfTransaction;
}
