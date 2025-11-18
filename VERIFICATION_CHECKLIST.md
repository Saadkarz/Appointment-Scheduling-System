# ✅ Setup Verification Checklist

Use this checklist to verify your Appointment System installation.

## Prerequisites Check

- [ ] Docker Desktop installed and running
- [ ] Java 17+ installed (for local dev)
- [ ] Maven 3.8+ installed (for local dev)
- [ ] Node.js 18+ installed (for local dev)
- [ ] Git installed

## Environment Configuration

- [ ] Created `backend/.env` from `backend/.env.example`
- [ ] Set `JWT_SECRET` in `backend/.env` (min 32 chars)
- [ ] Set `DB_PASSWORD` in `backend/.env`
- [ ] Created `frontend/.env` from `frontend/.env.example`
- [ ] Configured `VITE_API_BASE_URL` in `frontend/.env`

## Docker Services

Run `docker-compose ps` to verify:

- [ ] MySQL container running (port 3306)
- [ ] phpMyAdmin container running (port 8080)
- [ ] Redis container running (port 6379)
- [ ] Backend container running (port 8081)
- [ ] Frontend container running (port 5173)

## Service Health Checks

### Database
- [ ] Access phpMyAdmin at http://localhost:8080
- [ ] Login with user: `root`, password: `rootpassword`
- [ ] Verify `appointment_system` database exists
- [ ] Check that 13 tables are created:
  - [ ] users
  - [ ] staff_profiles
  - [ ] services
  - [ ] appointments
  - [ ] staff_working_hours
  - [ ] staff_breaks
  - [ ] oauth_tokens
  - [ ] reminders
  - [ ] calendar_events
  - [ ] webhook_subscriptions
  - [ ] activity_log
  - [ ] refresh_tokens
  - [ ] service_staff

### Backend API
- [ ] Access http://localhost:8081/api/health
- [ ] Response shows: `{"status":"UP"}`
- [ ] Check logs: `docker-compose logs backend`
- [ ] No errors in backend logs

### Frontend
- [ ] Access http://localhost:5173
- [ ] Frontend loads without errors
- [ ] Check browser console (F12) - no errors
- [ ] CSS styles are applied correctly

## Functionality Tests

### User Registration
- [ ] Click "Sign Up" on login page
- [ ] Fill in registration form:
  - [ ] First Name
  - [ ] Last Name
  - [ ] Email
  - [ ] Phone (optional)
  - [ ] Password (min 8 chars)
- [ ] Submit registration
- [ ] Registration succeeds
- [ ] Redirected to dashboard

### User Login
- [ ] Navigate to http://localhost:5173/login
- [ ] Enter registered email and password
- [ ] Click "Sign In"
- [ ] Login succeeds
- [ ] Redirected to dashboard
- [ ] Welcome message shows user's first name

### Dashboard
- [ ] Dashboard displays three stat cards:
  - [ ] Upcoming appointments
  - [ ] Completed appointments
  - [ ] Total appointments
- [ ] Navigation menu shows:
  - [ ] Dashboard
  - [ ] Book Appointment
  - [ ] My Appointments
  - [ ] Schedule (if STAFF role)
  - [ ] Admin (if ADMIN role)
- [ ] User can logout
- [ ] After logout, redirected to login page

### Protected Routes
- [ ] Logout
- [ ] Try accessing http://localhost:5173/ directly
- [ ] Should redirect to login page
- [ ] Login again
- [ ] Should redirect back to dashboard

### API Endpoints (using curl or Postman)

**Health Check:**
```bash
curl http://localhost:8081/api/health
```
- [ ] Returns: `{"status":"UP","service":"Appointment System API","timestamp":...}`

**Register:**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "phone": "+1234567890"
  }'
```
- [ ] Returns: `{"token":"...", "user":{...}}`

**Login:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```
- [ ] Returns: `{"token":"...", "user":{...}}`

**Get Appointments (requires JWT token):**
```bash
curl http://localhost:8081/api/appointments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```
- [ ] Returns: `[]` (empty array if no appointments)

## Database Verification

Connect to MySQL:
```bash
docker exec -it mysql mysql -u root -p
# Password: rootpassword
```

Run queries:
```sql
USE appointment_system;

-- Check users table
SELECT id, email, first_name, role, created_at FROM users;

-- Check Flyway migrations
SELECT * FROM flyway_schema_history;

-- Verify indexes
SHOW INDEX FROM appointments;

-- Check constraints
SELECT * FROM information_schema.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = 'appointment_system';
```

- [ ] At least one user exists (your registration)
- [ ] Flyway migration V1__init.sql applied successfully
- [ ] Indexes are created on appointments table
- [ ] Foreign key constraints exist

## Performance Check

- [ ] Frontend loads in < 2 seconds
- [ ] Login response in < 500ms
- [ ] Dashboard loads in < 1 second
- [ ] No memory warnings in Docker Desktop

## Log Verification

**Backend Logs:**
```bash
docker-compose logs backend | tail -n 50
```
- [ ] No ERROR level messages
- [ ] Spring Boot started successfully
- [ ] Database connection successful
- [ ] Flyway migrations applied

**Frontend Logs:**
```bash
docker-compose logs frontend | tail -n 50
```
- [ ] Nginx started successfully
- [ ] No build errors

**MySQL Logs:**
```bash
docker-compose logs mysql | tail -n 50
```
- [ ] MySQL started successfully
- [ ] Ready for connections

## Security Verification

- [ ] JWT tokens are generated with HS512 algorithm
- [ ] Passwords are hashed (not plain text in database)
- [ ] CORS is configured (check browser Network tab)
- [ ] Unauthorized API requests return 401
- [ ] Protected routes redirect to login

## Optional Features (if configured)

### Google Calendar Integration
- [ ] `GOOGLE_CLIENT_ID` set in `backend/.env`
- [ ] `GOOGLE_CLIENT_SECRET` set in `backend/.env`
- [ ] OAuth redirect URI configured in Google Console

### Microsoft Outlook Integration
- [ ] `MS_CLIENT_ID` set in `backend/.env`
- [ ] `MS_CLIENT_SECRET` set in `backend/.env`
- [ ] OAuth redirect URI configured in Azure Portal

### Twilio SMS
- [ ] `TWILIO_ACCOUNT_SID` set in `backend/.env`
- [ ] `TWILIO_AUTH_TOKEN` set in `backend/.env`
- [ ] `TWILIO_PHONE_NUMBER` set in `backend/.env`

### SendGrid Email
- [ ] `SENDGRID_API_KEY` set in `backend/.env`
- [ ] `SENDGRID_FROM_EMAIL` set in `backend/.env`

## Troubleshooting Completed

If you checked "No" on any item above:

### Backend won't start
- [ ] Checked Docker logs: `docker-compose logs backend`
- [ ] Verified MySQL is running
- [ ] Checked database credentials in `.env`
- [ ] Verified port 8081 is not in use

### Frontend won't load
- [ ] Checked browser console for errors
- [ ] Verified backend is running
- [ ] Checked CORS settings
- [ ] Verified port 5173 is not in use

### Database connection failed
- [ ] Verified MySQL container is running
- [ ] Checked `DB_HOST`, `DB_PORT`, `DB_NAME` in `.env`
- [ ] Verified database credentials
- [ ] Checked MySQL logs

### Login not working
- [ ] Verified user was created in database
- [ ] Checked backend logs for errors
- [ ] Verified JWT_SECRET is set
- [ ] Checked browser Network tab for API responses

## Final Verification

- [ ] All services running
- [ ] Can register new user
- [ ] Can login
- [ ] Can access dashboard
- [ ] Can logout
- [ ] Protected routes work
- [ ] API endpoints respond correctly
- [ ] No errors in any logs

## Next Steps

Once all checks pass:

1. **Read the documentation:**
   - [ ] [SETUP.md](./SETUP.md) - Detailed setup guide
   - [ ] [API.md](./API.md) - REST API reference
   - [ ] [ARCHITECTURE.md](./ARCHITECTURE.md) - System architecture
   - [ ] [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Feature status

2. **Start development:**
   - [ ] Implement BookingPage
   - [ ] Create StaffController
   - [ ] Add unit tests
   - [ ] Implement calendar integration

3. **Configure optional features:**
   - [ ] Setup Google Calendar OAuth
   - [ ] Setup Microsoft Outlook OAuth
   - [ ] Configure Twilio SMS
   - [ ] Configure SendGrid Email

---

**✅ Setup Complete!** You now have a fully functional appointment scheduling system running locally.

**Need Help?** 
- Check [SETUP.md](./SETUP.md) for troubleshooting
- Review [PROJECT_STATUS.md](./PROJECT_STATUS.md) for feature roadmap
- See [API.md](./API.md) for API documentation
