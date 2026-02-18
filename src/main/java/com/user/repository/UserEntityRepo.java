package com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.entity.UserEntity;

@Repository
public interface UserEntityRepo extends JpaRepository<UserEntity,Long>{

	UserEntity findByEmail(String email);
	
}
