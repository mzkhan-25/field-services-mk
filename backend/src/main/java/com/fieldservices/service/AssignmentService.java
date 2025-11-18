package com.fieldservices.service;

import com.fieldservices.dto.AssignmentRequest;
import com.fieldservices.dto.TaskResponse;
import com.fieldservices.dto.TechnicianResponse;
import com.fieldservices.model.Task;
import com.fieldservices.model.User;
import com.fieldservices.repository.TaskRepository;
import com.fieldservices.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Assign a task to a technician
     */
    @Transactional
    public TaskResponse assignTask(Long taskId, AssignmentRequest request) {
        log.info("Assigning task {} to technician {}", taskId, request.getTechnicianId());

        // Get the task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        // Check if task is already assigned
        if (task.getAssignedTechnician() != null) {
            throw new IllegalStateException("Task is already assigned to technician: " + 
                    task.getAssignedTechnician().getUsername());
        }

        // Get the technician
        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new EntityNotFoundException("Technician not found with id: " + request.getTechnicianId()));

        // Verify the user is a technician
        if (technician.getRole() != User.Role.TECHNICIAN) {
            throw new IllegalArgumentException("User is not a technician");
        }

        // Check if technician is available (active)
        if (!technician.getActive()) {
            throw new IllegalStateException("Technician is not available");
        }

        // Get the dispatcher who is assigning the task
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User dispatcher = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        // Assign the task
        task.setAssignedTechnician(technician);
        task.setAssignedAt(LocalDateTime.now());
        task.setAssignedBy(dispatcher);
        task.setStatus(Task.TaskStatus.ASSIGNED);

        Task assignedTask = taskRepository.save(task);
        log.info("Task {} assigned successfully to technician {}", taskId, technician.getUsername());

        // TODO: Trigger notification to technician and customer
        log.info("TODO: Send notification to technician {} and customer", technician.getUsername());

        return TaskResponse.fromEntity(assignedTask);
    }

    /**
     * Get all available technicians
     */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> getAvailableTechnicians() {
        log.info("Fetching available technicians");
        
        List<User> technicians = userRepository.findByRoleAndActive(User.Role.TECHNICIAN, true);
        
        return technicians.stream()
                .map(TechnicianResponse::fromUser)
                .collect(Collectors.toList());
    }
}
