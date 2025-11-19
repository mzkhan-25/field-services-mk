import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import TechnicianMarker from './TechnicianMarker';

// Mock react-leaflet components
vi.mock('react-leaflet', () => ({
  Marker: ({ children }) => <div data-testid="marker">{children}</div>,
  Popup: ({ children }) => <div data-testid="popup">{children}</div>,
}));

describe('TechnicianMarker Component', () => {
  const mockTechnician = {
    id: 1,
    name: 'John Doe',
    status: 'AVAILABLE',
    workload: 3,
    currentTask: 'Installing fiber optic',
    location: { lat: 37.7749, lng: -122.4194 },
  };

  it('should render marker and popup', () => {
    render(<TechnicianMarker technician={mockTechnician} />);
    expect(screen.getByTestId('marker')).toBeInTheDocument();
    expect(screen.getByTestId('popup')).toBeInTheDocument();
  });

  it('should display technician name', () => {
    render(<TechnicianMarker technician={mockTechnician} />);
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });

  it('should display technician status', () => {
    render(<TechnicianMarker technician={mockTechnician} />);
    expect(screen.getByText(/Status:/)).toBeInTheDocument();
    expect(screen.getByText(/AVAILABLE/)).toBeInTheDocument();
  });

  it('should display technician workload', () => {
    render(<TechnicianMarker technician={mockTechnician} />);
    expect(screen.getByText(/Workload:/)).toBeInTheDocument();
    expect(screen.getByText(/3 tasks/)).toBeInTheDocument();
  });

  it('should display current task when available', () => {
    render(<TechnicianMarker technician={mockTechnician} />);
    expect(screen.getByText(/Current Task:/)).toBeInTheDocument();
    expect(screen.getByText(/Installing fiber optic/)).toBeInTheDocument();
  });

  it('should not display current task when not available', () => {
    const technicianNoTask = { ...mockTechnician, currentTask: undefined };
    render(<TechnicianMarker technician={technicianNoTask} />);
    expect(screen.queryByText(/Current Task:/)).not.toBeInTheDocument();
  });

  it('should default to AVAILABLE status if not provided', () => {
    const technicianNoStatus = { 
      ...mockTechnician, 
      status: undefined,
    };
    render(<TechnicianMarker technician={technicianNoStatus} />);
    expect(screen.getByText(/AVAILABLE/)).toBeInTheDocument();
  });

  it('should default to 0 workload if not provided', () => {
    const technicianNoWorkload = { 
      ...mockTechnician, 
      workload: undefined,
    };
    render(<TechnicianMarker technician={technicianNoWorkload} />);
    expect(screen.getByText(/0 tasks/)).toBeInTheDocument();
  });

  it('should render with BUSY status', () => {
    const busyTechnician = { ...mockTechnician, status: 'BUSY' };
    render(<TechnicianMarker technician={busyTechnician} />);
    expect(screen.getByText(/BUSY/)).toBeInTheDocument();
  });

  it('should render with OFFLINE status', () => {
    const offlineTechnician = { ...mockTechnician, status: 'OFFLINE' };
    render(<TechnicianMarker technician={offlineTechnician} />);
    expect(screen.getByText(/OFFLINE/)).toBeInTheDocument();
  });
});
