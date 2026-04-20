# OnlyFeed Agent Instructions

## Purpose

Build a compact, fun, and complete full-stack social app that demonstrates Java + Spring Boot fundamentals with React and MySQL.

## Alignment Targets (from helper guidance)

- Spring Boot backend with REST APIs
- React frontend with routed, component-based UI
- MySQL relational schema with JPA/Hibernate
- Layered architecture: controller, service, repository, entity, DTO
- Input validation and exception handling
- Authentication and authorization using JWT
- Unit testing with JUnit and Mockito
- Version-controlled, submission-ready project structure

## Product Rules

- Public users can browse global posts.
- Posting requires login.
- Like and comment require login.
- Feed has Global and Following views.
- Public profile shows only that user's posts.
- Logged-in user can view:
  - Own posts
  - Posts they liked
  - Posts they commented on

## Engineering Rules

1. Architecture

- Keep strict separation between controller, service, and repository.
- Use DTOs for request/response payloads.
- Avoid exposing entity internals directly in API responses.

2. Security

- Hash passwords before storage.
- Use JWT for protected endpoints.
- Protect create/update/delete social actions.
- Do not return secrets or password hashes.

3. Validation

- Validate all incoming payloads.
- Return consistent and user-friendly error responses.
- Enforce uniqueness and relationship constraints.

4. Data Integrity

- Unique username.
- Unique follower-following pair.
- Unique user-post like pair.
- Foreign keys for post author, comment author, and follow relations.

5. API Quality

- Keep endpoint naming clean and predictable.
- Support pagination for feeds and comments.
- Return proper HTTP status codes.

6. Testing

- Cover auth flows, protected route behavior, feed queries, likes/comments constraints, and validation errors.
- Include both service-level and controller-level tests for critical paths.

7. Frontend Quality

- Keep flows simple and intuitive.
- Show loading, empty, and error states.
- Guard protected actions in UI and handle token expiry gracefully.

## Definition of Done

- End-to-end flow works from frontend to DB.
- Required features implemented:
  register/login, post, global/following feeds, follow/unfollow, like/unlike, comment, profile/my-activity.
- Validation and security are enforced.
- Core tests pass.
- README documents setup, run, API summary, and demo steps.

## Scope Guardrails

- Prefer depth in fundamentals over many features.
- Do not add advanced distributed/federation features in this version.
- Prioritize correctness, clean architecture, and demo clarity.
