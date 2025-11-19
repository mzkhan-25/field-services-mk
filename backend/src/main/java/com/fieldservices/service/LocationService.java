package com.fieldservices.service;

import com.fieldservices.dto.LocationResponse;
import com.fieldservices.dto.LocationUpdateRequest;
import com.fieldservices.model.Location;
import com.fieldservices.model.Task;
import com.fieldservices.model.User;
import com.fieldservices.repository.LocationRepository;
import com.fieldservices.repository.TaskRepository;
import com.fieldservices.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Throttle map to track last update time per user
    private final Map<Long, LocalDateTime> lastUpdateMap = new ConcurrentHashMap<>();
    private static final int THROTTLE_SECONDS = 30;

    /**
     * Update technician location with throttling
     */
    @Transactional
    public LocationResponse updateLocation(LocationUpdateRequest request) {
        log.info("Updating location for user: {}", request.getUserId());

        // Verify user exists and is a technician
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));

        if (user.getRole() != User.Role.TECHNICIAN) {
            throw new IllegalArgumentException("Only technicians can update location");
        }

        // Check throttling
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdate = lastUpdateMap.get(request.getUserId());

        if (lastUpdate != null) {
            long secondsSinceLastUpdate = java.time.Duration.between(lastUpdate, now).getSeconds();
            if (secondsSinceLastUpdate < THROTTLE_SECONDS) {
                long remainingSeconds = THROTTLE_SECONDS - secondsSinceLastUpdate;
                throw new IllegalStateException(
                    String.format("Location update throttled. Please wait %d seconds.", remainingSeconds)
                );
            }
        }

        // Create and save location
        Location location = new Location();
        location.setUserId(request.getUserId());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAccuracy(request.getAccuracy());
        location.setTimestamp(now);

        Location savedLocation = locationRepository.save(location);
        lastUpdateMap.put(request.getUserId(), now);

        log.info("Location updated successfully for user: {}", request.getUserId());
        
        // Broadcast location update via WebSocket
        LocationResponse response = LocationResponse.fromEntity(savedLocation);
        messagingTemplate.convertAndSend("/topic/locations", response);
        
        return response;
    }

    /**
     * Get all active technician locations (latest location for each technician)
     */
    @Transactional(readOnly = true)
    public List<LocationResponse> getAllTechnicianLocations() {
        log.info("Fetching all technician locations");

        // Get all technicians
        List<Long> technicianIds = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.TECHNICIAN)
                .map(User::getId)
                .collect(Collectors.toList());

        if (technicianIds.isEmpty()) {
            return List.of();
        }

        // Get latest locations within last 5 minutes to show "active" technicians
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return locationRepository.findLatestLocationForEachUserSince(fiveMinutesAgo).stream()
                .filter(location -> technicianIds.contains(location.getUserId()))
                .map(LocationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all task locations (for unassigned and in-progress tasks)
     */
    @Transactional(readOnly = true)
    public List<TaskLocationResponse> getAllTaskLocations() {
        log.info("Fetching all task locations");

        // Get unassigned and in-progress tasks that have addresses
        List<Task> tasks = taskRepository.findAll().stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.UNASSIGNED ||
                               task.getStatus() == Task.TaskStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        // Convert to location responses (using geocoding placeholder)
        // In a real implementation, you would geocode the addresses
        return tasks.stream()
                .map(this::convertTaskToLocation)
                .collect(Collectors.toList());
    }

    /**
     * Get location for a specific user
     */
    @Transactional(readOnly = true)
    public Optional<LocationResponse> getLocationByUserId(Long userId) {
        log.info("Fetching location for user: {}", userId);
        return locationRepository.findFirstByUserIdOrderByTimestampDesc(userId)
                .map(LocationResponse::fromEntity);
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Convert task to location response (placeholder for geocoding)
     * In a real implementation, this would geocode the address
     */
    private TaskLocationResponse convertTaskToLocation(Task task) {
        // For now, return a placeholder location
        // In production, you would use a geocoding service like Google Maps API
        return TaskLocationResponse.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .address(task.getClientAddress())
                .status(task.getStatus())
                .priority(task.getPriority())
                .latitude(null) // Would be geocoded
                .longitude(null) // Would be geocoded
                .build();
    }

    /**
     * DTO for task location responses
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TaskLocationResponse {
        private Long taskId;
        private String title;
        private String address;
        private Task.TaskStatus status;
        private Task.Priority priority;
        private Double latitude;
        private Double longitude;
    }
}
