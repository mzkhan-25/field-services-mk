import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';

// Mock axios
vi.mock('axios', () => ({
  default: {
    create: vi.fn(),
  },
}));

describe('authAPI', () => {
  let authAPI;
  let mockPost;
  let mockGet;

  beforeEach(async () => {
    vi.clearAllMocks();
    vi.resetModules();
    
    // Setup mocks
    mockPost = vi.fn();
    mockGet = vi.fn();

    // Mock axios.create to return our mock client
    vi.mocked(axios.create).mockReturnValue({
      post: mockPost,
      get: mockGet,
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
    });

    // Dynamically import authAPI after mocking
    const module = await import('../api/authAPI.js');
    authAPI = module.default;
  });

  describe('login', () => {
    it('should make POST request to /auth/login and return response data', async () => {
      const mockResponse = {
        data: {
          token: 'test-token',
          username: 'testuser',
          email: 'test@example.com',
          role: 'DISPATCHER',
        },
      };

      mockPost.mockResolvedValue(mockResponse);

      const result = await authAPI.login('testuser', 'password');

      expect(mockPost).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'password',
      });
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle login errors', async () => {
      const error = new Error('Login failed');
      mockPost.mockRejectedValue(error);

      await expect(authAPI.login('testuser', 'wrongpassword')).rejects.toThrow('Login failed');
    });
  });

  describe('healthCheck', () => {
    it('should make GET request to /auth/health and return response data', async () => {
      const mockResponse = {
        data: {
          status: 'UP',
          service: 'Auth Service',
        },
      };

      mockGet.mockResolvedValue(mockResponse);

      const result = await authAPI.healthCheck();

      expect(mockGet).toHaveBeenCalledWith('/auth/health');
      expect(result).toEqual(mockResponse.data);
    });
  });
});
