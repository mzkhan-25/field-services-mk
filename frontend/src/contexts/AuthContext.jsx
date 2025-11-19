import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import authAPI from '../api/authAPI';
import authUtils from '../utils/authUtils';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [sessionTimeRemaining, setSessionTimeRemaining] = useState(0);

  // Initialize authentication state on mount
  useEffect(() => {
    const initAuth = () => {
      if (authUtils.isAuthenticated()) {
        const storedUser = authUtils.getUser();
        setUser(storedUser);
        setSessionTimeRemaining(authUtils.getSessionTimeRemaining());
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  // Session timeout monitoring
  useEffect(() => {
    if (!user) return;

    const interval = setInterval(() => {
      const remaining = authUtils.getSessionTimeRemaining();
      setSessionTimeRemaining(remaining);

      if (remaining <= 0) {
        logout();
      }
    }, 60000); // Check every minute

    return () => clearInterval(interval);
  }, [user]);

  const login = useCallback(async (username, password) => {
    try {
      const response = await authAPI.login(username, password);
      
      const userData = {
        username: response.username,
        email: response.email,
        role: response.role,
      };

      authUtils.saveAuth(response.token, userData);
      setUser(userData);
      setSessionTimeRemaining(authUtils.getSessionTimeRemaining());

      return { success: true };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Login failed. Please try again.';
      return { success: false, error: errorMessage };
    }
  }, []);

  const logout = useCallback(() => {
    authUtils.clearAuth();
    setUser(null);
    setSessionTimeRemaining(0);
  }, []);

  const hasRole = useCallback((role) => {
    return authUtils.hasRole(role);
  }, []);

  const hasAnyRole = useCallback((roles) => {
    return authUtils.hasAnyRole(roles);
  }, []);

  const value = {
    user,
    loading,
    sessionTimeRemaining,
    isAuthenticated: !!user,
    login,
    logout,
    hasRole,
    hasAnyRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthContext;
