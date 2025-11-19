package com.fieldservices.controller;

import com.fieldservices.dto.LocationResponse;
import com.fieldservices.dto.LocationUpdateRequest;
import com.fieldservices.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Location Tracking
 * 
 * Endpoints:
 * - POST /api/locations - Update technician location (TECHNICIAN)
 * - GET /api/locations/technicians - Get all technician locations (all authenticated users)
 * - GET /api/locations/tasks - Get all task locations (all authenticated users)
 */
@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    /**
     * Update technician location
     * Accessible by: TECHNICIAN
     * Throttled to one update per 30 seconds
     */
    @PostMapping
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<LocationResponse> updateLocation(@Valid @RequestBody LocationUpdateRequest request) {
        log.info("Received request to update location for user: {}", request.getUserId());
        try {
            LocationResponse response = locationService.updateLocation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            // Throttling error
            log.warn("Location update throttled: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        } catch (IllegalArgumentException e) {
            // Validation error
            log.warn("Invalid location update request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all active technician locations
     * Accessible by: all authenticated users
     * Returns latest location for each technician (within last 5 minutes)
     */
    @GetMapping("/technicians")
    public ResponseEntity<List<LocationResponse>> getAllTechnicianLocations() {
        log.info("Received request to get all technician locations");
        List<LocationResponse> locations = locationService.getAllTechnicianLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * Get all task locations
     * Accessible by: all authenticated users
     * Returns locations for unassigned and in-progress tasks
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<LocationService.TaskLocationResponse>> getAllTaskLocations() {
        log.info("Received request to get all task locations");
        List<LocationService.TaskLocationResponse> locations = locationService.getAllTaskLocations();
        return ResponseEntity.ok(locations);
    }
}
