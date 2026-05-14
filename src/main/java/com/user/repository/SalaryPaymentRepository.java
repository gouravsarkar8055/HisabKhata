package com.user.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.entity.SalaryPayment;

@Repository
public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, Long> {

	@Query(value = """
		    SELECT COUNT(*)
		    FROM salary_payment sp
		    WHERE sp.staff_id = :staffId
		      AND sp.payment_date BETWEEN :startDate AND :endDate
		    """, nativeQuery = true)
		Long countSalaryPaid(@Param("staffId") Long staffId,
		                     @Param("startDate") LocalDate startDate,
		                     @Param("endDate") LocalDate endDate);

	boolean existsByStaffId(Long staffId);

}
