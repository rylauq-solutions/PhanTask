package com.phantask.helpline.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantask.authentication.entity.User;
import com.phantask.authentication.repository.UserRepository;
import com.phantask.helpline.dto.RaiseHelplineTicketDTO;
import com.phantask.helpline.entity.HelplineTicket;
import com.phantask.helpline.service.HelplineTicketService;

/**
 * Integration tests for HelplineTicketController
 * Tests raising, viewing, and resolving helpline tickets
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelplineTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HelplineTicketService helplineTicketService;

    @MockBean
    private UserRepository userRepository;

    private User user;
    private HelplineTicket ticket;
    private RaiseHelplineTicketDTO raiseTicketDTO;

    @BeforeEach
    void setUp() {
        reset(helplineTicketService, userRepository);

        // Setup user
        user = new User();
        user.setUid(100L);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");

        // Setup ticket
        ticket = new HelplineTicket();
        ticket.setTicketId(1L);
        ticket.setRaisedByUserId(100L);
        ticket.setRaisedByEmail("testuser@example.com");
        ticket.setAssignedRoleName("HR");
        ticket.setDescription("Test Issue");
        ticket.setPriority("HIGH");
        ticket.setStatus("PENDING");
        ticket.setRaisedAt(LocalDateTime.now());
        ticket.setDueDate(LocalDateTime.now().plusDays(1));

        // Setup DTO
        raiseTicketDTO = new RaiseHelplineTicketDTO();
        raiseTicketDTO.setAssignedRoleName("HR");
        raiseTicketDTO.setDescription("Test Issue");
        raiseTicketDTO.setPriority("HIGH");
    }

    // ==================== POST /api/helpline/raise (Raise Ticket) Tests ====================

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void raiseTicket_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(helplineTicketService.raiseTicket(anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ticket);

        // Act & Assert
        mockMvc.perform(post("/api/helpline/raise")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(raiseTicketDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(userRepository).findByUsername("testuser");
        verify(helplineTicketService).raiseTicket(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void raiseTicket_WithUserNotFound_ShouldReturn400() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/helpline/raise")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(raiseTicketDTO)))
                .andExpect(status().isBadRequest());

        verify(userRepository).findByUsername("testuser");
        verify(helplineTicketService, never()).raiseTicket(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void raiseTicket_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/helpline/raise")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(raiseTicketDTO)))
                .andExpect(status().isForbidden());

        verify(userRepository, never()).findByUsername(anyString());
        verify(helplineTicketService, never()).raiseTicket(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void raiseTicket_WithServiceException_ShouldReturn500() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(helplineTicketService.raiseTicket(anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/helpline/raise")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(raiseTicketDTO)))
                .andExpect(status().isBadRequest());

        verify(helplineTicketService).raiseTicket(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    // ==================== GET /api/helpline/my/raised Tests ====================

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyRaisedTickets_WithExistingTickets_ShouldReturn200() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(helplineTicketService.getTicketsRaisedByUser(100L))
                .thenReturn(Arrays.asList(ticket));

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/raised"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketId").value(1))
                .andExpect(jsonPath("$.length()").value(1));

        verify(userRepository).findByUsername("testuser");
        verify(helplineTicketService).getTicketsRaisedByUser(100L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyRaisedTickets_WithNoTickets_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(helplineTicketService.getTicketsRaisedByUser(100L))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/raised"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(helplineTicketService).getTicketsRaisedByUser(100L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyRaisedTickets_WithNullResult_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(helplineTicketService.getTicketsRaisedByUser(100L)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/raised"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(helplineTicketService).getTicketsRaisedByUser(100L);
    }

    @Test
    void getMyRaisedTickets_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/raised"))
                .andExpect(status().isForbidden());

        verify(userRepository, never()).findByUsername(anyString());
        verify(helplineTicketService, never()).getTicketsRaisedByUser(anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyRaisedTickets_WithUserNotFound_ShouldReturn500() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/raised"))
                .andExpect(status().isInternalServerError());

        verify(userRepository).findByUsername("testuser");
        verify(helplineTicketService, never()).getTicketsRaisedByUser(anyLong());
    }

    // ==================== GET /api/helpline/my/pending Tests ====================

    @Test
    @WithMockUser(username = "hruser", roles = "HR")
    void getMyPendingTickets_WithHRRole_ShouldReturn200() throws Exception {
        // Arrange
        when(helplineTicketService.getPendingTickets(anyList()))
                .thenReturn(Arrays.asList(ticket));

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(helplineTicketService).getPendingTickets(anyList());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMyPendingTickets_WithAdminRole_ShouldReturnAllPending() throws Exception {
        // Arrange
        HelplineTicket ticket2 = new HelplineTicket();
        ticket2.setTicketId(2L);
        ticket2.setStatus("PENDING");
        
        when(helplineTicketService.getPendingTickets(anyList()))
                .thenReturn(Arrays.asList(ticket, ticket2));

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(helplineTicketService).getPendingTickets(anyList());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getMyPendingTickets_WithNoMatches_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(helplineTicketService.getPendingTickets(anyList()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(helplineTicketService).getPendingTickets(anyList());
    }

    @Test
    void getMyPendingTickets_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/pending"))
                .andExpect(status().isForbidden());

        verify(helplineTicketService, never()).getPendingTickets(anyList());
    }

    // ==================== GET /api/helpline/my/resolved Tests ====================

    @Test
    @WithMockUser(username = "hruser", roles = "HR")
    void getMyResolvedTickets_WithHRRole_ShouldReturn200() throws Exception {
        // Arrange
        ticket.setStatus("RESOLVED");
        when(helplineTicketService.getResolvedTickets(anyList()))
                .thenReturn(Arrays.asList(ticket));

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/resolved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("RESOLVED"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(helplineTicketService).getResolvedTickets(anyList());
    }

    @Test
    @WithMockUser(username = "support", roles = "SUPPORT")
    void getMyResolvedTickets_WithSupportRole_ShouldReturnAllResolved() throws Exception {
        // Arrange
        ticket.setStatus("RESOLVED");
        when(helplineTicketService.getResolvedTickets(anyList()))
                .thenReturn(Arrays.asList(ticket));

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/resolved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(helplineTicketService).getResolvedTickets(anyList());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getMyResolvedTickets_WithNoMatches_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(helplineTicketService.getResolvedTickets(anyList()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/resolved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(helplineTicketService).getResolvedTickets(anyList());
    }

    @Test
    void getMyResolvedTickets_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/helpline/my/resolved"))
                .andExpect(status().isForbidden());

        verify(helplineTicketService, never()).getResolvedTickets(anyList());
    }

    // ==================== PUT /api/helpline/resolve/{ticketId} Tests ====================

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void resolveTicket_WithValidAdminUser_ShouldReturn200() throws Exception {
        // Arrange
        ticket.setStatus("RESOLVED");
        ticket.setResolvedAt(LocalDateTime.now());
        
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(helplineTicketService.resolveTicket(eq(1L), anyLong(), anyList()))
                .thenReturn(ticket);

        // Act & Assert
        mockMvc.perform(put("/api/helpline/resolve/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        verify(userRepository).findByUsername("admin");
        verify(helplineTicketService).resolveTicket(eq(1L), anyLong(), anyList());
    }

    @Test
    @WithMockUser(username = "hruser", roles = "HR")
    void resolveTicket_WithAssignedRole_ShouldReturn200() throws Exception {
        // Arrange
        ticket.setStatus("RESOLVED");
        when(userRepository.findByUsername("hruser")).thenReturn(Optional.of(user));
        when(helplineTicketService.resolveTicket(eq(1L), anyLong(), anyList()))
                .thenReturn(ticket);

        // Act & Assert
        mockMvc.perform(put("/api/helpline/resolve/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());

        verify(helplineTicketService).resolveTicket(eq(1L), anyLong(), anyList());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void resolveTicket_WithUnauthorizedRole_ShouldReturn403() throws Exception {
        // Arrange
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(helplineTicketService.resolveTicket(eq(1L), anyLong(), anyList()))
                .thenThrow(new RuntimeException("Not authorized"));

        // Act & Assert
        mockMvc.perform(put("/api/helpline/resolve/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        verify(helplineTicketService).resolveTicket(eq(1L), anyLong(), anyList());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void resolveTicket_WithInvalidTicketId_ShouldReturn403() throws Exception {
        // Arrange
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(helplineTicketService.resolveTicket(eq(999L), anyLong(), anyList()))
                .thenThrow(new RuntimeException("Ticket not found"));

        // Act & Assert
        mockMvc.perform(put("/api/helpline/resolve/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        verify(helplineTicketService).resolveTicket(eq(999L), anyLong(), anyList());
    }

    @Test
    void resolveTicket_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/helpline/resolve/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        verify(userRepository, never()).findByUsername(anyString());
        verify(helplineTicketService, never()).resolveTicket(anyLong(), anyLong(), anyList());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void resolveTicket_WithUserNotFound_ShouldReturn403() throws Exception {
        // Arrange
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/helpline/resolve/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        verify(userRepository).findByUsername("admin");
        verify(helplineTicketService, never()).resolveTicket(anyLong(), anyLong(), anyList());
    }
}
