package com.fieldservices.dto;

import com.fieldservices.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private String clientAddress;
    private Task.Priority priority;
    private Integer estimatedDuration;
    private Task.TaskStatus status;
    private Long assignedTechnicianId;
    private String assignedTechnicianName;
    private LocalDateTime assignedAt;
    private Long assignedById;
    private String assignedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse fromEntity(Task task) {
        TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .clientAddress(task.getClientAddress())
                .priority(task.getPriority())
                .estimatedDuration(task.getEstimatedDuration())
                .status(task.getStatus())
                .assignedAt(task.getAssignedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt());

        if (task.getAssignedTechnician() != null) {
            builder.assignedTechnicianId(task.getAssignedTechnician().getId())
                   .assignedTechnicianName(task.getAssignedTechnician().getUsername());
        }

        if (task.getAssignedBy() != null) {
            builder.assignedById(task.getAssignedBy().getId())
                   .assignedByName(task.getAssignedBy().getUsername());
        }

        return builder.build();
    }
}
