import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import TaskCard from './TaskCard';

describe('TaskCard Component', () => {
  const mockTask = {
    id: 1,
    title: 'Fix HVAC System',
    description: 'Customer reports AC not working',
    clientAddress: '123 Main Street, Springfield, IL',
    priority: 'HIGH',
    estimatedDuration: 120,
    status: 'UNASSIGNED',
  };

  it('should render task card with all information', () => {
    render(<TaskCard task={mockTask} />);

    expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
    expect(screen.getByText('Customer reports AC not working')).toBeInTheDocument();
    expect(screen.getByText('123 Main Street, Springfield, IL')).toBeInTheDocument();
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getByText('120 min')).toBeInTheDocument();
    expect(screen.getByText('UNASSIGNED')).toBeInTheDocument();
  });

  it('should render task card without optional description', () => {
    const taskWithoutDescription = {
      ...mockTask,
      description: undefined,
    };
    render(<TaskCard task={taskWithoutDescription} />);

    expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
    expect(screen.queryByText('Customer reports AC not working')).not.toBeInTheDocument();
  });

  it('should render task card without optional estimatedDuration', () => {
    const taskWithoutDuration = {
      ...mockTask,
      estimatedDuration: undefined,
    };
    render(<TaskCard task={taskWithoutDuration} />);

    expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
    expect(screen.queryByText(/min$/)).not.toBeInTheDocument();
  });

  it('should apply correct priority class for HIGH priority', () => {
    const { container } = render(<TaskCard task={mockTask} />);
    const taskCard = container.querySelector('.task-card');
    expect(taskCard).toHaveClass('priority-high');
  });

  it('should apply correct priority class for MEDIUM priority', () => {
    const mediumTask = { ...mockTask, priority: 'MEDIUM' };
    const { container } = render(<TaskCard task={mediumTask} />);
    const taskCard = container.querySelector('.task-card');
    expect(taskCard).toHaveClass('priority-medium');
  });

  it('should apply correct priority class for LOW priority', () => {
    const lowTask = { ...mockTask, priority: 'LOW' };
    const { container } = render(<TaskCard task={lowTask} />);
    const taskCard = container.querySelector('.task-card');
    expect(taskCard).toHaveClass('priority-low');
  });

  it('should handle missing priority with default', () => {
    const taskWithoutPriority = {
      ...mockTask,
      priority: undefined,
    };
    render(<TaskCard task={taskWithoutPriority} />);

    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
  });

  it('should handle missing status', () => {
    const taskWithoutStatus = {
      ...mockTask,
      status: undefined,
    };
    render(<TaskCard task={taskWithoutStatus} />);

    expect(screen.getByText('UNASSIGNED')).toBeInTheDocument();
  });
});
