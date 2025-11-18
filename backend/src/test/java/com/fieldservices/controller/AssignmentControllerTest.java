package com.fieldservices.controller;

import com.fieldservices.dto.AssignmentRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.dto.TechnicianResponse;
import com.fieldservices.model.Task;
import com.fieldservices.model.User;
import com.fieldservices.service.AssignmentService;
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
class AssignmentControllerTest {

    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private AssignmentController assignmentController;

    private AssignmentRequest assignmentRequest;
    private TaskResponse taskResponse;
    private TechnicianResponse technicianResponse;

    @BeforeEach
    void setUp() {
        assignmentRequest = new AssignmentRequest();
        assignmentRequest.setTechnicianId(1L);

        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Task.Priority.HIGH)
                .status(Task.TaskStatus.ASSIGNED)
                .assignedTechnicianId(1L)
                .assignedTechnicianName("tech1")
                .assignedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        technicianResponse = TechnicianResponse.builder()
                .id(1L)
                .username("tech1")
                .email("tech1@example.com")
                .role(User.Role.TECHNICIAN)
                .available(true)
                .build();
    }

    @Test
    void assignTask_ValidRequest_ReturnsAssignedTask() {
        // Arrange
        when(assignmentService.assignTask(eq(1L), any(AssignmentRequest.class)))
                .thenReturn(taskResponse);

        // Act
        ResponseEntity<TaskResponse> response = assignmentController.assignTask(1L, assignmentRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(response.getBody().getAssignedTechnicianId()).isEqualTo(1L);
        assertThat(response.getBody().getAssignedTechnicianName()).isEqualTo("tech1");
        verify(assignmentService, times(1)).assignTask(eq(1L), any(AssignmentRequest.class));
    }

    @Test
    void assignTask_TaskNotFound_ThrowsException() {
        // Arrange
        when(assignmentService.assignTask(eq(999L), any(AssignmentRequest.class)))
                .thenThrow(new EntityNotFoundException("Task not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
                () -> assignmentController.assignTask(999L, assignmentRequest));
        verify(assignmentService, times(1)).assignTask(eq(999L), any(AssignmentRequest.class));
    }

    @Test
    void assignTask_TechnicianNotFound_ThrowsException() {
        // Arrange
        assignmentRequest.setTechnicianId(999L);
        when(assignmentService.assignTask(eq(1L), any(AssignmentRequest.class)))
                .thenThrow(new EntityNotFoundException("Technician not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
                () -> assignmentController.assignTask(1L, assignmentRequest));
        verify(assignmentService, times(1)).assignTask(eq(1L), any(AssignmentRequest.class));
    }

    @Test
    void assignTask_TaskAlreadyAssigned_ThrowsException() {
        // Arrange
        when(assignmentService.assignTask(eq(1L), any(AssignmentRequest.class)))
                .thenThrow(new IllegalStateException("Task is already assigned"));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> assignmentController.assignTask(1L, assignmentRequest));
        verify(assignmentService, times(1)).assignTask(eq(1L), any(AssignmentRequest.class));
    }

    @Test
    void assignTask_TechnicianNotAvailable_ThrowsException() {
        // Arrange
        when(assignmentService.assignTask(eq(1L), any(AssignmentRequest.class)))
                .thenThrow(new IllegalStateException("Technician is not available"));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> assignmentController.assignTask(1L, assignmentRequest));
        verify(assignmentService, times(1)).assignTask(eq(1L), any(AssignmentRequest.class));
    }

    @Test
    void getAvailableTechnicians_ReturnsList() {
        // Arrange
        TechnicianResponse tech2 = TechnicianResponse.builder()
                .id(2L)
                .username("tech2")
                .email("tech2@example.com")
                .role(User.Role.TECHNICIAN)
                .available(true)
                .build();

        List<TechnicianResponse> technicians = Arrays.asList(technicianResponse, tech2);
        when(assignmentService.getAvailableTechnicians()).thenReturn(technicians);

        // Act
        ResponseEntity<List<TechnicianResponse>> response = assignmentController.getAvailableTechnicians();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getUsername()).isEqualTo("tech1");
        assertThat(response.getBody().get(1).getUsername()).isEqualTo("tech2");
        assertThat(response.getBody().get(0).getAvailable()).isTrue();
        assertThat(response.getBody().get(1).getAvailable()).isTrue();
        verify(assignmentService, times(1)).getAvailableTechnicians();
    }

    @Test
    void getAvailableTechnicians_EmptyList() {
        // Arrange
        when(assignmentService.getAvailableTechnicians()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<TechnicianResponse>> response = assignmentController.getAvailableTechnicians();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
        verify(assignmentService, times(1)).getAvailableTechnicians();
    }
}
