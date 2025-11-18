#!/bin/bash

# Field Services Management System - Local Development Setup Script

set -e

echo "========================================"
echo "Field Services Management System"
echo "Local Development Setup"
echo "========================================"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "Error: Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "Creating .env file from .env.example..."
    cp .env.example .env
    echo "✓ Created .env file"
    echo ""
    echo "⚠️  Please review and update the .env file with your configuration."
    echo ""
fi

# Parse command line arguments
COMMAND=${1:-up}

case $COMMAND in
    up)
        echo "Starting all services..."
        docker-compose up -d
        echo ""
        echo "✓ Services started successfully!"
        echo ""
        echo "Access the application at:"
        echo "  - Frontend: http://localhost:5173"
        echo "  - Backend API: http://localhost:8080/api"
        echo "  - Database: localhost:5432"
        echo ""
        echo "To view logs, run: ./start-dev.sh logs"
        ;;
    
    down)
        echo "Stopping all services..."
        docker-compose down
        echo "✓ Services stopped successfully!"
        ;;
    
    restart)
        echo "Restarting all services..."
        docker-compose restart
        echo "✓ Services restarted successfully!"
        ;;
    
    logs)
        echo "Showing logs (Ctrl+C to exit)..."
        docker-compose logs -f
        ;;
    
    build)
        echo "Building all services..."
        docker-compose build
        echo "✓ Build completed successfully!"
        ;;
    
    clean)
        echo "Stopping services and removing volumes..."
        read -p "This will delete all data. Are you sure? (y/N) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            docker-compose down -v
            echo "✓ Services stopped and volumes removed!"
        else
            echo "Operation cancelled."
        fi
        ;;
    
    status)
        echo "Service status:"
        docker-compose ps
        ;;
    
    help|*)
        echo "Usage: ./start-dev.sh [COMMAND]"
        echo ""
        echo "Commands:"
        echo "  up       - Start all services (default)"
        echo "  down     - Stop all services"
        echo "  restart  - Restart all services"
        echo "  logs     - Show and follow logs"
        echo "  build    - Build all services"
        echo "  clean    - Stop services and remove all data"
        echo "  status   - Show service status"
        echo "  help     - Show this help message"
        ;;
esac
