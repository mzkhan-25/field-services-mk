import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import TaskMarker from './TaskMarker';

// Mock react-leaflet components
vi.mock('react-leaflet', () => ({
  Marker: ({ children }) => <div data-testid="marker">{children}</div>,
  Popup: ({ children }) => <div data-testid="popup">{children}</div>,
}));

describe('TaskMarker Component', () => {
  const mockTask = {
    id: 1,
    title: 'Repair HVAC System',
    priority: 'HIGH',
    clientAddress: '123 Main Street',
    description: 'Fix broken AC unit',
    estimatedDuration: 120,
    location: { lat: 37.7749, lng: -122.4194 },
  };

  it('should render marker and popup', () => {
    render(<TaskMarker task={mockTask} />);
    expect(screen.getByTestId('marker')).toBeInTheDocument();
    expect(screen.getByTestId('popup')).toBeInTheDocument();
  });

  it('should display task title', () => {
    render(<TaskMarker task={mockTask} />);
    expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
  });

  it('should display task priority', () => {
    render(<TaskMarker task={mockTask} />);
    expect(screen.getByText(/Priority:/)).toBeInTheDocument();
    expect(screen.getByText(/HIGH/)).toBeInTheDocument();
  });

  it('should display task address', () => {
    render(<TaskMarker task={mockTask} />);
    expect(screen.getByText(/Address:/)).toBeInTheDocument();
    expect(screen.getByText(/123 Main Street/)).toBeInTheDocument();
  });

  it('should display task description when available', () => {
    render(<TaskMarker task={mockTask} />);
    expect(screen.getByText(/Description:/)).toBeInTheDocument();
    expect(screen.getByText(/Fix broken AC unit/)).toBeInTheDocument();
  });

  it('should not display description when not available', () => {
    const taskNoDescription = { ...mockTask, description: undefined };
    render(<TaskMarker task={taskNoDescription} />);
    expect(screen.queryByText(/Description:/)).not.toBeInTheDocument();
  });

  it('should display estimated duration when available', () => {
    render(<TaskMarker task={mockTask} />);
    expect(screen.getByText(/Estimated Duration:/)).toBeInTheDocument();
    expect(screen.getByText(/120 minutes/)).toBeInTheDocument();
  });

  it('should not display estimated duration when not available', () => {
    const taskNoDuration = { ...mockTask, estimatedDuration: undefined };
    render(<TaskMarker task={taskNoDuration} />);
    expect(screen.queryByText(/Estimated Duration:/)).not.toBeInTheDocument();
  });

  it('should display assign button when onAssign is provided', () => {
    const mockOnAssign = vi.fn();
    render(<TaskMarker task={mockTask} onAssign={mockOnAssign} />);
    expect(screen.getByText('Assign Task')).toBeInTheDocument();
  });

  it('should not display assign button when onAssign is not provided', () => {
    render(<TaskMarker task={mockTask} />);
    expect(screen.queryByText('Assign Task')).not.toBeInTheDocument();
  });

  it('should call onAssign with task id when assign button is clicked', () => {
    const mockOnAssign = vi.fn();
    render(<TaskMarker task={mockTask} onAssign={mockOnAssign} />);
    const assignButton = screen.getByText('Assign Task');
    fireEvent.click(assignButton);
    expect(mockOnAssign).toHaveBeenCalledWith(1);
  });

  it('should default to MEDIUM priority if not provided', () => {
    const taskNoPriority = { ...mockTask, priority: undefined };
    render(<TaskMarker task={taskNoPriority} />);
    expect(screen.getByText(/MEDIUM/)).toBeInTheDocument();
  });

  it('should render with MEDIUM priority', () => {
    const mediumTask = { ...mockTask, priority: 'MEDIUM' };
    render(<TaskMarker task={mediumTask} />);
    expect(screen.getByText(/MEDIUM/)).toBeInTheDocument();
  });

  it('should render with LOW priority', () => {
    const lowTask = { ...mockTask, priority: 'LOW' };
    render(<TaskMarker task={lowTask} />);
    expect(screen.getByText(/LOW/)).toBeInTheDocument();
  });

  it('should apply priority class to priority text', () => {
    render(<TaskMarker task={mockTask} />);
    const priorityElement = screen.getByText('HIGH');
    expect(priorityElement).toHaveClass('priority-high');
  });
});
