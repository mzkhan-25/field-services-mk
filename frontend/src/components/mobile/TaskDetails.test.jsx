import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import TaskDetails from './TaskDetails';

describe('TaskDetails Component', () => {
  const mockTask = {
    id: 1,
    title: 'Repair HVAC System',
    description: 'Fix broken AC unit in building A',
    clientAddress: '123 Main Street, Suite 100',
    priority: 'HIGH',
    estimatedDuration: 120,
    status: 'PENDING',
  };

  beforeEach(() => {
    // Mock window.open
    vi.stubGlobal('open', vi.fn());
  });

  it('should render task details', () => {
    render(<TaskDetails task={mockTask} />);
    
    expect(screen.getByText('Task Details')).toBeInTheDocument();
    expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
    expect(screen.getByText('Fix broken AC unit in building A')).toBeInTheDocument();
    expect(screen.getByText('123 Main Street, Suite 100')).toBeInTheDocument();
  });

  it('should render priority badge', () => {
    render(<TaskDetails task={mockTask} />);
    expect(screen.getByText('HIGH')).toBeInTheDocument();
  });

  it('should render status badge', () => {
    render(<TaskDetails task={mockTask} />);
    expect(screen.getByText('PENDING')).toBeInTheDocument();
  });

  it('should render estimated duration', () => {
    render(<TaskDetails task={mockTask} />);
    expect(screen.getByText(/120 minutes/)).toBeInTheDocument();
  });

  it('should render navigate button', () => {
    render(<TaskDetails task={mockTask} />);
    expect(screen.getByText(/Navigate/)).toBeInTheDocument();
  });

  it('should render start button for PENDING tasks', () => {
    render(<TaskDetails task={mockTask} />);
    expect(screen.getByText('Start Task')).toBeInTheDocument();
  });

  it('should not render start button for IN_PROGRESS tasks', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
    render(<TaskDetails task={inProgressTask} />);
    expect(screen.queryByText('Start Task')).not.toBeInTheDocument();
  });

  it('should render complete button for IN_PROGRESS tasks', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
    render(<TaskDetails task={inProgressTask} />);
    expect(screen.getByText('Complete Task')).toBeInTheDocument();
  });

  it('should not render complete button for PENDING tasks', () => {
    render(<TaskDetails task={mockTask} />);
    expect(screen.queryByText('Complete Task')).not.toBeInTheDocument();
  });

  it('should not render start or complete button for COMPLETED tasks', () => {
    const completedTask = { ...mockTask, status: 'COMPLETED' };
    render(<TaskDetails task={completedTask} />);
    expect(screen.queryByText('Start Task')).not.toBeInTheDocument();
    expect(screen.queryByText('Complete Task')).not.toBeInTheDocument();
  });

  it('should call onStatusChange with IN_PROGRESS when start button is clicked', () => {
    const mockOnStatusChange = vi.fn();
    render(<TaskDetails task={mockTask} onStatusChange={mockOnStatusChange} />);
    
    const startButton = screen.getByText('Start Task');
    fireEvent.click(startButton);
    
    expect(mockOnStatusChange).toHaveBeenCalledWith(1, 'IN_PROGRESS');
  });

  it('should show work summary form when complete button is clicked', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
    render(<TaskDetails task={inProgressTask} />);
    
    const completeButton = screen.getByText('Complete Task');
    fireEvent.click(completeButton);
    
    expect(screen.getByText('Complete Task')).toBeInTheDocument();
    expect(screen.getByLabelText('Work Summary *')).toBeInTheDocument();
  });

  it('should allow entering work summary', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
    render(<TaskDetails task={inProgressTask} />);
    
    fireEvent.click(screen.getByText('Complete Task'));
    
    const textarea = screen.getByLabelText('Work Summary *');
    fireEvent.change(textarea, { target: { value: 'Replaced AC filter and recharged refrigerant' } });
    
    expect(textarea).toHaveValue('Replaced AC filter and recharged refrigerant');
  });

  it('should submit completion with work summary', () => {
    const mockOnStatusChange = vi.fn();
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
    render(<TaskDetails task={inProgressTask} onStatusChange={mockOnStatusChange} />);
    
    fireEvent.click(screen.getByText('Complete Task'));
    
    const textarea = screen.getByLabelText('Work Summary *');
    fireEvent.change(textarea, { target: { value: 'Work completed successfully' } });
    
    const submitButton = screen.getByText('Submit Completion');
    fireEvent.click(submitButton);
    
    expect(mockOnStatusChange).toHaveBeenCalledWith(1, 'COMPLETED', 'Work completed successfully');
  });

  it('should disable submit button when work summary is empty', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
    render(<TaskDetails task={inProgressTask} />);
    
    fireEvent.click(screen.getByText('Complete Task'));
    
    const submitButton = screen.getByText('Submit Completion');
    expect(submitButton).toBeDisabled();
  });

  it('should cancel work summary form', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
    render(<TaskDetails task={inProgressTask} />);
    
    fireEvent.click(screen.getByText('Complete Task'));
    expect(screen.getByLabelText('Work Summary *')).toBeInTheDocument();
    
    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);
    
    expect(screen.queryByLabelText('Work Summary *')).not.toBeInTheDocument();
  });

  it('should call onNavigate when navigate button is clicked', () => {
    const mockOnNavigate = vi.fn();
    render(<TaskDetails task={mockTask} onNavigate={mockOnNavigate} />);
    
    const navigateButton = screen.getByText(/Navigate/);
    fireEvent.click(navigateButton);
    
    expect(mockOnNavigate).toHaveBeenCalledWith('123 Main Street, Suite 100');
  });

  it('should open maps when navigate is clicked without onNavigate', () => {
    const mockWindowOpen = vi.fn();
    window.open = mockWindowOpen;
    
    render(<TaskDetails task={mockTask} />);
    
    const navigateButton = screen.getByText(/Navigate/);
    fireEvent.click(navigateButton);
    
    expect(mockWindowOpen).toHaveBeenCalled();
  });

  it('should call onBack when back button is clicked', () => {
    const mockOnBack = vi.fn();
    render(<TaskDetails task={mockTask} onBack={mockOnBack} />);
    
    const backButton = screen.getByText('‹ Back');
    fireEvent.click(backButton);
    
    expect(mockOnBack).toHaveBeenCalled();
  });

  it('should not render back button when onBack is not provided', () => {
    render(<TaskDetails task={mockTask} />);
    expect(screen.queryByText('‹ Back')).not.toBeInTheDocument();
  });

  it('should disable buttons when isUpdating is true', () => {
    render(<TaskDetails task={mockTask} isUpdating={true} />);
    
    const startButton = screen.getByText('Starting...');
    expect(startButton).toBeDisabled();
  });

  it('should render without description', () => {
    const taskNoDescription = { ...mockTask, description: undefined };
    render(<TaskDetails task={taskNoDescription} />);
    
    expect(screen.queryByText('Fix broken AC unit in building A')).not.toBeInTheDocument();
  });

  it('should render without estimated duration', () => {
    const taskNoDuration = { ...mockTask, estimatedDuration: undefined };
    render(<TaskDetails task={taskNoDuration} />);
    
    expect(screen.queryByText(/minutes/)).not.toBeInTheDocument();
  });

  it('should render without status', () => {
    const taskNoStatus = { ...mockTask, status: undefined };
    render(<TaskDetails task={taskNoStatus} />);
    
    expect(screen.queryByText('PENDING')).not.toBeInTheDocument();
  });
});
