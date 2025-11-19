import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskForm from './TaskForm';

describe('TaskForm Component', () => {
  const mockOnSubmit = vi.fn();
  const mockOnCancel = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render task form with all fields', () => {
    render(<TaskForm onSubmit={mockOnSubmit} />);

    expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/client address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/priority/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/estimated duration/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create task/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
  });

  it('should show validation error when title is empty', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    expect(await screen.findByText('Title is required')).toBeInTheDocument();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should show validation error when title is too short', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    await user.type(titleInput, 'Ab');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    expect(await screen.findByText('Title must be at least 3 characters')).toBeInTheDocument();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should show validation error when title is too long', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const longTitle = 'A'.repeat(201);
    await user.type(titleInput, longTitle);
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    expect(await screen.findByText('Title must not exceed 200 characters')).toBeInTheDocument();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should show validation error when address is empty', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    await user.type(titleInput, 'Valid Title');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    expect(await screen.findByText('Address is required')).toBeInTheDocument();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should show validation error when address is too short', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const addressInput = screen.getByLabelText(/client address/i);
    
    await user.type(titleInput, 'Valid Title');
    await user.type(addressInput, 'A1');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    expect(await screen.findByText('Address must be at least 5 characters')).toBeInTheDocument();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should show validation error when address lacks numbers', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const addressInput = screen.getByLabelText(/client address/i);
    
    await user.type(titleInput, 'Valid Title');
    await user.type(addressInput, 'Main Street');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    expect(await screen.findByText('Address must contain both letters and numbers')).toBeInTheDocument();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should show validation error when address lacks letters', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const addressInput = screen.getByLabelText(/client address/i);
    
    await user.type(titleInput, 'Valid Title');
    await user.type(addressInput, '12345');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    expect(await screen.findByText('Address must contain both letters and numbers')).toBeInTheDocument();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should handle zero duration as valid', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const addressInput = screen.getByLabelText(/client address/i);
    const durationInput = screen.getByLabelText(/estimated duration/i);
    
    await user.type(titleInput, 'Valid Title');
    await user.type(addressInput, '123 Main Street');
    await user.clear(durationInput);
    await user.type(durationInput, '0');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        title: 'Valid Title',
        description: undefined,
        clientAddress: '123 Main Street',
        priority: 'MEDIUM',
        estimatedDuration: 0,
      });
    });
  });

  it('should submit valid form data', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const descriptionInput = screen.getByLabelText(/description/i);
    const addressInput = screen.getByLabelText(/client address/i);
    const prioritySelect = screen.getByLabelText(/priority/i);
    const durationInput = screen.getByLabelText(/estimated duration/i);
    
    await user.type(titleInput, 'Fix HVAC System');
    await user.type(descriptionInput, 'Customer reports AC not working');
    await user.type(addressInput, '123 Main Street');
    await user.selectOptions(prioritySelect, 'HIGH');
    await user.type(durationInput, '120');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        title: 'Fix HVAC System',
        description: 'Customer reports AC not working',
        clientAddress: '123 Main Street',
        priority: 'HIGH',
        estimatedDuration: 120,
      });
    });
  });

  it('should submit form without optional fields', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const addressInput = screen.getByLabelText(/client address/i);
    
    await user.type(titleInput, 'Fix HVAC System');
    await user.type(addressInput, '123 Main Street');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        title: 'Fix HVAC System',
        description: undefined,
        clientAddress: '123 Main Street',
        priority: 'MEDIUM',
        estimatedDuration: undefined,
      });
    });
  });

  it('should clear error when user types in field', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    expect(await screen.findByText('Title is required')).toBeInTheDocument();

    const titleInput = screen.getByLabelText(/title/i);
    await user.type(titleInput, 'Valid Title');

    await waitFor(() => {
      expect(screen.queryByText('Title is required')).not.toBeInTheDocument();
    });
  });

  it('should call onCancel and reset form when cancel is clicked', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

    const titleInput = screen.getByLabelText(/title/i);
    await user.type(titleInput, 'Some Title');

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalled();
    expect(titleInput.value).toBe('');
  });

  it('should disable form fields when submitting', () => {
    render(<TaskForm onSubmit={mockOnSubmit} isSubmitting={true} />);

    expect(screen.getByLabelText(/title/i)).toBeDisabled();
    expect(screen.getByLabelText(/description/i)).toBeDisabled();
    expect(screen.getByLabelText(/client address/i)).toBeDisabled();
    expect(screen.getByLabelText(/priority/i)).toBeDisabled();
    expect(screen.getByLabelText(/estimated duration/i)).toBeDisabled();
    expect(screen.getByRole('button', { name: /creating.../i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
  });

  it('should trim whitespace from inputs', async () => {
    const user = userEvent.setup();
    render(<TaskForm onSubmit={mockOnSubmit} />);

    const titleInput = screen.getByLabelText(/title/i);
    const addressInput = screen.getByLabelText(/client address/i);
    
    await user.type(titleInput, '  Fix HVAC  ');
    await user.type(addressInput, '  123 Main Street  ');
    
    const submitButton = screen.getByRole('button', { name: /create task/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        title: 'Fix HVAC',
        description: undefined,
        clientAddress: '123 Main Street',
        priority: 'MEDIUM',
        estimatedDuration: undefined,
      });
    });
  });
});
