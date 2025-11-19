import { describe, it, expect, beforeEach, vi } from 'vitest';
import authUtils from '../utils/authUtils';

describe('authUtils', () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.clearAllMocks();
  });

  describe('saveAuth', () => {
    it('should save token and user to sessionStorage', () => {
      const token = 'test-token';
      const user = { username: 'testuser', email: 'test@example.com', role: 'DISPATCHER' };

      authUtils.saveAuth(token, user);

      expect(sessionStorage.getItem('token')).toBe(token);
      expect(sessionStorage.getItem('user')).toBe(JSON.stringify(user));
      expect(sessionStorage.getItem('token_timestamp')).toBeTruthy();
    });
  });

  describe('getToken', () => {
    it('should return stored token', () => {
      const token = 'test-token';
      sessionStorage.setItem('token', token);

      expect(authUtils.getToken()).toBe(token);
    });

    it('should return null if no token', () => {
      expect(authUtils.getToken()).toBeNull();
    });
  });

  describe('getUser', () => {
    it('should return parsed user object', () => {
      const user = { username: 'testuser', email: 'test@example.com', role: 'DISPATCHER' };
      sessionStorage.setItem('user', JSON.stringify(user));

      expect(authUtils.getUser()).toEqual(user);
    });

    it('should return null if no user', () => {
      expect(authUtils.getUser()).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('should return true if token and timestamp are valid', () => {
      sessionStorage.setItem('token', 'test-token');
      sessionStorage.setItem('token_timestamp', Date.now().toString());

      expect(authUtils.isAuthenticated()).toBe(true);
    });

    it('should return false if no token', () => {
      expect(authUtils.isAuthenticated()).toBe(false);
    });

    it('should return false if token is expired', () => {
      sessionStorage.setItem('token', 'test-token');
      // Set timestamp to 3 hours ago
      sessionStorage.setItem('token_timestamp', (Date.now() - 3 * 60 * 60 * 1000).toString());

      expect(authUtils.isAuthenticated()).toBe(false);
      expect(sessionStorage.getItem('token')).toBeNull();
    });
  });

  describe('getSessionTimeRemaining', () => {
    it('should return remaining time in milliseconds', () => {
      const now = Date.now();
      sessionStorage.setItem('token_timestamp', now.toString());

      const remaining = authUtils.getSessionTimeRemaining();
      expect(remaining).toBeGreaterThan(0);
      expect(remaining).toBeLessThanOrEqual(2 * 60 * 60 * 1000);
    });

    it('should return 0 if no timestamp', () => {
      expect(authUtils.getSessionTimeRemaining()).toBe(0);
    });

    it('should return 0 if timestamp is expired', () => {
      sessionStorage.setItem('token_timestamp', (Date.now() - 3 * 60 * 60 * 1000).toString());

      expect(authUtils.getSessionTimeRemaining()).toBe(0);
    });
  });

  describe('clearAuth', () => {
    it('should remove all auth data from sessionStorage', () => {
      sessionStorage.setItem('token', 'test-token');
      sessionStorage.setItem('user', JSON.stringify({ username: 'test' }));
      sessionStorage.setItem('token_timestamp', Date.now().toString());

      authUtils.clearAuth();

      expect(sessionStorage.getItem('token')).toBeNull();
      expect(sessionStorage.getItem('user')).toBeNull();
      expect(sessionStorage.getItem('token_timestamp')).toBeNull();
    });
  });

  describe('hasRole', () => {
    it('should return true if user has the specified role', () => {
      const user = { username: 'testuser', email: 'test@example.com', role: 'DISPATCHER' };
      sessionStorage.setItem('user', JSON.stringify(user));

      expect(authUtils.hasRole('DISPATCHER')).toBe(true);
    });

    it('should return false if user does not have the specified role', () => {
      const user = { username: 'testuser', email: 'test@example.com', role: 'TECHNICIAN' };
      sessionStorage.setItem('user', JSON.stringify(user));

      expect(authUtils.hasRole('DISPATCHER')).toBe(false);
    });

    it('should return false if no user', () => {
      expect(authUtils.hasRole('DISPATCHER')).toBeFalsy();
    });
  });

  describe('hasAnyRole', () => {
    it('should return true if user has any of the specified roles', () => {
      const user = { username: 'testuser', email: 'test@example.com', role: 'TECHNICIAN' };
      sessionStorage.setItem('user', JSON.stringify(user));

      expect(authUtils.hasAnyRole(['DISPATCHER', 'TECHNICIAN'])).toBe(true);
    });

    it('should return false if user does not have any of the specified roles', () => {
      const user = { username: 'testuser', email: 'test@example.com', role: 'SUPERVISOR' };
      sessionStorage.setItem('user', JSON.stringify(user));

      expect(authUtils.hasAnyRole(['DISPATCHER', 'TECHNICIAN'])).toBe(false);
    });

    it('should return false if no user', () => {
      expect(authUtils.hasAnyRole(['DISPATCHER', 'TECHNICIAN'])).toBeFalsy();
    });
  });
});
