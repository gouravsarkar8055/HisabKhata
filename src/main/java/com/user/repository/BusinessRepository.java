package com.user.repository;

<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.entity.BusinessEntity;

@Repository
public interface BusinessRepository extends JpaRepository<BusinessEntity, Long> {

    // Find by userId
<<<<<<< HEAD
    BusinessEntity findByUserId(Long userId);
=======
    List<BusinessEntity> findByUserId(Long userId);
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17

    // Find by email
    Optional<BusinessEntity> findByEmail(String email);

    // Delete by userId
    void deleteByUserId(Long userId);
}