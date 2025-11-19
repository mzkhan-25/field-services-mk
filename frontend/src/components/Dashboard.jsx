import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import TaskForm from './TaskForm';
import TaskList from './TaskList';
import { getUnassignedTasks, createTask } from '../api/taskAPI';
import '../styles/Dashboard.css';

const Dashboard = () => {
  const { user, logout, sessionTimeRemaining } = useAuth();
  const [showTaskForm, setShowTaskForm] = useState(false);
  const [tasks, setTasks] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [notification, setNotification] = useState(null);

  const formatTimeRemaining = (ms) => {
    const minutes = Math.floor(ms / 60000);
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    
    if (hours > 0) {
      return `${hours}h ${remainingMinutes}m`;
    }
    return `${remainingMinutes}m`;
  };

  // Load tasks on mount
  useEffect(() => {
    loadTasks();
  }, []);

  // Clear notifications after 5 seconds
  useEffect(() => {
    if (notification) {
      const timer = setTimeout(() => {
        setNotification(null);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [notification]);

  const loadTasks = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getUnassignedTasks();
      setTasks(data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load tasks');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateTask = async (taskData) => {
    try {
      const newTask = await createTask(taskData);
      setTasks((prev) => [newTask, ...prev]);
      setShowTaskForm(false);
      setNotification({ type: 'success', message: 'Task created successfully!' });
    } catch (err) {
      setNotification({
        type: 'error',
        message: err.response?.data?.message || 'Failed to create task',
      });
    }
  };

  const handleCancelForm = () => {
    setShowTaskForm(false);
  };

  const canCreateTask = user?.role === 'DISPATCHER' || user?.role === 'SUPERVISOR';

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

      {notification && (
        <div className={`notification notification-${notification.type}`}>
          {notification.message}
        </div>
      )}

      <main className="dashboard-main">
        {showTaskForm ? (
          <TaskForm
            onSubmit={handleCreateTask}
            onCancel={handleCancelForm}
            isSubmitting={isLoading}
          />
        ) : (
          <>
            {canCreateTask && (
              <div className="dashboard-actions">
                <button
                  onClick={() => setShowTaskForm(true)}
                  className="create-task-button"
                >
                  Create New Task
                </button>
              </div>
            )}
            <TaskList
              tasks={tasks}
              onRefresh={loadTasks}
              isLoading={isLoading}
              error={error}
            />
          </>
        )}
      </main>
    </div>
  );
};

export default Dashboard;
