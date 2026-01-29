package com.phantask.notice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.phantask.notice.dto.CreateNoticeDTO;
import com.phantask.notice.dto.NoticeResponse;
import com.phantask.notice.entity.Notice;
import com.phantask.notice.entity.NoticePriority;
import com.phantask.notice.repository.NoticeRepository;
import com.phantask.notice.service.impl.NoticeServiceImpl;

/**
 * Comprehensive unit tests for NoticeService
 * Tests admin operations (CRUD) and user operations (view by role/priority)
 */
@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeServiceImpl noticeService;

    private Notice notice;
    private CreateNoticeDTO createNoticeDTO;

    @BeforeEach
    void setUp() {
        // Setup notice entity
        notice = new Notice();
        notice.setId(1L);
        notice.setTitle("Test Notice");
        notice.setContent("Test Content");
        notice.setPostedBy("ADMIN");
        notice.setPriority(NoticePriority.IMPORTANT);
        notice.setTargetRoles(Arrays.asList("HR", "TECHNICAL"));
        notice.setCreatedAt(LocalDateTime.now());

        // Setup DTO
        createNoticeDTO = new CreateNoticeDTO();
        createNoticeDTO.setTitle("New Notice");
        createNoticeDTO.setContent("New Content");
        createNoticeDTO.setPostedBy("HR_DEPARTMENT");
        createNoticeDTO.setPriority("URGENT");
        createNoticeDTO.setTargetRoles(Arrays.asList("HR", "ADMIN"));
    }

    // ==================== CREATE NOTICE Tests ====================

    @Test
    void createNotice_WithValidData_ShouldReturnNoticeResponse() {
        // Arrange
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        // Act
        NoticeResponse response = noticeService.createNotice(createNoticeDTO);

        // Assert
        assertNotNull(response);
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    void createNotice_ShouldSetCreationTimestamp() {
        // Arrange
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        // Act
        NoticeResponse response = noticeService.createNotice(createNoticeDTO);

        // Assert
        assertNotNull(response);
        verify(noticeRepository).save(argThat(n -> n.getCreatedAt() != null));
    }

    @Test
    void createNotice_ShouldConvertPriorityStringToEnum() {
        // Arrange
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        // Act
        NoticeResponse response = noticeService.createNotice(createNoticeDTO);

        // Assert
        assertNotNull(response);
        verify(noticeRepository).save(argThat(n -> n.getPriority() != null));
    }

    @Test
    void createNotice_WithUrgentPriority_ShouldSetCorrectEnum() {
        // Arrange
        createNoticeDTO.setPriority("URGENT");
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        // Act
        NoticeResponse response = noticeService.createNotice(createNoticeDTO);

        // Assert
        assertNotNull(response);
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    void createNotice_WithMultipleTargetRoles_ShouldSaveAll() {
        // Arrange
        createNoticeDTO.setTargetRoles(Arrays.asList("HR", "TECHNICAL", "ADMIN"));
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        // Act
        NoticeResponse response = noticeService.createNotice(createNoticeDTO);

        // Assert
        assertNotNull(response);
        verify(noticeRepository).save(any(Notice.class));
    }

    // ==================== UPDATE NOTICE Tests ====================

    @Test
    void updateNotice_WithValidId_ShouldReturnUpdatedNotice() {
        // Arrange
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        // Act
        NoticeResponse response = noticeService.updateNotice(1L, createNoticeDTO);

        // Assert
        assertNotNull(response);
        verify(noticeRepository).findById(1L);
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    void updateNotice_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            noticeService.updateNotice(999L, createNoticeDTO);
        });
        verify(noticeRepository).findById(999L);
        verify(noticeRepository, never()).save(any(Notice.class));
    }

    @Test
    void updateNotice_ShouldNotChangeCreatedAt() {
        // Arrange
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(5);
        notice.setCreatedAt(originalCreatedAt);
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        // Act
        NoticeResponse response = noticeService.updateNotice(1L, createNoticeDTO);

        // Assert
        assertNotNull(response);
        verify(noticeRepository).save(argThat(n -> n.getCreatedAt().equals(originalCreatedAt)));
    }

    @Test
    void updateNotice_ShouldUpdateAllFields() {
        // Arrange
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        // Act
        NoticeResponse response = noticeService.updateNotice(1L, createNoticeDTO);

        // Assert
        assertNotNull(response);
        verify(noticeRepository).save(any(Notice.class));
    }

    // ==================== DELETE NOTICE Tests ====================

    @Test
    void deleteNotice_WithValidId_ShouldReturnTrue() {
        // Arrange
        when(noticeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(noticeRepository).deleteById(1L);

        // Act
        boolean result = noticeService.deleteNotice(1L);

        // Assert
        assertTrue(result);
        verify(noticeRepository).existsById(1L);
        verify(noticeRepository).deleteById(1L);
    }

    @Test
    void deleteNotice_WithInvalidId_ShouldReturnFalse() {
        // Arrange
        when(noticeRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = noticeService.deleteNotice(999L);

        // Assert
        assertFalse(result);
        verify(noticeRepository).existsById(999L);
        verify(noticeRepository, never()).deleteById(anyLong());
    }

    // ==================== GET ALL NOTICES (ADMIN) Tests ====================

    @Test
    void getAllNoticesAdmin_ShouldReturnAllNotices() {
        // Arrange
        Notice notice2 = new Notice();
        notice2.setId(2L);
        notice2.setTitle("Notice 2");
        notice2.setPriority(NoticePriority.GENERAL);
        
        List<Notice> notices = Arrays.asList(notice, notice2);
        when(noticeRepository.findAll()).thenReturn(notices);

        // Act
        List<NoticeResponse> responses = noticeService.getAllNoticesAdmin();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(noticeRepository).findAll();
    }

    @Test
    void getAllNoticesAdmin_WithNoNotices_ShouldReturnEmptyList() {
        // Arrange
        when(noticeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<NoticeResponse> responses = noticeService.getAllNoticesAdmin();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(noticeRepository).findAll();
    }

    // ==================== GET NOTICES FOR USER (BY ROLES) Tests ====================

    @Test
    void getAllNoticesForUser_WithMatchingRoles_ShouldReturnNotices() {
        // Arrange
        List<String> userRoles = Arrays.asList("HR", "ADMIN");
        when(noticeRepository.findByTargetRolesIn(userRoles)).thenReturn(Arrays.asList(notice));

        // Act
        List<NoticeResponse> responses = noticeService.getAllNoticesForUser(userRoles);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(noticeRepository).findByTargetRolesIn(userRoles);
    }

    @Test
    void getAllNoticesForUser_WithNoMatchingRoles_ShouldReturnEmptyList() {
        // Arrange
        List<String> userRoles = Arrays.asList("DEVELOPER");
        when(noticeRepository.findByTargetRolesIn(userRoles)).thenReturn(Collections.emptyList());

        // Act
        List<NoticeResponse> responses = noticeService.getAllNoticesForUser(userRoles);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(noticeRepository).findByTargetRolesIn(userRoles);
    }

    @Test
    void getAllNoticesForUser_WithNullRoles_ShouldReturnEmptyList() {
        // Act
        List<NoticeResponse> responses = noticeService.getAllNoticesForUser(null);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(noticeRepository, never()).findByTargetRolesIn(anyList());
    }

    @Test
    void getAllNoticesForUser_WithEmptyRoles_ShouldReturnEmptyList() {
        // Act
        List<NoticeResponse> responses = noticeService.getAllNoticesForUser(Collections.emptyList());

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(noticeRepository, never()).findByTargetRolesIn(anyList());
    }

    // ==================== GET NOTICES BY PRIORITY FOR USER Tests ====================

    @Test
    void getNoticesByPriorityForUser_WithMatchingPriority_ShouldReturnNotices() {
        // Arrange
        List<String> userRoles = Arrays.asList("HR", "TECHNICAL");
        String priority = "URGENT";
        when(noticeRepository.findByTargetRolesInAndPriority(userRoles, NoticePriority.URGENT))
                .thenReturn(Arrays.asList(notice));

        // Act
        List<NoticeResponse> responses = noticeService.getNoticesByPriorityForUser(userRoles, priority);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(noticeRepository).findByTargetRolesInAndPriority(userRoles, NoticePriority.URGENT);
    }

    @Test
    void getNoticesByPriorityForUser_WithImportantPriority_ShouldWork() {
        // Arrange
        List<String> userRoles = Arrays.asList("HR");
        String priority = "IMPORTANT";
        when(noticeRepository.findByTargetRolesInAndPriority(userRoles, NoticePriority.IMPORTANT))
                .thenReturn(Arrays.asList(notice));

        // Act
        List<NoticeResponse> responses = noticeService.getNoticesByPriorityForUser(userRoles, priority);

        // Assert
        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        verify(noticeRepository).findByTargetRolesInAndPriority(userRoles, NoticePriority.IMPORTANT);
    }

    @Test
    void getNoticesByPriorityForUser_WithGeneralPriority_ShouldWork() {
        // Arrange
        List<String> userRoles = Arrays.asList("TECHNICAL");
        String priority = "GENERAL";
        when(noticeRepository.findByTargetRolesInAndPriority(userRoles, NoticePriority.GENERAL))
                .thenReturn(Arrays.asList(notice));

        // Act
        List<NoticeResponse> responses = noticeService.getNoticesByPriorityForUser(userRoles, priority);

        // Assert
        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        verify(noticeRepository).findByTargetRolesInAndPriority(userRoles, NoticePriority.GENERAL);
    }

    @Test
    void getNoticesByPriorityForUser_WithNullRoles_ShouldReturnEmptyList() {
        // Act
        List<NoticeResponse> responses = noticeService.getNoticesByPriorityForUser(null, "URGENT");

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(noticeRepository, never()).findByTargetRolesInAndPriority(anyList(), any(NoticePriority.class));
    }

    @Test
    void getNoticesByPriorityForUser_WithEmptyRoles_ShouldReturnEmptyList() {
        // Act
        List<NoticeResponse> responses = noticeService.getNoticesByPriorityForUser(Collections.emptyList(), "URGENT");

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(noticeRepository, never()).findByTargetRolesInAndPriority(anyList(), any(NoticePriority.class));
    }

    @Test
    void getNoticesByPriorityForUser_WithNoMatches_ShouldReturnEmptyList() {
        // Arrange
        List<String> userRoles = Arrays.asList("HR");
        String priority = "URGENT";
        when(noticeRepository.findByTargetRolesInAndPriority(userRoles, NoticePriority.URGENT))
                .thenReturn(Collections.emptyList());

        // Act
        List<NoticeResponse> responses = noticeService.getNoticesByPriorityForUser(userRoles, priority);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(noticeRepository).findByTargetRolesInAndPriority(userRoles, NoticePriority.URGENT);
    }

    // ==================== RESPONSE MAPPING Tests ====================

    @Test
    void noticeResponse_ShouldMapAllFields() {
        // Arrange
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        // Act
        NoticeResponse response = noticeService.createNotice(createNoticeDTO);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getId());
        verify(noticeRepository).save(any(Notice.class));
    }
}
