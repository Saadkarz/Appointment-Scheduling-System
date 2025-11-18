# System Architecture

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                                │
│                                                                       │
│  ┌────────────────┐  ┌────────────────┐  ┌─────────────────┐       │
│  │  Web Browser   │  │  Mobile App    │  │  Calendar Apps  │       │
│  │  (React/Vite)  │  │  (Future)      │  │  (Google/MS)    │       │
│  └────────┬───────┘  └────────┬───────┘  └────────┬────────┘       │
│           │                   │                    │                 │
└───────────┼───────────────────┼────────────────────┼─────────────────┘
            │                   │                    │
            │ HTTPS/WSS         │ HTTPS              │ Webhooks
            │                   │                    │
┌───────────▼───────────────────▼────────────────────▼─────────────────┐
│                       APPLICATION LAYER                               │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              Spring Boot Backend (Port 8081)                  │   │
│  │                                                                │   │
│  │  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐    │   │
│  │  │   REST API  │  │  WebSocket   │  │  Scheduler       │    │   │
│  │  │  Controller │  │  Handler     │  │  (@Scheduled)    │    │   │
│  │  └──────┬──────┘  └──────┬───────┘  └──────┬───────────┘    │   │
│  │         │                │                  │                 │   │
│  │  ┌──────▼────────────────▼──────────────────▼───────────┐    │   │
│  │  │              Service Layer                            │    │   │
│  │  │  • AppointmentService (Atomic Booking)               │    │   │
│  │  │  • CalendarSyncService (Google/MS Integration)       │    │   │
│  │  │  • ReminderService (Email/SMS)                       │    │   │
│  │  │  • AvailabilityService (Real-time slots)             │    │   │
│  │  │  • AuthService (JWT)                                 │    │   │
│  │  └───────────────────────┬──────────────────────────────┘    │   │
│  │                          │                                    │   │
│  │  ┌───────────────────────▼──────────────────────────────┐    │   │
│  │  │           Repository Layer (Spring Data JPA)         │    │   │
│  │  └───────────────────────┬──────────────────────────────┘    │   │
│  │                          │                                    │   │
│  └──────────────────────────┼────────────────────────────────────┘   │
│                             │                                        │
│  ┌──────────────────────────▼────────────────────────────────────┐  │
│  │              Security Layer (Spring Security)                 │  │
│  │  • JWT Token Filter                                           │  │
│  │  • Role-based Access Control (USER, STAFF, ADMIN)            │  │
│  │  • OAuth 2.0 Integration                                      │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
└───────────────────────────────┬───────────────────────────────────────┘
                                │
                                │ JDBC
                                │
┌───────────────────────────────▼───────────────────────────────────────┐
│                         DATA LAYER                                    │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │              MySQL 8 Database (Port 3306)                     │    │
│  │                                                                │    │
│  │  Tables:                                                       │    │
│  │  • users (authentication)                                      │    │
│  │  • staff_profiles (staff info)                                │    │
│  │  • appointments (bookings with versioning)                    │    │
│  │  • staff_working_hours (schedules)                            │    │
│  │  • staff_breaks (break periods)                               │    │
│  │  • services (service catalog)                                 │    │
│  │  • oauth_tokens (calendar auth)                               │    │
│  │  • reminders (notification queue)                             │    │
│  │  • calendar_events (external sync)                            │    │
│  │                                                                │    │
│  │  Features:                                                     │    │
│  │  • UTC datetime storage                                       │    │
│  │  • Optimistic locking (version column)                        │    │
│  │  • Foreign key constraints                                    │    │
│  │  • Indexed queries                                            │    │
│  │  • Soft deletes                                               │    │
│  └──────────────────────────────────────────────────────────────┘    │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │            phpMyAdmin (Port 8080) - DB Admin UI               │    │
│  └──────────────────────────────────────────────────────────────┘    │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                         CACHE LAYER (Optional)                         │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────┐     │
│  │                 Redis (Port 6379)                             │     │
│  │  • Session storage                                            │     │
│  │  • Rate limiting                                              │     │
│  │  • Distributed locks                                          │     │
│  │  • Cache availability slots                                   │     │
│  └──────────────────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                     EXTERNAL SERVICES                                  │
│                                                                         │
│  ┌────────────────────┐  ┌────────────────────┐  ┌──────────────────┐│
│  │  Google Calendar   │  │  Microsoft Graph   │  │  Twilio (SMS)    ││
│  │  API (OAuth 2.0)   │  │  API (Office 365)  │  │                  ││
│  └────────────────────┘  └────────────────────┘  └──────────────────┘│
│                                                                         │
│  ┌────────────────────┐                                                │
│  │  SendGrid/SMTP     │                                                │
│  │  (Email Service)   │                                                │
│  └────────────────────┘                                                │
└────────────────────────────────────────────────────────────────────────┘
```

## Component Breakdown

### 1. Frontend Layer (React + Vite)

**Technology:** React 18, TypeScript, Tailwind CSS, Vite

**Responsibilities:**
- User interface rendering
- Client-side routing
- State management
- JWT token storage (localStorage)
- API communication via Axios
- Real-time updates via WebSocket
- Form validation
- Responsive design

**Key Components:**
- `LoginPage` - User authentication
- `SignupPage` - User registration
- `BookingPage` - Appointment booking interface
- `CalendarView` - Visual calendar display
- `AdminDashboard` - Analytics and metrics
- `StaffSchedule` - Staff availability management
- `AppointmentList` - Booking history

**Protected Routes:**
- User routes: `/bookings`, `/profile`
- Staff routes: `/schedule`, `/appointments`
- Admin routes: `/admin/*`

### 2. Backend Layer (Spring Boot)

**Technology:** Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA

**Modules:**

#### Controllers
Handle HTTP requests and WebSocket connections
- `AuthController` - Authentication endpoints
- `AppointmentController` - Appointment CRUD
- `StaffController` - Staff management
- `CalendarIntegrationController` - OAuth & sync
- `AdminController` - Analytics endpoints
- `WebSocketController` - Real-time updates

#### Services (Business Logic)
- `AppointmentService` - Atomic booking with conflict detection
- `AvailabilityService` - Calculate available time slots
- `CalendarSyncService` - Sync with Google/Microsoft
- `ReminderService` - Schedule and send reminders
- `AuthService` - JWT generation and validation
- `StaffService` - Manage staff profiles
- `NotificationService` - Email/SMS sending

#### Repositories (Data Access)
- `UserRepository`
- `AppointmentRepository`
- `StaffProfileRepository`
- `OAuthTokenRepository`
- `ReminderRepository`
- `CalendarEventRepository`

#### Security
- `JwtTokenProvider` - Create and validate JWT
- `JwtAuthenticationFilter` - Intercept requests
- `SecurityConfig` - Security rules and CORS
- `UserDetailsServiceImpl` - Load user details

#### Scheduled Tasks
- `ReminderScheduler` - Process reminder queue (every minute)
- `CalendarSyncScheduler` - Sync external calendars (every 15 min)

### 3. Database Layer (MySQL 8)

**Schema Design:**

```sql
users
├── id (PK)
├── email (UNIQUE)
├── password_hash
├── role (ENUM: USER, STAFF, ADMIN)
├── timezone
└── created_at

staff_profiles
├── id (PK)
├── user_id (FK → users)
├── name
├── title
├── bio
└── timezone

appointments
├── id (PK)
├── user_id (FK → users)
├── staff_id (FK → staff_profiles)
├── service_id (FK → services)
├── start_time (UTC)
├── end_time (UTC)
├── status (ENUM)
├── version (optimistic lock)
└── external_event_id

staff_working_hours
├── id (PK)
├── staff_id (FK → staff_profiles)
├── day_of_week (0-6)
├── start_time
└── end_time

staff_breaks
├── id (PK)
├── staff_id (FK → staff_profiles)
├── start_time (UTC)
└── end_time (UTC)

oauth_tokens
├── id (PK)
├── user_id (FK → users)
├── provider (google/microsoft)
├── access_token
├── refresh_token
└── expires_at

reminders
├── id (PK)
├── appointment_id (FK → appointments)
├── type (email/sms)
├── scheduled_time (UTC)
├── status (PENDING/SENT/FAILED)
└── retry_count

calendar_events
├── id (PK)
├── appointment_id (FK → appointments)
├── provider
├── external_event_id
└── last_synced_at
```

**Indexes:**
- `appointments(user_id, start_time)`
- `appointments(staff_id, start_time)`
- `appointments(status, start_time)`
- `reminders(scheduled_time, status)`
- `oauth_tokens(user_id, provider)`

### 4. External Integrations

#### Google Calendar API
- **OAuth Flow:** Authorization Code Grant
- **Scopes:** `calendar.events`, `calendar.readonly`
- **Operations:** Create, Update, Delete events
- **Webhooks:** Push notifications for changes

#### Microsoft Graph API
- **OAuth Flow:** Authorization Code Grant
- **Scopes:** `Calendars.ReadWrite`, `offline_access`
- **Operations:** Create, Update, Delete calendar events
- **Webhooks:** Change notifications via subscriptions

#### Twilio (SMS)
- **Service:** Programmable SMS
- **Operations:** Send reminder messages
- **Error Handling:** Retry on failure

#### SendGrid (Email)
- **Service:** Email API
- **Templates:** Appointment confirmation, reminders
- **Error Handling:** Retry logic

## Sequence Diagrams

### 1. Appointment Booking Flow

```
┌──────┐  ┌──────────┐  ┌─────────┐  ┌──────────┐  ┌────────────┐
│ User │  │ Frontend │  │ Backend │  │ Database │  │ Calendar   │
│      │  │          │  │         │  │          │  │ API        │
└──┬───┘  └────┬─────┘  └────┬────┘  └────┬─────┘  └─────┬──────┘
   │           │             │            │              │
   │ Select    │             │            │              │
   │ Staff &   │             │            │              │
   │ Time      │             │            │              │
   ├──────────►│             │            │              │
   │           │             │            │              │
   │           │ GET /staff/{id}/         │              │
   │           │     availability         │              │
   │           ├────────────►│            │              │
   │           │             │            │              │
   │           │             │ SELECT     │              │
   │           │             │ appointments,│            │
   │           │             │ working_hours,│           │
   │           │             │ breaks       │            │
   │           │             ├───────────►│              │
   │           │             │            │              │
   │           │             │◄───────────┤              │
   │           │             │ Available  │              │
   │           │◄────────────┤ Slots      │              │
   │           │ 200 OK      │            │              │
   │◄──────────┤             │            │              │
   │ Display   │             │            │              │
   │ Slots     │             │            │              │
   │           │             │            │              │
   │ Book      │             │            │              │
   │ Slot      │             │            │              │
   ├──────────►│             │            │              │
   │           │             │            │              │
   │           │ POST /appointments       │              │
   │           ├────────────►│            │              │
   │           │             │            │              │
   │           │             │ BEGIN TRANSACTION         │
   │           │             ├───────────►│              │
   │           │             │            │              │
   │           │             │ SELECT ... FOR UPDATE    │
   │           │             │ (lock conflicting slots) │
   │           │             ├───────────►│              │
   │           │             │            │              │
   │           │             │◄───────────┤              │
   │           │             │ No conflict│              │
   │           │             │            │              │
   │           │             │ INSERT     │              │
   │           │             │ appointment│              │
   │           │             ├───────────►│              │
   │           │             │            │              │
   │           │             │ INSERT     │              │
   │           │             │ reminders  │              │
   │           │             ├───────────►│              │
   │           │             │            │              │
   │           │             │ COMMIT     │              │
   │           │             ├───────────►│              │
   │           │             │            │              │
   │           │             │            │              │
   │           │             │ Create Event              │
   │           │             ├──────────────────────────►│
   │           │             │ (Google/Microsoft)        │
   │           │             │            │              │
   │           │             │◄──────────────────────────┤
   │           │             │ event_id   │              │
   │           │             │            │              │
   │           │             │ UPDATE appointment        │
   │           │             │ SET external_event_id     │
   │           │             ├───────────►│              │
   │           │             │            │              │
   │           │◄────────────┤            │              │
   │           │ 201 Created │            │              │
   │◄──────────┤             │            │              │
   │ Confirm   │             │            │              │
   │           │             │            │              │
```

**Key Points:**
1. **Atomic Booking:** Uses database transaction with `SELECT ... FOR UPDATE`
2. **Conflict Prevention:** Locks overlapping time slots before insertion
3. **Calendar Sync:** Creates event in external calendar after database commit
4. **Idempotency:** Stores `external_event_id` to prevent duplicates

### 2. Calendar Synchronization Flow

```
┌────────────┐  ┌─────────┐  ┌──────────┐  ┌────────────┐
│ Calendar   │  │ Backend │  │ Database │  │ User       │
│ API        │  │         │  │          │  │ Frontend   │
└─────┬──────┘  └────┬────┘  └────┬─────┘  └─────┬──────┘
      │              │            │              │
      │ Event        │            │              │
      │ Changed      │            │              │
      │ (Webhook)    │            │              │
      ├─────────────►│            │              │
      │ POST /webhook│            │              │
      │              │            │              │
      │              │ SELECT     │              │
      │              │ appointment│              │
      │              │ WHERE      │              │
      │              │ external_  │              │
      │              │ event_id   │              │
      │              ├───────────►│              │
      │              │            │              │
      │              │◄───────────┤              │
      │              │ Found      │              │
      │              │            │              │
      │              │ Fetch Event│              │
      │              │ Details    │              │
      │◄─────────────┤            │              │
      │ GET /events/{id}          │              │
      │              │            │              │
      ├─────────────►│            │              │
      │ Event Data   │            │              │
      │              │            │              │
      │              │ UPDATE     │              │
      │              │ appointment│              │
      │              │ (time/     │              │
      │              │  status)   │              │
      │              ├───────────►│              │
      │              │            │              │
      │              │            │ WebSocket    │
      │              │            │ Notification │
      │              ├───────────────────────────►│
      │              │            │              │
      │              │            │  Refresh UI  │
      │              │            │◄─────────────┤
      │              │            │              │
```

**Bidirectional Sync:**
- **App → Calendar:** Create/update/delete events via API
- **Calendar → App:** Receive webhooks for external changes
- **Duplicate Prevention:** Use `external_event_id` mapping

### 3. Reminder System Flow

```
┌───────────┐  ┌─────────┐  ┌──────────┐  ┌──────────┐
│ Scheduler │  │ Backend │  │ Database │  │ External │
│ (Cron)    │  │ Service │  │          │  │ Service  │
└─────┬─────┘  └────┬────┘  └────┬─────┘  └────┬─────┘
      │             │            │              │
      │ Every       │            │              │
      │ Minute      │            │              │
      ├────────────►│            │              │
      │ @Scheduled  │            │              │
      │             │            │              │
      │             │ SELECT     │              │
      │             │ reminders  │              │
      │             │ WHERE      │              │
      │             │ scheduled_time <= NOW()   │
      │             │ AND status = 'PENDING'    │
      │             ├───────────►│              │
      │             │            │              │
      │             │◄───────────┤              │
      │             │ Due        │              │
      │             │ Reminders  │              │
      │             │            │              │
      │             │ FOR EACH:  │              │
      │             │            │              │
      │             │ Send Email/SMS            │
      │             ├──────────────────────────►│
      │             │            │              │
      │             │◄──────────────────────────┤
      │             │ Success    │              │
      │             │            │              │
      │             │ UPDATE     │              │
      │             │ reminders  │              │
      │             │ SET status=│              │
      │             │ 'SENT'     │              │
      │             ├───────────►│              │
      │             │            │              │
      │ (If failed) │            │              │
      │             │ UPDATE     │              │
      │             │ retry_count│              │
      │             │ ++         │              │
      │             ├───────────►│              │
      │             │            │              │
      │             │ Reschedule │              │
      │             │ (exponential back-off)    │
      │             │            │              │
```

**Reminder Logic:**
- **Triggers:** 24h, 1h, 15min before appointment
- **Methods:** Email (SendGrid) and SMS (Twilio)
- **Retry:** Max 3 attempts with exponential backoff
- **Status Tracking:** PENDING → SENT/FAILED

## Data Flow

### Real-Time Availability Check

```
1. User requests availability for Staff X on Date Y
2. Backend queries:
   - staff_working_hours (base schedule)
   - staff_breaks (subtract break times)
   - appointments (subtract booked slots)
3. Calculate free slots (e.g., 30-min intervals)
4. Return available slots to frontend
5. Cache result in Redis (5 min TTL)
```

### Conflict Detection Strategy

```sql
-- Atomic booking with row-level locking
BEGIN TRANSACTION;

SELECT id FROM appointments
WHERE staff_id = ?
  AND status NOT IN ('CANCELLED')
  AND (
    (start_time < ? AND end_time > ?) OR  -- overlaps start
    (start_time < ? AND end_time > ?) OR  -- overlaps end
    (start_time >= ? AND end_time <= ?)   -- contained within
  )
FOR UPDATE;

-- If no rows returned, slot is free
INSERT INTO appointments (...) VALUES (...);

COMMIT;
```

## Security Architecture

### Authentication Flow
1. User submits credentials
2. Backend validates against `users` table
3. Generate JWT with claims: `user_id`, `role`, `exp`
4. Frontend stores JWT in `localStorage`
5. All subsequent requests include JWT in `Authorization` header

### Authorization Matrix

| Role  | Permissions |
|-------|-------------|
| USER  | Book/view own appointments, view staff availability |
| STAFF | Manage own schedule, view assigned appointments, update appointment status |
| ADMIN | Full access to all resources, analytics, user management |

### Security Measures
- Password hashing (BCrypt)
- JWT expiration (24h)
- Refresh tokens (7 days)
- Rate limiting (Redis)
- CORS configuration
- SQL injection prevention (JPA)
- XSS protection (React escaping)

## Scalability Considerations

### Horizontal Scaling
- Stateless backend (JWT auth)
- Load balancer (Nginx/AWS ALB)
- Multiple Spring Boot instances
- Redis for shared session state

### Database Optimization
- Connection pooling (HikariCP)
- Read replicas for analytics
- Index optimization
- Query result caching

### Caching Strategy
- Redis for:
  - Availability slot cache
  - Rate limiting counters
  - Distributed locks
  - Session storage

## Monitoring & Observability

### Metrics
- Request rate and latency
- Database query performance
- External API call success rate
- Reminder delivery rate
- Appointment booking success rate

### Logging
- Structured JSON logs
- Correlation IDs for tracing
- Error stack traces
- Audit trail for appointments

### Health Checks
- `/actuator/health` - Spring Boot health
- Database connectivity
- External API reachability
- Redis connectivity
