import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import '../styles/Dashboard.css';

const Dashboard = () => {
  const { user, logout, sessionTimeRemaining } = useAuth();

  const formatTimeRemaining = (ms) => {
    const minutes = Math.floor(ms / 60000);
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    
    if (hours > 0) {
      return `${hours}h ${remainingMinutes}m`;
    }
    return `${remainingMinutes}m`;
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>Field Services Management System</h1>
        <div className="user-info">
          <span className="user-name">
            Welcome, {user?.username || 'User'}
          </span>
          <span className="user-role">Role: {user?.role || 'N/A'}</span>
          {sessionTimeRemaining > 0 && (
            <span className="session-info">
              Session expires in: {formatTimeRemaining(sessionTimeRemaining)}
            </span>
          )}
          <button onClick={logout} className="logout-button">
            Logout
          </button>
        </div>
      </header>
      <main className="dashboard-main">
        <div className="welcome-section">
          <h2>Welcome to the Dashboard</h2>
          <p>You are successfully logged in!</p>
          <p>Email: {user?.email || 'N/A'}</p>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
