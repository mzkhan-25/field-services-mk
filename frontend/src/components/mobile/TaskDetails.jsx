import React, { useState } from 'react';
import PropTypes from 'prop-types';
import '../../styles/TaskDetails.css';

/**
 * TaskDetails Component
 * Displays full task information with action buttons
 */
const TaskDetails = ({ 
  task, 
  onStatusChange, 
  onNavigate, 
  onBack,
  isUpdating 
}) => {
  const [workSummary, setWorkSummary] = useState('');
  const [showWorkSummary, setShowWorkSummary] = useState(false);

  const handleStartTask = () => {
    if (onStatusChange) {
      onStatusChange(task.id, 'IN_PROGRESS');
    }
  };

  const handleCompleteTask = () => {
    setShowWorkSummary(true);
  };

  const handleSubmitCompletion = () => {
    if (onStatusChange) {
      onStatusChange(task.id, 'COMPLETED', workSummary);
      setShowWorkSummary(false);
      setWorkSummary('');
    }
  };

  const handleNavigateToTask = () => {
    if (onNavigate) {
      onNavigate(task.clientAddress);
    } else {
      // Fallback to default map apps
      const address = encodeURIComponent(task.clientAddress);
      const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
      const url = isIOS
        ? `maps://maps.apple.com/?q=${address}`
        : `https://www.google.com/maps/search/?api=1&query=${address}`;
      window.open(url, '_blank');
    }
  };

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

  if (showWorkSummary) {
    return (
      <div className="task-details-container">
        <header className="task-details-header">
          <button onClick={() => setShowWorkSummary(false)} className="back-button">
            ‹ Back
          </button>
          <h2>Complete Task</h2>
        </header>

        <div className="task-details-content">
          <h3>{task.title}</h3>
          <div className="work-summary-section">
            <label htmlFor="work-summary">Work Summary *</label>
            <textarea
              id="work-summary"
              value={workSummary}
              onChange={(e) => setWorkSummary(e.target.value)}
              placeholder="Enter a summary of the work completed..."
              rows={6}
              className="work-summary-input"
              required
            />
          </div>

          <div className="task-details-actions">
            <button
              onClick={handleSubmitCompletion}
              className="action-button complete-button"
              disabled={!workSummary.trim() || isUpdating}
            >
              {isUpdating ? 'Completing...' : 'Submit Completion'}
            </button>
            <button
              onClick={() => setShowWorkSummary(false)}
              className="action-button cancel-button"
              disabled={isUpdating}
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="task-details-container">
      <header className="task-details-header">
        {onBack && (
          <button onClick={onBack} className="back-button">
            ‹ Back
          </button>
        )}
        <h2>Task Details</h2>
      </header>

      <div className="task-details-content">
        <div className="task-details-title-row">
          <h3>{task.title}</h3>
          <span
            className="task-priority-badge"
            style={{ backgroundColor: priorityColors[task.priority] || priorityColors.MEDIUM }}
          >
            {task.priority || 'MEDIUM'}
          </span>
        </div>

        {task.status && (
          <div className="task-status-section">
            <span
              className="status-badge"
              style={{ backgroundColor: statusColors[task.status] || statusColors.PENDING }}
            >
              {task.status.replace('_', ' ')}
            </span>
          </div>
        )}

        {task.description && (
          <div className="task-details-section">
            <h4>Description</h4>
            <p>{task.description}</p>
          </div>
        )}

        <div className="task-details-section">
          <h4>Address</h4>
          <p className="task-address">
            <span className="address-icon">📍</span>
            {task.clientAddress}
          </p>
        </div>

        {task.estimatedDuration && (
          <div className="task-details-section">
            <h4>Estimated Duration</h4>
            <p>⏱️ {task.estimatedDuration} minutes</p>
          </div>
        )}

        <div className="task-details-actions">
          <button
            onClick={handleNavigateToTask}
            className="action-button navigate-button"
          >
            📍 Navigate
          </button>

          {task.status !== 'COMPLETED' && task.status !== 'IN_PROGRESS' && (
            <button
              onClick={handleStartTask}
              className="action-button start-button"
              disabled={isUpdating}
            >
              {isUpdating ? 'Starting...' : 'Start Task'}
            </button>
          )}

          {task.status === 'IN_PROGRESS' && (
            <button
              onClick={handleCompleteTask}
              className="action-button complete-button"
              disabled={isUpdating}
            >
              Complete Task
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

TaskDetails.propTypes = {
  task: PropTypes.shape({
    id: PropTypes.number.isRequired,
    title: PropTypes.string.isRequired,
    description: PropTypes.string,
    clientAddress: PropTypes.string.isRequired,
    priority: PropTypes.oneOf(['HIGH', 'MEDIUM', 'LOW']),
    estimatedDuration: PropTypes.number,
    status: PropTypes.string,
  }).isRequired,
  onStatusChange: PropTypes.func,
  onNavigate: PropTypes.func,
  onBack: PropTypes.func,
  isUpdating: PropTypes.bool,
};

export default TaskDetails;
