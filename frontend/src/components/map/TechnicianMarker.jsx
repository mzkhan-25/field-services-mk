import React from 'react';
import PropTypes from 'prop-types';
import { Marker, Popup } from 'react-leaflet';
import L from 'leaflet';

/**
 * TechnicianMarker Component
 * Displays a marker for a technician with a popup showing details
 */
const TechnicianMarker = ({ technician }) => {
  // Create custom icon for technician
  const technicianIcon = new L.Icon({
    iconUrl: 'data:image/svg+xml;base64,' + btoa(`
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="32" height="32">
        <circle cx="12" cy="12" r="10" fill="#4CAF50" stroke="white" stroke-width="2"/>
        <path d="M12 6 L12 12 L16 12" stroke="white" stroke-width="2" fill="none"/>
      </svg>
    `),
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32],
  });

  const statusColors = {
    AVAILABLE: '#4CAF50',
    BUSY: '#FF9800',
    OFFLINE: '#9E9E9E',
  };

  const statusIcon = new L.Icon({
    iconUrl: 'data:image/svg+xml;base64,' + btoa(`
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="32" height="32">
        <circle cx="12" cy="12" r="10" fill="${statusColors[technician.status] || '#4CAF50'}" stroke="white" stroke-width="2"/>
        <circle cx="12" cy="12" r="4" fill="white"/>
      </svg>
    `),
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32],
  });

  return (
    <Marker
      position={[technician.location.lat, technician.location.lng]}
      icon={statusIcon}
    >
      <Popup>
        <div className="technician-popup">
          <h3>{technician.name}</h3>
          <p><strong>Status:</strong> {technician.status || 'AVAILABLE'}</p>
          <p><strong>Workload:</strong> {technician.workload || 0} tasks</p>
          {technician.currentTask && (
            <p><strong>Current Task:</strong> {technician.currentTask}</p>
          )}
        </div>
      </Popup>
    </Marker>
  );
};

TechnicianMarker.propTypes = {
  technician: PropTypes.shape({
    id: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired,
    status: PropTypes.string,
    workload: PropTypes.number,
    currentTask: PropTypes.string,
    location: PropTypes.shape({
      lat: PropTypes.number.isRequired,
      lng: PropTypes.number.isRequired,
    }).isRequired,
  }).isRequired,
};

export default TechnicianMarker;
