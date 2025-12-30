package com.phantask.config;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.phantask.authentication.entity.Role;
import com.phantask.authentication.entity.User;
import com.phantask.authentication.entity.UserProfile;
import com.phantask.authentication.repository.RoleRepository;
import com.phantask.authentication.repository.UserProfileRepository;
import com.phantask.authentication.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for initializing the default admin user on first application run.
 * Uses @Transactional to ensure proper entity state management and atomic operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializerService {

    // Repositories and encoder injected via constructor
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserProfileRepository profileRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates default admin user if it doesn't exist.
     * This method is transactional to ensure all operations succeed or fail together.
     */
    @Transactional
    public void createDefaultAdminIfNotExists() {
        String adminEmail = "admin@phantask.in";
        // Extract username from email (matching your createAccount logic)
        String adminUsername = adminEmail.substring(0, adminEmail.indexOf("@")); // "admin"

        // 1. Check if admin user already exists by username
        if (userRepo.existsByUsername(adminUsername)) {
            log.info("Default admin user already exists. Skipping initialization.");
            return;
        }

        log.info("No admin user found. Creating default admin user...");

        // 2. Ensure the ADMIN role exists (create if missing)
        Role adminRole = roleRepo.findByRoleName("ADMIN").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRoleName("ADMIN"); // Role name used in security checks
            return roleRepo.save(newRole);
        });

        // 3. Create default admin user with temporary password
        User adminUser = new User();
        adminUser.setUsername(adminUsername);                      // Username: "admin" (derived from email)
        adminUser.setEmail(adminEmail);                            // Email: "admin@phantask.in"
        adminUser.setPassword(passwordEncoder.encode("Temp@123")); // Default temp password (hashed with BCrypt)
        adminUser.setEnabled(true);                                // Account is active
        adminUser.setFirstLogin(true);                             // Force password change on first login
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUser.getRoles().add(adminRole);                       // Grant ADMIN role

        // Save user (entity remains managed within this transaction)
        User savedUser = userRepo.save(adminUser);

        // 4. Create a basic profile for the admin user
        // Since we're in the same transaction, savedUser is still managed
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setFullName("System Administrator");
        profileRepo.save(profile);

        // 5. Log credentials (only visible in dev/test environments)
        log.info("Default admin user created successfully!");
        log.info("Username: {}", adminUsername);
        log.info("Email: {}", adminEmail);
        log.info("Password: Temp@123");
        log.info("IMPORTANT: Change the password on first login!");
    }
}
