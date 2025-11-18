package com.fieldservices.service;

import com.fieldservices.dto.TaskRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.model.Task;
import com.fieldservices.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Create a new task
     */
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating new task with title: {}", request.getTitle());

        // Validate address
        validateAddress(request.getClientAddress());

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setClientAddress(request.getClientAddress());
        task.setPriority(request.getPriority());
        task.setEstimatedDuration(request.getEstimatedDuration());
        task.setStatus(Task.TaskStatus.UNASSIGNED);

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with id: {}", savedTask.getId());

        return TaskResponse.fromEntity(savedTask);
    }

    /**
     * Get all tasks
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        log.info("Fetching all tasks");
        return taskRepository.findAll().stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.info("Fetching task with id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        return TaskResponse.fromEntity(task);
    }

    /**
     * Get all unassigned tasks sorted by priority
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getUnassignedTasks() {
        log.info("Fetching unassigned tasks sorted by priority");
        return taskRepository.findUnassignedTasksSortedByPriority().stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update task
     */
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        // Validate address if it's being updated
        if (request.getClientAddress() != null && !request.getClientAddress().equals(task.getClientAddress())) {
            validateAddress(request.getClientAddress());
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getClientAddress() != null) {
            task.setClientAddress(request.getClientAddress());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getEstimatedDuration() != null) {
            task.setEstimatedDuration(request.getEstimatedDuration());
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully with id: {}", updatedTask.getId());

        return TaskResponse.fromEntity(updatedTask);
    }

    /**
     * Validate address format
     * Basic validation: address should contain letters, numbers, and common address characters
     */
    private void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }

        // Basic address validation - must contain at least one digit (house/building number)
        // and one letter (street name)
        boolean hasDigit = address.chars().anyMatch(Character::isDigit);
        boolean hasLetter = address.chars().anyMatch(Character::isLetter);

        if (!hasDigit || !hasLetter) {
            throw new IllegalArgumentException("Invalid address format. Address must contain both letters and numbers.");
        }

        // Check for minimum meaningful content (not just special characters)
        String cleaned = address.replaceAll("[^a-zA-Z0-9]", "");
        if (cleaned.length() < 3) {
            throw new IllegalArgumentException("Address must contain meaningful content");
        }

        log.debug("Address validation passed for: {}", address);
    }
}
