package com.fieldservices.service;

import com.fieldservices.dto.LocationResponse;
import com.fieldservices.dto.LocationUpdateRequest;
import com.fieldservices.model.Location;
import com.fieldservices.model.Task;
import com.fieldservices.model.User;
import com.fieldservices.repository.LocationRepository;
import com.fieldservices.repository.TaskRepository;
import com.fieldservices.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private LocationService locationService;

    private User technicianUser;
    private User dispatcherUser;

    @BeforeEach
    void setUp() {
        technicianUser = new User();
        technicianUser.setId(1L);
        technicianUser.setUsername("tech1");
        technicianUser.setRole(User.Role.TECHNICIAN);

        dispatcherUser = new User();
        dispatcherUser.setId(2L);
        dispatcherUser.setUsername("dispatcher1");
        dispatcherUser.setRole(User.Role.DISPATCHER);
    }

    @Test
    void testUpdateLocation_Success() {
        // Given
        LocationUpdateRequest request = new LocationUpdateRequest();
        request.setUserId(1L);
        request.setLatitude(40.7128);
        request.setLongitude(-74.0060);
        request.setAccuracy(10.0);

        Location savedLocation = new Location();
        savedLocation.setId(1L);
        savedLocation.setUserId(request.getUserId());
        savedLocation.setLatitude(request.getLatitude());
        savedLocation.setLongitude(request.getLongitude());
        savedLocation.setAccuracy(request.getAccuracy());
        savedLocation.setTimestamp(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(technicianUser));
        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);

        // When
        LocationResponse response = locationService.updateLocation(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getLatitude()).isEqualTo(40.7128);
        assertThat(response.getLongitude()).isEqualTo(-74.0060);
        verify(locationRepository).save(any(Location.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/locations"), any(LocationResponse.class));
    }

    @Test
    void testUpdateLocation_UserNotFound() {
        // Given
        LocationUpdateRequest request = new LocationUpdateRequest();
        request.setUserId(999L);
        request.setLatitude(40.7128);
        request.setLongitude(-74.0060);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> locationService.updateLocation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testUpdateLocation_NotTechnician() {
        // Given
        LocationUpdateRequest request = new LocationUpdateRequest();
        request.setUserId(2L);
        request.setLatitude(40.7128);
        request.setLongitude(-74.0060);

        when(userRepository.findById(2L)).thenReturn(Optional.of(dispatcherUser));

        // When/Then
        assertThatThrownBy(() -> locationService.updateLocation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only technicians can update location");
    }

    @Test
    void testUpdateLocation_Throttled() throws InterruptedException {
        // Given
        LocationUpdateRequest request = new LocationUpdateRequest();
        request.setUserId(1L);
        request.setLatitude(40.7128);
        request.setLongitude(-74.0060);

        Location savedLocation = new Location();
        savedLocation.setId(1L);
        savedLocation.setUserId(1L);
        savedLocation.setLatitude(40.7128);
        savedLocation.setLongitude(-74.0060);
        savedLocation.setTimestamp(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(technicianUser));
        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);

        // When - first update succeeds
        locationService.updateLocation(request);

        // When/Then - second update within 30 seconds is throttled
        assertThatThrownBy(() -> locationService.updateLocation(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Location update throttled");

        verify(locationRepository, times(1)).save(any(Location.class));
    }

    @Test
    void testGetAllTechnicianLocations() {
        // Given
        Location location = new Location();
        location.setId(1L);
        location.setUserId(1L);
        location.setLatitude(40.7128);
        location.setLongitude(-74.0060);
        location.setTimestamp(LocalDateTime.now().minusMinutes(2));

        when(userRepository.findAll()).thenReturn(Arrays.asList(technicianUser, dispatcherUser));
        when(locationRepository.findLatestLocationForEachUserSince(any(LocalDateTime.class)))
                .thenReturn(List.of(location));

        // When
        List<LocationResponse> responses = locationService.getAllTechnicianLocations();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void testGetAllTechnicianLocations_NoTechnicians() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of(dispatcherUser));

        // When
        List<LocationResponse> responses = locationService.getAllTechnicianLocations();

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    void testGetAllTaskLocations() {
        // Given
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setClientAddress("123 Main St");
        task1.setStatus(Task.TaskStatus.UNASSIGNED);
        task1.setPriority(Task.Priority.HIGH);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setClientAddress("456 Oak Ave");
        task2.setStatus(Task.TaskStatus.IN_PROGRESS);
        task2.setPriority(Task.Priority.MEDIUM);

        Task task3 = new Task();
        task3.setId(3L);
        task3.setTitle("Task 3");
        task3.setClientAddress("789 Elm St");
        task3.setStatus(Task.TaskStatus.COMPLETED);
        task3.setPriority(Task.Priority.LOW);

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2, task3));

        // When
        List<LocationService.TaskLocationResponse> responses = locationService.getAllTaskLocations();

        // Then
        assertThat(responses).hasSize(2); // Only UNASSIGNED and IN_PROGRESS
        assertThat(responses).extracting(LocationService.TaskLocationResponse::getTaskId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void testGetLocationByUserId() {
        // Given
        Location location = new Location();
        location.setId(1L);
        location.setUserId(1L);
        location.setLatitude(40.7128);
        location.setLongitude(-74.0060);
        location.setTimestamp(LocalDateTime.now());

        when(locationRepository.findFirstByUserIdOrderByTimestampDesc(1L))
                .thenReturn(Optional.of(location));

        // When
        Optional<LocationResponse> response = locationService.getLocationByUserId(1L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getUserId()).isEqualTo(1L);
    }

    @Test
    void testGetLocationByUserId_NotFound() {
        // Given
        when(locationRepository.findFirstByUserIdOrderByTimestampDesc(999L))
                .thenReturn(Optional.empty());

        // When
        Optional<LocationResponse> response = locationService.getLocationByUserId(999L);

        // Then
        assertThat(response).isEmpty();
    }

    @Test
    void testCalculateDistance() {
        // Given - New York to Los Angeles (approx 3940 km)
        double lat1 = 40.7128;
        double lon1 = -74.0060;
        double lat2 = 34.0522;
        double lon2 = -118.2437;

        // When
        double distance = locationService.calculateDistance(lat1, lon1, lat2, lon2);

        // Then - Allow for some margin of error
        assertThat(distance).isBetween(3900.0, 4000.0);
    }

    @Test
    void testCalculateDistance_SameLocation() {
        // Given
        double lat = 40.7128;
        double lon = -74.0060;

        // When
        double distance = locationService.calculateDistance(lat, lon, lat, lon);

        // Then
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void testCalculateDistance_ShortDistance() {
        // Given - Two nearby locations (approx 1 km apart)
        double lat1 = 40.7128;
        double lon1 = -74.0060;
        double lat2 = 40.7228;
        double lon2 = -74.0060;

        // When
        double distance = locationService.calculateDistance(lat1, lon1, lat2, lon2);

        // Then
        assertThat(distance).isGreaterThan(0.0);
        assertThat(distance).isLessThan(15.0); // Should be around 11 km
    }
}
