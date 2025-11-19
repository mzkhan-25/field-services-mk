import React from 'react';
import PropTypes from 'prop-types';
import { Marker, Popup } from 'react-leaflet';
import L from 'leaflet';

/**
 * TaskMarker Component
 * Displays a marker for a task with a popup showing details and assign option
 */
const TaskMarker = ({ task, onAssign }) => {
  // Priority colors for task markers
  const priorityColors = {
    HIGH: '#F44336',
    MEDIUM: '#FF9800',
    LOW: '#2196F3',
  };

  const taskIcon = new L.Icon({
    iconUrl: 'data:image/svg+xml;base64,' + btoa(`
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="32" height="32">
        <path d="M12 2 L2 7 L12 12 L22 7 Z" fill="${priorityColors[task.priority] || '#2196F3'}" stroke="white" stroke-width="1"/>
        <path d="M2 17 L12 22 L22 17 L22 7 L12 12 L2 7 Z" fill="${priorityColors[task.priority] || '#2196F3'}" opacity="0.7" stroke="white" stroke-width="1"/>
      </svg>
    `),
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32],
  });

  const handleAssign = () => {
    if (onAssign) {
      onAssign(task.id);
    }
  };

  return (
    <Marker
      position={[task.location.lat, task.location.lng]}
      icon={taskIcon}
    >
      <Popup>
        <div className="task-popup">
          <h3>{task.title}</h3>
          <p><strong>Priority:</strong> <span className={`priority-${task.priority?.toLowerCase()}`}>{task.priority || 'MEDIUM'}</span></p>
          <p><strong>Address:</strong> {task.clientAddress}</p>
          {task.description && (
            <p><strong>Description:</strong> {task.description}</p>
          )}
          {task.estimatedDuration && (
            <p><strong>Estimated Duration:</strong> {task.estimatedDuration} minutes</p>
          )}
          {onAssign && (
            <button 
              onClick={handleAssign}
              className="assign-button"
            >
              Assign Task
            </button>
          )}
        </div>
      </Popup>
    </Marker>
  );
};

TaskMarker.propTypes = {
  task: PropTypes.shape({
    id: PropTypes.number.isRequired,
    title: PropTypes.string.isRequired,
    priority: PropTypes.string,
    clientAddress: PropTypes.string,
    description: PropTypes.string,
    estimatedDuration: PropTypes.number,
    location: PropTypes.shape({
      lat: PropTypes.number.isRequired,
      lng: PropTypes.number.isRequired,
    }).isRequired,
  }).isRequired,
  onAssign: PropTypes.func,
};

export default TaskMarker;
