package com.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.entity.UserDeviceToken;

public interface UserDeviceTokenRepository
        extends JpaRepository<UserDeviceToken, Long> {

    List<UserDeviceToken> findByUserIdAndActiveTrue(Long userId);

    Optional<UserDeviceToken> findByDeviceToken(String deviceToken);

    Optional<UserDeviceToken> findByUserIdAndDeviceId(
        Long userId, String deviceId
    );
}