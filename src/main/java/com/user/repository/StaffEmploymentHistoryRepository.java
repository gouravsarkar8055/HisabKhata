package com.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.entity.StaffEmploymentHistory;

@Repository
public interface StaffEmploymentHistoryRepository extends JpaRepository<StaffEmploymentHistory, Long>{

	@Query("SELECT h FROM StaffEmploymentHistory h WHERE h.staff.id = :staffId AND h.isActive = true")
	Optional<StaffEmploymentHistory> findActiveByStaffId(@Param("staffId") Long staffId);
}
