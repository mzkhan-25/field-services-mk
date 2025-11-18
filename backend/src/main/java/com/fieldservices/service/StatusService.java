package com.fieldservices.service;

import com.fieldservices.dto.TaskCompleteRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.model.Task;
import com.fieldservices.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusService {

    private final TaskRepository taskRepository;

    /**
     * Mark task as in progress
     * Only assigned tasks can be started
     */
    @Transactional
    public TaskResponse startTask(Long taskId) {
        log.info("Starting task with id: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        // Validate status transition
        if (task.getStatus() != Task.TaskStatus.ASSIGNED) {
            throw new IllegalStateException(
                String.format("Cannot start task with status %s. Only ASSIGNED tasks can be started.", 
                    task.getStatus())
            );
        }

        // Update task status
        task.setStatus(Task.TaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        log.info("Task {} marked as IN_PROGRESS", taskId);

        return TaskResponse.fromEntity(updatedTask);
    }

    /**
     * Mark task as completed with work summary
     * Only in-progress tasks can be completed
     */
    @Transactional
    public TaskResponse completeTask(Long taskId, TaskCompleteRequest request) {
        log.info("Completing task with id: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        // Validate status transition
        if (task.getStatus() != Task.TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                String.format("Cannot complete task with status %s. Only IN_PROGRESS tasks can be completed.", 
                    task.getStatus())
            );
        }

        // Validate work summary is provided
        if (request.getWorkSummary() == null || request.getWorkSummary().trim().isEmpty()) {
            throw new IllegalArgumentException("Work summary is required for task completion");
        }

        // Update task status
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setWorkSummary(request.getWorkSummary());

        Task updatedTask = taskRepository.save(task);
        log.info("Task {} marked as COMPLETED", taskId);

        return TaskResponse.fromEntity(updatedTask);
    }

    /**
     * Get current status of a task
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskStatus(Long taskId) {
        log.info("Fetching status for task with id: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        return TaskResponse.fromEntity(task);
    }

    /**
     * Validate if a task can transition to a new status
     */
    public boolean canTransitionTo(Task.TaskStatus currentStatus, Task.TaskStatus newStatus) {
        return switch (currentStatus) {
            case UNASSIGNED -> newStatus == Task.TaskStatus.ASSIGNED;
            case ASSIGNED -> newStatus == Task.TaskStatus.IN_PROGRESS || newStatus == Task.TaskStatus.UNASSIGNED;
            case IN_PROGRESS -> newStatus == Task.TaskStatus.COMPLETED || newStatus == Task.TaskStatus.ASSIGNED;
            case COMPLETED, CANCELLED -> false; // Terminal states
        };
    }
}
