import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import Dashboard from '../components/Dashboard';
import { AuthProvider } from '../contexts/AuthContext';
import authUtils from '../utils/authUtils';

vi.mock('../utils/authUtils');

const renderDashboard = () => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        <Dashboard />
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('Dashboard Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it('should render dashboard with user information', async () => {
    const mockUser = {
      username: 'testuser',
      email: 'test@example.com',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000); // 1 hour

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText('Field Services Management System')).toBeInTheDocument();
      expect(screen.getByText(/Welcome, testuser/)).toBeInTheDocument();
      expect(screen.getByText(/Role: DISPATCHER/)).toBeInTheDocument();
      expect(screen.getByText(/Email: test@example.com/)).toBeInTheDocument();
    });
  });

  it('should display session time remaining', async () => {
    const mockUser = {
      username: 'testuser',
      email: 'test@example.com',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000); // 1 hour

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText(/Session expires in: 1h 0m/)).toBeInTheDocument();
    });
  });

  it('should format time remaining correctly for minutes only', async () => {
    const mockUser = {
      username: 'testuser',
      email: 'test@example.com',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(1800000); // 30 minutes

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText(/Session expires in: 30m/)).toBeInTheDocument();
    });
  });

  it('should call logout when logout button is clicked', async () => {
    const user = userEvent.setup();
    const mockUser = {
      username: 'testuser',
      email: 'test@example.com',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);
    vi.mocked(authUtils.clearAuth).mockImplementation(() => {});

    renderDashboard();

    const logoutButton = screen.getByRole('button', { name: /logout/i });
    await user.click(logoutButton);

    await waitFor(() => {
      expect(authUtils.clearAuth).toHaveBeenCalled();
    });
  });

  it('should handle missing user data gracefully', async () => {
    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(null);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(0);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText(/Welcome, User/)).toBeInTheDocument();
      expect(screen.getByText(/Role: N\/A/)).toBeInTheDocument();
      expect(screen.getByText(/Email: N\/A/)).toBeInTheDocument();
    });
  });
});
