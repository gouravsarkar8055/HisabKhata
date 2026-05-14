package com.user.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.entity.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
	
	List<Attendance> findByStaffId(Long staffId);

	List<Attendance> findByStaffIdAndDateBetween(Long staffId, LocalDate start, LocalDate end);

	boolean existsByStaffId(Long staffId);
	
	@Query(value = "SELECT * FROM attendance a WHERE a.staff_id = :staffId AND a.date = :date LIMIT 1", 
		       nativeQuery = true)
		Optional<Attendance> findByStaffAndDate(@Param("staffId") Long staffId,
		                                        @Param("date") LocalDate date);

}
