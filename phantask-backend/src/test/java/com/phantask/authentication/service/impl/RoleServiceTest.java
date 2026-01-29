package com.phantask.authentication.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.phantask.authentication.entity.Role;
import com.phantask.authentication.repository.RoleRepository;

/**
 * Unit tests for RoleService
 * 
 * Tests cover:
 * - Adding new roles
 * - Duplicate role prevention
 * - Role name normalization (uppercase)
 * - Retrieving all roles
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role adminRole;
    private Role userRole;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        // Setup test roles
        adminRole = new Role();
        adminRole.setRid(1L);
        adminRole.setRoleName("ADMIN");

        userRole = new Role();
        userRole.setRid(2L);
        userRole.setRoleName("USER");

        managerRole = new Role();
        managerRole.setRid(3L);
        managerRole.setRoleName("MANAGER");
    }

    // ==================== addRole() Tests ====================

    @Test
    void addRole_WithValidRoleName_ShouldCreateRole() {
        // Arrange
        String roleName = "EDITOR";
        when(roleRepository.existsByRoleName("EDITOR")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        roleService.addRole(roleName);

        // Assert
        verify(roleRepository).existsByRoleName("EDITOR");
        verify(roleRepository).save(argThat(role -> 
            role.getRoleName().equals("EDITOR")
        ));
    }

    @Test
    void addRole_WithLowercaseRoleName_ShouldConvertToUppercase() {
        // Arrange
        String roleName = "editor";
        when(roleRepository.existsByRoleName("EDITOR")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        roleService.addRole(roleName);

        // Assert
        verify(roleRepository).existsByRoleName("EDITOR");
        verify(roleRepository).save(argThat(role -> 
            role.getRoleName().equals("EDITOR")
        ));
    }

    @Test
    void addRole_WithMixedCaseRoleName_ShouldConvertToUppercase() {
        // Arrange
        String roleName = "EdItOr";
        when(roleRepository.existsByRoleName("EDITOR")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        roleService.addRole(roleName);

        // Assert
        verify(roleRepository).existsByRoleName("EDITOR");
        verify(roleRepository).save(argThat(role -> 
            role.getRoleName().equals("EDITOR")
        ));
    }

    @Test
    void addRole_WithWhitespace_ShouldTrimAndConvert() {
        // Arrange
        String roleName = "  editor  ";
        when(roleRepository.existsByRoleName("EDITOR")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        roleService.addRole(roleName);

        // Assert
        verify(roleRepository).existsByRoleName("EDITOR");
        verify(roleRepository).save(argThat(role -> 
            role.getRoleName().equals("EDITOR")
        ));
    }

    @Test
    void addRole_WithDuplicateRoleName_ShouldThrowException() {
        // Arrange
        String roleName = "ADMIN";
        when(roleRepository.existsByRoleName("ADMIN")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> roleService.addRole(roleName)
        );

        assertEquals("Role 'ADMIN' already exists", exception.getMessage());
        verify(roleRepository).existsByRoleName("ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void addRole_WithDuplicateRoleNameDifferentCase_ShouldThrowException() {
        // Arrange
        String roleName = "admin"; // lowercase but will be normalized to ADMIN
        when(roleRepository.existsByRoleName("ADMIN")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> roleService.addRole(roleName)
        );

        assertEquals("Role 'ADMIN' already exists", exception.getMessage());
        verify(roleRepository).existsByRoleName("ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
    }

    // ==================== getAllRoles() Tests ====================

    @Test
    void getAllRoles_WithMultipleRoles_ShouldReturnSortedList() {
        // Arrange
        List<Role> roles = Arrays.asList(userRole, adminRole, managerRole);
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        List<String> result = roleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify sorting (alphabetical)
        assertEquals("ADMIN", result.get(0));
        assertEquals("MANAGER", result.get(1));
        assertEquals("USER", result.get(2));

        verify(roleRepository).findAll();
    }

    @Test
    void getAllRoles_WithNoRoles_ShouldReturnEmptyList() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<String> result = roleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roleRepository).findAll();
    }

    @Test
    void getAllRoles_WithSingleRole_ShouldReturnSingleItemList() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(Arrays.asList(adminRole));

        // Act
        List<String> result = roleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0));
        verify(roleRepository).findAll();
    }

    // ==================== Edge Cases ====================

    @Test
    void addRole_WithEmptyStringAfterTrim_ShouldProcessAsEmpty() {
        // Arrange
        String roleName = "   ";
        when(roleRepository.existsByRoleName("")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        roleService.addRole(roleName);

        // Assert
        verify(roleRepository).existsByRoleName("");
        verify(roleRepository).save(argThat(role -> 
            role.getRoleName().equals("")
        ));
    }
}
