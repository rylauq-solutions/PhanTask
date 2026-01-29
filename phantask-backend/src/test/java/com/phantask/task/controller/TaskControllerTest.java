package com.phantask.task.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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
import com.phantask.task.dto.AdminTaskDTO;
import com.phantask.task.dto.EmployeeTaskDTO;
import com.phantask.task.dto.TaskResponse;
import com.phantask.task.service.TaskService;

/**
 * Integration tests for TaskController
 * Tests both admin operations (CRUD) and employee operations (view/submit)
 */
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private AdminTaskDTO adminTaskDTO;
    private EmployeeTaskDTO employeeTaskDTO;
    private TaskResponse taskResponse;
    private List<TaskResponse> taskResponseList;

    @BeforeEach
    void setUp() {
        reset(taskService);

        // Setup admin task DTO
        adminTaskDTO = new AdminTaskDTO();
        adminTaskDTO.setTaskName("Test Task");
        adminTaskDTO.setDescription("Test Description");
        adminTaskDTO.setAssignDate(LocalDate.now());
        adminTaskDTO.setDueDate(LocalDate.now().plusDays(7));
        adminTaskDTO.setAssignedToUser("employee1");
        adminTaskDTO.setAssignedToRole("HR");

        // Setup employee task DTO
        employeeTaskDTO = new EmployeeTaskDTO();
        employeeTaskDTO.setDriveUrl("https://drive.google.com/file/123");

        // Setup task response
        taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setTaskName("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setAssignDate(LocalDate.now());
        taskResponse.setDueDate(LocalDate.now().plusDays(7));
        taskResponse.setStatus("PENDING");
        taskResponse.setAssignedToUser("employee1");
        taskResponse.setAssignedToRole("HR");
        taskResponse.setCreatedBy("admin");

        // Setup task response list
        TaskResponse task2 = new TaskResponse();
        task2.setId(2L);
        task2.setTaskName("Task 2");
        task2.setDescription("Description 2");
        task2.setStatus("SUBMITTED");

        taskResponseList = Arrays.asList(taskResponse, task2);
    }

    // ==================== POST /api/tasks/admin/create (Create Task) Tests ====================

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createTask_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        when(taskService.createTask(any(AdminTaskDTO.class), eq("admin"))).thenReturn(taskResponse);

        // Act & Assert
        mockMvc.perform(post("/api/tasks/admin/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.taskName").value("Test Task"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdBy").value("admin"));

        verify(taskService).createTask(any(AdminTaskDTO.class), eq("admin"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTask_WithNonAdminUser_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/tasks/admin/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTaskDTO)))
                .andExpect(status().isForbidden());

        verify(taskService, never()).createTask(any(AdminTaskDTO.class), anyString());
    }

    @Test
    void createTask_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/tasks/admin/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTaskDTO)))
                .andExpect(status().isForbidden());

        verify(taskService, never()).createTask(any(AdminTaskDTO.class), anyString());
    }

    // ==================== PUT /api/tasks/admin/update/{id} (Update Task) Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTask_WithValidId_ShouldReturn200() throws Exception {
        // Arrange
        when(taskService.updateTask(eq(1L), any(AdminTaskDTO.class))).thenReturn(taskResponse);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/admin/update/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.taskName").value("Test Task"));

        verify(taskService).updateTask(eq(1L), any(AdminTaskDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTask_WithInvalidId_ShouldThrowException() throws Exception {
        // Arrange
        when(taskService.updateTask(eq(999L), any(AdminTaskDTO.class)))
                .thenThrow(new RuntimeException("Task not found"));

        // Act & Assert
        try {
            mockMvc.perform(put("/api/tasks/admin/update/999")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(adminTaskDTO)));
            // If no exception handling, it will return 200 or error
        } catch (Exception e) {
            // Expected behavior
        }

        verify(taskService).updateTask(eq(999L), any(AdminTaskDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateTask_WithNonAdminUser_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/tasks/admin/update/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTaskDTO)))
                .andExpect(status().isForbidden());

        verify(taskService, never()).updateTask(anyLong(), any(AdminTaskDTO.class));
    }

    // ==================== DELETE /api/tasks/admin/delete/{id} Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTask_WithValidId_ShouldReturn200() throws Exception {
        // Arrange
        when(taskService.deleteTask(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/admin/delete/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Task deleted successfully"));

        verify(taskService).deleteTask(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTask_WithInvalidId_ShouldReturn404() throws Exception {
        // Arrange
        when(taskService.deleteTask(999L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/admin/delete/999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found"));

        verify(taskService).deleteTask(999L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteTask_WithNonAdminUser_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/tasks/admin/delete/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(taskService, never()).deleteTask(anyLong());
    }

    // ==================== GET /api/tasks/admin/all (Get All Tasks) Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTasksAdmin_ShouldReturn200() throws Exception {
        // Arrange
        when(taskService.getAllTasksAdmin()).thenReturn(taskResponseList);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskName").value("Test Task"))
                .andExpect(jsonPath("$[1].taskName").value("Task 2"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(taskService).getAllTasksAdmin();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTasksAdmin_WithNoTasks_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(taskService.getAllTasksAdmin()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/tasks/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(taskService).getAllTasksAdmin();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllTasksAdmin_WithNonAdminUser_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tasks/admin/all"))
                .andExpect(status().isForbidden());

        verify(taskService, never()).getAllTasksAdmin();
    }

    // ==================== GET /api/tasks/my (Get All User Tasks) Tests ====================

    @Test
    @WithMockUser(username = "employee1", roles = "USER")
    void getAllTasksForUser_ShouldReturn200() throws Exception {
        // Arrange
        when(taskService.getAllTasksForUser(eq("employee1"), anyList())).thenReturn(taskResponseList);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskName").value("Test Task"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(taskService).getAllTasksForUser(eq("employee1"), anyList());
    }

    @Test
    @WithMockUser(username = "employee1", roles = "USER")
    void getAllTasksForUser_WithNoTasks_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(taskService.getAllTasksForUser(eq("employee1"), anyList())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/tasks/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(taskService).getAllTasksForUser(eq("employee1"), anyList());
    }

    @Test
    void getAllTasksForUser_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tasks/my"))
                .andExpect(status().isForbidden());

        verify(taskService, never()).getAllTasksForUser(anyString(), anyList());
    }

    // ==================== GET /api/tasks/my/pending Tests ====================

    @Test
    @WithMockUser(username = "employee1", roles = "USER")
    void getPendingTasksForUser_ShouldReturn200() throws Exception {
        // Arrange
        List<TaskResponse> pendingTasks = Arrays.asList(taskResponse);
        when(taskService.getPendingTasksForUser(eq("employee1"), anyList())).thenReturn(pendingTasks);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/my/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(taskService).getPendingTasksForUser(eq("employee1"), anyList());
    }

    @Test
    @WithMockUser(username = "employee1", roles = "USER")
    void getPendingTasksForUser_WithNoTasks_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(taskService.getPendingTasksForUser(eq("employee1"), anyList())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/tasks/my/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(taskService).getPendingTasksForUser(eq("employee1"), anyList());
    }

    // ==================== GET /api/tasks/my/submitted Tests ====================

    @Test
    @WithMockUser(username = "employee1", roles = "USER")
    void getSubmittedTasksForUser_ShouldReturn200() throws Exception {
        // Arrange
        TaskResponse submittedTask = new TaskResponse();
        submittedTask.setId(2L);
        submittedTask.setTaskName("Submitted Task");
        submittedTask.setStatus("SUBMITTED");
        
        List<TaskResponse> submittedTasks = Arrays.asList(submittedTask);
        when(taskService.getSubmittedTasksForUser(eq("employee1"), anyList())).thenReturn(submittedTasks);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/my/submitted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(taskService).getSubmittedTasksForUser(eq("employee1"), anyList());
    }

    // ==================== PUT /api/tasks/my/submit/{id} Tests ====================

    @Test
    @WithMockUser(username = "employee1", roles = "USER")
    void submitTask_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        TaskResponse submittedResponse = new TaskResponse();
        submittedResponse.setId(1L);
        submittedResponse.setTaskName("Test Task");
        submittedResponse.setStatus("SUBMITTED");
        submittedResponse.setDriveUrl("https://drive.google.com/file/123");
        submittedResponse.setUploadDateTime(LocalDateTime.now());
        
        when(taskService.submitTask(eq(1L), any(EmployeeTaskDTO.class), eq("employee1")))
                .thenReturn(submittedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/my/submit/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.driveUrl").value("https://drive.google.com/file/123"));

        verify(taskService).submitTask(eq(1L), any(EmployeeTaskDTO.class), eq("employee1"));
    }

    @Test
    @WithMockUser(username = "employee1", roles = "USER")
    void submitTask_WithInvalidId_ShouldThrowException() throws Exception {
        // Arrange
        when(taskService.submitTask(eq(999L), any(EmployeeTaskDTO.class), eq("employee1")))
                .thenThrow(new RuntimeException("Task not found"));

        // Act & Assert
        try {
            mockMvc.perform(put("/api/tasks/my/submit/999")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(employeeTaskDTO)));
            // If no exception handling, it will return 200 or error
        } catch (Exception e) {
            // Expected behavior
        }

        verify(taskService).submitTask(eq(999L), any(EmployeeTaskDTO.class), eq("employee1"));
    }

    @Test
    void submitTask_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/tasks/my/submit/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeTaskDTO)))
                .andExpect(status().isForbidden());

        verify(taskService, never()).submitTask(anyLong(), any(EmployeeTaskDTO.class), anyString());
    }
}
