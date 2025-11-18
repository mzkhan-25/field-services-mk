# Agentic DevSecOps Workflow

## Template Repository

**Template:** https://github.com/HHG-HAIL/agentic-devsecops-template

Contains `.github/agents/` folder with the following agents:

---

## Agents

**Business Analyst** (`.github/agents/business_analyst.md`)
- Converts business cases into GitHub issues: personas, journeys, epics, stories, and MVP prioritization

**Solution Architect** (`.github/agents/solution_architect.md`)
- Transforms stories into sequenced technical tasks with dependency mapping

**Backend Microservice Coder** (`.github/agents/microservice_coder.md`)
- Spring Boot microservices with REST APIs, JPA/Hibernate, and 85%+ test coverage

**React UI Agent** (`.github/agents/react_ui.md`)
- React components with Vite/Vitest and 85%+ test coverage

**Test Generator** (`.github/agents/test_generator.md`)
- Auto-generates tests for any language/framework

---

## Workflow

```
Business Analyst → Solution Architect → Backend/Frontend Agents → Test Generator
```

---

## Process

### Step 1: Setup

1. Clone the template repo into your new repo

---

### Step 2: Configure GitHub MCP Server

1. Go to your repo `settings/copilot/coding_agent`
2. Paste the following configuration:

```json
{
  "mcpServers": {
    "github-mcp-server": {
      "type": "http",
      "url": "https://api.githubcopilot.com/mcp/",
      "tools": ["*"]
    }
  }
}
```

---

### Step 3: Setup GitHub Token

1. Go to your repo `settings/environments`
2. Create environment named: **copilot**
3. In that environment, create a new secret:
   - **Name:** `COPILOT_MCP_GITHUB_PERSONAL_ACCESS_TOKEN`
   - **Value:** 
     - Go to https://github.com/settings/tokens
     - Select all permissions
     - Copy the generated token
     - Paste it as the secret value

---

### Step 4: Business Analyst Agent

1. Go to https://github.com/copilot/agents
2. Select your new repo
3. Select main branch
4. Assign Business Analyst agent
5. Provide business requirements prompt

**Prompt (I provided):**
```
The product is a Field Service Management System designed to help dispatchers and field technicians efficiently manage service tasks. The system allows a dispatcher to create new service tasks that include a title, description, client address, priority, and estimated duration. Dispatchers can assign tasks to technicians either through a map-based interface or by other assignment methods. The map view displays both technician locations and unassigned task locations in real time. The dispatcher can also reassign a task to another technician if the originally assigned technician becomes unavailable.

Field technicians use a mobile interface to view their assigned tasks, which display the title, description, address, and priority. The mobile system shows the location of each assigned task on a map, helping technicians navigate to client sites. Technicians can update the status of their assigned tasks, marking them as "In Progress" or "Completed." These updates are reflected in near real time on a central dashboard accessible to dispatchers. When marking a task as "Completed," technicians can enter a summary of the work performed.

The central dashboard displays key performance metrics such as total tasks, completed versus open tasks, average completion time, and task priority distribution. A more advanced analytics and reporting dashboard allows dispatch supervisors to monitor the overall operational status using a map-based view. It supports filtering, sorting, and aggregation of data by task type, geography, technician, and time period.

The system also manages real-time notifications and alerts. When a dispatcher assigns a task to a technician, the system automatically sends an alert to the requesting customer, informing them of the assigned technician's name and the estimated arrival time (ETA). Simultaneously, an alert is sent to the assigned technician. When a technician marks a task as "In Progress," the system alerts the customer with an updated ETA. The ETA can either be manually entered by the technician or automatically calculated based on the technician's current location, the task location, and real-time traffic conditions.

The overall solution should support both mobile and web-based interfaces to ensure that dispatchers, supervisors, and technicians can access the system seamlessly from different devices.

Based on this description, generate detailed personas (dispatcher, technician, supervisor, customer), user journeys, epics, and prioritized user stories to cover all key workflows and system interactions described above.
```

**Output:** 
- Generates epics, personas, stories, MVP
- Creates a pull request
- Creates new branch named `copilot/{branch-name}`

---

### Step 5: Solution Architect Agent

1. Go back to agents panel
2. Same repo and branch
3. Assign Solution Architect agent
4. **Prompt:** "Look at the MVP generated from Business Analyst agent and create actionable tasks"

**Output:**
- Generates actionable tasks
- Creates another MVP Roadmap with phases on what to run first
- Creates a pull request
- Creates new branch named `copilot/{branch-name}`

---

### Step 6: Implementation Agents

1. Go to the Agents panel again
2. Select the same repository and branch (usually the main branch)
3. Based on the MVP roadmap generated by the Solution Architect agent, follow the task/issue order it provides. For each issue, assign the correct agent:
   - **Microservice Coder agent** → backend / API / database issues
   - **React UI agent** → frontend / UI / UX issues
4. After identifying the correct agent for the task, go to the Agents panel again, select:
   - your repository
   - the branch (main)
   - the agent you want to run
5. In the prompt, give a direct instruction like:

```
Work on issue #45
```
---

### Step 7: Code Review

1. Go to the pull request page after agent completes its task in the previous step
2. Assign Copilot for review

<img width="1887" height="906" alt="image" src="https://github.com/user-attachments/assets/36f86d7c-6440-4292-b91c-34b94b65696b" />

---

### Step 8: Implementation Fixes

1. Based on review feedback
2. In PR page, click the **"Implementation"** button

<img width="1905" height="903" alt="image" src="https://github.com/user-attachments/assets/232fdb3b-7ddd-45f8-80c1-171077f17641" />

3. Agent runs again and makes changes

---

### Step 7: Merge

1. Merge PR to main

---

### Step 8: Repeat

1. Repeat Steps 6-8 for all other issues in the Roadmap generated by the Solution Architect agent

---

### Step 9: Test Generator Agent

1. After backend and frontend agents finish working on all their issues
2. Go to agents panel
3. Assign Test Generator agent
4. Agent generates additional tests for the implemented code

---

## API Documentation

### Authentication Endpoints

#### Login
Authenticates a user and returns a JWT token.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "dispatcher",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "dispatcher",
  "email": "dispatcher@fieldservices.com",
  "role": "DISPATCHER"
}
```

**Response (401 Unauthorized):**
```json
{
  "message": "Invalid username or password"
}
```

#### Health Check
Checks if the authentication service is running.

**Endpoint:** `GET /api/auth/health`

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "Auth Service"
}
```

### Default Users

The system initializes with three default users for testing:

| Username   | Password    | Role       | Email                           |
|------------|-------------|------------|---------------------------------|
| dispatcher | password123 | DISPATCHER | dispatcher@fieldservices.com    |
| technician | password123 | TECHNICIAN | technician@fieldservices.com    |
| supervisor | password123 | SUPERVISOR | supervisor@fieldservices.com    |

### Role-Based Access Control

The system implements three roles with different permissions:

#### DISPATCHER Role
- Can create and assign tasks
- Can view all tasks
- Can access task creation endpoints: `POST /api/tasks/create`, `POST /api/tasks/assign`

#### TECHNICIAN Role
- Can view assigned tasks
- Can update task status
- Can access task status update endpoints: `PUT /api/tasks/*/status`

#### SUPERVISOR Role
- Read-only access to all data
- Can view analytics and reports
- Can access analytics endpoints: `GET /api/analytics/**`

### Using JWT Tokens

After logging in, include the JWT token in the Authorization header for authenticated requests:

```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/tasks
```

### Session Management

- JWT tokens expire after 2 hours (7200000 milliseconds)
- Sessions are stateless and managed through JWT tokens
- No server-side session storage is required

### Testing the API

**Example: Login as Dispatcher**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"dispatcher","password":"password123"}'
```

**Example: Check Health**
```bash
curl http://localhost:8080/api/auth/health
```

**Example: Access Protected Endpoint**
```bash
# First, login and save the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"dispatcher","password":"password123"}' | jq -r '.token')

# Then use the token to access protected endpoints
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/tasks
```

---

## Docker Setup for Local Development

### Prerequisites

- Docker Desktop installed
- Docker Compose installed (included with Docker Desktop)
- Git installed

### Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/mzkhan-25/field-services-mk.git
   cd field-services-mk
   ```

2. **Set up environment variables:**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` file and update the variables as needed (optional for local development).

3. **Start all services:**
   ```bash
   ./start-dev.sh up
   ```
   Or using Docker Compose directly:
   ```bash
   docker-compose up -d
   ```

4. **Access the application:**
   - **Frontend:** http://localhost:5173
   - **Backend API:** http://localhost:8080/api
   - **Database:** localhost:5432

### Available Commands

The `start-dev.sh` script provides convenient commands for managing the development environment:

```bash
./start-dev.sh up       # Start all services
./start-dev.sh down     # Stop all services
./start-dev.sh restart  # Restart all services
./start-dev.sh logs     # View logs from all services
./start-dev.sh build    # Build all services
./start-dev.sh clean    # Stop services and remove volumes (deletes data)
./start-dev.sh status   # Show service status
./start-dev.sh help     # Show help message
```

### Services

#### PostgreSQL Database
- **Port:** 5432
- **Database:** field_services
- **Username:** fsadmin
- **Password:** fspassword (configurable in .env)
- **Volume:** postgres_data (persistent storage)

#### Spring Boot Backend
- **Port:** 8080
- **Context Path:** /api
- **Health Check:** http://localhost:8080/api/actuator/health
- **Dependencies:** PostgreSQL
- **Features:**
  - Spring Data JPA with Hibernate
  - Spring Security with JWT authentication
  - PostgreSQL database connection
  - Actuator for health monitoring

#### React Frontend (Vite)
- **Port:** 5173
- **Development Server:** Vite with HMR (Hot Module Replacement)
- **Dependencies:** Backend API
- **Features:**
  - React 18
  - Vite dev server
  - Live reload on file changes

### Health Checks

All services include health checks that ensure proper startup:

- **PostgreSQL:** Verifies database is accepting connections
- **Backend:** Checks `/actuator/health` endpoint
- **Frontend:** Verifies Vite dev server is running

### Environment Variables

Key environment variables (see `.env.example` for complete list):

```bash
# Database
POSTGRES_DB=field_services
POSTGRES_USER=fsadmin
POSTGRES_PASSWORD=fspassword
POSTGRES_PORT=5432

# Backend
BACKEND_PORT=8080
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRATION=7200000

# Frontend
FRONTEND_PORT=5173
VITE_API_URL=http://localhost:8080/api
```

### Troubleshooting

**Services won't start:**
```bash
# Check service logs
./start-dev.sh logs

# Check service status
./start-dev.sh status

# Rebuild services
./start-dev.sh build
./start-dev.sh up
```

**Port conflicts:**
- Edit `.env` file and change port numbers for conflicting services
- Stop conflicting applications using the ports

**Database connection issues:**
- Ensure PostgreSQL health check passes: `docker-compose ps`
- Check backend logs: `docker-compose logs backend`

**Clean slate:**
```bash
# Stop services and remove all data
./start-dev.sh clean

# Start fresh
./start-dev.sh up
```

### Development Workflow

1. **Make code changes** in `backend/` or `frontend/` directories
2. **Backend changes:** Rebuild the backend service
   ```bash
   docker-compose up -d --build backend
   ```
3. **Frontend changes:** Auto-reload with HMR (no rebuild needed)
4. **Database changes:** Handled automatically by Hibernate DDL auto-update

### Stopping the Application

```bash
# Stop all services
./start-dev.sh down

# Or using Docker Compose
docker-compose down

# Stop and remove volumes (deletes all data)
./start-dev.sh clean
```
