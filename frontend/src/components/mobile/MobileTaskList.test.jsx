import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import MobileTaskList from './MobileTaskList';

vi.mock('./MobileTaskCard', () => ({
  default: ({ task, onClick }) => (
    <div 
      data-testid={`mobile-task-card-${task.id}`}
      onClick={onClick}
    >
      {task.title}
    </div>
  ),
}));

describe('MobileTaskList Component', () => {
  const mockTasks = [
    {
      id: 1,
      title: 'Repair HVAC',
      description: 'Fix broken AC',
      clientAddress: '123 Main St',
      priority: 'HIGH',
      estimatedDuration: 120,
      status: 'PENDING',
    },
    {
      id: 2,
      title: 'Install Router',
      description: 'Set up WiFi',
      clientAddress: '456 Oak Ave',
      priority: 'MEDIUM',
      estimatedDuration: 60,
      status: 'IN_PROGRESS',
    },
    {
      id: 3,
      title: 'Replace Cable',
      clientAddress: '789 Elm St',
      priority: 'LOW',
      estimatedDuration: 30,
    },
  ];

  it('should render task list header', () => {
    render(<MobileTaskList tasks={[]} />);
    expect(screen.getByText('My Tasks')).toBeInTheDocument();
  });

  it('should render all tasks', () => {
    render(<MobileTaskList tasks={mockTasks} />);
    expect(screen.getByTestId('mobile-task-card-1')).toBeInTheDocument();
    expect(screen.getByTestId('mobile-task-card-2')).toBeInTheDocument();
    expect(screen.getByTestId('mobile-task-card-3')).toBeInTheDocument();
  });

  it('should sort tasks by priority', () => {
    const { container } = render(<MobileTaskList tasks={mockTasks} />);
    const cards = container.querySelectorAll('[data-testid^="mobile-task-card-"]');
    expect(cards[0]).toHaveAttribute('data-testid', 'mobile-task-card-1'); // HIGH
    expect(cards[1]).toHaveAttribute('data-testid', 'mobile-task-card-2'); // MEDIUM
    expect(cards[2]).toHaveAttribute('data-testid', 'mobile-task-card-3'); // LOW
  });

  it('should call onTaskClick when task is clicked', () => {
    const mockOnClick = vi.fn();
    render(<MobileTaskList tasks={mockTasks} onTaskClick={mockOnClick} />);
    
    const taskCard = screen.getByTestId('mobile-task-card-1');
    fireEvent.click(taskCard);
    
    expect(mockOnClick).toHaveBeenCalledWith(mockTasks[0]);
  });

  it('should render refresh button', () => {
    const mockOnRefresh = vi.fn();
    render(<MobileTaskList tasks={[]} onRefresh={mockOnRefresh} />);
    
    const refreshButton = screen.getByText('↻');
    expect(refreshButton).toBeInTheDocument();
  });

  it('should call onRefresh when refresh button is clicked', () => {
    const mockOnRefresh = vi.fn();
    render(<MobileTaskList tasks={[]} onRefresh={mockOnRefresh} />);
    
    const refreshButton = screen.getByText('↻');
    fireEvent.click(refreshButton);
    
    expect(mockOnRefresh).toHaveBeenCalled();
  });

  it('should not render refresh button when onRefresh is not provided', () => {
    render(<MobileTaskList tasks={[]} />);
    expect(screen.queryByText('↻')).not.toBeInTheDocument();
  });

  it('should display loading state', () => {
    render(<MobileTaskList tasks={[]} isLoading={true} />);
    expect(screen.getByText('Loading tasks...')).toBeInTheDocument();
  });

  it('should display empty state when no tasks', () => {
    render(<MobileTaskList tasks={[]} />);
    expect(screen.getByText('No assigned tasks at the moment.')).toBeInTheDocument();
  });

  it('should display error state', () => {
    const mockOnRefresh = vi.fn();
    render(
      <MobileTaskList 
        tasks={[]} 
        error="Failed to load tasks"
        onRefresh={mockOnRefresh}
      />
    );
    
    expect(screen.getByText(/Error: Failed to load tasks/)).toBeInTheDocument();
    expect(screen.getByText('Try Again')).toBeInTheDocument();
  });

  it('should call onRefresh when try again button is clicked in error state', () => {
    const mockOnRefresh = vi.fn();
    render(
      <MobileTaskList 
        tasks={[]} 
        error="Failed to load tasks"
        onRefresh={mockOnRefresh}
      />
    );
    
    const tryAgainButton = screen.getByText('Try Again');
    fireEvent.click(tryAgainButton);
    
    expect(mockOnRefresh).toHaveBeenCalled();
  });

  it('should handle touch start for pull-to-refresh', () => {
    const { container } = render(<MobileTaskList tasks={mockTasks} onRefresh={vi.fn()} />);
    const listContainer = container.firstChild;
    
    // Mock scrollY
    Object.defineProperty(window, 'scrollY', { value: 0, writable: true });
    
    fireEvent.touchStart(listContainer, {
      touches: [{ clientY: 100 }],
    });
    
    // Should not throw error
    expect(listContainer).toBeInTheDocument();
  });

  it('should not show refresh button when loading', () => {
    render(<MobileTaskList tasks={[]} onRefresh={vi.fn()} isLoading={true} />);
    expect(screen.queryByText('↻')).not.toBeInTheDocument();
  });

  it('should sort tasks with same priority by id descending', () => {
    const tasksSamePriority = [
      { id: 1, title: 'Task 1', clientAddress: 'Address 1', priority: 'HIGH' },
      { id: 3, title: 'Task 3', clientAddress: 'Address 3', priority: 'HIGH' },
      { id: 2, title: 'Task 2', clientAddress: 'Address 2', priority: 'HIGH' },
    ];
    
    const { container } = render(<MobileTaskList tasks={tasksSamePriority} />);
    const cards = container.querySelectorAll('[data-testid^="mobile-task-card-"]');
    
    expect(cards[0]).toHaveAttribute('data-testid', 'mobile-task-card-3');
    expect(cards[1]).toHaveAttribute('data-testid', 'mobile-task-card-2');
    expect(cards[2]).toHaveAttribute('data-testid', 'mobile-task-card-1');
  });
});
