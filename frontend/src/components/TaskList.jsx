import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import TaskCard from './TaskCard';
import '../styles/TaskList.css';

/**
 * TaskList Component
 * Displays a list of tasks with filtering and sorting by priority
 */
const TaskList = ({ tasks, onRefresh, isLoading, error }) => {
  const [filterPriority, setFilterPriority] = useState('ALL');
  const [sortedTasks, setSortedTasks] = useState([]);

  // Priority order for sorting
  const priorityOrder = { HIGH: 1, MEDIUM: 2, LOW: 3 };

  useEffect(() => {
    let filtered = [...tasks];

    // Filter by priority
    if (filterPriority !== 'ALL') {
      filtered = filtered.filter((task) => task.priority === filterPriority);
    }

    // Sort by priority (HIGH -> MEDIUM -> LOW)
    filtered.sort((a, b) => {
      const priorityA = priorityOrder[a.priority] || 2;
      const priorityB = priorityOrder[b.priority] || 2;
      return priorityA - priorityB;
    });

    setSortedTasks(filtered);
  }, [tasks, filterPriority]);

  const handleFilterChange = (e) => {
    setFilterPriority(e.target.value);
  };

  if (error) {
    return (
      <div className="task-list-container">
        <div className="task-list-error">
          <p className="error-message">Error loading tasks: {error}</p>
          {onRefresh && (
            <button onClick={onRefresh} className="refresh-button">
              Try Again
            </button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="task-list-container">
      <div className="task-list-header">
        <h2>Unassigned Tasks</h2>
        <div className="task-list-controls">
          <div className="filter-group">
            <label htmlFor="priority-filter">Filter by Priority:</label>
            <select
              id="priority-filter"
              value={filterPriority}
              onChange={handleFilterChange}
              className="priority-filter"
            >
              <option value="ALL">All Priorities</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>
          {onRefresh && (
            <button onClick={onRefresh} className="refresh-button" disabled={isLoading}>
              {isLoading ? 'Loading...' : 'Refresh'}
            </button>
          )}
        </div>
      </div>

      {isLoading ? (
        <div className="task-list-loading">
          <p>Loading tasks...</p>
        </div>
      ) : sortedTasks.length === 0 ? (
        <div className="task-list-empty">
          <p>
            {filterPriority === 'ALL'
              ? 'No unassigned tasks at the moment.'
              : `No unassigned tasks with ${filterPriority} priority.`}
          </p>
        </div>
      ) : (
        <div className="task-list-grid">
          {sortedTasks.map((task) => (
            <TaskCard key={task.id} task={task} />
          ))}
        </div>
      )}
    </div>
  );
};

TaskList.propTypes = {
  tasks: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      title: PropTypes.string.isRequired,
      description: PropTypes.string,
      clientAddress: PropTypes.string.isRequired,
      priority: PropTypes.oneOf(['HIGH', 'MEDIUM', 'LOW']),
      estimatedDuration: PropTypes.number,
      status: PropTypes.string,
    })
  ).isRequired,
  onRefresh: PropTypes.func,
  isLoading: PropTypes.bool,
  error: PropTypes.string,
};

TaskList.defaultProps = {
  onRefresh: null,
  isLoading: false,
  error: null,
};

export default TaskList;
