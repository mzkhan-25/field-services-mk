package com.fieldservices.repository;

import com.fieldservices.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Find the latest location for a specific user
     */
    Optional<Location> findFirstByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find locations within a time range
     */
    List<Location> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find latest location for each user (technicians)
     * This gets the most recent location per user
     */
    @Query("SELECT l FROM Location l WHERE l.timestamp = " +
           "(SELECT MAX(l2.timestamp) FROM Location l2 WHERE l2.userId = l.userId)")
    List<Location> findLatestLocationForEachUser();

    /**
     * Find locations for specific users
     */
    List<Location> findByUserIdIn(List<Long> userIds);

    /**
     * Find latest location for each user within a specific time range
     */
    @Query("SELECT l FROM Location l WHERE l.timestamp >= :since AND l.timestamp = " +
           "(SELECT MAX(l2.timestamp) FROM Location l2 WHERE l2.userId = l.userId AND l2.timestamp >= :since)")
    List<Location> findLatestLocationForEachUserSince(@Param("since") LocalDateTime since);
}
