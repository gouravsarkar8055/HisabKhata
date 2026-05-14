package com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.entity.UserEntity;

import jakarta.transaction.Transactional;

@Repository
public interface UserEntityRepo extends JpaRepository<UserEntity,Long>{

	UserEntity findByEmail(String email);
	
	@Transactional
	@Modifying
	@Query("UPDATE UserEntity u SET u.shopName = :name WHERE u.id = :id")
	void updateShopName(@Param("id") Long id, @Param("name") String name);
	
}
