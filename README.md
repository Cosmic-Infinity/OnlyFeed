# OnlyFeed

A compact full-stack social media application built with Spring Boot, React, and PostgreSQL.

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA / Hibernate |
| Frontend | React 18, React Router 6, Vite, Vanilla CSS |
| Database | PostgreSQL 16 (Docker) |
| Admin Panel | Separate React app (`godmode/`) served at `/godmode/` path |

## Implemented Features

- Register and login with username/password (JWT-based stateless auth)
- Create, browse, and delete posts
- Global feed (public, no login required)
- Following feed (authenticated users only)
- Like / unlike posts
- Comment on posts and view comments
- Follow / unfollow users
- Public user profile pages
- **My Activity** dashboard:
  - My Posts (with delete)
  - Liked Posts
  - Commented Posts (with delete comment)
- **Godmode Admin Panel** (`/godmode/`) — separate isolated app for admins:
  - Manage users (create, delete)
  - Moderate posts and comments
  - Live stats dashboard with sidebar navigation
- Soft-deleted posts show as `[This post was deleted]` so orphaned comments remain coherent
- In-app confirm dialogs (no browser `window.confirm`)
- Contextual login hints for unauthenticated users

## Local Development Setup

### Prerequisites

- Docker Desktop
- Node.js (v18+)
- A `.env` file in the project root (see below)

### 1. Create `.env`

```env
DB_PASSWORD=onlyfeed
JWT_SECRET=SUPER_SECRET_KEY_FOR_JWT_THAT_IS_AT_LEAST_32_CHARS
ADMIN_PASSWORD=admin
```

### 2. Start backend + database

From the project root:

```powershell
docker compose up -d
```

This starts:
- PostgreSQL on port `5432` (internal)
- Spring Boot backend on port `8088` (mapped from internal `8080`)

### 3. Start the main frontend

```powershell
cd frontend
npm install
npm run dev
```

Runs at **http://localhost:5173** — proxies `/api` to `http://localhost:8088`.

### 4. Start the admin panel (godmode)

```powershell
cd godmode
npm install
npm run dev -- --port 5174
```

Runs at **http://localhost:5174/godmode/**

Admin credentials (from `.env`):
- Username: `admin`
- Password: `admin` (or whatever `ADMIN_PASSWORD` is set to)

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for step-by-step deployment on Cloudflare Pages (frontend) and Oracle Cloud (backend + database).

## Key API Routes

### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`

### Posts & Feed
- `GET  /api/posts/global?page=&size=`
- `GET  /api/posts/following?page=&size=`
- `POST /api/posts`
- `DELETE /api/posts/{postId}`

### Likes
- `POST   /api/posts/{postId}/likes`
- `DELETE /api/posts/{postId}/likes`

### Comments
- `GET    /api/posts/{postId}/comments?page=&size=`
- `POST   /api/posts/{postId}/comments`
- `DELETE /api/comments/{commentId}`

### Users, Follow & Profile
- `GET    /api/users/profile/{username}`
- `GET    /api/users/{userId}/posts?page=&size=`
- `GET    /api/users/search?query=`
- `POST   /api/users/{userId}/follow`
- `DELETE /api/users/{userId}/follow`

### My Activity
- `GET /api/users/me/posts?page=&size=`
- `GET /api/users/me/liked-posts?page=&size=`
- `GET /api/users/me/commented-posts?page=&size=`

### Admin (requires ADMIN role JWT)
- `GET    /api/admin/users?page=&size=`
- `POST   /api/admin/users`
- `DELETE /api/admin/users/{userId}`
- `GET    /api/admin/posts?page=&size=`
- `DELETE /api/admin/posts/{postId}`
- `GET    /api/admin/comments?page=&size=`
- `DELETE /api/admin/comments/{commentId}`

## Notes

- Maven is not required locally — the backend runs inside Docker via `docker-compose`.
- The godmode admin panel is a standalone Vite app; it is **not** linked from the public frontend.
- For production, the `godmode/dist` folder is served at the `/godmode/` sub-path of your web server.
