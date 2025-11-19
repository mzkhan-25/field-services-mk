import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import Dashboard from '../components/Dashboard';
import { AuthProvider } from '../contexts/AuthContext';
import authUtils from '../utils/authUtils';
import * as taskAPI from '../api/taskAPI';

vi.mock('../utils/authUtils');
vi.mock('../api/taskAPI');

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
    // Mock successful task fetch by default
    vi.mocked(taskAPI.getUnassignedTasks).mockResolvedValue([]);
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
    });
  });

  it('should load tasks on mount', async () => {
    const mockUser = {
      username: 'testuser',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);

    renderDashboard();

    await waitFor(() => {
      expect(taskAPI.getUnassignedTasks).toHaveBeenCalled();
    });
  });

  it('should show create task button for DISPATCHER role', async () => {
    const mockUser = {
      username: 'dispatcher',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create new task/i })).toBeInTheDocument();
    });
  });

  it('should not show create task button for TECHNICIAN role', async () => {
    const mockUser = {
      username: 'technician',
      role: 'TECHNICIAN',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);

    renderDashboard();

    await waitFor(() => {
      expect(screen.queryByRole('button', { name: /create new task/i })).not.toBeInTheDocument();
    });
  });

  it('should show task form when create task button is clicked', async () => {
    const user = userEvent.setup();
    const mockUser = {
      username: 'dispatcher',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create new task/i })).toBeInTheDocument();
    });

    const createButton = screen.getByRole('button', { name: /create new task/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText('Create New Task')).toBeInTheDocument();
    });
  });

  it('should handle task creation successfully', async () => {
    const user = userEvent.setup();
    const mockUser = {
      username: 'dispatcher',
      role: 'DISPATCHER',
    };

    const newTask = {
      id: 1,
      title: 'New Task',
      clientAddress: '123 Main St',
      priority: 'HIGH',
      status: 'UNASSIGNED',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);
    vi.mocked(taskAPI.createTask).mockResolvedValue(newTask);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create new task/i })).toBeInTheDocument();
    });

    const createButton = screen.getByRole('button', { name: /create new task/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText('Create New Task')).toBeInTheDocument();
    });

    // Fill and submit the form
    const titleInput = screen.getByLabelText(/title/i);
    const addressInput = screen.getByLabelText(/client address/i);
    
    await user.type(titleInput, 'New Task');
    await user.type(addressInput, '123 Main St');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(taskAPI.createTask).toHaveBeenCalled();
      expect(screen.getByText('Task created successfully!')).toBeInTheDocument();
    });
  });

  it('should handle task creation error', async () => {
    const user = userEvent.setup();
    const mockUser = {
      username: 'dispatcher',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);
    vi.mocked(taskAPI.createTask).mockRejectedValue(new Error('Failed to create task'));

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create new task/i })).toBeInTheDocument();
    });

    const createButton = screen.getByRole('button', { name: /create new task/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText('Create New Task')).toBeInTheDocument();
    });

    // Fill and submit the form
    const titleInput = screen.getByLabelText(/title/i);
    const addressInput = screen.getByLabelText(/client address/i);
    
    await user.type(titleInput, 'New Task');
    await user.type(addressInput, '123 Main St');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Failed to create task')).toBeInTheDocument();
    });
  });

  it('should cancel task form', async () => {
    const user = userEvent.setup();
    const mockUser = {
      username: 'dispatcher',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create new task/i })).toBeInTheDocument();
    });

    const createButton = screen.getByRole('button', { name: /create new task/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /create new task/i })).toBeInTheDocument();
    });

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    await waitFor(() => {
      expect(screen.queryByRole('heading', { name: /create new task/i })).not.toBeInTheDocument();
      expect(screen.getByText('Unassigned Tasks')).toBeInTheDocument();
    });
  });
});
