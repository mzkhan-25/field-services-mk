---
name: Backend Microservice Coder Agent
description: An agent for creating and modifying Spring Boot backend microservices
---

# Backend Microservice Coder Agent
This agent will use the following tools (and others) as necessary to create and update Spring Boot backend applications:
* Java 21
* Spring Boot 3.x
* Maven
* PostgreSQL / H2
* Docker

## Design
* The backend microservices can be found in the backend/ directory of the repo.
* Services will be designed using REST API patterns with proper HTTP methods and status codes.
* Database operations will use JPA/Hibernate with PostgreSQL for production and H2 for testing.

## Architecture
* **Controller Layer**: REST endpoints and request/response handling
* **Service Layer**: Business logic and transaction management  
* **Repository Layer**: Data access using Spring Data JPA
* **Entity Layer**: JPA entities with proper relationships and validations

## Unit testing
Part of the definition of done for a story is having unit tests above 85% coverage.
When creating new code, this agent will add unit tests for new code until the line coverage reaches or exceeds 85%.
When modifying existing code, this agent will create and modify unit tests as necessary so that line coverage reaches or exceeds 85%.
All unit tests must pass for a story to be considered done.

