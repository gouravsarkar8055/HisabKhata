package com.user.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.entity.StaffSalary;

@Repository
public interface StaffSalaryRepository extends JpaRepository<StaffSalary, Long> {

    @Query("""
        SELECT s FROM StaffSalary s
        WHERE s.staff.id = :staffId
        AND s.effectiveFrom <= :date
        AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)
    """)
    StaffSalary findActiveSalary(@Param("staffId") Long staffId,
                                 @Param("date") LocalDate date);

    StaffSalary findTopByStaffIdAndEffectiveToIsNull(Long staffId);

    @Query(value = """
    	    SELECT * FROM staff_salary s
    	    WHERE s.staff_id = :staffId
    	      AND s.effective_to IS NULL
    	    LIMIT 1
    	    """, nativeQuery = true)
    	StaffSalary findCurrentSalary(@Param("staffId") Long staffId);

	boolean existsByStaffId(Long staffId);
}