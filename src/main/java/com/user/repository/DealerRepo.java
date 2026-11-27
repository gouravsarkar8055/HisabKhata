package com.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.entity.Dealer;

@Repository
public interface DealerRepo extends JpaRepository<Dealer, Long> {

	Optional<Dealer> findById(Long id);

	void deleteById(Long id);
	
	Dealer findByEmail(String email);
	
}