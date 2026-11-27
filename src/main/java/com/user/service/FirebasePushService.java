package com.user.service;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.*;

@Service
public class FirebasePushService {

    public void sendPush(
            String token,
            String title,
            String body,
            Long notificationId,
            String notificationType,
            Long referenceId) {

        try {

            Message message = Message.builder()

                .setToken(token)

                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )

                .putData("notificationId",
                    String.valueOf(notificationId))

                .putData("type", notificationType)

                .putData("referenceId",
                    String.valueOf(referenceId))

                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(
                            AndroidNotification.builder()
                                .setChannelId("HisabKhata Notifications")
                                .setSound("default")
                                .build()
                        )
                        .build()
                )

                .setApnsConfig(
                    ApnsConfig.builder()
                        .setAps(
                            Aps.builder()
                                .setSound("default")
                                .build()
                        )
                        .build()
                )

                .build();

            String response =
                FirebaseMessaging.getInstance().send(message);

            System.out.println(
                "Firebase notification sent: " + response
            );

        } catch (FirebaseMessagingException e) {

            System.err.println(
                "Firebase notification failed: " + e.getMessage()
            );
        }
    }
}