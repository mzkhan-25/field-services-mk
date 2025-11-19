# Task Management and Assignment API

## Overview
This document describes the REST API endpoints for Task Management and Task Assignment features implemented in the Field Services Management Backend.

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

## Notification Endpoints

### Send Notification
Send a notification to a customer (internal use or manual triggering).

**Endpoint:** `POST /api/notifications/send`

**Access:** DISPATCHER, SUPERVISOR

**Request Body:**
```json
{
  "taskId": 1,
  "customerId": 100,
  "type": "TASK_ASSIGNED",
  "message": "Your task has been assigned to a technician",
  "channel": "EMAIL",
  "recipientContact": "customer@example.com"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "taskId": 1,
  "customerId": 100,
  "type": "TASK_ASSIGNED",
  "message": "Your task has been assigned to a technician",
  "channel": "EMAIL",
  "recipientContact": "customer@example.com",
  "deliveryStatus": "SENT",
  "sentAt": "2025-11-18T23:10:00",
  "retryCount": 0,
  "errorMessage": null,
  "createdAt": "2025-11-18T23:10:00",
  "updatedAt": "2025-11-18T23:10:00"
}
```

**Validations:**
- `taskId`: Required
- `customerId`: Required
- `type`: Required, one of: TASK_ASSIGNED, TASK_IN_PROGRESS, TASK_COMPLETED, TASK_CANCELLED
- `message`: Required
- `channel`: Optional, defaults to EMAIL. Values: EMAIL, SMS, BOTH
- `recipientContact`: Optional, recipient's email or phone number

---

### Get Notifications for Task
Retrieve all notifications sent for a specific task.

**Endpoint:** `GET /api/notifications/{taskId}`

**Access:** All authenticated users

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "taskId": 1,
    "customerId": 100,
    "type": "TASK_ASSIGNED",
    "message": "Task Assignment Notification...",
    "channel": "EMAIL",
    "recipientContact": "customer@example.com",
    "deliveryStatus": "SENT",
    "sentAt": "2025-11-18T23:10:00",
    "retryCount": 0,
    "createdAt": "2025-11-18T23:10:00",
    "updatedAt": "2025-11-18T23:10:00"
  },
  {
    "id": 2,
    "taskId": 1,
    "customerId": 100,
    "type": "TASK_IN_PROGRESS",
    "message": "Task In Progress Notification...",
    "channel": "EMAIL",
    "recipientContact": "customer@example.com",
    "deliveryStatus": "SENT",
    "sentAt": "2025-11-18T23:15:00",
    "retryCount": 0,
    "createdAt": "2025-11-18T23:15:00",
    "updatedAt": "2025-11-18T23:15:00"
  }
]
```

---

### Retry Failed Notifications
Retry sending all failed notifications (up to 3 retries).

**Endpoint:** `POST /api/notifications/retry`

**Access:** SUPERVISOR

**Response:** `200 OK`

**Business Rules:**
- Only notifications with status "FAILED" are retried
- Maximum of 3 retry attempts per notification
- Notifications exceeding max retries are logged but not retried

---

## Notification Features

### Automatic Notifications
The system automatically sends notifications when:
- **Task Assignment**: When a task is assigned to a technician, a notification is sent to the customer with:
  - Task details (title, description, address, priority)
  - Assigned technician information
  - Estimated time of arrival (ETA)
  - Estimated duration

- **Task In Progress**: When task status changes to IN_PROGRESS (future implementation)

### Notification Templates
Pre-built templates include:
- Task assignment details with technician info and ETA
- Task in-progress status with updated ETA
- Customizable message content

### Email Notifications
- Uses Spring Mail for email delivery
- Configurable SMTP settings
- Includes task details and technician information
- Formatted with clear subject lines

### SMS Notifications
- Mock implementation (ready for Twilio integration)
- Can be configured for production SMS service
- Supports same message content as email

### Async Processing
- Notifications are sent asynchronously using `@Async`
- Does not block task assignment operations
- Failures in notification sending do not affect task operations

### Delivery Status Tracking
- Tracks delivery status: PENDING, SENT, DELIVERED, FAILED
- Records sent timestamp
- Captures error messages for failed deliveries
- Maintains retry count

### ETA Calculation
- Automatically calculates estimated time of arrival
- Includes 30-minute travel time
- Adds task estimated duration
- Formatted in human-readable date/time

---

## Data Models

### NotificationType Enum
- `TASK_ASSIGNED`
- `TASK_IN_PROGRESS`
- `TASK_COMPLETED`
- `TASK_CANCELLED`

### DeliveryStatus Enum
- `PENDING`
- `SENT`
- `DELIVERED`
- `FAILED`

### NotificationChannel Enum
- `EMAIL`
- `SMS`
- `BOTH`

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
- **NotificationService: 96% line coverage**
- **NotificationController: 100% line coverage**
- **NotificationRepository: 100% line coverage**

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
- Task reassignment functionality
- Task status updates by technicians
- Task completion workflow
- Task history and audit trail
- Production SMS integration (Twilio)
- Customer portal for viewing notification history
- Push notifications for mobile apps
