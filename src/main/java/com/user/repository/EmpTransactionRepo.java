package com.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.entity.EmpTransaction;

@Repository
public interface EmpTransactionRepo extends JpaRepository<EmpTransaction, Integer> {
	@Query(value = "SELECT * FROM Emp_Transaction t WHERE t.created_by = :id", nativeQuery = true)
    List<EmpTransaction> findTransactionsByCreatedBy(@Param("id") Long id);
	
	@Query(value = "SELECT * FROM Emp_Transaction t WHERE t.created_by = :userId AND t.emp_id = :id", nativeQuery = true)
    List<EmpTransaction> findTransactionsByEmpId(@Param("userId") Long userId, @Param("id") Long id);
}
