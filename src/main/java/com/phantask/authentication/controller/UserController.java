package com.phantask.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phantask.authentication.dto.PasswordChangeRequest;
import com.phantask.authentication.dto.RegisterRequest;
import com.phantask.authentication.dto.UpdateProfileRequest;
import com.phantask.authentication.entity.Role;
import com.phantask.authentication.entity.User;
import com.phantask.authentication.entity.UserProfile;
import com.phantask.authentication.repository.RoleRepository;
import com.phantask.authentication.repository.UserRepository;
import com.phantask.authentication.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    
    @PostMapping("/create-student")
    public ResponseEntity<?> createStudent(@RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode("Temp@123")); // default password
        user.setEnabled(true);
        user.setFirstLogin(true); // force password change on first login
        userRepo.save(user);
        Role studentRole = roleRepo.findByRoleName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Role STUDENT not found"));
        user.getRoles().add(studentRole);

        userRepo.save(user);
        return ResponseEntity.ok("Student account created successfully. Temporary password: Temp@123");
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile(Authentication auth) {
        return ResponseEntity.ok(userService.getProfile(auth.getName()));
    }

    @PostMapping("/update-profile")
    public ResponseEntity<String> updateProfile(
        Authentication auth,
        @RequestBody UpdateProfileRequest req
    ) {
        return ResponseEntity.ok(userService.updateProfile(auth.getName(), req));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
        Authentication auth,
        @RequestBody PasswordChangeRequest req
    ) {
        return ResponseEntity.ok(userService.changePassword(auth.getName(), req));
    }
}


