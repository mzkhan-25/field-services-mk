package com.fieldservices.repository;

import com.fieldservices.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all unassigned tasks sorted by priority (HIGH, MEDIUM, LOW)
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTechnician IS NULL " +
           "AND t.status = 'UNASSIGNED' " +
           "ORDER BY CASE t.priority " +
           "  WHEN 'HIGH' THEN 1 " +
           "  WHEN 'MEDIUM' THEN 2 " +
           "  WHEN 'LOW' THEN 3 " +
           "END")
    List<Task> findUnassignedTasksSortedByPriority();

    /**
     * Find tasks by status
     */
    List<Task> findByStatus(Task.TaskStatus status);

    /**
     * Find tasks assigned to a specific technician
     */
    List<Task> findByAssignedTechnicianId(Long technicianId);
}
