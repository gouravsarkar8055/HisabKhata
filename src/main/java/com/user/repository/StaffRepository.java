package com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.entity.Staff;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

}
