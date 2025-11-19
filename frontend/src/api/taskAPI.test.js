import { describe, it, expect, vi, beforeEach } from 'vitest';
import apiClient from './apiClient';
import {
  getUnassignedTasks,
  createTask,
  getAllTasks,
  getTaskById,
  updateTask,
} from './taskAPI';

// Mock apiClient
vi.mock('./apiClient');

describe('taskAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getUnassignedTasks', () => {
    it('should fetch unassigned tasks', async () => {
      const mockTasks = [
        { id: 1, title: 'Task 1', priority: 'HIGH', status: 'UNASSIGNED' },
        { id: 2, title: 'Task 2', priority: 'MEDIUM', status: 'UNASSIGNED' },
      ];
      apiClient.get.mockResolvedValue({ data: mockTasks });

      const result = await getUnassignedTasks();

      expect(apiClient.get).toHaveBeenCalledWith('/api/tasks/unassigned');
      expect(result).toEqual(mockTasks);
    });
  });

  describe('createTask', () => {
    it('should create a new task', async () => {
      const taskData = {
        title: 'Fix HVAC',
        description: 'Customer reports AC not working',
        clientAddress: '123 Main St, City',
        priority: 'HIGH',
        estimatedDuration: 120,
      };
      const mockResponse = { id: 1, ...taskData, status: 'UNASSIGNED' };
      apiClient.post.mockResolvedValue({ data: mockResponse });

      const result = await createTask(taskData);

      expect(apiClient.post).toHaveBeenCalledWith('/api/tasks', taskData);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getAllTasks', () => {
    it('should fetch all tasks', async () => {
      const mockTasks = [
        { id: 1, title: 'Task 1', status: 'UNASSIGNED' },
        { id: 2, title: 'Task 2', status: 'ASSIGNED' },
      ];
      apiClient.get.mockResolvedValue({ data: mockTasks });

      const result = await getAllTasks();

      expect(apiClient.get).toHaveBeenCalledWith('/api/tasks');
      expect(result).toEqual(mockTasks);
    });
  });

  describe('getTaskById', () => {
    it('should fetch a task by ID', async () => {
      const mockTask = { id: 1, title: 'Task 1', status: 'UNASSIGNED' };
      apiClient.get.mockResolvedValue({ data: mockTask });

      const result = await getTaskById(1);

      expect(apiClient.get).toHaveBeenCalledWith('/api/tasks/1');
      expect(result).toEqual(mockTask);
    });
  });

  describe('updateTask', () => {
    it('should update a task', async () => {
      const taskData = { title: 'Updated Task', priority: 'LOW' };
      const mockResponse = { id: 1, ...taskData };
      apiClient.put.mockResolvedValue({ data: mockResponse });

      const result = await updateTask(1, taskData);

      expect(apiClient.put).toHaveBeenCalledWith('/api/tasks/1', taskData);
      expect(result).toEqual(mockResponse);
    });
  });
});
