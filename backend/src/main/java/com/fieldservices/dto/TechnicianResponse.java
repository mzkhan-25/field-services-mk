package com.fieldservices.dto;

import com.fieldservices.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianResponse {

    private Long id;
    private String username;
    private String email;
    private User.Role role;
    private Boolean available;

    public static TechnicianResponse fromUser(User user) {
        return TechnicianResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .available(user.getActive()) // Using active status as availability for now
                .build();
    }
}
