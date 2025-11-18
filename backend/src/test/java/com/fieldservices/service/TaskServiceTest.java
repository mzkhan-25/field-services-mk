package com.fieldservices.service;

import com.fieldservices.dto.TaskRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.model.Task;
import com.fieldservices.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private TaskRequest testRequest;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setClientAddress("123 Main St, City");
        testTask.setPriority(Task.Priority.HIGH);
        testTask.setEstimatedDuration(60);
        testTask.setStatus(Task.TaskStatus.UNASSIGNED);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());

        testRequest = new TaskRequest();
        testRequest.setTitle("Test Task");
        testRequest.setDescription("Test Description");
        testRequest.setClientAddress("123 Main St, City");
        testRequest.setPriority(Task.Priority.HIGH);
        testRequest.setEstimatedDuration(60);
    }

    @Test
    void createTask_ValidRequest_ReturnsTaskResponse() {
        // Arrange
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        TaskResponse response = taskService.createTask(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testTask.getId(), response.getId());
        assertEquals(testTask.getTitle(), response.getTitle());
        assertEquals(testTask.getClientAddress(), response.getClientAddress());
        assertEquals(testTask.getPriority(), response.getPriority());
        assertEquals(Task.TaskStatus.UNASSIGNED, response.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_InvalidAddress_ThrowsException() {
        // Arrange
        testRequest.setClientAddress("InvalidAddress"); // No numbers

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(testRequest));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_AddressWithoutLetters_ThrowsException() {
        // Arrange
        testRequest.setClientAddress("12345"); // No letters

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(testRequest));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_EmptyAddress_ThrowsException() {
        // Arrange
        testRequest.setClientAddress("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(testRequest));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getAllTasks_ReturnsList() {
        // Arrange
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setClientAddress("456 Oak Ave, Town");
        task2.setPriority(Task.Priority.MEDIUM);
        task2.setStatus(Task.TaskStatus.UNASSIGNED);
        task2.setCreatedAt(LocalDateTime.now());
        task2.setUpdatedAt(LocalDateTime.now());

        when(taskRepository.findAll()).thenReturn(Arrays.asList(testTask, task2));

        // Act
        List<TaskResponse> tasks = taskService.getAllTasks();

        // Assert
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getTaskById_ExistingId_ReturnsTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act
        TaskResponse response = taskService.getTaskById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(testTask.getId(), response.getId());
        assertEquals(testTask.getTitle(), response.getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_NonExistingId_ThrowsException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> taskService.getTaskById(999L));
        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    void getUnassignedTasks_ReturnsSortedList() {
        // Arrange
        Task highPriorityTask = new Task();
        highPriorityTask.setId(1L);
        highPriorityTask.setTitle("High Priority Task");
        highPriorityTask.setClientAddress("123 Main St");
        highPriorityTask.setPriority(Task.Priority.HIGH);
        highPriorityTask.setStatus(Task.TaskStatus.UNASSIGNED);
        highPriorityTask.setCreatedAt(LocalDateTime.now());
        highPriorityTask.setUpdatedAt(LocalDateTime.now());

        Task lowPriorityTask = new Task();
        lowPriorityTask.setId(2L);
        lowPriorityTask.setTitle("Low Priority Task");
        lowPriorityTask.setClientAddress("456 Oak Ave");
        lowPriorityTask.setPriority(Task.Priority.LOW);
        lowPriorityTask.setStatus(Task.TaskStatus.UNASSIGNED);
        lowPriorityTask.setCreatedAt(LocalDateTime.now());
        lowPriorityTask.setUpdatedAt(LocalDateTime.now());

        when(taskRepository.findUnassignedTasksSortedByPriority())
                .thenReturn(Arrays.asList(highPriorityTask, lowPriorityTask));

        // Act
        List<TaskResponse> tasks = taskService.getUnassignedTasks();

        // Assert
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertEquals(Task.Priority.HIGH, tasks.get(0).getPriority());
        assertEquals(Task.Priority.LOW, tasks.get(1).getPriority());
        verify(taskRepository, times(1)).findUnassignedTasksSortedByPriority();
    }

    @Test
    void updateTask_ValidRequest_ReturnsUpdatedTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription("Updated Description");
        updateRequest.setClientAddress("789 Pine Rd, Village");
        updateRequest.setPriority(Task.Priority.LOW);
        updateRequest.setEstimatedDuration(90);

        // Act
        TaskResponse response = taskService.updateTask(1L, updateRequest);

        // Assert
        assertNotNull(response);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_NonExistingId_ThrowsException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> taskService.updateTask(999L, testRequest));
        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_InvalidAddress_ThrowsException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setClientAddress("NoNumbers"); // Invalid address

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(1L, updateRequest));
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_PartialUpdate_UpdatesOnlyProvidedFields() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Title Only");

        // Act
        TaskResponse response = taskService.updateTask(1L, updateRequest);

        // Assert
        assertNotNull(response);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }
}
