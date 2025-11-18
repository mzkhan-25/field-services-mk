package com.fieldservices.controller;

import com.fieldservices.dto.AssignmentRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.dto.TechnicianResponse;
import com.fieldservices.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Task Assignment
 * 
 * Endpoints:
 * - POST /api/tasks/{id}/assign - Assign task to technician (DISPATCHER, SUPERVISOR)
 * - GET /api/technicians/available - Get available technicians (DISPATCHER, SUPERVISOR)
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * Assign a task to a technician
     * Accessible by: DISPATCHER, SUPERVISOR
     */
    @PostMapping("/tasks/{id}/assign")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'SUPERVISOR')")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest request) {
        log.info("Received request to assign task {} to technician {}", id, request.getTechnicianId());
        TaskResponse response = assignmentService.assignTask(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all available technicians
     * Accessible by: DISPATCHER, SUPERVISOR
     */
    @GetMapping("/technicians/available")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'SUPERVISOR')")
    public ResponseEntity<List<TechnicianResponse>> getAvailableTechnicians() {
        log.info("Received request to get available technicians");
        List<TechnicianResponse> technicians = assignmentService.getAvailableTechnicians();
        return ResponseEntity.ok(technicians);
    }
}
