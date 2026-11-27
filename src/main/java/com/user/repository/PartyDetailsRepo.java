package com.user.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.user.entity.Customer;
import com.user.entity.PartyDetails;

@Repository
public interface PartyDetailsRepo extends JpaRepository<PartyDetails, Long> {

	Optional<PartyDetails> findById(Long id);
	
	Page<PartyDetails> findAllByCreatedBy(Long createdBy, Pageable pageable);
	
	PartyDetails findByEmail(String email);
	
	PartyDetails findByName(String name);

	void deleteById(Long id);
	
	List<PartyDetails> findAllByCreatedBy(Long userId);
	
	@Query("""
		    SELECT p
		    FROM PartyDetails p
		    WHERE p.createdBy = :userId
		    AND (:key IS NULL OR p.partyKey = :key)
		""")
		Page<PartyDetails> findParties(
		        @Param("userId") Long userId,
		        @Param("key") Character key,
		        Pageable pageable
		);
	
	@Query(value = "SELECT id, name FROM party_table WHERE created_by = :userId", nativeQuery = true)
	List<Map<String, Object>> getAllPartiesIdName(@Param("userId") Long userId);
	
	@Query(value = "SELECT * FROM party_table WHERE created_by = :userId AND id = :id", nativeQuery = true)
	PartyDetails getPartyById(@Param("userId") Long userId, @Param("id") Long id);
	
	@Query(value = "SELECT * FROM party_table WHERE created_by = :userId AND name = :name", nativeQuery = true)
	PartyDetails getPartyByName(@Param("userId") Long userId, @Param("name") String name);
	
	boolean existsByNameAndCreatedBy(String name, Long createdBy);

    PartyDetails findByNameAndCreatedBy(String name, Long createdBy);
    
    @Modifying
	@Transactional
	@Query(value = "UPDATE party_table SET balance = :balance, date_of_last_transaction = :dateOfLastTransaction WHERE id = :entityId", nativeQuery = true)
	void updateBalanceAndLastTransaction(@Param("entityId") Long entityId, 
	                                     @Param("balance") Long balance, 
	                                     @Param("dateOfLastTransaction") LocalDate lastTransaction);
    
    @Modifying
	@Transactional
	@Query(value = "UPDATE party_table SET balance = :balance WHERE id = :entityId", nativeQuery = true)
	void updateBalance(@Param("entityId") Long entityId, 
	                                     @Param("balance") Long balance);
    
    @Query(value = "SELECT balance FROM party_table WHERE id = :id", nativeQuery = true)
    Long findBalanceById(@Param("id") Long id);
    
    @Query(value = "SELECT date_of_last_transaction FROM party_table WHERE id = :id", nativeQuery = true)
    LocalDate findDateOfLastTransactionById(@Param("id") Long id);


}