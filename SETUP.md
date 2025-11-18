# 🚀 Appointment System - Setup Guide

This guide walks you through setting up and running the complete Appointment Scheduling System.

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Docker Desktop** (v20.10+) - [Download](https://www.docker.com/products/docker-desktop/)
- **Java 17+** (for local backend development) - [Download OpenJDK](https://adoptium.net/)
- **Maven 3.8+** (for backend builds) - [Download](https://maven.apache.org/download.cgi)
- **Node.js 18+** and **npm** (for frontend) - [Download](https://nodejs.org/)
- **Git** - [Download](https://git-scm.com/downloads)

## 🎯 Quick Start with Docker (Recommended)

The easiest way to run the entire system is using Docker Compose:

### 1. Clone the Repository

```bash
git clone <your-repo-url>
cd Sonar_cube
```

### 2. Configure Environment Variables

#### Backend Configuration

Create `backend/.env` from the template:

```bash
cp backend/.env.example backend/.env
```

Edit `backend/.env` and configure:

```properties
# Database
DB_HOST=mysql
DB_PORT=3306
DB_NAME=appointment_system
DB_USER=root
DB_PASSWORD=rootpassword

# JWT Secret (Generate a strong random string)
JWT_SECRET=your-secure-random-jwt-secret-key-here-min-32-chars

# Google Calendar OAuth (Optional - for calendar sync)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8081/api/calendar/google/callback

# Microsoft OAuth (Optional - for Outlook sync)
MS_CLIENT_ID=your-microsoft-client-id
MS_CLIENT_SECRET=your-microsoft-client-secret
MS_REDIRECT_URI=http://localhost:8081/api/calendar/microsoft/callback

# Twilio SMS (Optional - for SMS reminders)
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_PHONE_NUMBER=+1234567890

# SendGrid Email (Optional - for email reminders)
SENDGRID_API_KEY=your-sendgrid-api-key
SENDGRID_FROM_EMAIL=noreply@example.com

# Redis (for caching)
REDIS_HOST=redis
REDIS_PORT=6379

# CORS (frontend URL)
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

#### Frontend Configuration

Create `frontend/.env` from the template:

```bash
cp frontend/.env.example frontend/.env
```

Edit `frontend/.env`:

```properties
VITE_API_BASE_URL=http://localhost:8081/api
VITE_WS_BASE_URL=ws://localhost:8081/ws
VITE_ENABLE_GOOGLE_CALENDAR=false
VITE_ENABLE_MS_CALENDAR=false
```

### 3. Start All Services

```bash
docker-compose up -d
```

This command will:
- ✅ Start MySQL database (port 3306)
- ✅ Start phpMyAdmin (port 8080)
- ✅ Start Redis cache (port 6379)
- ✅ Build and start Spring Boot backend (port 8081)
- ✅ Build and start React frontend (port 5173)

### 4. Verify Services

Check if all containers are running:

```bash
docker-compose ps
```

Expected output:
```
NAME                STATUS              PORTS
mysql               Up 2 minutes        0.0.0.0:3306->3306/tcp
phpmyadmin          Up 2 minutes        0.0.0.0:8080->80/tcp
redis               Up 2 minutes        0.0.0.0:6379->6379/tcp
backend             Up 1 minute         0.0.0.0:8081->8081/tcp
frontend            Up 1 minute         0.0.0.0:5173->80/tcp
```

### 5. Access the Application

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8081/api
- **phpMyAdmin**: http://localhost:8080 (user: `root`, password: `rootpassword`)
- **API Documentation**: See [API.md](./API.md)

### 6. Create Your First User

Navigate to http://localhost:5173 and click "Sign Up" to create your account.

## 🛠️ Local Development Setup

For active development, you may want to run services individually:

### Backend Development

1. **Start MySQL and Redis**:

```bash
docker-compose up -d mysql redis phpmyadmin
```

2. **Build and Run Backend**:

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will be available at http://localhost:8081

3. **Watch Logs**:

```bash
tail -f logs/application.log
```

### Frontend Development

1. **Install Dependencies**:

```bash
cd frontend
npm install
```

2. **Start Development Server**:

```bash
npm run dev
```

Frontend will be available at http://localhost:5173 with hot-reload enabled.

3. **Build for Production**:

```bash
npm run build
```

4. **Run Linting**:

```bash
npm run lint
```

## 🗄️ Database Management

### Access phpMyAdmin

1. Navigate to http://localhost:8080
2. Login with:
   - Server: `mysql`
   - Username: `root`
   - Password: `rootpassword`

### Database Schema

The database schema is automatically created via Flyway migrations on first startup. See `backend/src/main/resources/db/migration/V1__init.sql`.

### Manual Database Access

```bash
docker exec -it mysql mysql -u root -p
# Enter password: rootpassword

USE appointment_system;
SHOW TABLES;
```

## 📊 Architecture Overview

```
┌─────────────┐      ┌──────────────┐      ┌──────────┐
│   React     │─────→│ Spring Boot  │─────→│  MySQL   │
│  Frontend   │      │   Backend    │      │ Database │
│ (Port 5173) │      │  (Port 8081) │      │(Port 3306)│
└─────────────┘      └──────────────┘      └──────────┘
                            │
                            ├─────→ Redis (Cache)
                            ├─────→ Google Calendar API
                            ├─────→ Microsoft Graph API
                            ├─────→ Twilio (SMS)
                            └─────→ SendGrid (Email)
```

## 🔧 Common Development Tasks

### View Backend Logs

```bash
docker-compose logs -f backend
```

### View Frontend Logs

```bash
docker-compose logs -f frontend
```

### Rebuild Backend After Code Changes

```bash
docker-compose up -d --build backend
```

### Rebuild Frontend After Code Changes

```bash
docker-compose up -d --build frontend
```

### Reset Database

```bash
docker-compose down -v
docker-compose up -d
```

### Access Backend Container Shell

```bash
docker exec -it backend bash
```

### Run Backend Tests

```bash
cd backend
mvn test
```

### Run Frontend Tests

```bash
cd frontend
npm test
```

## 🔐 Security Configuration

### Generate JWT Secret

Use a strong random string (minimum 32 characters):

```bash
openssl rand -base64 32
```

Copy the output to `JWT_SECRET` in `backend/.env`.

### Configure OAuth (Optional)

#### Google Calendar Integration

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable Google Calendar API
4. Create OAuth 2.0 credentials
5. Add `http://localhost:8081/api/calendar/google/callback` as redirect URI
6. Copy Client ID and Secret to `backend/.env`

#### Microsoft Outlook Integration

1. Go to [Azure Portal](https://portal.azure.com/)
2. Register a new application
3. Add Calendar.ReadWrite permission
4. Add `http://localhost:8081/api/calendar/microsoft/callback` as redirect URI
5. Copy Client ID and Secret to `backend/.env`

### Configure SMS (Optional)

1. Sign up at [Twilio](https://www.twilio.com/)
2. Get Account SID, Auth Token, and phone number
3. Add credentials to `backend/.env`

### Configure Email (Optional)

1. Sign up at [SendGrid](https://sendgrid.com/)
2. Create API key
3. Add API key to `backend/.env`

## 📦 Production Deployment

### Build Docker Images

```bash
docker build -t appointment-backend:latest ./backend
docker build -t appointment-frontend:latest ./frontend
```

### Push to Registry

```bash
docker tag appointment-backend:latest your-registry/appointment-backend:latest
docker push your-registry/appointment-backend:latest

docker tag appointment-frontend:latest your-registry/appointment-frontend:latest
docker push your-registry/appointment-frontend:latest
```

### Deploy to Cloud

See [ARCHITECTURE.md](./ARCHITECTURE.md) for deployment architecture and cloud provider guides.

## 🧪 Testing

### Backend Unit Tests

```bash
cd backend
mvn test
```

### Backend Integration Tests

```bash
cd backend
mvn verify
```

### Frontend Unit Tests

```bash
cd frontend
npm test
```

### E2E Tests (Coming Soon)

```bash
cd frontend
npm run test:e2e
```

## 🐛 Troubleshooting

### Backend won't start

1. Check if MySQL is ready:
   ```bash
   docker-compose logs mysql
   ```

2. Verify database connection in `backend/.env`

3. Check backend logs:
   ```bash
   docker-compose logs backend
   ```

### Frontend can't connect to backend

1. Verify backend is running:
   ```bash
   curl http://localhost:8081/api/health
   ```

2. Check CORS settings in `backend/.env`

3. Verify `VITE_API_BASE_URL` in `frontend/.env`

### Database migration errors

1. Check Flyway migration files in `backend/src/main/resources/db/migration/`

2. Reset database:
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```

### Port already in use

If ports are already occupied, modify `docker-compose.yml`:

```yaml
services:
  backend:
    ports:
      - "8082:8081"  # Change host port
```

## 📚 Additional Resources

- [Architecture Documentation](./ARCHITECTURE.md)
- [API Documentation](./API.md)
- [README](./README.md)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React Documentation](https://react.dev/)
- [Docker Documentation](https://docs.docker.com/)

## 🆘 Getting Help

- Check [Issues](https://github.com/your-repo/issues)
- Review [API Documentation](./API.md)
- Read [Architecture Guide](./ARCHITECTURE.md)

## ✅ Verification Checklist

After setup, verify the following:

- [ ] MySQL is running and accessible
- [ ] phpMyAdmin can connect to MySQL
- [ ] Backend API responds at http://localhost:8081/api/health
- [ ] Frontend loads at http://localhost:5173
- [ ] You can create a new user account
- [ ] You can log in successfully
- [ ] Dashboard displays after login

---

**Next Steps**: Once setup is complete, see [API.md](./API.md) for API usage and [ARCHITECTURE.md](./ARCHITECTURE.md) for system design details.
