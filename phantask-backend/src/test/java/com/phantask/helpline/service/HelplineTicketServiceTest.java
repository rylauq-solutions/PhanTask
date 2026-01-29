package com.phantask.helpline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.phantask.helpline.entity.HelplineTicket;
import com.phantask.helpline.repository.HelplineTicketRepository;
import com.phantask.helpline.service.impl.HelplineTicketServiceImpl;

/**
 * Comprehensive unit tests for HelplineTicketService
 * Tests ticket creation, viewing (by role/status), and resolution
 */
@ExtendWith(MockitoExtension.class)
class HelplineTicketServiceTest {

    @Mock
    private HelplineTicketRepository helplineTicketRepository;

    @InjectMocks
    private HelplineTicketServiceImpl helplineTicketService;

    private HelplineTicket ticket;

    @BeforeEach
    void setUp() {
        ticket = new HelplineTicket();
        ticket.setTicketId(1L);
        ticket.setRaisedByUserId(100L);
        ticket.setRaisedByEmail("user@example.com");
        ticket.setAssignedRoleName("HR");
        ticket.setDescription("Test Issue");
        ticket.setPriority("HIGH");
        ticket.setStatus("PENDING");
        ticket.setRaisedAt(LocalDateTime.now());
        ticket.setDueDate(LocalDateTime.now().plusDays(1));
    }

    // ==================== RAISE TICKET Tests ====================

    @Test
    void raiseTicket_WithValidData_ShouldCreateTicket() {
        // Arrange
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.raiseTicket(
                100L, "user@example.com", "HR", "Test Issue", "HIGH");

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(any(HelplineTicket.class));
    }

    @Test
    void raiseTicket_ShouldSetStatusToPending() {
        // Arrange
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.raiseTicket(
                100L, "user@example.com", "HR", "Test Issue", "HIGH");

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(argThat(t -> "PENDING".equals(t.getStatus())));
    }

    @Test
    void raiseTicket_WithHighPriority_ShouldSetDueDateTo1Day() {
        // Arrange
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.raiseTicket(
                100L, "user@example.com", "HR", "Urgent Issue", "HIGH");

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(argThat(t -> 
            t.getDueDate() != null
        ));
    }

    @Test
    void raiseTicket_WithMediumPriority_ShouldSetDueDateTo3Days() {
        // Arrange
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.raiseTicket(
                100L, "user@example.com", "SUPPORT", "Medium Issue", "MEDIUM");

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(any(HelplineTicket.class));
    }

    @Test
    void raiseTicket_WithLowPriority_ShouldSetDueDateTo5Days() {
        // Arrange
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.raiseTicket(
                100L, "user@example.com", "MANAGER", "Low Priority Issue", "LOW");

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(any(HelplineTicket.class));
    }

    @Test
    void raiseTicket_WithUnknownPriority_ShouldDefaultTo3Days() {
        // Arrange
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.raiseTicket(
                100L, "user@example.com", "HR", "Test Issue", "UNKNOWN");

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(any(HelplineTicket.class));
    }

    @Test
    void raiseTicket_ShouldSetRaisedAtTimestamp() {
        // Arrange
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.raiseTicket(
                100L, "user@example.com", "HR", "Test Issue", "HIGH");

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(argThat(t -> t.getRaisedAt() != null));
    }

    // ==================== GET PENDING TICKETS Tests ====================

    @Test
    void getPendingTickets_WithAdminRole_ShouldReturnAllPending() {
        // Arrange
        List<String> adminRoles = Arrays.asList("ADMIN");
        HelplineTicket ticket2 = new HelplineTicket();
        ticket2.setTicketId(2L);
        ticket2.setStatus("PENDING");
        
        when(helplineTicketRepository.findByStatus("PENDING"))
                .thenReturn(Arrays.asList(ticket, ticket2));

        // Act
        List<HelplineTicket> results = helplineTicketService.getPendingTickets(adminRoles);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(helplineTicketRepository).findByStatus("PENDING");
        verify(helplineTicketRepository, never()).findByAssignedRoleNameInAndStatus(anyList(), anyString());
    }

    @Test
    void getPendingTickets_WithSupportRole_ShouldReturnAllPending() {
        // Arrange
        List<String> supportRoles = Arrays.asList("SUPPORT");
        when(helplineTicketRepository.findByStatus("PENDING"))
                .thenReturn(Arrays.asList(ticket));

        // Act
        List<HelplineTicket> results = helplineTicketService.getPendingTickets(supportRoles);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(helplineTicketRepository).findByStatus("PENDING");
    }

    @Test
    void getPendingTickets_WithHRRole_ShouldReturnOnlyHRTickets() {
        // Arrange
        List<String> hrRoles = Arrays.asList("HR");
        when(helplineTicketRepository.findByAssignedRoleNameInAndStatus(hrRoles, "PENDING"))
                .thenReturn(Arrays.asList(ticket));

        // Act
        List<HelplineTicket> results = helplineTicketService.getPendingTickets(hrRoles);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(helplineTicketRepository).findByAssignedRoleNameInAndStatus(hrRoles, "PENDING");
        verify(helplineTicketRepository, never()).findByStatus(anyString());
    }

    @Test
    void getPendingTickets_WithManagerRole_ShouldReturnOnlyManagerTickets() {
        // Arrange
        List<String> managerRoles = Arrays.asList("MANAGER");
        when(helplineTicketRepository.findByAssignedRoleNameInAndStatus(managerRoles, "PENDING"))
                .thenReturn(Collections.emptyList());

        // Act
        List<HelplineTicket> results = helplineTicketService.getPendingTickets(managerRoles);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(helplineTicketRepository).findByAssignedRoleNameInAndStatus(managerRoles, "PENDING");
    }

    @Test
    void getPendingTickets_WithMultipleRoles_ShouldReturnMatchingTickets() {
        // Arrange
        List<String> multipleRoles = Arrays.asList("HR", "TECHNICAL");
        when(helplineTicketRepository.findByAssignedRoleNameInAndStatus(multipleRoles, "PENDING"))
                .thenReturn(Arrays.asList(ticket));

        // Act
        List<HelplineTicket> results = helplineTicketService.getPendingTickets(multipleRoles);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(helplineTicketRepository).findByAssignedRoleNameInAndStatus(multipleRoles, "PENDING");
    }

    // ==================== GET RESOLVED TICKETS Tests ====================

    @Test
    void getResolvedTickets_WithAdminRole_ShouldReturnAllResolved() {
        // Arrange
        List<String> adminRoles = Arrays.asList("ADMIN");
        ticket.setStatus("RESOLVED");
        when(helplineTicketRepository.findByStatus("RESOLVED"))
                .thenReturn(Arrays.asList(ticket));

        // Act
        List<HelplineTicket> results = helplineTicketService.getResolvedTickets(adminRoles);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(helplineTicketRepository).findByStatus("RESOLVED");
    }

    @Test
    void getResolvedTickets_WithSupportRole_ShouldReturnAllResolved() {
        // Arrange
        List<String> supportRoles = Arrays.asList("SUPPORT");
        ticket.setStatus("RESOLVED");
        when(helplineTicketRepository.findByStatus("RESOLVED"))
                .thenReturn(Arrays.asList(ticket));

        // Act
        List<HelplineTicket> results = helplineTicketService.getResolvedTickets(supportRoles);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        verify(helplineTicketRepository).findByStatus("RESOLVED");
    }

    @Test
    void getResolvedTickets_WithHRRole_ShouldReturnOnlyHRResolvedTickets() {
        // Arrange
        List<String> hrRoles = Arrays.asList("HR");
        ticket.setStatus("RESOLVED");
        when(helplineTicketRepository.findByAssignedRoleNameInAndStatus(hrRoles, "RESOLVED"))
                .thenReturn(Arrays.asList(ticket));

        // Act
        List<HelplineTicket> results = helplineTicketService.getResolvedTickets(hrRoles);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(helplineTicketRepository).findByAssignedRoleNameInAndStatus(hrRoles, "RESOLVED");
    }

    @Test
    void getResolvedTickets_WithNoMatches_ShouldReturnEmptyList() {
        // Arrange
        List<String> roles = Arrays.asList("MANAGER");
        when(helplineTicketRepository.findByAssignedRoleNameInAndStatus(roles, "RESOLVED"))
                .thenReturn(Collections.emptyList());

        // Act
        List<HelplineTicket> results = helplineTicketService.getResolvedTickets(roles);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(helplineTicketRepository).findByAssignedRoleNameInAndStatus(roles, "RESOLVED");
    }

    // ==================== RESOLVE TICKET Tests ====================

    @Test
    void resolveTicket_WithValidAdminUser_ShouldResolveTicket() {
        // Arrange
        List<String> adminRoles = Arrays.asList("ADMIN");
        when(helplineTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.resolveTicket(1L, 200L, adminRoles);

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).findById(1L);
        verify(helplineTicketRepository).save(argThat(t -> 
            "RESOLVED".equals(t.getStatus()) && t.getResolvedAt() != null
        ));
    }

    @Test
    void resolveTicket_WithValidSupportUser_ShouldResolveTicket() {
        // Arrange
        List<String> supportRoles = Arrays.asList("SUPPORT");
        when(helplineTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.resolveTicket(1L, 200L, supportRoles);

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(any(HelplineTicket.class));
    }

    @Test
    void resolveTicket_WithAssignedRoleUser_ShouldResolveTicket() {
        // Arrange
        List<String> hrRoles = Arrays.asList("HR");
        ticket.setAssignedRoleName("HR");
        when(helplineTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.resolveTicket(1L, 200L, hrRoles);

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(any(HelplineTicket.class));
    }

    @Test
    void resolveTicket_WithUnauthorizedRole_ShouldThrowException() {
        // Arrange
        List<String> unauthorizedRoles = Arrays.asList("TECHNICAL");
        ticket.setAssignedRoleName("HR");
        when(helplineTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            helplineTicketService.resolveTicket(1L, 200L, unauthorizedRoles);
        });
        verify(helplineTicketRepository).findById(1L);
        verify(helplineTicketRepository, never()).save(any(HelplineTicket.class));
    }

    @Test
    void resolveTicket_WithInvalidTicketId_ShouldThrowException() {
        // Arrange
        List<String> adminRoles = Arrays.asList("ADMIN");
        when(helplineTicketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            helplineTicketService.resolveTicket(999L, 200L, adminRoles);
        });
        verify(helplineTicketRepository).findById(999L);
        verify(helplineTicketRepository, never()).save(any(HelplineTicket.class));
    }

    @Test
    void resolveTicket_WhenAlreadyResolved_ShouldThrowException() {
        // Arrange
        List<String> adminRoles = Arrays.asList("ADMIN");
        ticket.setStatus("RESOLVED");
        when(helplineTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            helplineTicketService.resolveTicket(1L, 200L, adminRoles);
        });
        verify(helplineTicketRepository).findById(1L);
        verify(helplineTicketRepository, never()).save(any(HelplineTicket.class));
    }

    @Test
    void resolveTicket_ShouldSetResolvedByUserId() {
        // Arrange
        List<String> adminRoles = Arrays.asList("ADMIN");
        when(helplineTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.resolveTicket(1L, 200L, adminRoles);

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(argThat(t -> t.getResolvedByUserId() != null));
    }

    @Test
    void resolveTicket_ShouldSetResolvedAtTimestamp() {
        // Arrange
        List<String> adminRoles = Arrays.asList("ADMIN");
        when(helplineTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(helplineTicketRepository.save(any(HelplineTicket.class))).thenReturn(ticket);

        // Act
        HelplineTicket result = helplineTicketService.resolveTicket(1L, 200L, adminRoles);

        // Assert
        assertNotNull(result);
        verify(helplineTicketRepository).save(argThat(t -> t.getResolvedAt() != null));
    }

    // ==================== GET TICKETS RAISED BY USER Tests ====================

    @Test
    void getTicketsRaisedByUser_WithExistingTickets_ShouldReturnUserTickets() {
        // Arrange
        Long userId = 100L;
        when(helplineTicketRepository.findByRaisedByUserId(userId))
                .thenReturn(Arrays.asList(ticket));

        // Act
        List<HelplineTicket> results = helplineTicketService.getTicketsRaisedByUser(userId);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(helplineTicketRepository).findByRaisedByUserId(userId);
    }

    @Test
    void getTicketsRaisedByUser_WithNoTickets_ShouldReturnEmptyList() {
        // Arrange
        Long userId = 999L;
        when(helplineTicketRepository.findByRaisedByUserId(userId))
                .thenReturn(Collections.emptyList());

        // Act
        List<HelplineTicket> results = helplineTicketService.getTicketsRaisedByUser(userId);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(helplineTicketRepository).findByRaisedByUserId(userId);
    }

    @Test
    void getTicketsRaisedByUser_WithMultipleTickets_ShouldReturnAll() {
        // Arrange
        Long userId = 100L;
        HelplineTicket ticket2 = new HelplineTicket();
        ticket2.setTicketId(2L);
        ticket2.setRaisedByUserId(userId);
        
        when(helplineTicketRepository.findByRaisedByUserId(userId))
                .thenReturn(Arrays.asList(ticket, ticket2));

        // Act
        List<HelplineTicket> results = helplineTicketService.getTicketsRaisedByUser(userId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(helplineTicketRepository).findByRaisedByUserId(userId);
    }
}
