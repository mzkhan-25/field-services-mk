package com.fieldservices.repository;

import com.fieldservices.model.Task;
import com.fieldservices.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Task unassignedHighTask;
    private Task unassignedMediumTask;
    private Task unassignedLowTask;
    private Task assignedTask;
    private User technician;

    @BeforeEach
    void setUp() {
        // Create a technician
        technician = new User();
        technician.setUsername("tech1");
        technician.setPassword("password");
        technician.setEmail("tech1@example.com");
        technician.setRole(User.Role.TECHNICIAN);
        technician.setActive(true);
        entityManager.persist(technician);

        // Create unassigned tasks
        unassignedHighTask = createTask("High Priority Task", Task.Priority.HIGH, null);
        unassignedMediumTask = createTask("Medium Priority Task", Task.Priority.MEDIUM, null);
        unassignedLowTask = createTask("Low Priority Task", Task.Priority.LOW, null);

        // Create assigned task
        assignedTask = createTask("Assigned Task", Task.Priority.HIGH, technician);
        assignedTask.setStatus(Task.TaskStatus.ASSIGNED);

        entityManager.persist(unassignedHighTask);
        entityManager.persist(unassignedMediumTask);
        entityManager.persist(unassignedLowTask);
        entityManager.persist(assignedTask);
        entityManager.flush();
    }

    private Task createTask(String title, Task.Priority priority, User assignedTechnician) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description for " + title);
        task.setClientAddress("123 Test St, City");
        task.setPriority(priority);
        task.setEstimatedDuration(60);
        task.setStatus(Task.TaskStatus.UNASSIGNED);
        task.setAssignedTechnician(assignedTechnician);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    @Test
    void findUnassignedTasksSortedByPriority_ReturnsOnlyUnassignedTasksSortedByPriority() {
        // Act
        List<Task> tasks = taskRepository.findUnassignedTasksSortedByPriority();

        // Assert
        assertNotNull(tasks);
        assertEquals(3, tasks.size());

        // Verify sorting: HIGH -> MEDIUM -> LOW
        assertEquals(Task.Priority.HIGH, tasks.get(0).getPriority());
        assertEquals(Task.Priority.MEDIUM, tasks.get(1).getPriority());
        assertEquals(Task.Priority.LOW, tasks.get(2).getPriority());

        // Verify all are unassigned
        tasks.forEach(task -> {
            assertNull(task.getAssignedTechnician());
            assertEquals(Task.TaskStatus.UNASSIGNED, task.getStatus());
        });
    }

    @Test
    void findByStatus_UnassignedStatus_ReturnsUnassignedTasks() {
        // Act
        List<Task> tasks = taskRepository.findByStatus(Task.TaskStatus.UNASSIGNED);

        // Assert
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        tasks.forEach(task -> assertEquals(Task.TaskStatus.UNASSIGNED, task.getStatus()));
    }

    @Test
    void findByStatus_AssignedStatus_ReturnsAssignedTasks() {
        // Act
        List<Task> tasks = taskRepository.findByStatus(Task.TaskStatus.ASSIGNED);

        // Assert
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(Task.TaskStatus.ASSIGNED, tasks.get(0).getStatus());
    }

    @Test
    void findByAssignedTechnicianId_ReturnsTechniciansTasks() {
        // Act
        List<Task> tasks = taskRepository.findByAssignedTechnicianId(technician.getId());

        // Assert
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(assignedTask.getId(), tasks.get(0).getId());
        assertEquals(technician.getId(), tasks.get(0).getAssignedTechnician().getId());
    }

    @Test
    void findByAssignedTechnicianId_NonExistingTechnician_ReturnsEmptyList() {
        // Act
        List<Task> tasks = taskRepository.findByAssignedTechnicianId(999L);

        // Assert
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void save_NewTask_PersistsTask() {
        // Arrange
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");
        newTask.setClientAddress("456 New Ave");
        newTask.setPriority(Task.Priority.MEDIUM);
        newTask.setEstimatedDuration(90);
        newTask.setStatus(Task.TaskStatus.UNASSIGNED);
        newTask.setCreatedAt(LocalDateTime.now());
        newTask.setUpdatedAt(LocalDateTime.now());

        // Act
        Task savedTask = taskRepository.save(newTask);

        // Assert
        assertNotNull(savedTask.getId());
        assertEquals("New Task", savedTask.getTitle());
        
        Task foundTask = entityManager.find(Task.class, savedTask.getId());
        assertNotNull(foundTask);
        assertEquals("New Task", foundTask.getTitle());
    }

    @Test
    void findById_ExistingId_ReturnsTask() {
        // Act
        Task foundTask = taskRepository.findById(unassignedHighTask.getId()).orElse(null);

        // Assert
        assertNotNull(foundTask);
        assertEquals(unassignedHighTask.getId(), foundTask.getId());
        assertEquals(unassignedHighTask.getTitle(), foundTask.getTitle());
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        // Act
        var result = taskRepository.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }
}
