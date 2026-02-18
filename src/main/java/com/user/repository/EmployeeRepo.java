package com.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.entity.Employee;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long>{

	Optional<Employee> findById(Long id);

	Employee findByPhone(Long phone);
	
	Employee findByEmail(String email);
	
	List<Employee> findAllById(Long userId);
	
	@Query(value = "SELECT * FROM Emp_Table emp WHERE emp.created_by = :id", nativeQuery = true)
	List<Employee> findByCreatedBy(@Param("id") Long id);

	void deleteById(Long id);

}
