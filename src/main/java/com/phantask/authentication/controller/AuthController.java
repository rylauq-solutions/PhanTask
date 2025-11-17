package com.phantask.authentication.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phantask.authentication.dto.LoginRequest;
import com.phantask.authentication.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /*@PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest req) {
    	 System.out.println("Register end-point hit: " + req.getUsername());
        return ResponseEntity.ok(authService.register(req));
    }*/

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid LoginRequest req) {
        Map<String, Object> response = authService.login(req);
        return ResponseEntity.ok(response);
    }
    
    //Logout (front-end should just delete token, optional server blacklist if needed)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok("Logged out successfully");
    }
    
    //Extend session (token refresh)
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String newToken = authService.refreshToken(authHeader);
        return ResponseEntity.ok(Map.of("token", newToken));
    }
}


