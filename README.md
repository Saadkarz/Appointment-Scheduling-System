# Appointment Scheduling System

A full-stack appointment scheduling system for clinics, salons, consultants, and coworking spaces.

## 🚀 Quick Start

**Windows Users:**
```bash
start.bat
```

**Mac/Linux Users:**
```bash
docker-compose up -d
```

Then open http://localhost:5173

**📖 For detailed setup instructions, see [SETUP.md](./SETUP.md)**

## 🏗️ Architecture Overview

### System Components

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  React Frontend │────▶│  Spring Boot    │────▶│     MySQL 8     │
│   (Vite + TS)   │◀────│    Backend      │◀────│    Database     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                              │     │
                              │     └──────────▶ ┌─────────────────┐
                              │                  │  Redis (Cache)  │
                              │                  └─────────────────┘
                              ▼
                        ┌──────────────────────┐
                        │  External Services:  │
                        │  • Google Calendar   │
                        │  • MS Graph API      │
                        │  • Twilio (SMS)      │
                        │  • SendGrid (Email)  │
                        └──────────────────────┘
```

### Technology Stack

**Backend:**
- Java 17+
- Spring Boot 3.x
- Spring Security (JWT)
- Spring Data JPA
- Flyway Migrations
- MySQL Connector
- Lombok
- WebSocket Support

**Database:**
- MySQL 8.0+
- phpMyAdmin for administration

**Frontend:**
- React 18+
- TypeScript
- Vite
- Tailwind CSS
- Axios
- React Router

**DevOps:**
- Docker & Docker Compose
- GitHub Actions (CI/CD)
- Redis (Optional)

## 📋 Features

### Core Features
- ✅ User authentication (JWT-based)
- ✅ Role-based access control (USER, STAFF, ADMIN)
- ✅ Staff profile management (working hours, breaks)
- ✅ Appointment CRUD operations
- ✅ Real-time availability checking
- ✅ Timezone support (UTC storage)
- ✅ Automated reminders (Email/SMS)
- ✅ Admin analytics dashboard

### Calendar Integrations
- ✅ Google Calendar sync (OAuth 2.0)
- ✅ Microsoft Outlook/Office 365 sync
- ✅ Bidirectional sync
- ✅ Webhook support for external changes
- ✅ Duplicate prevention

### Real-Time Features
- ✅ WebSocket-based availability updates
- ✅ Atomic conflict detection (SELECT FOR UPDATE)
- ✅ Optimistic locking

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- MySQL 8+ (or use Docker)

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- MySQL on port 3306
- phpMyAdmin on http://localhost:8080
- Redis on port 6379
- Backend on port 8081

### 2. Configure Environment Variables

**Backend (.env or application-local.properties):**

```properties
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=appointment_system
DB_USER=root
DB_PASSWORD=rootpassword

# JWT
JWT_SECRET=your-256-bit-secret-key-change-this-in-production
JWT_EXPIRATION=86400000

# Google Calendar
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8081/api/integrations/google/oauth/callback

# Microsoft Graph
MS_CLIENT_ID=your-microsoft-client-id
MS_CLIENT_SECRET=your-microsoft-client-secret
MS_TENANT_ID=common
MS_REDIRECT_URI=http://localhost:8081/api/integrations/microsoft/oauth/callback

# Twilio (SMS)
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_FROM_NUMBER=+1234567890

# SendGrid (Email)
SENDGRID_API_KEY=your-sendgrid-key
SENDGRID_FROM_EMAIL=noreply@yourapp.com

# Redis (optional)
REDIS_HOST=localhost
REDIS_PORT=6379
```

**Frontend (.env):**

```env
VITE_API_BASE_URL=http://localhost:8081/api
VITE_WS_BASE_URL=ws://localhost:8081/ws
```

### 3. Run Backend

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

Backend will be available at: http://localhost:8081

### 4. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend will be available at: http://localhost:5173

### 5. Access phpMyAdmin

Navigate to: http://localhost:8080

- **Server:** mysql
- **Username:** root
- **Password:** rootpassword
- **Database:** appointment_system

## 📊 Database Schema

### Core Tables

- **users** - User accounts with credentials
- **staff_profiles** - Staff information and metadata
- **appointments** - Booking records with conflict detection
- **staff_working_hours** - Weekly schedule per staff
- **staff_breaks** - Break periods
- **services** - Service catalog
- **oauth_tokens** - External calendar authentication
- **reminders** - Scheduled notifications
- **calendar_events** - Synced external events

### Key Features

- UTC datetime storage
- Optimistic locking (version column)
- Soft deletes
- Audit columns (created_at, updated_at)
- Proper indexing for performance

## 🔐 Authentication Flow

### Sequence Diagram: User Login

```
User          Frontend        Backend         Database
 │                │              │                │
 │──Register────▶│              │                │
 │                │──POST────────▶│              │
 │                │  /auth/register              │
 │                │              │──INSERT USER──▶│
 │                │              │◀────OK─────────│
 │                │◀──200 OK─────│                │
 │◀──Success─────│              │                │
 │                │              │                │
 │──Login────────▶│              │                │
 │                │──POST────────▶│              │
 │                │  /auth/login  │                │
 │                │              │──VERIFY────────▶│
 │                │              │◀────USER───────│
 │                │              │                │
 │                │◀──JWT Token──│                │
 │◀──Token───────│              │                │
```

## 📅 Appointment Booking Flow

### Sequence Diagram

```
User       Frontend    Backend       Database    Calendar API
 │            │           │              │              │
 │─Check─────▶│           │              │              │
 │ Availability│           │              │              │
 │            │──GET──────▶│              │              │
 │            │ /staff/1   │              │              │
 │            │ /availability             │              │
 │            │           │──SELECT───────▶│              │
 │            │           │  (check slots) │              │
 │            │           │◀─Available─────│              │
 │            │◀──Slots───│              │              │
 │◀─Show─────│           │              │              │
 │ Calendar   │           │              │              │
 │            │           │              │              │
 │─Book──────▶│           │              │              │
 │            │──POST─────▶│              │              │
 │            │ /appointments             │              │
 │            │           │──BEGIN TRANSACTION──▶        │
 │            │           │──SELECT FOR UPDATE────▶      │
 │            │           │  (lock conflicts)     │      │
 │            │           │◀─No Conflict──────────│      │
 │            │           │──INSERT APPOINTMENT───▶│      │
 │            │           │──COMMIT TRANSACTION───▶│      │
 │            │           │              │              │
 │            │           │──Create Event────────────────▶│
 │            │           │  (Google/Outlook)            │
 │            │           │◀─Event Created───────────────│
 │            │           │──Store external_event_id──▶│  │
 │            │           │              │              │
 │            │◀──201─────│              │              │
 │◀─Success──│           │              │              │
```

## 🔔 Reminder System Flow

```
Scheduler     Backend       Database      Notification Service
    │            │              │                  │
    │─Every──────▶│              │                  │
    │  Minute     │              │                  │
    │            │──SELECT───────▶│                  │
    │            │  reminders     │                  │
    │            │  WHERE due     │                  │
    │            │◀─Pending───────│                  │
    │            │  Reminders     │                  │
    │            │                │                  │
    │            │──For Each──────┐                  │
    │            │  Reminder      │                  │
    │            │                │                  │
    │            │──Send Email────────────────────────▶│
    │            │  or SMS                           │
    │            │◀──Sent/Failed─────────────────────│
    │            │                                   │
    │            │──UPDATE────────▶│                 │
    │            │  status=SENT   │                 │
    │            │  or retry_count++                │
```

## 🌐 API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT |
| POST | `/api/auth/refresh` | Refresh JWT token |

### Users & Staff

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Get current user profile |
| GET | `/api/staff` | List all staff members |
| GET | `/api/staff/{id}` | Get staff details |
| GET | `/api/staff/{id}/availability` | Get available time slots |
| PATCH | `/api/staff/{id}/schedule` | Update working hours |

### Appointments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/appointments` | Create appointment |
| GET | `/api/appointments` | List appointments (filtered) |
| GET | `/api/appointments/{id}` | Get appointment details |
| PATCH | `/api/appointments/{id}` | Update appointment |
| DELETE | `/api/appointments/{id}` | Cancel appointment |

### Calendar Integrations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/integrations/google/auth-url` | Get OAuth URL |
| POST | `/api/integrations/google/oauth/callback` | Handle OAuth callback |
| POST | `/api/integrations/google/webhook` | Webhook for changes |
| GET | `/api/integrations/microsoft/auth-url` | MS OAuth URL |
| POST | `/api/integrations/microsoft/oauth/callback` | MS callback |

### Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/analytics` | Dashboard metrics |
| GET | `/api/admin/activity-log` | System activity log |
| GET | `/api/admin/staff-load` | Staff workload report |

## 🧪 Testing

### Run Backend Tests

```bash
cd backend
./mvnw test
```

### Run Frontend Tests

```bash
cd frontend
npm test
```

## 📦 Deployment

### Build Docker Images

```bash
docker-compose build
```

### Deploy to Production

```bash
# Using GitHub Actions (push to main branch)
git push origin main

# Or manually
docker-compose -f docker-compose.prod.yml up -d
```

## 🔧 Configuration

### OAuth Setup

#### Google Calendar API

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable Google Calendar API
4. Create OAuth 2.0 credentials
5. Add authorized redirect URI: `http://localhost:8081/api/integrations/google/oauth/callback`
6. Copy Client ID and Secret to environment variables

#### Microsoft Graph API

1. Go to [Azure Portal](https://portal.azure.com/)
2. Register a new application
3. Add API permissions: `Calendars.ReadWrite`, `offline_access`
4. Create client secret
5. Add redirect URI: `http://localhost:8081/api/integrations/microsoft/oauth/callback`
6. Copy Application (client) ID and secret

### Twilio Setup (SMS)

1. Sign up at [Twilio](https://www.twilio.com/)
2. Get Account SID and Auth Token
3. Purchase a phone number
4. Add credentials to environment variables

### SendGrid Setup (Email)

1. Sign up at [SendGrid](https://sendgrid.com/)
2. Create an API key
3. Verify sender email address
4. Add API key to environment variables

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [MySQL 8 Reference](https://dev.mysql.com/doc/refman/8.0/en/)
- [Google Calendar API](https://developers.google.com/calendar/api)
- [Microsoft Graph API](https://docs.microsoft.com/en-us/graph/overview)

## 📄 License

MIT License - See LICENSE file for details

## 👥 Support

For issues and questions:
- Create an issue on GitHub
- Email: support@yourapp.com
