import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { MapContainer, TileLayer, ZoomControl } from 'react-leaflet';
import TechnicianMarker from './TechnicianMarker';
import TaskMarker from './TaskMarker';
import 'leaflet/dist/leaflet.css';
import '../../styles/MapView.css';

/**
 * MapView Component
 * Displays a real-time map with technician and task locations
 */
const MapView = ({ 
  technicians = [], 
  tasks = [], 
  onTaskAssign,
  center = [37.7749, -122.4194], // Default to San Francisco
  zoom = 12 
}) => {
  const [mapCenter, setMapCenter] = useState(center);
  const [mapZoom, setMapZoom] = useState(zoom);

  // Update map center when tasks or technicians change
  useEffect(() => {
    if (tasks.length > 0 || technicians.length > 0) {
      // Calculate center based on all markers
      const allMarkers = [
        ...tasks.map(t => t.location),
        ...technicians.map(t => t.location)
      ].filter(Boolean);

      if (allMarkers.length > 0) {
        const avgLat = allMarkers.reduce((sum, loc) => sum + loc.lat, 0) / allMarkers.length;
        const avgLng = allMarkers.reduce((sum, loc) => sum + loc.lng, 0) / allMarkers.length;
        setMapCenter([avgLat, avgLng]);
      }
    }
  }, [tasks, technicians]);

  return (
    <div className="map-view-container">
      <MapContainer
        center={mapCenter}
        zoom={mapZoom}
        className="map-container"
        zoomControl={false}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ZoomControl position="topright" />

        {/* Render technician markers */}
        {technicians.map((technician) => (
          technician.location && (
            <TechnicianMarker
              key={technician.id}
              technician={technician}
            />
          )
        ))}

        {/* Render task markers */}
        {tasks.map((task) => (
          task.location && (
            <TaskMarker
              key={task.id}
              task={task}
              onAssign={onTaskAssign}
            />
          )
        ))}
      </MapContainer>
    </div>
  );
};

MapView.propTypes = {
  technicians: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      name: PropTypes.string.isRequired,
      status: PropTypes.string,
      workload: PropTypes.number,
      location: PropTypes.shape({
        lat: PropTypes.number.isRequired,
        lng: PropTypes.number.isRequired,
      }),
    })
  ),
  tasks: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      title: PropTypes.string.isRequired,
      priority: PropTypes.string,
      clientAddress: PropTypes.string,
      location: PropTypes.shape({
        lat: PropTypes.number.isRequired,
        lng: PropTypes.number.isRequired,
      }),
    })
  ),
  onTaskAssign: PropTypes.func,
  center: PropTypes.arrayOf(PropTypes.number),
  zoom: PropTypes.number,
};

export default MapView;
