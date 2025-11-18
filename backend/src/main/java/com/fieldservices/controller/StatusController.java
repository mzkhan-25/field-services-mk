package com.fieldservices.controller;

import com.fieldservices.dto.TaskCompleteRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.service.StatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Task Status Management
 * 
 * Endpoints:
 * - PUT /api/tasks/{id}/start - Mark task as in progress (TECHNICIAN)
 * - PUT /api/tasks/{id}/complete - Mark task as completed (TECHNICIAN)
 * - GET /api/tasks/{id}/status - Get current status (all authenticated users)
 */
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
public class StatusController {

    private final StatusService statusService;

    /**
     * Mark task as in progress
     * Accessible by: TECHNICIAN
     * Only assigned tasks can be started
     */
    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<TaskResponse> startTask(@PathVariable Long id) {
        log.info("Received request to start task with id: {}", id);
        TaskResponse response = statusService.startTask(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark task as completed with work summary
     * Accessible by: TECHNICIAN
     * Only in-progress tasks can be completed
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskCompleteRequest request) {
        log.info("Received request to complete task with id: {}", id);
        TaskResponse response = statusService.completeTask(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current status of a task
     * Accessible by: all authenticated users
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<TaskResponse> getTaskStatus(@PathVariable Long id) {
        log.info("Received request to get status for task with id: {}", id);
        TaskResponse response = statusService.getTaskStatus(id);
        return ResponseEntity.ok(response);
    }
}
