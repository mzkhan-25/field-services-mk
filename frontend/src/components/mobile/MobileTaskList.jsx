import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import MobileTaskCard from './MobileTaskCard';
import '../../styles/MobileTaskList.css';

/**
 * MobileTaskList Component
 * Displays a touch-optimized list of tasks for mobile devices
 */
const MobileTaskList = ({ tasks, onTaskClick, onRefresh, isLoading, error }) => {
  const [sortedTasks, setSortedTasks] = useState([]);
  const [isPulling, setIsPulling] = useState(false);
  const [pullDistance, setPullDistance] = useState(0);
  const [touchStart, setTouchStart] = useState(0);

  // Priority order for sorting
  const priorityOrder = { HIGH: 1, MEDIUM: 2, LOW: 3 };

  useEffect(() => {
    // Sort tasks by priority and time
    const sorted = [...tasks].sort((a, b) => {
      const priorityA = priorityOrder[a.priority] || 2;
      const priorityB = priorityOrder[b.priority] || 2;
      if (priorityA !== priorityB) {
        return priorityA - priorityB;
      }
      // If priority is same, sort by creation time (assuming newer tasks first)
      return (b.id || 0) - (a.id || 0);
    });
    setSortedTasks(sorted);
  }, [tasks]);

  // Pull-to-refresh handlers
  const handleTouchStart = (e) => {
    if (window.scrollY === 0) {
      setTouchStart(e.touches[0].clientY);
    }
  };

  const handleTouchMove = (e) => {
    if (touchStart > 0 && window.scrollY === 0) {
      const currentTouch = e.touches[0].clientY;
      const distance = currentTouch - touchStart;
      if (distance > 0 && distance < 150) {
        setPullDistance(distance);
        setIsPulling(true);
      }
    }
  };

  const handleTouchEnd = () => {
    if (isPulling && pullDistance > 80) {
      if (onRefresh) {
        onRefresh();
      }
    }
    setIsPulling(false);
    setPullDistance(0);
    setTouchStart(0);
  };

  if (error) {
    return (
      <div className="mobile-task-list-container">
        <div className="mobile-task-list-error">
          <p className="error-message">Error: {error}</p>
          {onRefresh && (
            <button onClick={onRefresh} className="mobile-refresh-button">
              Try Again
            </button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div
      className="mobile-task-list-container"
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
    >
      {isPulling && (
        <div
          className="pull-refresh-indicator"
          style={{ height: `${pullDistance}px` }}
        >
          <span className="pull-refresh-text">
            {pullDistance > 80 ? 'Release to refresh' : 'Pull to refresh'}
          </span>
        </div>
      )}

      <div className="mobile-task-list-header">
        <h2>My Tasks</h2>
        {onRefresh && !isLoading && (
          <button onClick={onRefresh} className="mobile-refresh-button-icon">
            ↻
          </button>
        )}
      </div>

      {isLoading ? (
        <div className="mobile-task-list-loading">
          <div className="loading-spinner"></div>
          <p>Loading tasks...</p>
        </div>
      ) : sortedTasks.length === 0 ? (
        <div className="mobile-task-list-empty">
          <p>No assigned tasks at the moment.</p>
        </div>
      ) : (
        <div className="mobile-task-list-items">
          {sortedTasks.map((task) => (
            <MobileTaskCard
              key={task.id}
              task={task}
              onClick={() => onTaskClick && onTaskClick(task)}
            />
          ))}
        </div>
      )}
    </div>
  );
};

MobileTaskList.propTypes = {
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
  onTaskClick: PropTypes.func,
  onRefresh: PropTypes.func,
  isLoading: PropTypes.bool,
  error: PropTypes.string,
};

export default MobileTaskList;
