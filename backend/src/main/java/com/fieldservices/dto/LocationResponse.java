package com.fieldservices.dto;

import com.fieldservices.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationResponse {

    private Long id;
    private Long userId;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private LocalDateTime timestamp;

    public static LocationResponse fromEntity(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .userId(location.getUserId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .accuracy(location.getAccuracy())
                .timestamp(location.getTimestamp())
                .build();
    }
}
