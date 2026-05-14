package com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.entity.FinanceEntry;

@Repository
public interface FinanceEntryRepository extends JpaRepository<FinanceEntry, Long> {

}
