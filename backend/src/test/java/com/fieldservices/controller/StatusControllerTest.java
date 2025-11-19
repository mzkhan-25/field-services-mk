package com.fieldservices.controller;

import com.fieldservices.dto.TaskCompleteRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.model.Task;
import com.fieldservices.service.StatusService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {

    @Mock
    private StatusService statusService;

    @InjectMocks
    private StatusController statusController;

    private TaskResponse testResponse;

    @BeforeEach
    void setUp() {
        testResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Task.Priority.HIGH)
                .status(Task.TaskStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testStartTask_Success() {
        // Given
        when(statusService.startTask(1L)).thenReturn(testResponse);

        // When
        ResponseEntity<TaskResponse> response = statusController.startTask(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(Task.TaskStatus.IN_PROGRESS);
    }

    @Test
    void testStartTask_TaskNotFound() {
        // Given
        when(statusService.startTask(999L))
                .thenThrow(new EntityNotFoundException("Task not found with id: 999"));

        // When/Then
        assertThatThrownBy(() -> statusController.startTask(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void testStartTask_InvalidStatus() {
        // Given
        when(statusService.startTask(1L))
                .thenThrow(new IllegalStateException("Cannot start task with status IN_PROGRESS"));

        // When/Then
        assertThatThrownBy(() -> statusController.startTask(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot start task");
    }

    @Test
    void testCompleteTask_Success() {
        // Given
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setWorkSummary("Replaced the faulty component and tested the system.");

        TaskResponse completedResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Task.Priority.HIGH)
                .status(Task.TaskStatus.COMPLETED)
                .workSummary(request.getWorkSummary())
                .completedAt(LocalDateTime.now())
                .build();

        when(statusService.completeTask(eq(1L), any(TaskCompleteRequest.class)))
                .thenReturn(completedResponse);

        // When
        ResponseEntity<TaskResponse> response = statusController.completeTask(1L, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
        assertThat(response.getBody().getWorkSummary()).isEqualTo(request.getWorkSummary());
    }

    @Test
    void testCompleteTask_TaskNotFound() {
        // Given
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setWorkSummary("Work summary");

        when(statusService.completeTask(eq(999L), any(TaskCompleteRequest.class)))
                .thenThrow(new EntityNotFoundException("Task not found with id: 999"));

        // When/Then
        assertThatThrownBy(() -> statusController.completeTask(999L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void testCompleteTask_InvalidStatus() {
        // Given
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setWorkSummary("Work summary");

        when(statusService.completeTask(eq(1L), any(TaskCompleteRequest.class)))
                .thenThrow(new IllegalStateException("Cannot complete task with status ASSIGNED"));

        // When/Then
        assertThatThrownBy(() -> statusController.completeTask(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete task");
    }

    @Test
    void testGetTaskStatus_Success() {
        // Given
        when(statusService.getTaskStatus(1L)).thenReturn(testResponse);

        // When
        ResponseEntity<TaskResponse> response = statusController.getTaskStatus(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getStatus()).isEqualTo(Task.TaskStatus.IN_PROGRESS);
    }

    @Test
    void testGetTaskStatus_TaskNotFound() {
        // Given
        when(statusService.getTaskStatus(999L))
                .thenThrow(new EntityNotFoundException("Task not found with id: 999"));

        // When/Then
        assertThatThrownBy(() -> statusController.getTaskStatus(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task not found");
    }
}
