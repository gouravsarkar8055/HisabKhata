package com.user.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.user.entity.UserDeviceToken;
import com.user.repository.UserDeviceTokenRepository;

@Service
public class NotificationPushService {

    private final UserDeviceTokenRepository repository;
    private final FirebasePushService firebasePushService;

    public NotificationPushService(
            UserDeviceTokenRepository repository,
            FirebasePushService firebasePushService) {

        this.repository = repository;
        this.firebasePushService = firebasePushService;
    }

    public void sendToUser(
            Long userId,
            Long notificationId,
            String title,
            String body,
            String type,
            Long referenceId) {

        List<UserDeviceToken> devices =
            repository.findByUserIdAndActiveTrue(userId);

        for (UserDeviceToken device : devices) {

            firebasePushService.sendPush(
                device.getDeviceToken(),
                title,
                body,
                notificationId,
                type,
                referenceId
            );
        }
    }
}