import React from 'react';
import PropTypes from 'prop-types';
import '../styles/TaskCard.css';

/**
 * TaskCard Component
 * Displays a single task with its details
 */
const TaskCard = ({ task }) => {
  const priorityClass = `priority-${task.priority?.toLowerCase() || 'medium'}`;

  return (
    <div className={`task-card ${priorityClass}`}>
      <div className="task-card-header">
        <h3 className="task-title">{task.title}</h3>
        <span className={`priority-badge ${priorityClass}`}>
          {task.priority || 'MEDIUM'}
        </span>
      </div>
      <div className="task-card-body">
        {task.description && (
          <p className="task-description">{task.description}</p>
        )}
        <div className="task-info">
          <div className="info-item">
            <span className="info-label">Address:</span>
            <span className="info-value">{task.clientAddress}</span>
          </div>
          {task.estimatedDuration && (
            <div className="info-item">
              <span className="info-label">Duration:</span>
              <span className="info-value">{task.estimatedDuration} min</span>
            </div>
          )}
          <div className="info-item">
            <span className="info-label">Status:</span>
            <span className="info-value">{task.status || 'UNASSIGNED'}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

TaskCard.propTypes = {
  task: PropTypes.shape({
    id: PropTypes.number,
    title: PropTypes.string.isRequired,
    description: PropTypes.string,
    clientAddress: PropTypes.string.isRequired,
    priority: PropTypes.oneOf(['HIGH', 'MEDIUM', 'LOW']),
    estimatedDuration: PropTypes.number,
    status: PropTypes.string,
  }).isRequired,
};

export default TaskCard;
