package com.fieldservices.service;

import com.fieldservices.dto.TaskCompleteRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.model.Task;
import com.fieldservices.model.User;
import com.fieldservices.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private StatusService statusService;

    private Task assignedTask;
    private Task inProgressTask;
    private Task completedTask;
    private Task unassignedTask;
    private User technician;

    @BeforeEach
    void setUp() {
        technician = new User();
        technician.setId(1L);
        technician.setUsername("tech1");
        technician.setRole(User.Role.TECHNICIAN);

        assignedTask = new Task();
        assignedTask.setId(1L);
        assignedTask.setTitle("Assigned Task");
        assignedTask.setClientAddress("123 Main St");
        assignedTask.setPriority(Task.Priority.HIGH);
        assignedTask.setStatus(Task.TaskStatus.ASSIGNED);
        assignedTask.setAssignedTechnician(technician);

        inProgressTask = new Task();
        inProgressTask.setId(2L);
        inProgressTask.setTitle("In Progress Task");
        inProgressTask.setClientAddress("456 Oak Ave");
        inProgressTask.setPriority(Task.Priority.MEDIUM);
        inProgressTask.setStatus(Task.TaskStatus.IN_PROGRESS);
        inProgressTask.setAssignedTechnician(technician);
        inProgressTask.setStartedAt(LocalDateTime.now());

        completedTask = new Task();
        completedTask.setId(3L);
        completedTask.setTitle("Completed Task");
        completedTask.setClientAddress("789 Elm St");
        completedTask.setPriority(Task.Priority.LOW);
        completedTask.setStatus(Task.TaskStatus.COMPLETED);
        completedTask.setWorkSummary("Task completed successfully");
        completedTask.setCompletedAt(LocalDateTime.now());

        unassignedTask = new Task();
        unassignedTask.setId(4L);
        unassignedTask.setTitle("Unassigned Task");
        unassignedTask.setClientAddress("321 Pine Rd");
        unassignedTask.setPriority(Task.Priority.HIGH);
        unassignedTask.setStatus(Task.TaskStatus.UNASSIGNED);
    }

    @Test
    void testStartTask_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(assignedTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TaskResponse response = statusService.startTask(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Task.TaskStatus.IN_PROGRESS);
        assertThat(response.getStartedAt()).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testStartTask_TaskNotFound() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> statusService.startTask(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void testStartTask_InvalidStatus() {
        // Given
        when(taskRepository.findById(2L)).thenReturn(Optional.of(inProgressTask));

        // When/Then
        assertThatThrownBy(() -> statusService.startTask(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot start task with status IN_PROGRESS");
    }

    @Test
    void testStartTask_UnassignedTask() {
        // Given
        when(taskRepository.findById(4L)).thenReturn(Optional.of(unassignedTask));

        // When/Then
        assertThatThrownBy(() -> statusService.startTask(4L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot start task with status UNASSIGNED");
    }

    @Test
    void testCompleteTask_Success() {
        // Given
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setWorkSummary("Replaced the faulty component and tested the system. All working correctly now.");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(inProgressTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TaskResponse response = statusService.completeTask(2L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
        assertThat(response.getCompletedAt()).isNotNull();
        assertThat(response.getWorkSummary()).isEqualTo(request.getWorkSummary());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testCompleteTask_TaskNotFound() {
        // Given
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setWorkSummary("Work summary");

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> statusService.completeTask(999L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void testCompleteTask_InvalidStatus() {
        // Given
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setWorkSummary("Work summary");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(assignedTask));

        // When/Then
        assertThatThrownBy(() -> statusService.completeTask(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete task with status ASSIGNED");
    }

    @Test
    void testCompleteTask_EmptyWorkSummary() {
        // Given
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setWorkSummary("");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(inProgressTask));

        // When/Then
        assertThatThrownBy(() -> statusService.completeTask(2L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Work summary is required");
    }

    @Test
    void testCompleteTask_NullWorkSummary() {
        // Given
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setWorkSummary(null);

        when(taskRepository.findById(2L)).thenReturn(Optional.of(inProgressTask));

        // When/Then
        assertThatThrownBy(() -> statusService.completeTask(2L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Work summary is required");
    }

    @Test
    void testGetTaskStatus_Success() {
        // Given
        when(taskRepository.findById(2L)).thenReturn(Optional.of(inProgressTask));

        // When
        TaskResponse response = statusService.getTaskStatus(2L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo(Task.TaskStatus.IN_PROGRESS);
    }

    @Test
    void testGetTaskStatus_TaskNotFound() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> statusService.getTaskStatus(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void testCanTransitionTo_UnassignedToAssigned() {
        // When/Then
        assertThat(statusService.canTransitionTo(Task.TaskStatus.UNASSIGNED, Task.TaskStatus.ASSIGNED)).isTrue();
        assertThat(statusService.canTransitionTo(Task.TaskStatus.UNASSIGNED, Task.TaskStatus.IN_PROGRESS)).isFalse();
    }

    @Test
    void testCanTransitionTo_AssignedToInProgress() {
        // When/Then
        assertThat(statusService.canTransitionTo(Task.TaskStatus.ASSIGNED, Task.TaskStatus.IN_PROGRESS)).isTrue();
        assertThat(statusService.canTransitionTo(Task.TaskStatus.ASSIGNED, Task.TaskStatus.UNASSIGNED)).isTrue();
        assertThat(statusService.canTransitionTo(Task.TaskStatus.ASSIGNED, Task.TaskStatus.COMPLETED)).isFalse();
    }

    @Test
    void testCanTransitionTo_InProgressToCompleted() {
        // When/Then
        assertThat(statusService.canTransitionTo(Task.TaskStatus.IN_PROGRESS, Task.TaskStatus.COMPLETED)).isTrue();
        assertThat(statusService.canTransitionTo(Task.TaskStatus.IN_PROGRESS, Task.TaskStatus.ASSIGNED)).isTrue();
        assertThat(statusService.canTransitionTo(Task.TaskStatus.IN_PROGRESS, Task.TaskStatus.UNASSIGNED)).isFalse();
    }

    @Test
    void testCanTransitionTo_CompletedIsTerminal() {
        // When/Then
        assertThat(statusService.canTransitionTo(Task.TaskStatus.COMPLETED, Task.TaskStatus.IN_PROGRESS)).isFalse();
        assertThat(statusService.canTransitionTo(Task.TaskStatus.COMPLETED, Task.TaskStatus.ASSIGNED)).isFalse();
        assertThat(statusService.canTransitionTo(Task.TaskStatus.COMPLETED, Task.TaskStatus.UNASSIGNED)).isFalse();
    }

    @Test
    void testCanTransitionTo_CancelledIsTerminal() {
        // When/Then
        assertThat(statusService.canTransitionTo(Task.TaskStatus.CANCELLED, Task.TaskStatus.IN_PROGRESS)).isFalse();
        assertThat(statusService.canTransitionTo(Task.TaskStatus.CANCELLED, Task.TaskStatus.ASSIGNED)).isFalse();
    }
}
