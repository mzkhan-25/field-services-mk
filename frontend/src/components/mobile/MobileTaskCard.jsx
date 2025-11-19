import React from 'react';
import PropTypes from 'prop-types';
import '../../styles/MobileTaskCard.css';

/**
 * MobileTaskCard Component
 * Touch-friendly task card for mobile devices
 */
const MobileTaskCard = ({ task, onClick }) => {
  const priorityColors = {
    HIGH: '#F44336',
    MEDIUM: '#FF9800',
    LOW: '#2196F3',
  };

  const statusColors = {
    PENDING: '#9E9E9E',
    IN_PROGRESS: '#FF9800',
    COMPLETED: '#4CAF50',
  };

  return (
    <div
      className="mobile-task-card"
      onClick={onClick}
      role="button"
      tabIndex={0}
      onKeyPress={(e) => {
        if (e.key === 'Enter' && onClick) {
          onClick();
        }
      }}
    >
      <div className="mobile-task-card-header">
        <h3 className="mobile-task-title">{task.title}</h3>
        <span
          className="mobile-task-priority"
          style={{ backgroundColor: priorityColors[task.priority] || priorityColors.MEDIUM }}
        >
          {task.priority || 'MEDIUM'}
        </span>
      </div>

      {task.status && (
        <div className="mobile-task-status">
          <span
            className="status-badge"
            style={{ backgroundColor: statusColors[task.status] || statusColors.PENDING }}
          >
            {task.status.replace('_', ' ')}
          </span>
        </div>
      )}

      <div className="mobile-task-address">
        <span className="address-icon">📍</span>
        {task.clientAddress}
      </div>

      {task.description && (
        <p className="mobile-task-description">{task.description}</p>
      )}

      <div className="mobile-task-footer">
        {task.estimatedDuration && (
          <span className="mobile-task-duration">
            ⏱️ {task.estimatedDuration} min
          </span>
        )}
        <span className="mobile-task-arrow">›</span>
      </div>
    </div>
  );
};

MobileTaskCard.propTypes = {
  task: PropTypes.shape({
    id: PropTypes.number.isRequired,
    title: PropTypes.string.isRequired,
    description: PropTypes.string,
    clientAddress: PropTypes.string.isRequired,
    priority: PropTypes.oneOf(['HIGH', 'MEDIUM', 'LOW']),
    estimatedDuration: PropTypes.number,
    status: PropTypes.string,
  }).isRequired,
  onClick: PropTypes.func,
};

export default MobileTaskCard;
