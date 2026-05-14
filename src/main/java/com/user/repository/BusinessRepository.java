package com.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.entity.BusinessEntity;

@Repository
public interface BusinessRepository extends JpaRepository<BusinessEntity, Long> {

    // Find by userId
    List<BusinessEntity> findByUserId(Long userId);

    // Find by email
    Optional<BusinessEntity> findByEmail(String email);

    // Delete by userId
    void deleteByUserId(Long userId);
}