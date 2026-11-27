package com.user.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.user.enums.SalaryType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "staff")
@Data
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String role;

    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    private Long userId;

    private Long currentSalary; // for quick reference
    
    private Boolean isActive = true;
    private Boolean isDeleted = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}