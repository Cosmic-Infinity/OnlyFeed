# OnlyFeed Project Plan

## 1. Project Concept

OnlyFeed is an open social platform with simple account-based participation.

- Anyone can browse the global feed.
- Posting, liking, and commenting require login.
- Users can follow other users.
- The app has two feed views:
  - Global feed: newest posts from everyone.
  - Following feed: newest posts only from followed accounts.

This project is intentionally compact but complete, designed to showcase full-stack fundamentals using Spring Boot, React, and MySQL.

## 2. Scope

### In Scope (MVP)

- Username + password registration
- Username + password login
- JWT-based authentication for protected actions
- Create and view posts
- Like and unlike a post (login required)
- Comment on a post (login required)
- Follow and unfollow users
- Global feed and following feed
- Public user profile (shows only that user's posts)
- Logged-in "My Profile" view:
  - My posts
  - Posts I liked
  - Posts I commented on
- Backend validation and error handling
- Basic frontend validation and friendly messages
- Unit tests for core backend logic

### Out of Scope (for now)

- Media upload (images/videos)
- Direct messaging
- Reposts/shares
- Notifications
- Real-time sockets
- Federated/decentralized multi-server architecture

## 3. Functional Requirements

### Authentication

- New users can register with unique username and password.
- Returning users can login.
- Authenticated users receive JWT and use it for protected endpoints.

### Posts

- Authenticated users can create text posts.
- Posts appear in global feed in reverse chronological order.
- Posts from followed users appear in following feed.

### Likes

- Authenticated users can like a post once.
- Authenticated users can remove their like.
- Like count is shown per post.

### Comments

- Authenticated users can add comments on posts.
- Post detail card/list shows comments (paged if needed).
- Comment count is shown per post.

### Follow System

- Authenticated users can follow or unfollow another user.
- A user cannot follow themselves.
- Following relationship is unique (no duplicates).

### Profiles

- Public profile page:
  - Username
  - Join date
  - All posts by that user
  - Does not show that user's comments list
- Logged-in My Profile page:
  - My posts
  - Posts I liked
  - Posts I commented on

## 4. Non-Functional Requirements

- Layered backend architecture (controller, service, repository, entity, DTO)
- Clean API contracts with JSON
- Input validation and standardized error responses
- Secure password storage (hashed)
- Basic pagination for feeds and comments
- Maintainable code with clear module boundaries

## 5. Suggested Data Model

### users

- id (PK)
- username (unique, not null)
- password_hash (not null)
- created_at

### posts

- id (PK)
- author_id (FK -> users.id)
- content
- created_at

### follows

- id (PK)
- follower_id (FK -> users.id)
- following_id (FK -> users.id)
- created_at
- unique(follower_id, following_id)

### post_likes

- id (PK)
- user_id (FK -> users.id)
- post_id (FK -> posts.id)
- created_at
- unique(user_id, post_id)

### comments

- id (PK)
- post_id (FK -> posts.id)
- author_id (FK -> users.id)
- content
- created_at

## 6. API Blueprint

### Auth

- POST /api/auth/register
- POST /api/auth/login

### Users and Follow

- GET /api/users/{userId}
- GET /api/users/{userId}/posts?page=&size=
- GET /api/users/search?query=
- POST /api/users/{userId}/follow
- DELETE /api/users/{userId}/follow

### Posts and Feeds

- GET /api/posts/global?page=&size=
- GET /api/posts/following?page=&size=
- POST /api/posts
- GET /api/posts/{postId}
- DELETE /api/posts/{postId}

### Likes

- POST /api/posts/{postId}/likes
- DELETE /api/posts/{postId}/likes

### Comments

- GET /api/posts/{postId}/comments?page=&size=
- POST /api/posts/{postId}/comments
- DELETE /api/comments/{commentId}

### My Activity

- GET /api/users/me/posts?page=&size=
- GET /api/users/me/liked-posts?page=&size=
- GET /api/users/me/commented-posts?page=&size=

## 7. Frontend Plan

### Main Pages

- Login
- Register
- Home (Global/Following tabs)
- Public Profile
- My Profile

### Core Components

- PostComposer
- PostCard
- LikeButton
- CommentList and CommentComposer
- FeedTabs
- UserSearch and FollowButton

### UX Rules

- Global feed is visible without login.
- Following feed prompts login if unauthenticated.
- Composer, like button, and comment form require login.
- Clear empty states and loading states.

## 8. Validation and Security Rules

- username: 3 to 30 chars, unique
- password: minimum 6 chars
- post content: 1 to 280 chars
- comment content: 1 to 280 chars
- JWT required for protected routes
- Passwords must be hashed (never returned in responses)

## 9. Testing Checklist

- Auth: register/login success and failure cases
- Posts: create post auth checks and validations
- Feed: global and following query correctness
- Likes: duplicate-like prevention and unlike behavior
- Comments: auth checks and content validation
- Follow: no self-follow and no duplicate follow
- Error handling: consistent error body for validation and access failures

## 10. Demo Checklist

- Register two users and login
- User A creates posts
- User B follows User A
- Compare global vs following feed
- User B likes and comments on User A posts
- Show User A profile contains only User A posts
- Show My Profile tabs for liked and commented posts
- Show unauthenticated create/like/comment request rejection

## 11. Submission Checklist

- README with setup and run steps (backend + frontend + DB)
- API endpoint summary with sample requests/responses
- Screenshots of key flows
- Test report/snippets
- Clean GitHub commits and project structure
