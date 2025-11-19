import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import MobileTaskCard from './MobileTaskCard';

describe('MobileTaskCard Component', () => {
  const mockTask = {
    id: 1,
    title: 'Repair HVAC System',
    description: 'Fix broken AC unit in building A',
    clientAddress: '123 Main Street, Suite 100',
    priority: 'HIGH',
    estimatedDuration: 120,
    status: 'PENDING',
  };

  it('should render task title', () => {
    render(<MobileTaskCard task={mockTask} />);
    expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
  });

  it('should render task priority', () => {
    render(<MobileTaskCard task={mockTask} />);
    expect(screen.getByText('HIGH')).toBeInTheDocument();
  });

  it('should render task address', () => {
    render(<MobileTaskCard task={mockTask} />);
    expect(screen.getByText('123 Main Street, Suite 100')).toBeInTheDocument();
  });

  it('should render task description', () => {
    render(<MobileTaskCard task={mockTask} />);
    expect(screen.getByText('Fix broken AC unit in building A')).toBeInTheDocument();
  });

  it('should render estimated duration', () => {
    render(<MobileTaskCard task={mockTask} />);
    expect(screen.getByText(/120 min/)).toBeInTheDocument();
  });

  it('should render task status', () => {
    render(<MobileTaskCard task={mockTask} />);
    expect(screen.getByText('PENDING')).toBeInTheDocument();
  });

  it('should call onClick when card is clicked', () => {
    const mockOnClick = vi.fn();
    render(<MobileTaskCard task={mockTask} onClick={mockOnClick} />);
    
    const card = screen.getByRole('button');
    fireEvent.click(card);
    
    expect(mockOnClick).toHaveBeenCalled();
  });

  it('should call onClick when Enter key is pressed', () => {
    const mockOnClick = vi.fn();
    render(<MobileTaskCard task={mockTask} onClick={mockOnClick} />);
    
    const card = screen.getByRole('button');
    fireEvent.keyPress(card, { key: 'Enter', code: 'Enter', charCode: 13 });
    
    expect(mockOnClick).toHaveBeenCalled();
  });

  it('should not call onClick when other key is pressed', () => {
    const mockOnClick = vi.fn();
    render(<MobileTaskCard task={mockTask} onClick={mockOnClick} />);
    
    const card = screen.getByRole('button');
    fireEvent.keyPress(card, { key: 'Space', code: 'Space' });
    
    expect(mockOnClick).not.toHaveBeenCalled();
  });

  it('should render without description', () => {
    const taskNoDescription = { ...mockTask, description: undefined };
    render(<MobileTaskCard task={taskNoDescription} />);
    
    expect(screen.queryByText('Fix broken AC unit in building A')).not.toBeInTheDocument();
  });

  it('should render without estimated duration', () => {
    const taskNoDuration = { ...mockTask, estimatedDuration: undefined };
    render(<MobileTaskCard task={taskNoDuration} />);
    
    expect(screen.queryByText(/min/)).not.toBeInTheDocument();
  });

  it('should render without status', () => {
    const taskNoStatus = { ...mockTask, status: undefined };
    render(<MobileTaskCard task={taskNoStatus} />);
    
    expect(screen.queryByText('PENDING')).not.toBeInTheDocument();
  });

  it('should default to MEDIUM priority if not provided', () => {
    const taskNoPriority = { ...mockTask, priority: undefined };
    render(<MobileTaskCard task={taskNoPriority} />);
    
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
  });

  it('should render with MEDIUM priority', () => {
    const mediumTask = { ...mockTask, priority: 'MEDIUM' };
    render(<MobileTaskCard task={mediumTask} />);
    
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
  });

  it('should render with LOW priority', () => {
    const lowTask = { ...mockTask, priority: 'LOW' };
    render(<MobileTaskCard task={lowTask} />);
    
    expect(screen.getByText('LOW')).toBeInTheDocument();
  });

  it('should render with IN_PROGRESS status', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
    render(<MobileTaskCard task={inProgressTask} />);
    
    expect(screen.getByText('IN PROGRESS')).toBeInTheDocument();
  });

  it('should render with COMPLETED status', () => {
    const completedTask = { ...mockTask, status: 'COMPLETED' };
    render(<MobileTaskCard task={completedTask} />);
    
    expect(screen.getByText('COMPLETED')).toBeInTheDocument();
  });

  it('should render address icon', () => {
    render(<MobileTaskCard task={mockTask} />);
    expect(screen.getByText('📍')).toBeInTheDocument();
  });

  it('should render arrow icon', () => {
    render(<MobileTaskCard task={mockTask} />);
    expect(screen.getByText('›')).toBeInTheDocument();
  });
});
