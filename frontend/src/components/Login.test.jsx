import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import Login from '../components/Login';
import { AuthProvider } from '../contexts/AuthContext';
import authAPI from '../api/authAPI';

vi.mock('../api/authAPI');

const renderLogin = () => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        <Login />
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('Login Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it('should render login form', () => {
    renderLogin();

    expect(screen.getByText('Field Services')).toBeInTheDocument();
    expect(screen.getByText('Management System')).toBeInTheDocument();
    expect(screen.getByLabelText('Username')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('should show validation errors for empty fields', async () => {
    const user = userEvent.setup();
    renderLogin();

    const submitButton = screen.getByRole('button', { name: /login/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Username is required')).toBeInTheDocument();
      expect(screen.getByText('Password is required')).toBeInTheDocument();
    });
  });

  it('should show validation error for short password', async () => {
    const user = userEvent.setup();
    renderLogin();

    const passwordInput = screen.getByLabelText('Password');
    await user.type(passwordInput, 'ab');

    const submitButton = screen.getByRole('button', { name: /login/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Password must be at least 3 characters')).toBeInTheDocument();
    });
  });

  it('should clear field error when user starts typing', async () => {
    const user = userEvent.setup();
    renderLogin();

    const submitButton = screen.getByRole('button', { name: /login/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Username is required')).toBeInTheDocument();
    });

    const usernameInput = screen.getByLabelText('Username');
    await user.type(usernameInput, 'test');

    await waitFor(() => {
      expect(screen.queryByText('Username is required')).not.toBeInTheDocument();
    });
  });

  it('should submit login form with valid credentials', async () => {
    const user = userEvent.setup();
    const mockResponse = {
      token: 'test-token',
      username: 'testuser',
      email: 'test@example.com',
      role: 'DISPATCHER',
    };

    vi.mocked(authAPI.login).mockResolvedValue(mockResponse);

    renderLogin();

    const usernameInput = screen.getByLabelText('Username');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /login/i });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password');
    await user.click(submitButton);

    await waitFor(() => {
      expect(authAPI.login).toHaveBeenCalledWith('testuser', 'password');
    });
  });

  it('should show error message on login failure', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Invalid username or password';

    vi.mocked(authAPI.login).mockRejectedValue({
      response: { data: { message: errorMessage } },
    });

    renderLogin();

    const usernameInput = screen.getByLabelText('Username');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /login/i });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'wrongpassword');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should disable form inputs during submission', async () => {
    const user = userEvent.setup();
    vi.mocked(authAPI.login).mockImplementation(
      () => new Promise((resolve) => setTimeout(resolve, 100))
    );

    renderLogin();

    const usernameInput = screen.getByLabelText('Username');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /login/i });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password');
    await user.click(submitButton);

    expect(usernameInput).toBeDisabled();
    expect(passwordInput).toBeDisabled();
    expect(submitButton).toBeDisabled();
    expect(screen.getByText('Logging in...')).toBeInTheDocument();
  });

  it('should clear login error when user starts typing', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Invalid username or password';

    vi.mocked(authAPI.login).mockRejectedValue({
      response: { data: { message: errorMessage } },
    });

    renderLogin();

    const usernameInput = screen.getByLabelText('Username');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /login/i });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'wrongpassword');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });

    await user.type(usernameInput, 'a');

    await waitFor(() => {
      expect(screen.queryByText(errorMessage)).not.toBeInTheDocument();
    });
  });
});
