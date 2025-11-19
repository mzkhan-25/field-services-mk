# Authentication Module Documentation

## Overview
This module implements a complete authentication flow for the Field Services Management System frontend, including login, JWT token management, protected routes, and role-based access control.

## Components

### Login Component (`src/components/Login.jsx`)
The Login component provides a form for users to authenticate with username and password.

**Features:**
- Form validation (required fields, minimum password length)
- Real-time error clearing as user types
- Loading state during authentication
- Error message display for invalid credentials
- Responsive design

**Usage:**
```jsx
import Login from './components/Login';

<Route path="/login" element={<Login />} />
```

**Validation Rules:**
- Username: Required
- Password: Required, minimum 3 characters

### Dashboard Component (`src/components/Dashboard.jsx`)
A simple dashboard that displays after successful authentication.

**Features:**
- Displays user information (username, email, role)
- Shows session expiration countdown
- Logout button
- Responsive header design

**Usage:**
```jsx
import Dashboard from './components/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';

<Route 
  path="/" 
  element={
    <ProtectedRoute>
      <Dashboard />
    </ProtectedRoute>
  } 
/>
```

### ProtectedRoute Component (`src/components/ProtectedRoute.jsx`)
A wrapper component that protects routes from unauthorized access.

**Features:**
- Redirects unauthenticated users to login page
- Supports role-based access control
- Loading state during authentication check
- Access denied page for insufficient permissions

**Usage:**
```jsx
import ProtectedRoute from './components/ProtectedRoute';

// Basic usage - requires authentication only
<Route 
  path="/dashboard" 
  element={
    <ProtectedRoute>
      <Dashboard />
    </ProtectedRoute>
  } 
/>

// With role restrictions
<Route 
  path="/admin" 
  element={
    <ProtectedRoute allowedRoles={['DISPATCHER', 'SUPERVISOR']}>
      <AdminPanel />
    </ProtectedRoute>
  } 
/>
```

**Props:**
- `children` (ReactNode, required): The component to render if authorized
- `allowedRoles` (array, optional): Array of role strings that are allowed to access the route

## Context

### AuthContext (`src/contexts/AuthContext.jsx`)
Provides authentication state and methods throughout the application.

**Usage:**
```jsx
import { useAuth } from './contexts/AuthContext';

function MyComponent() {
  const { 
    user, 
    isAuthenticated, 
    loading,
    sessionTimeRemaining,
    login, 
    logout,
    hasRole,
    hasAnyRole 
  } = useAuth();

  // Use authentication state and methods
}
```

**API:**
- `user`: Current user object `{ username, email, role }` or `null`
- `isAuthenticated`: Boolean indicating if user is logged in
- `loading`: Boolean indicating if auth state is being initialized
- `sessionTimeRemaining`: Time remaining in current session (milliseconds)
- `login(username, password)`: Async function to authenticate user
  - Returns: `{ success: boolean, error?: string }`
- `logout()`: Function to clear authentication state
- `hasRole(role)`: Check if user has a specific role
- `hasAnyRole(roles)`: Check if user has any of the specified roles

**Provider Setup:**
```jsx
import { AuthProvider } from './contexts/AuthContext';

<AuthProvider>
  <App />
</AuthProvider>
```

## API Client

### API Client (`src/api/apiClient.js`)
Axios instance configured with base URL and authentication interceptors.

**Features:**
- Automatically adds JWT token to all requests
- Handles 401 errors by clearing session and redirecting to login
- Configured with base URL from environment variable

### Auth API (`src/api/authAPI.js`)
API methods for authentication endpoints.

**Methods:**
- `login(username, password)`: Authenticate user and get JWT token
- `healthCheck()`: Check if auth service is running

**Usage:**
```jsx
import authAPI from './api/authAPI';

// Login
const response = await authAPI.login('username', 'password');
// Returns: { token, username, email, role }

// Health check
const status = await authAPI.healthCheck();
// Returns: { status: 'UP', service: 'Auth Service' }
```

## Utilities

### Auth Utils (`src/utils/authUtils.js`)
Helper functions for authentication management.

**Methods:**
- `saveAuth(token, user)`: Save authentication data to sessionStorage
- `getToken()`: Get stored JWT token
- `getUser()`: Get stored user object
- `isAuthenticated()`: Check if user is authenticated (checks token and expiration)
- `getSessionTimeRemaining()`: Get time remaining in session (milliseconds)
- `clearAuth()`: Clear all authentication data
- `hasRole(role)`: Check if user has a specific role
- `hasAnyRole(roles)`: Check if user has any of the specified roles

**Constants:**
- Session duration: 2 hours (7,200,000 milliseconds)

**Usage:**
```jsx
import authUtils from './utils/authUtils';

// Save authentication
authUtils.saveAuth(token, { username, email, role });

// Check authentication
if (authUtils.isAuthenticated()) {
  // User is logged in and session is valid
}

// Check roles
if (authUtils.hasRole('DISPATCHER')) {
  // User has dispatcher role
}

if (authUtils.hasAnyRole(['DISPATCHER', 'SUPERVISOR'])) {
  // User has at least one of these roles
}

// Clear authentication
authUtils.clearAuth();
```

## Security Features

### Session Timeout
- Sessions automatically expire after 2 hours of inactivity
- Session time is checked every minute when user is logged in
- Automatic logout when session expires
- Session countdown displayed in Dashboard

### Token Management
- JWT tokens stored in sessionStorage (cleared on browser close)
- Tokens automatically included in all API requests
- Token timestamp tracked for expiration checking

### Route Protection
- Unauthorized users redirected to login page
- Protected routes support role-based access control
- Access denied page for insufficient permissions

## Environment Configuration

Add the following to your `.env` file:

```
VITE_API_URL=http://localhost:8080
```

This sets the base URL for API requests.

## Testing

The authentication module includes comprehensive unit tests with 85%+ coverage.

**Run tests:**
```bash
npm test
```

**Run tests with coverage:**
```bash
npm run test:coverage
```

**Test files:**
- `src/components/Login.test.jsx` - Login component tests
- `src/components/Dashboard.test.jsx` - Dashboard component tests
- `src/components/ProtectedRoute.test.jsx` - Protected route tests
- `src/contexts/AuthContext.test.jsx` - Auth context tests
- `src/utils/authUtils.test.js` - Auth utilities tests
- `src/api/authAPI.test.js` - API client tests

## User Roles

The system supports the following roles:
- `DISPATCHER`: Can create and assign tasks
- `TECHNICIAN`: Can view and complete assigned tasks
- `SUPERVISOR`: Can manage tasks and users

## Example Application Structure

```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          {/* Add more protected routes as needed */}
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
```

## Troubleshooting

### Session expires immediately
- Check that `token_timestamp` is being saved correctly in sessionStorage
- Verify the SESSION_DURATION constant is set correctly (2 hours in milliseconds)

### Unable to login
- Check that backend auth service is running
- Verify VITE_API_URL environment variable is set correctly
- Check browser console for API errors

### Protected routes not working
- Ensure routes are wrapped in `<ProtectedRoute>` component
- Verify `<AuthProvider>` wraps the entire application
- Check that user has required role for role-restricted routes

## Future Enhancements

- Remember me functionality (localStorage instead of sessionStorage)
- Password reset flow
- Two-factor authentication
- Refresh token mechanism
- Session extension before expiration
