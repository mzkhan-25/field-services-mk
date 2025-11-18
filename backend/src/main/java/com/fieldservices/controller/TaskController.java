package com.fieldservices.controller;

import com.fieldservices.dto.TaskRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Task Management
 * 
 * Endpoints:
 * - POST /api/tasks - Create a new task (DISPATCHER, SUPERVISOR)
 * - GET /api/tasks - List all tasks (all authenticated users)
 * - GET /api/tasks/unassigned - List unassigned tasks sorted by priority (all authenticated users)
 * - GET /api/tasks/{id} - Get task details (all authenticated users)
 * - PUT /api/tasks/{id} - Update task details (DISPATCHER, SUPERVISOR)
 */
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    /**
     * Create a new task
     * Accessible by: DISPATCHER, SUPERVISOR
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'SUPERVISOR')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        log.info("Received request to create task: {}", request.getTitle());
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all tasks
     * Accessible by: all authenticated users
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        log.info("Received request to get all tasks");
        List<TaskResponse> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get unassigned tasks sorted by priority
     * Accessible by: all authenticated users
     */
    @GetMapping("/unassigned")
    public ResponseEntity<List<TaskResponse>> getUnassignedTasks() {
        log.info("Received request to get unassigned tasks");
        List<TaskResponse> tasks = taskService.getUnassignedTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get task by ID
     * Accessible by: all authenticated users
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        log.info("Received request to get task with id: {}", id);
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    /**
     * Update task
     * Accessible by: DISPATCHER, SUPERVISOR
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'SUPERVISOR')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        log.info("Received request to update task with id: {}", id);
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }
}
