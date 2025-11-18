package com.fieldservices.dto;

import com.fieldservices.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    private String description;

    @NotBlank(message = "Client address is required")
    @Size(min = 5, max = 500, message = "Client address must be between 5 and 500 characters")
    private String clientAddress;

    @NotNull(message = "Priority is required")
    private Task.Priority priority;

    private Integer estimatedDuration;
}
