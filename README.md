# Agentic DevSecOps Template

AI agents for automating software development workflows.

## Agents

**Business Analyst** (`.github/agents/business_analyst.md`)  
Converts business cases into GitHub issues: personas, journeys, epics, stories, and MVP prioritization.

**Solution Architect** (`.github/agents/solution_architect.md`)  
Transforms stories into sequenced technical tasks with dependency mapping.

**Backend Microservice Coder** (`.github/agents/microservice_coder.md`)  
Spring Boot microservices with REST APIs, JPA/Hibernate, and 85%+ test coverage.

**React UI Agent** (`.github/agents/react_ui.md`)  
React components with Vite/Vitest and 85%+ test coverage.

**Test Generator** (`.github/agents/test_generator.md`)  
Auto-generates tests for any language/framework. 

## Workflow

Business Analyst → Solution Architect → Backend/Frontend Agents → Test Generator

## Usage

Copy these agent definitions into your own repo under `.github/agents/`