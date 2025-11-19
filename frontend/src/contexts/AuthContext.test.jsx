import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider, useAuth } from '../contexts/AuthContext';
import authAPI from '../api/authAPI';
import authUtils from '../utils/authUtils';

vi.mock('../api/authAPI');
vi.mock('../utils/authUtils');

const TestComponent = () => {
  const { user, isAuthenticated, login, logout, loading } = useAuth();
  
  return (
    <div>
      {loading && <div>Loading...</div>}
      {isAuthenticated && <div>Authenticated: {user?.username}</div>}
      {!isAuthenticated && <div>Not Authenticated</div>}
      <button onClick={() => login('testuser', 'password')}>Login</button>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

const renderAuthProvider = () => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it('should provide authentication state', async () => {
    vi.mocked(authUtils.isAuthenticated).mockReturnValue(false);
    vi.mocked(authUtils.getUser).mockReturnValue(null);

    renderAuthProvider();

    await waitFor(() => {
      expect(screen.getByText('Not Authenticated')).toBeInTheDocument();
    });
  });

  it('should initialize with stored authentication', async () => {
    const mockUser = {
      username: 'testuser',
      email: 'test@example.com',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);

    renderAuthProvider();

    await waitFor(() => {
      expect(screen.getByText('Authenticated: testuser')).toBeInTheDocument();
    });
  });

  it('should handle successful login', async () => {
    const mockResponse = {
      token: 'test-token',
      username: 'testuser',
      email: 'test@example.com',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(false);
    vi.mocked(authUtils.getUser).mockReturnValue(null);
    vi.mocked(authAPI.login).mockResolvedValue(mockResponse);
    vi.mocked(authUtils.saveAuth).mockImplementation(() => {});
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);

    const { container } = renderAuthProvider();

    await waitFor(() => {
      expect(screen.getByText('Not Authenticated')).toBeInTheDocument();
    });

    const loginButton = screen.getByText('Login');
    loginButton.click();

    await waitFor(() => {
      expect(authAPI.login).toHaveBeenCalledWith('testuser', 'password');
      expect(authUtils.saveAuth).toHaveBeenCalledWith('test-token', {
        username: 'testuser',
        email: 'test@example.com',
        role: 'DISPATCHER',
      });
    });
  });

  it('should handle login failure', async () => {
    const errorMessage = 'Invalid username or password';

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(false);
    vi.mocked(authUtils.getUser).mockReturnValue(null);
    vi.mocked(authAPI.login).mockRejectedValue({
      response: { data: { message: errorMessage } },
    });

    renderAuthProvider();

    await waitFor(() => {
      expect(screen.getByText('Not Authenticated')).toBeInTheDocument();
    });

    const loginButton = screen.getByText('Login');
    loginButton.click();

    await waitFor(() => {
      expect(authAPI.login).toHaveBeenCalled();
    });
  });

  it('should handle logout', async () => {
    const mockUser = {
      username: 'testuser',
      email: 'test@example.com',
      role: 'DISPATCHER',
    };

    vi.mocked(authUtils.isAuthenticated).mockReturnValue(true);
    vi.mocked(authUtils.getUser).mockReturnValue(mockUser);
    vi.mocked(authUtils.getSessionTimeRemaining).mockReturnValue(3600000);
    vi.mocked(authUtils.clearAuth).mockImplementation(() => {});

    renderAuthProvider();

    await waitFor(() => {
      expect(screen.getByText('Authenticated: testuser')).toBeInTheDocument();
    });

    const logoutButton = screen.getByText('Logout');
    logoutButton.click();

    await waitFor(() => {
      expect(authUtils.clearAuth).toHaveBeenCalled();
    });
  });

  it('should throw error when useAuth is used outside AuthProvider', () => {
    const TestComponentWithoutProvider = () => {
      try {
        useAuth();
        return <div>No Error</div>;
      } catch (error) {
        return <div>Error: {error.message}</div>;
      }
    };

    render(<TestComponentWithoutProvider />);

    expect(screen.getByText(/useAuth must be used within an AuthProvider/)).toBeInTheDocument();
  });
});
