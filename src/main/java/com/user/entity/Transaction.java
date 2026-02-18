package com.user.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "TransactionTable")
@Data
public class Transaction{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long entityId;
    private String description;
    private Long amount;
    private Long discount;
    private String mode;
    private String transaction;
    private Long createdBy;
    private Long balance;
    private Long payment;
    private Long previousBalance;

    @JsonManagedReference
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionPayment> payments = new ArrayList<>();

    @Lob
    @Column(columnDefinition = "TEXT")
    private String productsJson;

    private Character entityKey;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfTransaction;


    // ---------- Products JSON Helpers ----------
    public List<Product> getProducts() {
        try {
            if (productsJson == null || productsJson.trim().isEmpty()) {
                return Collections.emptyList();
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(productsJson, new TypeReference<List<Product>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public void setProducts(List<Product> products) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.productsJson = mapper.writeValueAsString(products);
        } catch (Exception e) {
            this.productsJson = "[]";
        }
    }

    // -------- Payment Helpers (correct) ----------
    public void addPayment(TransactionPayment payment) {
        payments.add(payment);
        payment.setTransaction(this);
    }

    public void removePayment(TransactionPayment payment) {
        payments.remove(payment);
        payment.setTransaction(null);
    }
}
