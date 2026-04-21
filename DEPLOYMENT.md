# Deployment & Security Guide

This guide covers the deployment strategy for the OnlyFeed application. The architecture consists of:
- **Frontend (React SPA)**: Hosted on **Cloudflare Pages** for fast edge delivery.
- **Backend (Spring Boot)**: Hosted on **Oracle Cloud Infrastructure (OCI)** VM.
- **Database (PostgreSQL)**: Hosted alongside the backend or on a separate managed Oracle Cloud database.
- **Admin Panel**: An isolated React application served from the backend domain (`/godmode`).

---

## 1. Secrets & Passwords Configuration

Before deploying, you **MUST** update the default fallback passwords to secure secrets. 
Here are the **exact places** you need to change secrets and passwords:

1. **`docker-compose.yml` (Root folder)**
   - `POSTGRES_PASSWORD`: Used by the Postgres database container. Set this to a strong password.
   - `DB_PASSWORD`: Used by the Spring Boot container to connect to Postgres. **Must match** `POSTGRES_PASSWORD`.
   - `JWT_SECRET`: A highly secure random string (minimum 32 characters) used to sign JWT tokens.
   - `ADMIN_BOOTSTRAP_PASSWORD`: The initial password for the admin account.

2. **`backend/src/main/resources/application.yml`**
   - The values here act as fallbacks. In production, you should override them via environment variables (as done in `docker-compose.yml`), but ensure you never commit actual production secrets to this file.

3. **Backend `.env` file (Optional but recommended)**
   - Create a `.env` file in the same directory as `docker-compose.yml` on your production server. Populate it with your real secrets:
     ```env
     DB_PASSWORD=your_secure_db_password
     JWT_SECRET=your_secure_32_char_jwt_secret
     ADMIN_USERNAME=admin
     ADMIN_PASSWORD=your_secure_admin_password
     ```
     Docker compose will automatically pick these up when running `docker compose up -d`.

---

## 2. Tightening Security Checks

For production, verify the following security checkpoints:
1. **CORS Origins**: In `docker-compose.yml` or your `.env` file, ensure `APP_CORS_ALLOWED_ORIGIN_PATTERNS` is set **strictly** to your Cloudflare Pages domain (e.g., `https://onlyfeed.mydomain.com`). Do not leave it as `*` or `http://localhost:*`.
2. **Database Access**: Ensure your PostgreSQL port (`5432`) is **not** exposed to the public internet. `docker-compose.yml` exposes port `5430` to the host VM; make sure your Oracle Cloud firewall (Security Lists) blocks external access to `5430`.
3. **Hibernate DDL**: In `application.yml`, `spring.jpa.hibernate.ddl-auto` is set to `update`. In a strict production environment, this should ideally be set to `validate`, and you should use a tool like Flyway or Liquibase for migrations.

---

## 3. Deploying the Backend (Oracle Cloud)

1. **Provision an OCI Compute Instance** (e.g., Ubuntu).
2. **Open Firewall Ports**: In your Oracle Cloud VCN Security List, open port `8080` (or `80` / `443` if using Nginx as a reverse proxy).
3. **Install Docker**: 
   ```bash
   sudo apt update && sudo apt install docker.io docker-compose-plugin
   ```
4. **Clone the Repo** onto the VM.
5. **Configure Secrets**: Create a `.env` file as mentioned in Section 1.
6. **Start the Application**:
   ```bash
   docker compose up -d
   ```
   *Note: The backend runs on port `8080` of the host by default.*
7. **(Optional but Highly Recommended)**: Install Nginx, configure it as a reverse proxy to `127.0.0.1:8080`, and secure it with a free SSL certificate using Let's Encrypt (Certbot).

---

## 4. Deploying the Frontend (Cloudflare Pages)

We recommend using **Cloudflare Pages** for the frontend because it natively supports building and hosting Vite-based React SPAs with global CDN distribution out-of-the-box, without needing to manually write Workers.

1. Push your repository to GitHub.
2. Log into the Cloudflare Dashboard -> **Workers & Pages** -> **Create application** -> **Pages** -> **Connect to Git**.
3. Select the `OnlyFeed` repository.
4. **Build Settings**:
   - **Framework preset**: Vite
   - **Build command**: `npm run build`
   - **Build output directory**: `dist`
   - **Root directory**: `frontend` (important to specify the subfolder)
5. **Environment Variables**:
   - Add a variable named `VITE_API_BASE_URL` and set it to your Oracle Cloud Backend URL (e.g., `https://api.onlyfeed.mydomain.com`).
6. Click **Save and Deploy**.

---

## 5. Admin Panel (`/godmode`) Access Instructions

The `godmode` admin panel is a separate, isolated React SPA specifically built for administration.

### Accessing Locally
1. Open a new terminal and navigate to the `godmode` directory:
   ```bash
   cd godmode
   ```
2. Install dependencies and start the development server:
   ```bash
   npm install
   npm run dev
   ```
3. Open your browser and navigate to `http://localhost:5173` (or the port Vite provides).
4. Log in using the credentials defined in your `docker-compose.yml` (`ADMIN_BOOTSTRAP_USERNAME` and `ADMIN_BOOTSTRAP_PASSWORD`). By default, these are `admin` and `CHANGE_ME_IN_PROD`.

### Accessing in Deployment
The admin panel is designed to be served directly from the backend server to keep it completely isolated from the public Cloudflare Pages frontend.

1. On your Oracle Cloud VM, build the godmode project:
   ```bash
   cd godmode
   npm install
   npm run build
   ```
2. You can serve the resulting `godmode/dist` folder using Nginx on your Oracle VM, mapped to the `/godmode` path.
   **Example Nginx Configuration**:
   ```nginx
   server {
       server_name api.onlyfeed.mydomain.com;

       # Route API requests to Spring Boot
       location /api/ {
           proxy_pass http://127.0.0.1:8080;
       }

       # Serve Godmode Admin Panel
       location /godmode/ {
           alias /path/to/repo/godmode/dist/;
           try_files $uri $uri/ /index.html;
       }
   }
   ```
3. Once deployed, simply navigate to `https://api.onlyfeed.mydomain.com/godmode` (replacing the domain with your Oracle VM's domain) to access the admin panel.
