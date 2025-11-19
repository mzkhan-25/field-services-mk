import apiClient from './apiClient';

/**
 * Task API Client
 * Provides functions to interact with task-related endpoints
 */

/**
 * Get all unassigned tasks
 * @returns {Promise} Response containing unassigned tasks
 */
export const getUnassignedTasks = async () => {
  const response = await apiClient.get('/api/tasks/unassigned');
  return response.data;
};

/**
 * Create a new task
 * @param {Object} taskData - Task data
 * @param {string} taskData.title - Task title (3-200 characters)
 * @param {string} taskData.description - Task description (optional)
 * @param {string} taskData.clientAddress - Client address (5-500 characters, must contain letters and numbers)
 * @param {string} taskData.priority - Priority: HIGH, MEDIUM, or LOW
 * @param {number} taskData.estimatedDuration - Estimated duration in minutes (optional)
 * @returns {Promise} Response containing created task
 */
export const createTask = async (taskData) => {
  const response = await apiClient.post('/api/tasks', taskData);
  return response.data;
};

/**
 * Get all tasks
 * @returns {Promise} Response containing all tasks
 */
export const getAllTasks = async () => {
  const response = await apiClient.get('/api/tasks');
  return response.data;
};

/**
 * Get a task by ID
 * @param {number} id - Task ID
 * @returns {Promise} Response containing task details
 */
export const getTaskById = async (id) => {
  const response = await apiClient.get(`/api/tasks/${id}`);
  return response.data;
};

/**
 * Update a task
 * @param {number} id - Task ID
 * @param {Object} taskData - Updated task data
 * @returns {Promise} Response containing updated task
 */
export const updateTask = async (id, taskData) => {
  const response = await apiClient.put(`/api/tasks/${id}`, taskData);
  return response.data;
};
