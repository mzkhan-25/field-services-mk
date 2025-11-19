import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import React from 'react';
import { AuthProvider } from '../contexts/AuthContext';
import ProtectedRoute from '../components/ProtectedRoute';
import authUtils from '../utils/authUtils';

// Mock authUtils
vi.mock('../utils/authUtils');

const TestComponent = () => <div>Protected Content</div>;

const renderProtectedRoute = (allowedRoles = []) => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route
            path="/"
            element={
              <ProtectedRoute allowedRoles={allowedRoles}>
                <TestComponent />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('ProtectedRoute Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it('should render children when authenticated with no role restrictions', async () => {
    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue({
      username: 'testuser',
      email: 'test@example.com',
      role: 'DISPATCHER',
    });
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);

    renderProtectedRoute();

    // Wait for content to render
    await screen.findByText('Protected Content');
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should redirect to login when not authenticated', async () => {
    vi.mocked(authUtils.isAuthenticated).mockReturnValue(false);
    vi.mocked(authUtils.getUser).mockReturnValue(null);

    renderProtectedRoute();

    // Should redirect to login
    await screen.findByText('Login Page');
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });
});
