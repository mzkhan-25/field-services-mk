# Field Services Management Backend API

## Overview
This document describes the REST API endpoints for the Field Services Management Backend, including:
- Task Management
- Task Assignment
- Location Tracking
- Task Status Management

## Base URL
All endpoints are prefixed with `/api` (configured in `application.properties`)

## Authentication
All endpoints require JWT authentication. Include the JWT token in the `Authorization` header:
```
Authorization: Bearer <your-jwt-token>
```

## Task Management Endpoints

### Create Task
Create a new service task.

**Endpoint:** `POST /api/tasks`

**Access:** DISPATCHER, SUPERVISOR

**Request Body:**
```json
{
  "title": "Fix HVAC System",
  "description": "Customer reports AC not working",
  "clientAddress": "123 Main Street, Springfield, IL 62701",
  "priority": "HIGH",
  "estimatedDuration": 120
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "title": "Fix HVAC System",
  "description": "Customer reports AC not working",
  "clientAddress": "123 Main Street, Springfield, IL 62701",
  "priority": "HIGH",
  "estimatedDuration": 120,
  "status": "UNASSIGNED",
  "assignedTechnicianId": null,
  "assignedTechnicianName": null,
  "assignedAt": null,
  "assignedById": null,
  "assignedByName": null,
  "createdAt": "2025-11-18T23:00:00",
  "updatedAt": "2025-11-18T23:00:00"
}
```

**Validations:**
- `title`: Required, 3-200 characters
- `clientAddress`: Required, 5-500 characters, must contain both letters and numbers
- `priority`: Required, one of: HIGH, MEDIUM, LOW
- `estimatedDuration`: Optional, in minutes

---

### Get All Tasks
Retrieve all tasks.

**Endpoint:** `GET /api/tasks`

**Access:** All authenticated users

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Fix HVAC System",
    "clientAddress": "123 Main Street, Springfield, IL 62701",
    "priority": "HIGH",
    "status": "UNASSIGNED",
    ...
  },
  {
    "id": 2,
    "title": "Plumbing Repair",
    "clientAddress": "456 Oak Avenue, Springfield, IL 62702",
    "priority": "MEDIUM",
    "status": "ASSIGNED",
    ...
  }
]
```

---

### Get Unassigned Tasks
Retrieve all unassigned tasks, sorted by priority (HIGH → MEDIUM → LOW).

**Endpoint:** `GET /api/tasks/unassigned`

**Access:** All authenticated users

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Fix HVAC System",
    "priority": "HIGH",
    "status": "UNASSIGNED",
    ...
  },
  {
    "id": 3,
    "title": "Routine Maintenance",
    "priority": "LOW",
    "status": "UNASSIGNED",
    ...
  }
]
```

---

### Get Task by ID
Retrieve details of a specific task.

**Endpoint:** `GET /api/tasks/{id}`

**Access:** All authenticated users

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Fix HVAC System",
  "description": "Customer reports AC not working",
  "clientAddress": "123 Main Street, Springfield, IL 62701",
  "priority": "HIGH",
  "estimatedDuration": 120,
  "status": "UNASSIGNED",
  ...
}
```

**Error Response:** `404 Not Found` if task doesn't exist

---

### Update Task
Update task details.

**Endpoint:** `PUT /api/tasks/{id}`

**Access:** DISPATCHER, SUPERVISOR

**Request Body:** (all fields optional)
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "clientAddress": "789 Pine Road, Springfield, IL 62703",
  "priority": "LOW",
  "estimatedDuration": 90
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Updated Title",
  "description": "Updated description",
  ...
}
```

**Error Responses:**
- `404 Not Found` if task doesn't exist
- `400 Bad Request` if address validation fails

---

## Task Assignment Endpoints

### Assign Task to Technician
Assign a task to an available technician.

**Endpoint:** `POST /api/tasks/{id}/assign`

**Access:** DISPATCHER, SUPERVISOR

**Request Body:**
```json
{
  "technicianId": 5
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Fix HVAC System",
  "status": "ASSIGNED",
  "assignedTechnicianId": 5,
  "assignedTechnicianName": "john_tech",
  "assignedAt": "2025-11-18T23:05:00",
  "assignedById": 2,
  "assignedByName": "dispatcher1",
  ...
}
```

**Business Rules:**
- Task must not be already assigned
- Technician must exist and have TECHNICIAN role
- Technician must be available (active)
- Task status automatically changes to "ASSIGNED"
- Assignment timestamp and dispatcher info are recorded

**Error Responses:**
- `404 Not Found` if task or technician doesn't exist
- `400 Bad Request` if user is not a technician
- `409 Conflict` if task is already assigned
- `409 Conflict` if technician is not available

---

### Get Available Technicians
Retrieve list of available technicians.

**Endpoint:** `GET /api/technicians/available`

**Access:** DISPATCHER, SUPERVISOR

**Response:** `200 OK`
```json
[
  {
    "id": 5,
    "username": "john_tech",
    "email": "john@example.com",
    "role": "TECHNICIAN",
    "available": true
  },
  {
    "id": 6,
    "username": "jane_tech",
    "email": "jane@example.com",
    "role": "TECHNICIAN",
    "available": true
  }
]
```

---

## Data Models

### Priority Enum
- `HIGH`
- `MEDIUM`
- `LOW`

### TaskStatus Enum
- `UNASSIGNED`
- `ASSIGNED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

### User Role Enum
- `DISPATCHER`
- `TECHNICIAN`
- `SUPERVISOR`

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2025-11-18T23:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid address format. Address must contain both letters and numbers.",
  "path": "/api/tasks"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2025-11-18T23:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/api/tasks"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2025-11-18T23:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/tasks"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-11-18T23:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 999",
  "path": "/api/tasks/999"
}
```

---

## Testing

Unit tests provide 85%+ coverage for all components:
- TaskService: 89% line coverage
- TaskController: 100% line coverage
- AssignmentService: 100% line coverage
- AssignmentController: 100% line coverage
- LocationService: 100% instruction coverage, 92% branch coverage
- LocationController: 100% line coverage
- StatusService: 100% instruction and branch coverage
- StatusController: 100% line coverage

Total test count: 111 tests

Run tests with:
```bash
cd backend
mvn test
```

View coverage report:
```bash
open target/site/jacoco/index.html
```

---

## Future Enhancements

The following features are planned but not yet implemented:
- Notification system for technician and customer when task is assigned (currently has TODO placeholder)
- Task reassignment functionality
- Task history and audit trail

---

## Location Tracking Endpoints

### Update Technician Location
Update the current location of a technician. Limited to one update per 30 seconds.

**Endpoint:** `POST /api/locations`

**Access:** TECHNICIAN

**Request Body:**
```json
{
  "userId": 5,
  "latitude": 40.7128,
  "longitude": -74.0060,
  "accuracy": 10.5
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "userId": 5,
  "latitude": 40.7128,
  "longitude": -74.0060,
  "accuracy": 10.5,
  "timestamp": "2025-11-18T23:10:00"
}
```

**Error Responses:**
- `429 Too Many Requests` if location updated within last 30 seconds
- `400 Bad Request` if user not found or not a technician

**WebSocket:** Location updates are broadcast to `/topic/locations` in real-time

---

### Get All Technician Locations
Retrieve latest locations for all active technicians (within last 5 minutes).

**Endpoint:** `GET /api/locations/technicians`

**Access:** All authenticated users

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 5,
    "latitude": 40.7128,
    "longitude": -74.0060,
    "accuracy": 10.5,
    "timestamp": "2025-11-18T23:10:00"
  },
  {
    "id": 2,
    "userId": 6,
    "latitude": 40.7500,
    "longitude": -73.9900,
    "accuracy": 8.2,
    "timestamp": "2025-11-18T23:09:30"
  }
]
```

---

### Get All Task Locations
Retrieve locations for all unassigned and in-progress tasks.

**Endpoint:** `GET /api/locations/tasks`

**Access:** All authenticated users

**Response:** `200 OK`
```json
[
  {
    "taskId": 1,
    "title": "Fix HVAC System",
    "address": "123 Main Street, Springfield, IL 62701",
    "status": "UNASSIGNED",
    "priority": "HIGH",
    "latitude": null,
    "longitude": null
  },
  {
    "taskId": 2,
    "title": "Plumbing Repair",
    "address": "456 Oak Avenue, Springfield, IL 62702",
    "status": "IN_PROGRESS",
    "priority": "MEDIUM",
    "latitude": null,
    "longitude": null
  }
]
```

**Note:** Geocoding for addresses is not yet implemented. Latitude/longitude fields are placeholders.

---

## Task Status Management Endpoints

### Start Task
Mark an assigned task as in progress.

**Endpoint:** `PUT /api/tasks/{id}/start`

**Access:** TECHNICIAN

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Fix HVAC System",
  "status": "IN_PROGRESS",
  "startedAt": "2025-11-18T23:15:00",
  "assignedTechnicianId": 5,
  "assignedTechnicianName": "john_tech",
  ...
}
```

**Business Rules:**
- Only ASSIGNED tasks can be started
- Task status automatically changes to "IN_PROGRESS"
- Start timestamp is recorded

**Error Responses:**
- `404 Not Found` if task doesn't exist
- `400 Bad Request` if task is not in ASSIGNED status

---

### Complete Task
Mark an in-progress task as completed with work summary.

**Endpoint:** `PUT /api/tasks/{id}/complete`

**Access:** TECHNICIAN

**Request Body:**
```json
{
  "workSummary": "Replaced the faulty HVAC component and tested the system. All working correctly now. Customer satisfied with the repair."
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Fix HVAC System",
  "status": "COMPLETED",
  "startedAt": "2025-11-18T23:15:00",
  "completedAt": "2025-11-18T23:45:00",
  "workSummary": "Replaced the faulty HVAC component and tested the system. All working correctly now. Customer satisfied with the repair.",
  "assignedTechnicianId": 5,
  "assignedTechnicianName": "john_tech",
  ...
}
```

**Validations:**
- `workSummary`: Required, 10-2000 characters

**Business Rules:**
- Only IN_PROGRESS tasks can be completed
- Work summary is mandatory for completion
- Task status automatically changes to "COMPLETED"
- Completion timestamp is recorded

**Error Responses:**
- `404 Not Found` if task doesn't exist
- `400 Bad Request` if task is not in IN_PROGRESS status or work summary is missing/invalid

---

### Get Task Status
Retrieve current status of a task.

**Endpoint:** `GET /api/tasks/{id}/status`

**Access:** All authenticated users

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Fix HVAC System",
  "status": "IN_PROGRESS",
  "startedAt": "2025-11-18T23:15:00",
  "completedAt": null,
  "workSummary": null,
  "assignedTechnicianId": 5,
  "assignedTechnicianName": "john_tech",
  ...
}
```

**Error Response:** `404 Not Found` if task doesn't exist

---

## WebSocket Support

### Connection Endpoint
Connect to WebSocket for real-time updates.

**Endpoint:** `ws://localhost:8080/ws`

**Protocol:** STOMP over WebSocket with SockJS fallback

### Topics

#### Location Updates
**Topic:** `/topic/locations`

**Message Format:**
```json
{
  "id": 1,
  "userId": 5,
  "latitude": 40.7128,
  "longitude": -74.0060,
  "accuracy": 10.5,
  "timestamp": "2025-11-18T23:10:00"
}
```

**Usage:** Subscribe to receive real-time location updates whenever a technician updates their location.

---
