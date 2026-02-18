package com.user.entity;

import lombok.Data;

@Data
public class Product {

	private String prodName;
	
	private Double prodQty;
	
	private Double prodPrice;
	
	private Double discount;
	
	private Long amount;
	
	private Long dlrId;
	
}
