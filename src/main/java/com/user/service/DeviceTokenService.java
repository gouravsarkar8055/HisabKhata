package com.user.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user.entity.UserDeviceToken;
import com.user.repository.UserDeviceTokenRepository;

@Service
public class DeviceTokenService {

    private final UserDeviceTokenRepository repository;

    public DeviceTokenService(
            UserDeviceTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registerDevice(
            Long userId,
            String token,
            String deviceType,
            String deviceId) {

        Optional<UserDeviceToken> existing =
            repository.findByDeviceToken(token);

        UserDeviceToken device;

        if (existing.isPresent()) {
            device = existing.get();
        } else {
            device = new UserDeviceToken();
        }

        device.setUserId(userId);
        device.setDeviceToken(token);
        device.setDeviceType(deviceType);
        device.setDeviceId(deviceId);
        device.setActive(true);

        repository.save(device);
    }

    @Transactional
    public void unregisterDevice(Long userId, String token) {

        repository.findByDeviceToken(token)
            .filter(device -> userId.equals(device.getUserId()))
            .ifPresent(device -> {
                device.setActive(false);
                repository.save(device);
            });
    }
}