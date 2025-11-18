package com.fieldservices.controller;

import com.fieldservices.dto.TaskRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.model.Task;
import com.fieldservices.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private TaskRequest testRequest;
    private TaskResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new TaskRequest();
        testRequest.setTitle("Test Task");
        testRequest.setDescription("Test Description");
        testRequest.setClientAddress("123 Main St, City");
        testRequest.setPriority(Task.Priority.HIGH);
        testRequest.setEstimatedDuration(60);

        testResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Main St, City")
                .priority(Task.Priority.HIGH)
                .estimatedDuration(60)
                .status(Task.TaskStatus.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTask_ValidRequest_ReturnsCreated() {
        // Arrange
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(testResponse);

        // Act
        ResponseEntity<TaskResponse> response = taskController.createTask(testRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getTitle()).isEqualTo("Test Task");
        assertThat(response.getBody().getPriority()).isEqualTo(Task.Priority.HIGH);
        assertThat(response.getBody().getStatus()).isEqualTo(Task.TaskStatus.UNASSIGNED);
        verify(taskService, times(1)).createTask(any(TaskRequest.class));
    }

    @Test
    void getAllTasks_ReturnsTaskList() {
        // Arrange
        TaskResponse task2 = TaskResponse.builder()
                .id(2L)
                .title("Task 2")
                .clientAddress("456 Oak Ave")
                .priority(Task.Priority.MEDIUM)
                .status(Task.TaskStatus.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<TaskResponse> tasks = Arrays.asList(testResponse, task2);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // Act
        ResponseEntity<List<TaskResponse>> response = taskController.getAllTasks();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getId()).isEqualTo(1L);
        assertThat(response.getBody().get(1).getId()).isEqualTo(2L);
        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void getUnassignedTasks_ReturnsSortedList() {
        // Arrange
        List<TaskResponse> tasks = Arrays.asList(testResponse);
        when(taskService.getUnassignedTasks()).thenReturn(tasks);

        // Act
        ResponseEntity<List<TaskResponse>> response = taskController.getUnassignedTasks();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getPriority()).isEqualTo(Task.Priority.HIGH);
        verify(taskService, times(1)).getUnassignedTasks();
    }

    @Test
    void getTaskById_ExistingId_ReturnsTask() {
        // Arrange
        when(taskService.getTaskById(1L)).thenReturn(testResponse);

        // Act
        ResponseEntity<TaskResponse> response = taskController.getTaskById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getTitle()).isEqualTo("Test Task");
        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    void getTaskById_NonExistingId_ThrowsException() {
        // Arrange
        when(taskService.getTaskById(999L))
                .thenThrow(new EntityNotFoundException("Task not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> taskController.getTaskById(999L));
        verify(taskService, times(1)).getTaskById(999L);
    }

    @Test
    void updateTask_ValidRequest_ReturnsUpdated() {
        // Arrange
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setClientAddress("789 Pine Rd");
        updateRequest.setPriority(Task.Priority.LOW);

        TaskResponse updatedResponse = TaskResponse.builder()
                .id(1L)
                .title("Updated Task")
                .clientAddress("789 Pine Rd")
                .priority(Task.Priority.LOW)
                .status(Task.TaskStatus.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(updatedResponse);

        // Act
        ResponseEntity<TaskResponse> response = taskController.updateTask(1L, updateRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Task");
        assertThat(response.getBody().getPriority()).isEqualTo(Task.Priority.LOW);
        verify(taskService, times(1)).updateTask(eq(1L), any(TaskRequest.class));
    }

    @Test
    void updateTask_NonExistingId_ThrowsException() {
        // Arrange
        when(taskService.updateTask(eq(999L), any(TaskRequest.class)))
                .thenThrow(new EntityNotFoundException("Task not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> taskController.updateTask(999L, testRequest));
        verify(taskService, times(1)).updateTask(eq(999L), any(TaskRequest.class));
    }
}
