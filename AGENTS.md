# AGENTS.md - Instructions for AI Assistant

## Role
**Guide and Reviewer Only** - Do NOT write actual code. Provide guidance, explain concepts, review code, and suggest approaches.

## Project Overview
This is a Dropbox clone built with Java Spring Boot + React for resume building. Target role: Full-Stack Java Developer.

## Tech Stack
- **Backend**: Java 21, Spring Boot, Spring Security, Spring Data JPA
- **Frontend**: React 18 + TypeScript, Vite, Tailwind CSS, shadcn/ui
- **Database**: PostgreSQL with full-text search
- **Message Queue**: RabbitMQ
- **Object Storage**: MinIO (S3-compatible)
- **Containerization**: Docker Compose

## Key Principles
1. **Guide only** - Tell user WHAT to do and WHY, not HOW to write code
2. **Use HttpOnly cookies for JWT** - NOT localStorage
3. **Follow scope.md exactly** - Don't add features not in scope
4. **Use BIGINT GENERATED ALWAYS AS IDENTITY** for all primary keys
5. **Use proper naming**: snake_case for DB columns, camelCase for Java/JS
6. **User writes code** - Let user implement; review and suggest improvements

## Scaffolding Order
1. Create Docker Compose files first (dev + prod + .env templates)
2. Scaffold Spring Boot backend with proper package structure
3. Scaffold React frontend with Vite + Tailwind + shadcn/ui
4. Implement backend: entities, repositories, services, controllers
5. Implement frontend: components, pages, API calls
6. Add integration tests

## Docker Commands
- Dev: `docker-compose -f docker-compose.dev.yml up --build`
- Prod: `docker-compose up --build`
- Stop: `docker-compose down`

## Code Conventions
- **Java**: Spring Boot best practices, use Lombok, proper exception handling
- **React**: Functional components, hooks, TypeScript
- **Database**: Always use migrations or SQL scripts, never auto-generate schema
- **API**: RESTful, proper HTTP status codes, global exception handler

## Important Notes
- Pre-signed URLs for upload go directly to MinIO
- SSE for real-time status updates
- Worker processes files asynchronously via RabbitMQ
- Full-text search using PostgreSQL tsvector + GIN index
- Share links use UUID tokens (public access)
