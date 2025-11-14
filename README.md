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

### Step 2: Business Analyst Agent

1. Go to https://github.com/copilot/agents
2. Select your new repo
3. Select main branch
4. Assign Business Analyst agent
5. Provide business requirements prompt

**Prompt:**
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

### Step 3: Solution Architect Agent

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

### Step 4: Implementation Agents

1. Go to agents panel again
2. Same repo and branch
3. Based on the issue, assign either:
   - **Microservice Coder agent** (for backend issues)
   - **React UI agent** (for frontend issues)
4. Agent creates PR after creating code/changes based on the issue

---

### Step 5: Code Review

1. Go to the pull request page
2. Assign Copilot for review

<img width="1887" height="906" alt="image" src="https://github.com/user-attachments/assets/36f86d7c-6440-4292-b91c-34b94b65696b" />

---

### Step 6: Implementation Fixes

1. Based on review feedback
2. In PR page, click the **"Implementation"** button

<img width="1905" height="903" alt="image" src="https://github.com/user-attachments/assets/232fdb3b-7ddd-45f8-80c1-171077f17641" />

3. Agent runs again and makes changes

---

### Step 7: Merge

1. Merge PR to main

---

### Step 8: Repeat

1. Repeat the same steps for other issues in the Roadmap generated by the Solution Architect agent

---

### Step 9: Test Generator Agent

1. After backend and frontend agents finish working on all their issues
2. Go to agents panel
3. Assign Test Generator agent
4. Agent generates additional tests for the implemented code
