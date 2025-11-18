package com.fieldservices.controller;

import com.fieldservices.dto.LocationResponse;
import com.fieldservices.dto.LocationUpdateRequest;
import com.fieldservices.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private LocationUpdateRequest testRequest;
    private LocationResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new LocationUpdateRequest();
        testRequest.setUserId(1L);
        testRequest.setLatitude(40.7128);
        testRequest.setLongitude(-74.0060);
        testRequest.setAccuracy(10.0);

        testResponse = LocationResponse.builder()
                .id(1L)
                .userId(1L)
                .latitude(40.7128)
                .longitude(-74.0060)
                .accuracy(10.0)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void testUpdateLocation_Success() {
        // Given
        when(locationService.updateLocation(any(LocationUpdateRequest.class))).thenReturn(testResponse);

        // When
        ResponseEntity<LocationResponse> response = locationController.updateLocation(testRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo(1L);
        assertThat(response.getBody().getLatitude()).isEqualTo(40.7128);
        assertThat(response.getBody().getLongitude()).isEqualTo(-74.0060);
    }

    @Test
    void testUpdateLocation_Throttled() {
        // Given
        when(locationService.updateLocation(any(LocationUpdateRequest.class)))
                .thenThrow(new IllegalStateException("Location update throttled. Please wait 20 seconds."));

        // When
        ResponseEntity<LocationResponse> response = locationController.updateLocation(testRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void testUpdateLocation_InvalidRequest() {
        // Given
        when(locationService.updateLocation(any(LocationUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        // When
        ResponseEntity<LocationResponse> response = locationController.updateLocation(testRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testGetAllTechnicianLocations() {
        // Given
        LocationResponse response1 = LocationResponse.builder()
                .id(1L)
                .userId(1L)
                .latitude(40.7128)
                .longitude(-74.0060)
                .timestamp(LocalDateTime.now())
                .build();

        LocationResponse response2 = LocationResponse.builder()
                .id(2L)
                .userId(2L)
                .latitude(40.7500)
                .longitude(-73.9900)
                .timestamp(LocalDateTime.now())
                .build();

        List<LocationResponse> responses = Arrays.asList(response1, response2);
        when(locationService.getAllTechnicianLocations()).thenReturn(responses);

        // When
        ResponseEntity<List<LocationResponse>> response = locationController.getAllTechnicianLocations();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getUserId()).isEqualTo(1L);
        assertThat(response.getBody().get(1).getUserId()).isEqualTo(2L);
    }

    @Test
    void testGetAllTaskLocations() {
        // Given
        LocationService.TaskLocationResponse response1 = LocationService.TaskLocationResponse.builder()
                .taskId(1L)
                .title("Task 1")
                .address("123 Main St")
                .build();

        LocationService.TaskLocationResponse response2 = LocationService.TaskLocationResponse.builder()
                .taskId(2L)
                .title("Task 2")
                .address("456 Oak Ave")
                .build();

        List<LocationService.TaskLocationResponse> responses = Arrays.asList(response1, response2);
        when(locationService.getAllTaskLocations()).thenReturn(responses);

        // When
        ResponseEntity<List<LocationService.TaskLocationResponse>> response = locationController.getAllTaskLocations();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getTaskId()).isEqualTo(1L);
        assertThat(response.getBody().get(1).getTaskId()).isEqualTo(2L);
    }
}
