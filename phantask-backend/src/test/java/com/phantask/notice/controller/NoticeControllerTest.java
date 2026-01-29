package com.phantask.notice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import com.phantask.notice.dto.CreateNoticeDTO;
import com.phantask.notice.dto.NoticeResponse;
import com.phantask.notice.service.NoticeService;

/**
 * Integration tests for NoticeController
 * Tests both admin operations (CRUD) and user operations (view by role/priority)
 */
@SpringBootTest
@AutoConfigureMockMvc
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoticeService noticeService;

    private CreateNoticeDTO createNoticeDTO;
    private NoticeResponse noticeResponse;
    private List<NoticeResponse> noticeResponseList;

    @BeforeEach
    void setUp() {
        reset(noticeService);

        // Setup DTO
        createNoticeDTO = new CreateNoticeDTO();
        createNoticeDTO.setTitle("Test Notice");
        createNoticeDTO.setContent("Test Content");
        createNoticeDTO.setPostedBy("ADMIN");
        createNoticeDTO.setPriority("IMPORTANT");
        createNoticeDTO.setTargetRoles(Arrays.asList("HR", "TECHNICAL"));

        // Setup response
        noticeResponse = new NoticeResponse();
        noticeResponse.setId(1L);
        noticeResponse.setTitle("Test Notice");
        noticeResponse.setContent("Test Content");
        noticeResponse.setPostedBy("ADMIN");
        noticeResponse.setPriority("IMPORTANT");
        noticeResponse.setTargetRoles(Arrays.asList("HR", "TECHNICAL"));
        noticeResponse.setCreatedAt(LocalDateTime.now());

        // Setup response list
        NoticeResponse notice2 = new NoticeResponse();
        notice2.setId(2L);
        notice2.setTitle("Notice 2");
        notice2.setContent("Content 2");
        notice2.setPriority("URGENT");
        notice2.setTargetRoles(Arrays.asList("ADMIN"));

        noticeResponseList = Arrays.asList(noticeResponse, notice2);
    }

    // ==================== POST /api/notices/admin/create (Create Notice) Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createNotice_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        when(noticeService.createNotice(any(CreateNoticeDTO.class))).thenReturn(noticeResponse);

        // Act & Assert
        mockMvc.perform(post("/api/notices/admin/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createNoticeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Notice"))
                .andExpect(jsonPath("$.priority").value("IMPORTANT"));

        verify(noticeService).createNotice(any(CreateNoticeDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createNotice_WithNonAdminUser_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/notices/admin/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createNoticeDTO)))
                .andExpect(status().isForbidden());

        verify(noticeService, never()).createNotice(any(CreateNoticeDTO.class));
    }

    @Test
    void createNotice_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/notices/admin/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createNoticeDTO)))
                .andExpect(status().isForbidden());

        verify(noticeService, never()).createNotice(any(CreateNoticeDTO.class));
    }

    // ==================== PUT /api/notices/admin/update/{id} (Update Notice) Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateNotice_WithValidId_ShouldReturn200() throws Exception {
        // Arrange
        when(noticeService.updateNotice(eq(1L), any(CreateNoticeDTO.class))).thenReturn(noticeResponse);

        // Act & Assert
        mockMvc.perform(put("/api/notices/admin/update/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createNoticeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Notice"));

        verify(noticeService).updateNotice(eq(1L), any(CreateNoticeDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateNotice_WithNonAdminUser_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/notices/admin/update/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createNoticeDTO)))
                .andExpect(status().isForbidden());

        verify(noticeService, never()).updateNotice(anyLong(), any(CreateNoticeDTO.class));
    }

    // ==================== DELETE /api/notices/admin/delete/{id} Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNotice_WithValidId_ShouldReturn200() throws Exception {
        // Arrange
        when(noticeService.deleteNotice(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/notices/admin/delete/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Notice deleted successfully"));

        verify(noticeService).deleteNotice(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNotice_WithInvalidId_ShouldReturn404() throws Exception {
        // Arrange
        when(noticeService.deleteNotice(999L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/notices/admin/delete/999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Notice not found"));

        verify(noticeService).deleteNotice(999L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteNotice_WithNonAdminUser_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/notices/admin/delete/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(noticeService, never()).deleteNotice(anyLong());
    }

    // ==================== GET /api/notices/admin/all (Get All Notices) Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllNoticesAdmin_ShouldReturn200() throws Exception {
        // Arrange
        when(noticeService.getAllNoticesAdmin()).thenReturn(noticeResponseList);

        // Act & Assert
        mockMvc.perform(get("/api/notices/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Notice"))
                .andExpect(jsonPath("$[1].title").value("Notice 2"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(noticeService).getAllNoticesAdmin();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllNoticesAdmin_WithNoNotices_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(noticeService.getAllNoticesAdmin()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/notices/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(noticeService).getAllNoticesAdmin();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllNoticesAdmin_WithNonAdminUser_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/notices/admin/all"))
                .andExpect(status().isForbidden());

        verify(noticeService, never()).getAllNoticesAdmin();
    }

    // ==================== GET /api/notices/my (Get User Notices) Tests ====================

    @Test
    @WithMockUser(username = "user1", roles = "HR")
    void getMyNotices_WithHRRole_ShouldReturn200() throws Exception {
        // Arrange
        when(noticeService.getAllNoticesForUser(anyList())).thenReturn(noticeResponseList);

        // Act & Assert
        mockMvc.perform(get("/api/notices/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Notice"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(noticeService).getAllNoticesForUser(anyList());
    }

    @Test
    @WithMockUser(username = "user2", roles = "TECHNICAL")
    void getMyNotices_WithTechnicalRole_ShouldReturn200() throws Exception {
        // Arrange
        when(noticeService.getAllNoticesForUser(anyList())).thenReturn(Arrays.asList(noticeResponse));

        // Act & Assert
        mockMvc.perform(get("/api/notices/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(noticeService).getAllNoticesForUser(anyList());
    }

    @Test
    @WithMockUser(username = "user3", roles = "USER")
    void getMyNotices_WithNoMatchingNotices_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(noticeService.getAllNoticesForUser(anyList())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/notices/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(noticeService).getAllNoticesForUser(anyList());
    }

    @Test
    void getMyNotices_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/notices/my"))
                .andExpect(status().isForbidden());

        verify(noticeService, never()).getAllNoticesForUser(anyList());
    }

    // ==================== GET /api/notices/my/priority/{priority} Tests ====================

    @Test
    @WithMockUser(username = "user1", roles = "HR")
    void getMyNoticesByPriority_WithUrgentPriority_ShouldReturn200() throws Exception {
        // Arrange
        NoticeResponse urgentNotice = new NoticeResponse();
        urgentNotice.setId(3L);
        urgentNotice.setTitle("Urgent Notice");
        urgentNotice.setPriority("URGENT");
        
        when(noticeService.getNoticesByPriorityForUser(anyList(), eq("URGENT")))
                .thenReturn(Arrays.asList(urgentNotice));

        // Act & Assert
        mockMvc.perform(get("/api/notices/my/priority/URGENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("URGENT"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(noticeService).getNoticesByPriorityForUser(anyList(), eq("URGENT"));
    }

    @Test
    @WithMockUser(username = "user2", roles = "TECHNICAL")
    void getMyNoticesByPriority_WithImportantPriority_ShouldReturn200() throws Exception {
        // Arrange
        when(noticeService.getNoticesByPriorityForUser(anyList(), eq("IMPORTANT")))
                .thenReturn(Arrays.asList(noticeResponse));

        // Act & Assert
        mockMvc.perform(get("/api/notices/my/priority/IMPORTANT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("IMPORTANT"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(noticeService).getNoticesByPriorityForUser(anyList(), eq("IMPORTANT"));
    }

    @Test
    @WithMockUser(username = "user3", roles = "ADMIN")
    void getMyNoticesByPriority_WithGeneralPriority_ShouldReturn200() throws Exception {
        // Arrange
        NoticeResponse generalNotice = new NoticeResponse();
        generalNotice.setId(4L);
        generalNotice.setTitle("General Notice");
        generalNotice.setPriority("GENERAL");
        
        when(noticeService.getNoticesByPriorityForUser(anyList(), eq("GENERAL")))
                .thenReturn(Arrays.asList(generalNotice));

        // Act & Assert
        mockMvc.perform(get("/api/notices/my/priority/GENERAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("GENERAL"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(noticeService).getNoticesByPriorityForUser(anyList(), eq("GENERAL"));
    }

    @Test
    @WithMockUser(username = "user4", roles = "USER")
    void getMyNoticesByPriority_WithNoMatches_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(noticeService.getNoticesByPriorityForUser(anyList(), eq("URGENT")))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/notices/my/priority/URGENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(noticeService).getNoticesByPriorityForUser(anyList(), eq("URGENT"));
    }

    @Test
    void getMyNoticesByPriority_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/notices/my/priority/URGENT"))
                .andExpect(status().isForbidden());

        verify(noticeService, never()).getNoticesByPriorityForUser(anyList(), anyString());
    }

    // ==================== ROLE EXTRACTION Tests ====================

    @Test
    @WithMockUser(username = "multiRoleUser", roles = {"HR", "TECHNICAL", "ADMIN"})
    void getMyNotices_WithMultipleRoles_ShouldPassAllRoles() throws Exception {
        // Arrange
        when(noticeService.getAllNoticesForUser(anyList())).thenReturn(noticeResponseList);

        // Act & Assert
        mockMvc.perform(get("/api/notices/my"))
                .andExpect(status().isOk());

        verify(noticeService).getAllNoticesForUser(anyList());
    }

    @Test
    @WithMockUser(username = "hrUser", roles = "HR")
    void getMyNoticesByPriority_WithHRRole_ShouldExtractCorrectRole() throws Exception {
        // Arrange
        when(noticeService.getNoticesByPriorityForUser(anyList(), eq("IMPORTANT")))
                .thenReturn(Arrays.asList(noticeResponse));

        // Act & Assert
        mockMvc.perform(get("/api/notices/my/priority/IMPORTANT"))
                .andExpect(status().isOk());

        verify(noticeService).getNoticesByPriorityForUser(anyList(), eq("IMPORTANT"));
    }
}
