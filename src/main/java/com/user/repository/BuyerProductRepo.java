package com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.entity.BuyerProduct;

@Repository
public interface BuyerProductRepo extends JpaRepository<BuyerProduct, Long> {
	BuyerProduct findByTransactionId(Long transactionId);
}
