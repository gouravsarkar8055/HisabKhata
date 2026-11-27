package com.user.entity;

import java.time.LocalDate;

import com.user.enums.SalaryType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "staff_salary")
@Data
public class StaffSalary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    private Long salary;

    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo; // null = active
}