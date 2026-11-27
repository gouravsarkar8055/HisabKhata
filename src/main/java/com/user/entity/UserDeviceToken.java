package com.user.entity;

import java.util.Date;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_device_token")
@Data
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_token", nullable = false,
            length = 512, unique = true)
    private String deviceToken;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "is_active")
    private Boolean active = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    public void onCreate() {
        Date now = new Date();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = new Date();
    }

}