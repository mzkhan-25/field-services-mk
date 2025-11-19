package com.fieldservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompleteRequest {

    @NotBlank(message = "Work summary is required")
    @Size(min = 10, max = 2000, message = "Work summary must be between 10 and 2000 characters")
    private String workSummary;
}
