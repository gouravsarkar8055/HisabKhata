package com.user.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.dto.DashboardSummaryDTO;
import com.user.entity.Transaction;

import jakarta.transaction.Transactional;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {

	List<Transaction> findAllByCreatedBy(Long userId);
	
	Page<Transaction> findAllByCreatedBy(Long createdBy, Pageable pageable);
	
	@Query("SELECT COUNT(t) FROM Transaction t WHERE t.entityId = :entityId")
	int countByEntityId(@Param("entityId") Long entityId);
	
	@Query("SELECT id FROM Transaction t WHERE t.entityId = :entityId AND t.transaction = :transaction")
	Long getTransactionIdByEntityIdAndTransaction(@Param("entityId") Long entityId, @Param("transaction") String transaction);
	
	Page<Transaction> findByCreatedByAndDescriptionAndDateOfTransactionBetween(
            Long createdBy,
            String description,
            LocalDate start,
            LocalDate end,
            Pageable pageable);

    Page<Transaction> findByCreatedByAndDescriptionAndEntityIdAndDateOfTransactionBetween(
            Long createdBy,
            String description,
            Long entityId,
            LocalDate start,
            LocalDate end,
            Pageable pageable);
	
	@Query(value = "SELECT * FROM Transaction_Table WHERE id = :transactionId", nativeQuery = true)
	Transaction findByTransactionId(@Param("transactionId") Long transactionId);
	
	@Transactional
	@Modifying
	@Query(value = "DELETE FROM Transaction_Table WHERE id = :transactionId AND created_by = :userId", nativeQuery = true)
	void deleteByIdAndCreatedBy(@Param("transactionId") Long transactionId, @Param("userId") Long userId);
	
	@Query(value = "SELECT * FROM Transaction_Table t WHERE t.created_by = :userId AND t.entity_id = :entityId "
			+ "AND t.entity_key = :entityKey ORDER BY t.date_of_transaction DESC, t.id DESC", nativeQuery = true)
	List<Transaction> findByEntityKeyAndId(@Param("entityId") Long entityId,
			@Param("entityKey") Character entityKey, @Param("userId") Long userId);
	
	@Query(value = "SELECT * FROM Transaction_Table t WHERE t.created_by = :userId "
			+ "AND t.entity_key = :entityKey ORDER BY t.date_of_transaction, t.id", nativeQuery = true)
	List<Transaction> findByEntityKey(@Param("entityKey") Character entityKey, @Param("userId") Long userId);
	
	@Query("SELECT SUM(t.amount) FROM Transaction t "
		     + "WHERE t.createdBy = :createdBy "
		     + "AND t.description = :description "
		     + "AND t.dateOfTransaction BETWEEN :startDate AND :endDate")
		Long getTotalAmountAll(
		        @Param("createdBy") Long createdBy,
		        @Param("description") String description,
		        @Param("startDate") LocalDate startDate,
		        @Param("endDate") LocalDate endDate
		);

		@Query("SELECT SUM(t.amount) FROM Transaction t " +
		       "WHERE t.createdBy = :createdBy AND t.description = :description " +
		       "AND t.entityId = :entityId AND t.dateOfTransaction BETWEEN :start AND :end")
		Long getTotalAmountParty(
				@Param("createdBy") Long createdBy,
		        @Param("description") String description,
		        @Param("entityId") Long entityId,
		        @Param("start") LocalDate startDate,
		        @Param("end") LocalDate endDate
		);

	
	@Procedure(procedureName = "sale_report")
	List<Transaction> getSaleReport(
	        @Param("p_created_by") Long userId,
	        @Param("p_entity_id") Long entityId,
	        @Param("p_description") String description,
	        @Param("p_filter") String filter,
	        @Param("p_start_date") LocalDate startDate,
	        @Param("p_end_date") LocalDate endDate,
	        @Param("p_offset") Integer offset,
	        @Param("p_limit") Integer limit
	);
	
	@Query("SELECT SUM(t.amount), COUNT(t) FROM Transaction t " +
		       "WHERE t.dateOfTransaction BETWEEN :startDate AND :endDate " +
		       "AND t.createdBy = :createdBy " +
		       "AND t.description = :description " +
		       "AND (:filter = 'all' OR (:filter = 'party' AND t.entityId = :entityId))")
		List<Object[]> getSaleStats(@Param("startDate") LocalDate startDate,
		                             @Param("endDate") LocalDate endDate,
		                             @Param("createdBy") Long userId,
		                             @Param("description") String description,
		                             @Param("entityId") Long entityId,
		                             @Param("filter") String filter);
	
	@Procedure(procedureName = "UpdateTransactionBalances")
    void updateTransactionBalances(
    	@Param("p_id") Long id, 
        @Param("p_created_by") Long createdBy, 
        @Param("p_entity_key") Character entityKey, 
        @Param("p_entity_id") Long entityId, 
        @Param("p_transaction_date") LocalDate transactionDate, 
        @Param("p_initial_previous_balance") Long initialPreviousBalance
    );
	
	@Query("SELECT t FROM Transaction t WHERE t.entityKey = :entityKey AND t.entityId = :entityId AND " +
		       "(t.dateOfTransaction < :date OR (t.dateOfTransaction = :date AND t.id < :id)) " +
		       "ORDER BY t.dateOfTransaction DESC, t.id DESC")
		List<Transaction> findLastTransactionBefore(@Param("entityKey") Character entityKey, @Param("entityId") Long entityId, @Param("date") LocalDate date, @Param("id") Long id);

	@Query("SELECT t FROM Transaction t WHERE t.entityKey = :entityKey AND t.entityId = :entityId AND " +
		       "(t.dateOfTransaction > :date OR (t.dateOfTransaction = :date AND t.id > :id)) " +
		       "ORDER BY t.dateOfTransaction, t.id")
		List<Transaction> findAllAfter(@Param("entityKey") Character entityKey, @Param("entityId") Long entityId, @Param("date") LocalDate date, @Param("id") Long id);

	@Query("SELECT MAX(t.dateOfTransaction) FROM Transaction t WHERE t.entityKey = :entityKey AND t.entityId = :entityId")
	LocalDate getLastTransactionDate(@Param("entityKey") Character entityKey, @Param("entityId") Long entityId);
	
	@Query("SELECT t FROM Transaction t WHERE t.entityKey = :entityKey AND t.entityId = :entityId AND " +
		       "(t.dateOfTransaction > :existingDate OR (t.dateOfTransaction = :existingDate AND t.id > :id)) AND " +
		       "(t.dateOfTransaction < :updatedDate OR (t.dateOfTransaction = :updatedDate AND t.id < :id))" +
		       "ORDER BY t.dateOfTransaction, t.id")
		List<Transaction> findAllBeforeUpdatedDate(@Param("entityKey") Character entityKey, @Param("entityId") Long entityId, @Param("existingDate") LocalDate existingDate, @Param("updatedDate") LocalDate updatedDate, @Param("id") Long id);
	
	@Query("""
			SELECT new com.user.dto.DashboardSummaryDTO(
			    COALESCE(SUM(CASE WHEN t.transaction = 'SALE' THEN t.amount ELSE 0 END),0),
			    COALESCE(SUM(CASE WHEN t.transaction = 'PURCHASE' THEN t.amount ELSE 0 END),0),
			    COALESCE(SUM(CASE WHEN t.transaction = 'PAYMENT-IN' THEN t.amount ELSE 0 END),0),
			    COALESCE(SUM(CASE WHEN t.transaction = 'PAYMENT-OUT' THEN t.amount ELSE 0 END),0),
			    COALESCE(SUM(CASE WHEN t.transaction = 'EXPENSE' THEN t.amount ELSE 0 END),0),

			    COALESCE(SUM(CASE WHEN t.transaction = 'SALE' THEN t.amount ELSE 0 END),0) -
			    COALESCE(SUM(CASE WHEN t.transaction = 'PURCHASE' THEN t.amount ELSE 0 END),0) -
			    COALESCE(SUM(CASE WHEN t.transaction = 'EXPENSE' THEN t.amount ELSE 0 END),0),

			    COALESCE(SUM(CASE WHEN t.transaction = 'PAYMENT-IN' THEN t.amount ELSE 0 END),0) -
			    COALESCE(SUM(CASE WHEN t.transaction = 'PAYMENT-OUT' THEN t.amount ELSE 0 END),0)
			)
			FROM Transaction t
			WHERE t.createdBy=:userId AND DATE(t.dateOfTransaction) = CURRENT_DATE
			""")
			DashboardSummaryDTO getTodayDashboardSummary(@Param("userId") Long userId);

}
