package com.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.entity.DealerTransaction;

@Repository
public interface DlrTransactionRepo extends JpaRepository<DealerTransaction, Integer> {
	@Query("SELECT t FROM DealerTransaction t WHERE t.dlrId = :id")
    List<DealerTransaction> findTransactionsByDlrId(@Param("id") Long id);
}
