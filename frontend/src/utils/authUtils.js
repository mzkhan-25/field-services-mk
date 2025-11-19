const TOKEN_KEY = 'token';
const USER_KEY = 'user';
const TOKEN_TIMESTAMP_KEY = 'token_timestamp';
const SESSION_DURATION = 2 * 60 * 60 * 1000; // 2 hours in milliseconds

export const authUtils = {
  // Save authentication data to sessionStorage
  saveAuth: (token, user) => {
    sessionStorage.setItem(TOKEN_KEY, token);
    sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    sessionStorage.setItem(TOKEN_TIMESTAMP_KEY, Date.now().toString());
  },

  // Get stored token
  getToken: () => {
    return sessionStorage.getItem(TOKEN_KEY);
  },

  // Get stored user data
  getUser: () => {
    const userStr = sessionStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    const token = sessionStorage.getItem(TOKEN_KEY);
    const timestamp = sessionStorage.getItem(TOKEN_TIMESTAMP_KEY);
    
    if (!token || !timestamp) {
      return false;
    }

    // Check if token has expired (2 hours)
    const now = Date.now();
    const tokenAge = now - parseInt(timestamp, 10);
    
    if (tokenAge > SESSION_DURATION) {
      authUtils.clearAuth();
      return false;
    }

    return true;
  },

  // Get time remaining in session (in milliseconds)
  getSessionTimeRemaining: () => {
    const timestamp = sessionStorage.getItem(TOKEN_TIMESTAMP_KEY);
    if (!timestamp) {
      return 0;
    }

    const now = Date.now();
    const tokenAge = now - parseInt(timestamp, 10);
    const remaining = SESSION_DURATION - tokenAge;

    return remaining > 0 ? remaining : 0;
  },

  // Clear all authentication data
  clearAuth: () => {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    sessionStorage.removeItem(TOKEN_TIMESTAMP_KEY);
  },

  // Check if user has a specific role
  hasRole: (role) => {
    const user = authUtils.getUser();
    return user && user.role === role;
  },

  // Check if user has any of the specified roles
  hasAnyRole: (roles) => {
    const user = authUtils.getUser();
    return user && roles.includes(user.role);
  },
};

export default authUtils;
