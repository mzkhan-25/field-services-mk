package com.fieldservices.service;

import com.fieldservices.dto.AssignmentRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.dto.TechnicianResponse;
import com.fieldservices.model.Task;
import com.fieldservices.model.User;
import com.fieldservices.repository.TaskRepository;
import com.fieldservices.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AssignmentService assignmentService;

    private Task unassignedTask;
    private User technician;
    private User dispatcher;
    private AssignmentRequest assignmentRequest;

    @BeforeEach
    void setUp() {
        // Setup technician
        technician = new User();
        technician.setId(1L);
        technician.setUsername("tech1");
        technician.setEmail("tech1@example.com");
        technician.setPassword("password");
        technician.setRole(User.Role.TECHNICIAN);
        technician.setActive(true);

        // Setup dispatcher
        dispatcher = new User();
        dispatcher.setId(2L);
        dispatcher.setUsername("dispatcher1");
        dispatcher.setEmail("dispatcher1@example.com");
        dispatcher.setPassword("password");
        dispatcher.setRole(User.Role.DISPATCHER);
        dispatcher.setActive(true);

        // Setup unassigned task
        unassignedTask = new Task();
        unassignedTask.setId(1L);
        unassignedTask.setTitle("Test Task");
        unassignedTask.setDescription("Test Description");
        unassignedTask.setClientAddress("123 Main St");
        unassignedTask.setPriority(Task.Priority.HIGH);
        unassignedTask.setStatus(Task.TaskStatus.UNASSIGNED);
        unassignedTask.setCreatedAt(LocalDateTime.now());
        unassignedTask.setUpdatedAt(LocalDateTime.now());

        // Setup assignment request
        assignmentRequest = new AssignmentRequest();
        assignmentRequest.setTechnicianId(1L);
    }

    private void setupSecurityContext() {
        // Setup security context
        Authentication auth = new UsernamePasswordAuthenticationToken(dispatcher.getUsername(), null);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void assignTask_ValidRequest_AssignsTaskSuccessfully() {
        // Arrange
        setupSecurityContext();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(technician));
        when(userRepository.findByUsername("dispatcher1")).thenReturn(Optional.of(dispatcher));
        when(taskRepository.save(any(Task.class))).thenReturn(unassignedTask);

        // Act
        TaskResponse response = assignmentService.assignTask(1L, assignmentRequest);

        // Assert
        assertNotNull(response);
        assertEquals(Task.TaskStatus.ASSIGNED, unassignedTask.getStatus());
        assertNotNull(unassignedTask.getAssignedAt());
        assertEquals(technician, unassignedTask.getAssignedTechnician());
        assertEquals(dispatcher, unassignedTask.getAssignedBy());
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void assignTask_TaskNotFound_ThrowsException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
                () -> assignmentService.assignTask(999L, assignmentRequest));
        verify(taskRepository, times(1)).findById(999L);
        verify(userRepository, never()).findById(any());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTask_TaskAlreadyAssigned_ThrowsException() {
        // Arrange
        unassignedTask.setAssignedTechnician(technician);
        unassignedTask.setStatus(Task.TaskStatus.ASSIGNED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> assignmentService.assignTask(1L, assignmentRequest));
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, never()).findById(any());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTask_TechnicianNotFound_ThrowsException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        assignmentRequest.setTechnicianId(999L);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
                () -> assignmentService.assignTask(1L, assignmentRequest));
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTask_UserIsNotTechnician_ThrowsException() {
        // Arrange
        User nonTechnician = new User();
        nonTechnician.setId(3L);
        nonTechnician.setUsername("supervisor1");
        nonTechnician.setRole(User.Role.SUPERVISOR);
        nonTechnician.setActive(true);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(userRepository.findById(3L)).thenReturn(Optional.of(nonTechnician));
        
        assignmentRequest.setTechnicianId(3L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> assignmentService.assignTask(1L, assignmentRequest));
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(3L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTask_TechnicianNotAvailable_ThrowsException() {
        // Arrange
        technician.setActive(false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(technician));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> assignmentService.assignTask(1L, assignmentRequest));
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getAvailableTechnicians_ReturnsList() {
        // Arrange
        User tech2 = new User();
        tech2.setId(2L);
        tech2.setUsername("tech2");
        tech2.setEmail("tech2@example.com");
        tech2.setRole(User.Role.TECHNICIAN);
        tech2.setActive(true);

        List<User> technicians = Arrays.asList(technician, tech2);
        when(userRepository.findByRoleAndActive(User.Role.TECHNICIAN, true))
                .thenReturn(technicians);

        // Act
        List<TechnicianResponse> response = assignmentService.getAvailableTechnicians();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("tech1", response.get(0).getUsername());
        assertEquals("tech2", response.get(1).getUsername());
        assertTrue(response.get(0).getAvailable());
        assertTrue(response.get(1).getAvailable());
        verify(userRepository, times(1)).findByRoleAndActive(User.Role.TECHNICIAN, true);
    }

    @Test
    void getAvailableTechnicians_EmptyList() {
        // Arrange
        when(userRepository.findByRoleAndActive(User.Role.TECHNICIAN, true))
                .thenReturn(Arrays.asList());

        // Act
        List<TechnicianResponse> response = assignmentService.getAvailableTechnicians();

        // Assert
        assertNotNull(response);
        assertEquals(0, response.size());
        verify(userRepository, times(1)).findByRoleAndActive(User.Role.TECHNICIAN, true);
    }
}
