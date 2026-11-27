package com.user.entity;

import java.time.LocalDate;

import com.user.enums.FinanceType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "finance_entry")
@Data
public class FinanceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private FinanceType type;

    private String category; // SALARY / CASH_IN / etc.

    private String description;

    private String paymentMode;

    private LocalDate date;

    private Long createdBy;
}