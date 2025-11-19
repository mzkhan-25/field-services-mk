package com.fieldservices.repository;

import com.fieldservices.model.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void testSaveAndFindLocation() {
        // Given
        Location location = new Location();
        location.setUserId(1L);
        location.setLatitude(40.7128);
        location.setLongitude(-74.0060);
        location.setAccuracy(10.0);
        location.setTimestamp(LocalDateTime.now());

        // When
        Location saved = locationRepository.save(location);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getLatitude()).isEqualTo(40.7128);
        assertThat(saved.getLongitude()).isEqualTo(-74.0060);
        assertThat(saved.getAccuracy()).isEqualTo(10.0);
    }

    @Test
    void testFindFirstByUserIdOrderByTimestampDesc() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Location older = new Location();
        older.setUserId(1L);
        older.setLatitude(40.7128);
        older.setLongitude(-74.0060);
        older.setTimestamp(now.minusMinutes(10));
        locationRepository.save(older);

        Location newer = new Location();
        newer.setUserId(1L);
        newer.setLatitude(40.7500);
        newer.setLongitude(-73.9900);
        newer.setTimestamp(now);
        locationRepository.save(newer);

        // When
        Optional<Location> result = locationRepository.findFirstByUserIdOrderByTimestampDesc(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getLatitude()).isEqualTo(40.7500);
    }

    @Test
    void testFindByTimestampBetween() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Location location1 = new Location();
        location1.setUserId(1L);
        location1.setLatitude(40.7128);
        location1.setLongitude(-74.0060);
        location1.setTimestamp(now.minusHours(2));
        locationRepository.save(location1);

        Location location2 = new Location();
        location2.setUserId(1L);
        location2.setLatitude(40.7500);
        location2.setLongitude(-73.9900);
        location2.setTimestamp(now.minusMinutes(30));
        locationRepository.save(location2);

        Location location3 = new Location();
        location3.setUserId(1L);
        location3.setLatitude(40.7600);
        location3.setLongitude(-73.9800);
        location3.setTimestamp(now);
        locationRepository.save(location3);

        // When
        List<Location> results = locationRepository.findByTimestampBetween(now.minusHours(1), now.plusMinutes(1));

        // Then
        assertThat(results).hasSize(2);
    }

    @Test
    void testFindLatestLocationForEachUser() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // User 1 - two locations
        Location user1Old = new Location();
        user1Old.setUserId(1L);
        user1Old.setLatitude(40.7128);
        user1Old.setLongitude(-74.0060);
        user1Old.setTimestamp(now.minusMinutes(10));
        locationRepository.save(user1Old);

        Location user1New = new Location();
        user1New.setUserId(1L);
        user1New.setLatitude(40.7500);
        user1New.setLongitude(-73.9900);
        user1New.setTimestamp(now);
        locationRepository.save(user1New);

        // User 2 - one location
        Location user2 = new Location();
        user2.setUserId(2L);
        user2.setLatitude(40.7600);
        user2.setLongitude(-73.9800);
        user2.setTimestamp(now.minusMinutes(5));
        locationRepository.save(user2);

        // When
        List<Location> results = locationRepository.findLatestLocationForEachUser();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Location::getUserId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void testFindLatestLocationForEachUserSince() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // User 1 - recent location
        Location user1Recent = new Location();
        user1Recent.setUserId(1L);
        user1Recent.setLatitude(40.7500);
        user1Recent.setLongitude(-73.9900);
        user1Recent.setTimestamp(now.minusMinutes(2));
        locationRepository.save(user1Recent);

        // User 2 - old location
        Location user2Old = new Location();
        user2Old.setUserId(2L);
        user2Old.setLatitude(40.7600);
        user2Old.setLongitude(-73.9800);
        user2Old.setTimestamp(now.minusMinutes(10));
        locationRepository.save(user2Old);

        // When
        List<Location> results = locationRepository.findLatestLocationForEachUserSince(now.minusMinutes(5));

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void testFindByUserIdIn() {
        // Given
        Location location1 = new Location();
        location1.setUserId(1L);
        location1.setLatitude(40.7128);
        location1.setLongitude(-74.0060);
        location1.setTimestamp(LocalDateTime.now());
        locationRepository.save(location1);

        Location location2 = new Location();
        location2.setUserId(2L);
        location2.setLatitude(40.7500);
        location2.setLongitude(-73.9900);
        location2.setTimestamp(LocalDateTime.now());
        locationRepository.save(location2);

        Location location3 = new Location();
        location3.setUserId(3L);
        location3.setLatitude(40.7600);
        location3.setLongitude(-73.9800);
        location3.setTimestamp(LocalDateTime.now());
        locationRepository.save(location3);

        // When
        List<Location> results = locationRepository.findByUserIdIn(List.of(1L, 3L));

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Location::getUserId).containsExactlyInAnyOrder(1L, 3L);
    }
}
