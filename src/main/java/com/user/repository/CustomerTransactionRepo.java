package com.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.entity.CustomerTransaction;

@Repository
public interface CustomerTransactionRepo extends JpaRepository<CustomerTransaction, Long> {
    
    @Query(value = "SELECT * FROM Customer_Transaction t WHERE t.created_by = :userId", nativeQuery = true)
    List<CustomerTransaction> findByCreatedBy(@Param("userId") Long userId);
    
    @Query(value = "SELECT * FROM Customer_Transaction t WHERE t.created_by = :userId AND t.cust_id = :custId", nativeQuery = true)
    List<CustomerTransaction> findTransactionsByEmpId(@Param("userId") Long userId, @Param("custId") Long custId);
}
