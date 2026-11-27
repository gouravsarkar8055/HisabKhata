package com.user.entity;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Seller_Product")
@Data
public class SellerProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long custId;
    
    @Lob
    @Column(columnDefinition = "TEXT") // Store as JSON in DB
    private String productsJson; // Store products as JSON

    private Long transactionId;
    
    private Long createdBy;
    
    private Long amount;
    
    private Long discount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfSell;

    // Helper method to convert JSON string to List<Product>
    public List<Product> getProducts() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(this.productsJson, new TypeReference<List<Product>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing products JSON", e);
        }
    }

    // Helper method to set products list as JSON
    public void setProducts(List<Product> products) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.productsJson = objectMapper.writeValueAsString(products);
        } catch (Exception e) {
            throw new RuntimeException("Error converting products to JSON", e);
        }
    }
}
