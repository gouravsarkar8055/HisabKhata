package com.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.entity.IdempotencyEntity;

public interface IdempotencyRepository
extends JpaRepository<IdempotencyEntity, Long> {

Optional<IdempotencyEntity>
findByIdempotencyKey(String key);
}