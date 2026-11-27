package com.user.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.user.dto.DeviceTokenRequest;
import com.user.jwtauthentication.JwtAuthenticationFilter;
import com.user.service.DeviceTokenService;

@RestController
@RequestMapping("/device")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;
    
    @Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

    public DeviceTokenController(
            DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerDevice(
            @RequestBody DeviceTokenRequest request) {

    	Long userId = jwtAuthenticationFilter.getLoggedInUserId();

        if (request.getDeviceToken() == null ||
                request.getDeviceToken().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body("Device token is required");
        }

        Set<String> supportedDevices =
            new HashSet<>(Arrays.asList("ANDROID", "IOS"));

        if (request.getDeviceType() == null ||
                !supportedDevices.contains(
                    request.getDeviceType().toUpperCase())) {
            return ResponseEntity.badRequest()
                .body("Invalid device type");
        }

        deviceTokenService.registerDevice(
            userId,
            request.getDeviceToken(),
            request.getDeviceType().toUpperCase(),
            request.getDeviceId()
        );

        return ResponseEntity.ok("Device registered successfully");
    }

    @PostMapping("/unregister")
    public ResponseEntity<String> unregisterDevice(
            @RequestBody DeviceTokenRequest request) {

    	Long userId = jwtAuthenticationFilter.getLoggedInUserId();

        deviceTokenService.unregisterDevice(
            userId,
            request.getDeviceToken()
        );

        return ResponseEntity.ok("Device unregistered successfully");
    }
}
