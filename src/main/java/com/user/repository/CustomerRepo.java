package com.user.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.user.entity.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Long>{

	Optional<Customer> findById(Long id);
	
	Customer findByEmail(String email);

	void deleteById(Long id);

	List<Customer> findByCreatedBy(Long id);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE cust_table SET balance_amount = :balanceAmount WHERE created_by = :userId AND id = :entityId", nativeQuery = true)
	void updateBalanceAmount(@Param("userId") Long userId, @Param("entityId") Long entityId, @Param("balanceAmount") Long balanceAmount);

	@Modifying
	@Transactional
	@Query(value = "UPDATE cust_table SET balance_amount = :balanceAmount, date_of_last_transaction = :dateOfLastTransaction WHERE created_by = :userId AND id = :entityId", nativeQuery = true)
	void updateBalanceAmountAndLastTransaction(@Param("userId") Long userId, @Param("entityId") Long entityId, 
	                                           @Param("balanceAmount") Long balanceAmount, @Param("dateOfLastTransaction") LocalDate lastTransaction);
	
	@Query(value = "SELECT * FROM cust_table WHERE created_by = :userId AND name = :name", nativeQuery = true)
	List<Customer> findByCustomerName(@Param("userId") Long userId,@Param("name") String name);


}