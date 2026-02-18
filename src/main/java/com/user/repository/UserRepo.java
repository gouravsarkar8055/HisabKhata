package com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.entity.TempUser;
import com.user.entity.UserEntity;

@Repository
public interface UserRepo extends JpaRepository<TempUser,Integer>{

	TempUser findByEmail(String email);

	void save(UserEntity userEntity);
}


