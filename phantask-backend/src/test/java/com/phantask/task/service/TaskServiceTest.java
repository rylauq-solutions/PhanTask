package com.phantask.task.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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

import com.phantask.task.dto.AdminTaskDTO;
import com.phantask.task.dto.EmployeeTaskDTO;
import com.phantask.task.dto.TaskResponse;
import com.phantask.task.entity.TaskEntity;
import com.phantask.task.entity.TaskStatus;
import com.phantask.task.repository.TaskRepository;
import com.phantask.task.service.impl.TaskServiceImpl;

/**
 * Comprehensive unit tests for TaskService
 * Tests admin operations (CRUD) and employee operations (view/submit)
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskEntity taskEntity;
    private AdminTaskDTO adminTaskDTO;
    private EmployeeTaskDTO employeeTaskDTO;

    @BeforeEach
    void setUp() {
        // Setup task entity
        taskEntity = TaskEntity.builder()
                .id(1L)
                .taskName("Test Task")
                .description("Test Description")
                .assignDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .status(TaskStatus.PENDING)
                .assignedToUser("testuser")
                .assignedToRole("HR")
                .createdBy("admin")
                .build();

        // Setup admin DTO
        adminTaskDTO = new AdminTaskDTO();
        adminTaskDTO.setTaskName("New Task");
        adminTaskDTO.setDescription("New Description");
        adminTaskDTO.setDueDate(LocalDate.now().plusDays(5));
        adminTaskDTO.setAssignedToUser("employee1");
        adminTaskDTO.setAssignedToRole("TECHNICAL");

        // Setup employee DTO
        employeeTaskDTO = new EmployeeTaskDTO();
        employeeTaskDTO.setDriveUrl("https://drive.google.com/file/123");
    }

    // ==================== CREATE TASK Tests ====================

    @Test
    void createTask_WithValidData_ShouldReturnTaskResponse() {
        // Arrange
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(taskEntity);

        // Act
        TaskResponse response = taskService.createTask(adminTaskDTO, "admin");

        // Assert
        assertNotNull(response);
        assertEquals("Test Task", response.getTaskName());
        assertEquals("admin", response.getCreatedBy());
        assertEquals("PENDING", response.getStatus());
        verify(taskRepository).save(any(TaskEntity.class));
    }

    @Test
    void createTask_WithNoAssignDate_ShouldSetToday() {
        // Arrange
        adminTaskDTO.setAssignDate(null);
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(taskEntity);

        // Act
        TaskResponse response = taskService.createTask(adminTaskDTO, "admin");

        // Assert
        assertNotNull(response);
        verify(taskRepository).save(argThat(task -> 
            task.getAssignDate().equals(LocalDate.now())
        ));
    }

    @Test
    void createTask_WithUserAssignment_ShouldAssignToUser() {
        // Arrange
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(taskEntity);

        // Act
        TaskResponse response = taskService.createTask(adminTaskDTO, "admin");

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getAssignedToUser());
        verify(taskRepository).save(any(TaskEntity.class));
    }

    @Test
    void createTask_WithRoleAssignment_ShouldAssignToRole() {
        // Arrange
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(taskEntity);

        // Act
        TaskResponse response = taskService.createTask(adminTaskDTO, "admin");

        // Assert
        assertNotNull(response);
        assertEquals("HR", response.getAssignedToRole());
        verify(taskRepository).save(any(TaskEntity.class));
    }

    // ==================== UPDATE TASK Tests ====================

    @Test
    void updateTask_WithValidId_ShouldReturnUpdatedTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(taskEntity);

        // Act
        TaskResponse response = taskService.updateTask(1L, adminTaskDTO);

        // Assert
        assertNotNull(response);
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(TaskEntity.class));
    }

    @Test
    void updateTask_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taskService.updateTask(999L, adminTaskDTO);
        });
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(TaskEntity.class));
    }

    @Test
    void updateTask_ShouldNotChangeCreatedBy() {
        // Arrange
        TaskEntity existingTask = TaskEntity.builder()
                .id(1L)
                .taskName("Old Task")
                .createdBy("originalAdmin")
                .status(TaskStatus.PENDING)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponse response = taskService.updateTask(1L, adminTaskDTO);

        // Assert
        verify(taskRepository).save(argThat(task -> 
            "originalAdmin".equals(task.getCreatedBy())
        ));
    }

    @Test
    void updateTask_ShouldNotChangeStatus() {
        // Arrange
        TaskEntity existingTask = TaskEntity.builder()
                .id(1L)
                .taskName("Old Task")
                .status(TaskStatus.SUBMITTED)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponse response = taskService.updateTask(1L, adminTaskDTO);

        // Assert
        verify(taskRepository).save(argThat(task -> 
            TaskStatus.SUBMITTED.equals(task.getStatus())
        ));
    }

    // ==================== DELETE TASK Tests ====================

    @Test
    void deleteTask_WithValidId_ShouldReturnTrue() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskEntity));
        doNothing().when(taskRepository).delete(any(TaskEntity.class));

        // Act
        boolean result = taskService.deleteTask(1L);

        // Assert
        assertTrue(result);
        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(taskEntity);
    }

    @Test
    void deleteTask_WithInvalidId_ShouldReturnFalse() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = taskService.deleteTask(999L);

        // Assert
        assertFalse(result);
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).delete(any(TaskEntity.class));
    }

    // ==================== GET ALL TASKS (ADMIN) Tests ====================

    @Test
    void getAllTasksAdmin_ShouldReturnAllTasks() {
        // Arrange
        TaskEntity task2 = TaskEntity.builder()
                .id(2L)
                .taskName("Task 2")
                .status(TaskStatus.SUBMITTED)
                .build();

        when(taskRepository.findAll()).thenReturn(Arrays.asList(taskEntity, task2));

        // Act
        List<TaskResponse> responses = taskService.getAllTasksAdmin();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(taskRepository).findAll();
    }

    @Test
    void getAllTasksAdmin_WithNoTasks_ShouldReturnEmptyList() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<TaskResponse> responses = taskService.getAllTasksAdmin();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(taskRepository).findAll();
    }

    // ==================== GET ALL TASKS FOR USER Tests ====================

    @Test
    void getAllTasksForUser_WithUserTasks_ShouldReturnTasks() {
        // Arrange
        when(taskRepository.findByAssignedToUser("testuser")).thenReturn(Arrays.asList(taskEntity));
        when(taskRepository.findByAssignedToRole(anyString())).thenReturn(Collections.emptyList());

        // Act
        List<TaskResponse> responses = taskService.getAllTasksForUser("testuser", Arrays.asList("HR"));

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Task", responses.get(0).getTaskName());
        verify(taskRepository).findByAssignedToUser("testuser");
    }

    @Test
    void getAllTasksForUser_WithRoleTasks_ShouldReturnTasks() {
        // Arrange
        TaskEntity roleTask = TaskEntity.builder()
                .id(2L)
                .taskName("Role Task")
                .assignedToRole("HR")
                .status(TaskStatus.PENDING)
                .build();

        when(taskRepository.findByAssignedToUser("testuser")).thenReturn(Collections.emptyList());
        when(taskRepository.findByAssignedToRole("HR")).thenReturn(Arrays.asList(roleTask));

        // Act
        List<TaskResponse> responses = taskService.getAllTasksForUser("testuser", Arrays.asList("HR"));

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Role Task", responses.get(0).getTaskName());
        verify(taskRepository).findByAssignedToRole("HR");
    }

    @Test
    void getAllTasksForUser_WithBothUserAndRoleTasks_ShouldMergeAndDeduplicate() {
        // Arrange
        TaskEntity userTask = TaskEntity.builder()
                .id(1L)
                .taskName("User Task")
                .assignedToUser("testuser")
                .status(TaskStatus.PENDING)
                .build();

        TaskEntity roleTask = TaskEntity.builder()
                .id(2L)
                .taskName("Role Task")
                .assignedToRole("HR")
                .status(TaskStatus.PENDING)
                .build();

        TaskEntity duplicateTask = TaskEntity.builder()
                .id(1L)
                .taskName("User Task")
                .assignedToUser("testuser")
                .assignedToRole("HR")
                .status(TaskStatus.PENDING)
                .build();

        when(taskRepository.findByAssignedToUser("testuser")).thenReturn(Arrays.asList(userTask, duplicateTask));
        when(taskRepository.findByAssignedToRole("HR")).thenReturn(Arrays.asList(duplicateTask, roleTask));

        // Act
        List<TaskResponse> responses = taskService.getAllTasksForUser("testuser", Arrays.asList("HR"));

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size()); // Should deduplicate task with ID 1
        verify(taskRepository).findByAssignedToUser("testuser");
        verify(taskRepository).findByAssignedToRole("HR");
    }

    @Test
    void getAllTasksForUser_WithNullRoles_ShouldOnlyReturnUserTasks() {
        // Arrange
        when(taskRepository.findByAssignedToUser("testuser")).thenReturn(Arrays.asList(taskEntity));

        // Act
        List<TaskResponse> responses = taskService.getAllTasksForUser("testuser", null);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(taskRepository).findByAssignedToUser("testuser");
        verify(taskRepository, never()).findByAssignedToRole(anyString());
    }

    @Test
    void getAllTasksForUser_WithEmptyRoles_ShouldOnlyReturnUserTasks() {
        // Arrange
        when(taskRepository.findByAssignedToUser("testuser")).thenReturn(Arrays.asList(taskEntity));

        // Act
        List<TaskResponse> responses = taskService.getAllTasksForUser("testuser", Collections.emptyList());

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(taskRepository).findByAssignedToUser("testuser");
        verify(taskRepository, never()).findByAssignedToRole(anyString());
    }

    // ==================== GET PENDING TASKS FOR USER Tests ====================

    @Test
    void getPendingTasksForUser_WithPendingTasks_ShouldReturnOnlyPending() {
        // Arrange
        when(taskRepository.findByAssignedToUserAndStatus("testuser", TaskStatus.PENDING))
                .thenReturn(Arrays.asList(taskEntity));
        when(taskRepository.findByAssignedToRoleAndStatus(anyString(), eq(TaskStatus.PENDING)))
                .thenReturn(Collections.emptyList());

        // Act
        List<TaskResponse> responses = taskService.getPendingTasksForUser("testuser", Arrays.asList("HR"));

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("PENDING", responses.get(0).getStatus());
        verify(taskRepository).findByAssignedToUserAndStatus("testuser", TaskStatus.PENDING);
    }

    @Test
    void getPendingTasksForUser_WithNoTasks_ShouldReturnEmptyList() {
        // Arrange
        when(taskRepository.findByAssignedToUserAndStatus("testuser", TaskStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(taskRepository.findByAssignedToRoleAndStatus(anyString(), eq(TaskStatus.PENDING)))
                .thenReturn(Collections.emptyList());

        // Act
        List<TaskResponse> responses = taskService.getPendingTasksForUser("testuser", Arrays.asList("HR"));

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(taskRepository).findByAssignedToUserAndStatus("testuser", TaskStatus.PENDING);
    }

    // ==================== GET SUBMITTED TASKS FOR USER Tests ====================

    @Test
    void getSubmittedTasksForUser_WithSubmittedTasks_ShouldReturnOnlySubmitted() {
        // Arrange
        TaskEntity submittedTask = TaskEntity.builder()
                .id(1L)
                .taskName("Submitted Task")
                .status(TaskStatus.SUBMITTED)
                .assignedToUser("testuser")
                .build();

        when(taskRepository.findByAssignedToUserAndStatus("testuser", TaskStatus.SUBMITTED))
                .thenReturn(Arrays.asList(submittedTask));
        when(taskRepository.findByAssignedToRoleAndStatus(anyString(), eq(TaskStatus.SUBMITTED)))
                .thenReturn(Collections.emptyList());

        // Act
        List<TaskResponse> responses = taskService.getSubmittedTasksForUser("testuser", Arrays.asList("HR"));

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("SUBMITTED", responses.get(0).getStatus());
        verify(taskRepository).findByAssignedToUserAndStatus("testuser", TaskStatus.SUBMITTED);
    }

    // ==================== SUBMIT TASK Tests ====================

    @Test
    void submitTask_WithValidId_ShouldUpdateStatusAndDriveUrl() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponse response = taskService.submitTask(1L, employeeTaskDTO, "testuser");

        // Assert
        assertNotNull(response);
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(argThat(task -> 
            TaskStatus.SUBMITTED.equals(task.getStatus()) &&
            "https://drive.google.com/file/123".equals(task.getDriveUrl()) &&
            task.getUploadDateTime() != null
        ));
    }

    @Test
    void submitTask_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taskService.submitTask(999L, employeeTaskDTO, "testuser");
        });
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(TaskEntity.class));
    }

    @Test
    void submitTask_ShouldSetUploadDateTime() {
        // Arrange
        LocalDateTime beforeSubmit = LocalDateTime.now();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponse response = taskService.submitTask(1L, employeeTaskDTO, "testuser");

        // Assert
        verify(taskRepository).save(argThat(task -> {
            LocalDateTime uploadTime = task.getUploadDateTime();
            return uploadTime != null && 
                   (uploadTime.isEqual(beforeSubmit) || uploadTime.isAfter(beforeSubmit));
        }));
    }

    @Test
    void submitTask_WithAlreadySubmittedTask_ShouldStillUpdate() {
        // Arrange
        TaskEntity submittedTask = TaskEntity.builder()
                .id(1L)
                .taskName("Already Submitted")
                .status(TaskStatus.SUBMITTED)
                .driveUrl("https://old-url.com")
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(submittedTask));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponse response = taskService.submitTask(1L, employeeTaskDTO, "testuser");

        // Assert
        verify(taskRepository).save(argThat(task -> 
            "https://drive.google.com/file/123".equals(task.getDriveUrl())
        ));
    }
}
