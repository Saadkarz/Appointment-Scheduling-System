# API Documentation

## Base URL
```
http://localhost:8081/api
```

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

---

## Authentication Endpoints

### Register User
**POST** `/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "timezone": "America/New_York"
}
```

**Response:** `201 Created`
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "timezone": "America/New_York",
    "role": "USER"
  }
}
```

### Login
**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

---

## Appointment Endpoints

### Create Appointment
**POST** `/appointments`
**Auth Required:** Yes

**Request Body:**
```json
{
  "staffId": 2,
  "serviceId": 1,
  "startTime": "2024-12-20T10:00:00Z",
  "endTime": "2024-12-20T10:30:00Z",
  "notes": "First consultation"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "userId": 1,
  "staffId": 2,
  "serviceId": 1,
  "startTime": "2024-12-20T10:00:00Z",
  "endTime": "2024-12-20T10:30:00Z",
  "status": "PENDING",
  "notes": "First consultation",
  "createdAt": "2024-12-15T08:30:00Z"
}
```

### Get Appointments
**GET** `/appointments`
**Auth Required:** Yes

**Query Parameters:**
- `userId` (optional): Filter by user ID
- `staffId` (optional): Filter by staff ID
- `from` (optional): Start date-time (ISO 8601)
- `to` (optional): End date-time (ISO 8601)

**Example:**
```
GET /appointments?userId=1
GET /appointments?staffId=2&from=2024-12-20T00:00:00Z&to=2024-12-27T00:00:00Z
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "staffId": 2,
    "serviceId": 1,
    "startTime": "2024-12-20T10:00:00Z",
    "endTime": "2024-12-20T10:30:00Z",
    "status": "CONFIRMED",
    "notes": "First consultation",
    "createdAt": "2024-12-15T08:30:00Z"
  }
]
```

### Get Appointment by ID
**GET** `/appointments/{id}`
**Auth Required:** Yes

**Response:** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "staffId": 2,
  "serviceId": 1,
  "startTime": "2024-12-20T10:00:00Z",
  "endTime": "2024-12-20T10:30:00Z",
  "status": "CONFIRMED",
  "notes": "First consultation",
  "createdAt": "2024-12-15T08:30:00Z"
}
```

### Update Appointment
**PATCH** `/appointments/{id}`
**Auth Required:** Yes

**Request Body:**
```json
{
  "startTime": "2024-12-20T11:00:00Z",
  "endTime": "2024-12-20T11:30:00Z",
  "notes": "Updated notes"
}
```

**Response:** `200 OK`

### Cancel Appointment
**DELETE** `/appointments/{id}`
**Auth Required:** Yes

**Query Parameters:**
- `reason` (optional): Cancellation reason

**Example:**
```
DELETE /appointments/1?reason=Schedule conflict
```

**Response:** `204 No Content`

---

## Staff Endpoints

### Get All Staff
**GET** `/staff`
**Auth Required:** Yes

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 2,
    "title": "Senior Consultant",
    "bio": "10+ years experience",
    "specialization": "General Consultation",
    "timezone": "America/New_York",
    "isAvailable": true,
    "averageRating": 4.8,
    "totalReviews": 45
  }
]
```

### Get Staff by ID
**GET** `/staff/{id}`
**Auth Required:** Yes

**Response:** `200 OK`

### Get Staff Availability
**GET** `/staff/{id}/availability`
**Auth Required:** Yes

**Query Parameters:**
- `date` (required): Date in YYYY-MM-DD format

**Example:**
```
GET /staff/1/availability?date=2024-12-20
```

**Response:** `200 OK`
```json
{
  "date": "2024-12-20",
  "staffId": 1,
  "availableSlots": [
    {
      "startTime": "2024-12-20T09:00:00Z",
      "endTime": "2024-12-20T09:30:00Z"
    },
    {
      "startTime": "2024-12-20T09:30:00Z",
      "endTime": "2024-12-20T10:00:00Z"
    }
  ]
}
```

### Update Staff Schedule
**PATCH** `/staff/{id}/schedule`
**Auth Required:** STAFF, ADMIN

**Request Body:**
```json
{
  "workingHours": [
    {
      "dayOfWeek": 1,
      "startTime": "09:00",
      "endTime": "17:00"
    }
  ],
  "breaks": [
    {
      "dayOfWeek": 1,
      "startTime": "12:00",
      "endTime": "13:00",
      "reason": "Lunch break"
    }
  ]
}
```

**Response:** `200 OK`

---

## Service Endpoints

### Get All Services
**GET** `/services`
**Auth Required:** Yes

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "General Consultation",
    "description": "30-minute general consultation",
    "durationMinutes": 30,
    "price": 50.00,
    "isActive": true
  }
]
```

### Get Service by ID
**GET** `/services/{id}`
**Auth Required:** Yes

---

## Calendar Integration Endpoints

### Get Google OAuth URL
**GET** `/integrations/google/auth-url`
**Auth Required:** Yes

**Response:** `200 OK`
```json
{
  "authUrl": "https://accounts.google.com/o/oauth2/v2/auth?..."
}
```

### Google OAuth Callback
**POST** `/integrations/google/oauth/callback`
**Auth Required:** Yes

**Request Body:**
```json
{
  "code": "authorization_code_from_google"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Google Calendar connected successfully"
}
```

### Google Calendar Webhook
**POST** `/integrations/google/webhook`
**Auth Required:** No (validated by Google)

**Headers:**
```
X-Goog-Channel-ID: channel_id
X-Goog-Resource-State: updated
```

**Response:** `200 OK`

### Get Microsoft OAuth URL
**GET** `/integrations/microsoft/auth-url`
**Auth Required:** Yes

**Response:** `200 OK`
```json
{
  "authUrl": "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?..."
}
```

### Microsoft OAuth Callback
**POST** `/integrations/microsoft/oauth/callback`
**Auth Required:** Yes

**Request Body:**
```json
{
  "code": "authorization_code_from_microsoft"
}
```

**Response:** `200 OK`

---

## Admin Endpoints

### Get Analytics
**GET** `/admin/analytics`
**Auth Required:** ADMIN

**Response:** `200 OK`
```json
{
  "totalAppointments": 1250,
  "activeUsers": 350,
  "activeStaff": 15,
  "appointmentsByStatus": {
    "PENDING": 45,
    "CONFIRMED": 120,
    "COMPLETED": 980,
    "CANCELLED": 85,
    "NO_SHOW": 20
  },
  "revenueThisMonth": 45000.00,
  "averageRating": 4.7
}
```

### Get Activity Log
**GET** `/admin/activity-log`
**Auth Required:** ADMIN

**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "userId": 5,
      "entityType": "APPOINTMENT",
      "entityId": 123,
      "action": "CREATE",
      "timestamp": "2024-12-15T10:30:00Z",
      "ipAddress": "192.168.1.100"
    }
  ],
  "totalElements": 500,
  "totalPages": 25,
  "page": 0,
  "size": 20
}
```

### Get Staff Load
**GET** `/admin/staff-load`
**Auth Required:** ADMIN

**Response:** `200 OK`
```json
[
  {
    "staffId": 1,
    "name": "Dr. John Smith",
    "totalAppointments": 85,
    "completedAppointments": 75,
    "noShowCount": 3,
    "averageRating": 4.8,
    "utilizationRate": 0.78
  }
]
```

---

## User Endpoints

### Get Current User Profile
**GET** `/users/me`
**Auth Required:** Yes

**Response:** `200 OK`
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "timezone": "America/New_York",
  "role": "USER"
}
```

### Update User Profile
**PATCH** `/users/me`
**Auth Required:** Yes

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "timezone": "America/Los_Angeles"
}
```

**Response:** `200 OK`

---

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2024-12-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input",
  "details": {
    "fieldName": "error message"
  }
}
```

### Common HTTP Status Codes

- `200 OK` - Request succeeded
- `201 Created` - Resource created successfully
- `204 No Content` - Request succeeded, no content to return
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., time slot already booked)
- `500 Internal Server Error` - Server error

---

## Rate Limiting

API requests are rate-limited to:
- **100 requests per minute** per user
- **1000 requests per hour** per user

Rate limit headers:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1702656000
```

---

## Pagination

List endpoints support pagination:

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 20, max: 100)
- `sort` (optional, format: `field,direction`)

**Example:**
```
GET /appointments?page=0&size=20&sort=startTime,desc
```

**Response includes:**
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "page": 0,
  "size": 20
}
```
