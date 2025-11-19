import { describe, it, expect, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskList from './TaskList';

describe('TaskList Component', () => {
  const mockTasks = [
    {
      id: 1,
      title: 'High Priority Task',
      description: 'Urgent task',
      clientAddress: '123 Main St',
      priority: 'HIGH',
      estimatedDuration: 60,
      status: 'UNASSIGNED',
    },
    {
      id: 2,
      title: 'Medium Priority Task',
      description: 'Normal task',
      clientAddress: '456 Oak Ave',
      priority: 'MEDIUM',
      estimatedDuration: 90,
      status: 'UNASSIGNED',
    },
    {
      id: 3,
      title: 'Low Priority Task',
      description: 'Can wait',
      clientAddress: '789 Pine Rd',
      priority: 'LOW',
      estimatedDuration: 30,
      status: 'UNASSIGNED',
    },
    {
      id: 4,
      title: 'Another High Priority',
      description: 'Also urgent',
      clientAddress: '321 Elm St',
      priority: 'HIGH',
      estimatedDuration: 45,
      status: 'UNASSIGNED',
    },
  ];

  it('should render task list with all tasks', () => {
    render(<TaskList tasks={mockTasks} />);

    expect(screen.getByText('Unassigned Tasks')).toBeInTheDocument();
    expect(screen.getByText('High Priority Task')).toBeInTheDocument();
    expect(screen.getByText('Medium Priority Task')).toBeInTheDocument();
    expect(screen.getByText('Low Priority Task')).toBeInTheDocument();
    expect(screen.getByText('Another High Priority')).toBeInTheDocument();
  });

  it('should sort tasks by priority (HIGH, MEDIUM, LOW)', () => {
    const { container } = render(<TaskList tasks={mockTasks} />);
    
    const taskCards = container.querySelectorAll('.task-card');
    const titles = Array.from(taskCards).map(
      (card) => card.querySelector('.task-title').textContent
    );

    // HIGH priority tasks should come first, then MEDIUM, then LOW
    expect(titles[0]).toMatch(/High Priority Task|Another High Priority/);
    expect(titles[1]).toMatch(/High Priority Task|Another High Priority/);
    expect(titles[2]).toBe('Medium Priority Task');
    expect(titles[3]).toBe('Low Priority Task');
  });

  it('should filter tasks by HIGH priority', async () => {
    const user = userEvent.setup();
    render(<TaskList tasks={mockTasks} />);

    const filterSelect = screen.getByLabelText(/filter by priority/i);
    await user.selectOptions(filterSelect, 'HIGH');

    expect(screen.getByText('High Priority Task')).toBeInTheDocument();
    expect(screen.getByText('Another High Priority')).toBeInTheDocument();
    expect(screen.queryByText('Medium Priority Task')).not.toBeInTheDocument();
    expect(screen.queryByText('Low Priority Task')).not.toBeInTheDocument();
  });

  it('should filter tasks by MEDIUM priority', async () => {
    const user = userEvent.setup();
    render(<TaskList tasks={mockTasks} />);

    const filterSelect = screen.getByLabelText(/filter by priority/i);
    await user.selectOptions(filterSelect, 'MEDIUM');

    expect(screen.getByText('Medium Priority Task')).toBeInTheDocument();
    expect(screen.queryByText('High Priority Task')).not.toBeInTheDocument();
    expect(screen.queryByText('Low Priority Task')).not.toBeInTheDocument();
  });

  it('should filter tasks by LOW priority', async () => {
    const user = userEvent.setup();
    render(<TaskList tasks={mockTasks} />);

    const filterSelect = screen.getByLabelText(/filter by priority/i);
    await user.selectOptions(filterSelect, 'LOW');

    expect(screen.getByText('Low Priority Task')).toBeInTheDocument();
    expect(screen.queryByText('High Priority Task')).not.toBeInTheDocument();
    expect(screen.queryByText('Medium Priority Task')).not.toBeInTheDocument();
  });

  it('should show all tasks when filter is set to ALL', async () => {
    const user = userEvent.setup();
    render(<TaskList tasks={mockTasks} />);

    const filterSelect = screen.getByLabelText(/filter by priority/i);
    
    // First filter to HIGH
    await user.selectOptions(filterSelect, 'HIGH');
    expect(screen.queryByText('Medium Priority Task')).not.toBeInTheDocument();

    // Then back to ALL
    await user.selectOptions(filterSelect, 'ALL');
    expect(screen.getByText('High Priority Task')).toBeInTheDocument();
    expect(screen.getByText('Medium Priority Task')).toBeInTheDocument();
    expect(screen.getByText('Low Priority Task')).toBeInTheDocument();
  });

  it('should display empty state when no tasks', () => {
    render(<TaskList tasks={[]} />);

    expect(screen.getByText('No unassigned tasks at the moment.')).toBeInTheDocument();
  });

  it('should display filtered empty state message', async () => {
    const user = userEvent.setup();
    const tasksWithoutLow = mockTasks.filter((t) => t.priority !== 'LOW');
    render(<TaskList tasks={tasksWithoutLow} />);

    const filterSelect = screen.getByLabelText(/filter by priority/i);
    await user.selectOptions(filterSelect, 'LOW');

    expect(screen.getByText('No unassigned tasks with LOW priority.')).toBeInTheDocument();
  });

  it('should display loading state', () => {
    render(<TaskList tasks={[]} isLoading={true} />);

    expect(screen.getByText('Loading tasks...')).toBeInTheDocument();
  });

  it('should display error state with error message', () => {
    const errorMessage = 'Failed to fetch tasks';
    render(<TaskList tasks={[]} error={errorMessage} />);

    expect(screen.getByText(`Error loading tasks: ${errorMessage}`)).toBeInTheDocument();
  });

  it('should call onRefresh when refresh button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnRefresh = vi.fn();
    render(<TaskList tasks={mockTasks} onRefresh={mockOnRefresh} />);

    const refreshButton = screen.getByRole('button', { name: /refresh/i });
    await user.click(refreshButton);

    expect(mockOnRefresh).toHaveBeenCalled();
  });

  it('should show "Try Again" button in error state', async () => {
    const user = userEvent.setup();
    const mockOnRefresh = vi.fn();
    render(<TaskList tasks={[]} error="Network error" onRefresh={mockOnRefresh} />);

    const tryAgainButton = screen.getByRole('button', { name: /try again/i });
    await user.click(tryAgainButton);

    expect(mockOnRefresh).toHaveBeenCalled();
  });

  it('should disable refresh button when loading', () => {
    const mockOnRefresh = vi.fn();
    render(<TaskList tasks={mockTasks} onRefresh={mockOnRefresh} isLoading={true} />);

    const refreshButton = screen.getByRole('button', { name: /loading.../i });
    expect(refreshButton).toBeDisabled();
  });

  it('should not render refresh button when onRefresh is not provided', () => {
    render(<TaskList tasks={mockTasks} />);

    expect(screen.queryByRole('button', { name: /refresh/i })).not.toBeInTheDocument();
  });

  it('should handle tasks without priority gracefully', () => {
    const tasksWithoutPriority = [
      {
        id: 1,
        title: 'Task Without Priority',
        clientAddress: '123 Main St',
        status: 'UNASSIGNED',
      },
    ];
    
    render(<TaskList tasks={tasksWithoutPriority} />);
    expect(screen.getByText('Task Without Priority')).toBeInTheDocument();
  });
});
