# HireFlow AI — REST API Contract

**Base URL**: `https://api.hireflow.ai/api/v1`  
**Auth**: `Authorization: Bearer <access_token>` (all protected routes)  
**Content-Type**: `application/json` (unless noted)

---

## Error Envelope (All Errors)

```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable message",
  "path": "/api/v1/jobs",
  "traceId": "abc-123"
}
```

| Code | Meaning |
|---|---|
| 400 | Validation failure (field errors in `fieldErrors[]`) |
| 401 | Missing / expired / invalid JWT |
| 403 | Authenticated but insufficient role |
| 404 | Resource not found |
| 409 | Conflict (duplicate, already exists) |
| 422 | Business rule violation |
| 429 | Rate limit exceeded |
| 500 | Internal server error |

---

## Pagination Convention

All list endpoints accept:

| Query Param | Default | Description |
|---|---|---|
| `page` | `0` | 0-indexed page number |
| `size` | `20` | Items per page (max 100) |
| `sort` | varies | e.g. `createdAt,desc` |

All list responses wrap data in:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 145,
  "totalPages": 8
}
```

---

## 1. Auth

### `POST /auth/register`
**Auth**: None

**Request**
```json
{
  "email": "jane@example.com",
  "password": "Str0ng!Pass",
  "firstName": "Jane",
  "lastName": "Doe",
  "role": "CANDIDATE"        // "CANDIDATE" | "RECRUITER"
}
```

**Response** `201 Created`
```json
{
  "message": "Registration successful. Please verify your email.",
  "userId": "uuid"
}
```

---

### `POST /auth/login`
**Auth**: None  

**Request**
```json
{
  "email": "jane@example.com",
  "password": "Str0ng!Pass"
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "opaque-token-string",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "jane@example.com",
    "firstName": "Jane",
    "lastName": "Doe",
    "role": "CANDIDATE",
    "avatarUrl": null
  }
}
```

---

### `POST /auth/refresh`
**Auth**: None  

**Request**
```json
{
  "refreshToken": "opaque-token-string"
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "new-rotated-token",
  "expiresIn": 900
}
```

---

### `POST /auth/logout`
**Auth**: Bearer token  

**Request**
```json
{
  "refreshToken": "opaque-token-string"
}
```

**Response** `204 No Content`

---

### `GET /auth/google`
**Auth**: None  
Redirects to Google OAuth2 consent page. Spring Security handles this automatically.

---

### `GET /auth/google/callback`
**Auth**: None  
Handled by Spring Security. On success, redirects to frontend with tokens in query params (or sets cookie).

---

## 2. Users

### `GET /users/me`
**Auth**: Any role  

**Response** `200 OK`
```json
{
  "id": "uuid",
  "email": "jane@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "role": "CANDIDATE",
  "avatarUrl": "https://...",
  "isEmailVerified": true,
  "createdAt": "2025-01-01T00:00:00Z"
}
```

---

### `PATCH /users/me`
**Auth**: Any role  

**Request** (partial update — only include fields to change)
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "avatarUrl": "https://..."
}
```

**Response** `200 OK` → updated user object (same as GET /users/me)

---

### `GET /users/{id}`
**Auth**: ADMIN  

**Response** `200 OK` → user object

---

### `DELETE /users/{id}`
**Auth**: ADMIN  

**Response** `204 No Content`

---

## 3. Recruiter Profile

### `GET /recruiters/me`
**Auth**: RECRUITER  

**Response** `200 OK`
```json
{
  "id": "uuid",
  "userId": "uuid",
  "companyName": "TechCorp Inc.",
  "companyWebsite": "https://techcorp.io",
  "industry": "Software",
  "companySize": "100-500",
  "bio": "We build great things."
}
```

---

### `PUT /recruiters/me`
**Auth**: RECRUITER  

**Request**
```json
{
  "companyName": "TechCorp Inc.",
  "companyWebsite": "https://techcorp.io",
  "industry": "Software",
  "companySize": "100-500",
  "bio": "We build great things."
}
```

**Response** `200 OK` → recruiter profile object

---

## 4. Candidate Profile

### `GET /candidates/me`
**Auth**: CANDIDATE  

**Response** `200 OK`
```json
{
  "id": "uuid",
  "userId": "uuid",
  "headline": "Senior Java Developer",
  "location": "Bangalore, India",
  "yearsOfExp": 5,
  "linkedinUrl": "https://linkedin.com/in/jane",
  "portfolioUrl": null,
  "isOpenToWork": true
}
```

---

### `PUT /candidates/me`
**Auth**: CANDIDATE  

**Request** — same shape as GET response (omit id/userId)  
**Response** `200 OK` → candidate profile object

---

## 5. Jobs

### `GET /jobs`
**Auth**: None (public for ACTIVE jobs; all statuses for RECRUITER/ADMIN)  
**Query params**: `?status=ACTIVE&search=java&location=remote&page=0&size=20&sort=publishedAt,desc`

**Response** `200 OK` (paginated)
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "Senior Java Developer",
      "companyName": "TechCorp Inc.",
      "location": "Bangalore",
      "isRemote": false,
      "jobType": "FULL_TIME",
      "salaryMin": 80000,
      "salaryMax": 120000,
      "currency": "USD",
      "status": "ACTIVE",
      "publishedAt": "2025-01-10T00:00:00Z",
      "applicationDeadline": "2025-02-10T00:00:00Z",
      "skills": ["Java", "Spring Boot", "PostgreSQL"]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

---

### `GET /jobs/{id}`
**Auth**: None (ACTIVE jobs) / Bearer (DRAFT, PAUSED, CLOSED — recruiter owner or ADMIN)  

**Response** `200 OK`
```json
{
  "id": "uuid",
  "title": "Senior Java Developer",
  "description": "Full job description...",
  "companyName": "TechCorp Inc.",
  "recruiterId": "uuid",
  "location": "Bangalore",
  "isRemote": false,
  "jobType": "FULL_TIME",
  "salaryMin": 80000,
  "salaryMax": 120000,
  "currency": "USD",
  "status": "ACTIVE",
  "applicationDeadline": "2025-02-10T00:00:00Z",
  "publishedAt": "2025-01-10T00:00:00Z",
  "createdAt": "2025-01-09T00:00:00Z",
  "skills": [
    { "name": "Java", "required": true },
    { "name": "Docker", "required": false }
  ]
}
```

---

### `POST /jobs`
**Auth**: RECRUITER  

**Request**
```json
{
  "title": "Senior Java Developer",
  "description": "We are looking for...",
  "location": "Bangalore",
  "isRemote": false,
  "jobType": "FULL_TIME",
  "salaryMin": 80000,
  "salaryMax": 120000,
  "currency": "USD",
  "applicationDeadline": "2025-02-10T00:00:00Z",
  "skills": [
    { "name": "Java", "required": true },
    { "name": "Docker", "required": false }
  ]
}
```

**Response** `201 Created` → full job object  
**Header**: `Location: /api/v1/jobs/{id}`

---

### `PUT /jobs/{id}`
**Auth**: RECRUITER (must own the job)  
**Request**: Same as POST  
**Response** `200 OK` → updated job object

---

### `PATCH /jobs/{id}/status`
**Auth**: RECRUITER (owner) or ADMIN  

**Request**
```json
{ "status": "ACTIVE" }
```

**Response** `200 OK` → updated job object

---

### `DELETE /jobs/{id}`
**Auth**: RECRUITER (owner) or ADMIN  
**Response** `204 No Content`

---

### `GET /jobs/mine`
**Auth**: RECRUITER  
**Query**: `?status=ACTIVE&page=0&size=20`  
**Response** `200 OK` → paginated job list (same shape as GET /jobs)

---

## 6. Applications

### `POST /applications`
**Auth**: CANDIDATE  

**Request**
```json
{
  "jobId": "uuid",
  "resumeId": "uuid",          // must be candidate's own resume
  "coverLetter": "I am excited..."
}
```

**Response** `201 Created`
```json
{
  "id": "uuid",
  "jobId": "uuid",
  "jobTitle": "Senior Java Developer",
  "status": "APPLIED",
  "createdAt": "2025-01-15T00:00:00Z"
}
```

**Error**: `409 Conflict` if already applied to this job.

---

### `GET /applications`
**Auth**: RECRUITER (sees applications for their jobs) / ADMIN  
**Query**: `?jobId=uuid&status=SHORTLISTED&page=0&size=20&sort=score,desc`  

**Response** `200 OK` (paginated)
```json
{
  "content": [
    {
      "id": "uuid",
      "jobId": "uuid",
      "jobTitle": "Senior Java Developer",
      "candidate": {
        "id": "uuid",
        "firstName": "Jane",
        "lastName": "Doe",
        "headline": "Senior Java Developer",
        "location": "Bangalore"
      },
      "status": "REVIEWING",
      "matchScore": 87,
      "appliedAt": "2025-01-15T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 15,
  "totalPages": 1
}
```

---

### `GET /applications/mine`
**Auth**: CANDIDATE  
**Query**: `?status=SHORTLISTED&page=0&size=20`  

**Response** `200 OK` (paginated)
```json
{
  "content": [
    {
      "id": "uuid",
      "jobId": "uuid",
      "jobTitle": "Senior Java Developer",
      "companyName": "TechCorp Inc.",
      "status": "SHORTLISTED",
      "matchScore": 87,
      "appliedAt": "2025-01-15T00:00:00Z"
    }
  ]
}
```

---

### `GET /applications/{id}`
**Auth**: RECRUITER (for their job) / CANDIDATE (their own) / ADMIN  

**Response** `200 OK`
```json
{
  "id": "uuid",
  "jobId": "uuid",
  "jobTitle": "Senior Java Developer",
  "candidate": { "id": "uuid", "firstName": "Jane", "lastName": "Doe" },
  "resumeId": "uuid",
  "coverLetter": "I am excited...",
  "status": "REVIEWING",
  "statusUpdatedAt": "2025-01-16T00:00:00Z",
  "appliedAt": "2025-01-15T00:00:00Z",
  "matchScore": {
    "score": 87,
    "matchedSkills": ["Java", "Spring Boot"],
    "missingSkills": ["Kubernetes"],
    "experienceGap": "Requires 3+ years; candidate has 5 years.",
    "aiSummary": "Strong technical match. Missing cloud-native skills."
  }
}
```

---

### `PATCH /applications/{id}/status`
**Auth**: RECRUITER (for their job)  

**Request**
```json
{
  "status": "SHORTLISTED",
  "recruiterNotes": "Strong candidate, schedule technical round."
}
```

**Response** `200 OK` → updated application object

---

### `PATCH /applications/{id}/withdraw`
**Auth**: CANDIDATE (their own application)  
**Response** `200 OK` → `{ "status": "WITHDRAWN" }`

---

## 7. Resumes

### `POST /resumes/upload`
**Auth**: CANDIDATE  
**Content-Type**: `multipart/form-data`  

**Form fields**:
- `file`: PDF or DOCX, max 10 MB
- `isPrimary`: boolean (default `false`)

**Response** `202 Accepted`
```json
{
  "resumeId": "uuid",
  "fileName": "jane_doe_cv.pdf",
  "parseStatus": "PENDING",
  "message": "Resume uploaded. Parsing is in progress."
}
```

---

### `GET /resumes`
**Auth**: CANDIDATE  

**Response** `200 OK`
```json
[
  {
    "id": "uuid",
    "fileName": "jane_doe_cv.pdf",
    "isPrimary": true,
    "parseStatus": "DONE",
    "createdAt": "2025-01-10T00:00:00Z"
  }
]
```

---

### `GET /resumes/{id}`
**Auth**: CANDIDATE (own) / RECRUITER (if applied to their job) / ADMIN  

**Response** `200 OK`
```json
{
  "id": "uuid",
  "fileName": "jane_doe_cv.pdf",
  "parseStatus": "DONE",
  "skills": ["Java", "Spring Boot", "PostgreSQL"],
  "experiences": [
    {
      "jobTitle": "Software Engineer",
      "company": "Infosys",
      "startDate": "2020-06-01",
      "endDate": "2023-01-01",
      "description": "Worked on..."
    }
  ],
  "educations": [
    {
      "degree": "B.Tech",
      "fieldOfStudy": "Computer Science",
      "institution": "Anna University",
      "graduationYear": 2020
    }
  ]
}
```

---

### `GET /resumes/{id}/parse-status`
**Auth**: CANDIDATE  

**Response** `200 OK`
```json
{
  "resumeId": "uuid",
  "parseStatus": "PROCESSING"    // PENDING | PROCESSING | DONE | FAILED
}
```

---

### `DELETE /resumes/{id}`
**Auth**: CANDIDATE (own)  
**Response** `204 No Content`

---

## 8. Interviews

### `POST /interviews`
**Auth**: RECRUITER  

**Request**
```json
{
  "applicationId": "uuid",
  "interviewType": "VIDEO",
  "scheduledAt": "2025-01-25T10:00:00Z",
  "durationMinutes": 60,
  "meetingLink": "https://meet.google.com/abc-xyz",
  "locationNotes": null
}
```

**Response** `201 Created`
```json
{
  "id": "uuid",
  "applicationId": "uuid",
  "jobTitle": "Senior Java Developer",
  "candidateName": "Jane Doe",
  "interviewType": "VIDEO",
  "status": "SCHEDULED",
  "scheduledAt": "2025-01-25T10:00:00Z",
  "durationMinutes": 60,
  "meetingLink": "https://meet.google.com/abc-xyz",
  "createdAt": "2025-01-15T00:00:00Z"
}
```

---

### `GET /interviews`
**Auth**: RECRUITER (their jobs) / CANDIDATE (their interviews) / ADMIN  
**Query**: `?applicationId=uuid&status=SCHEDULED&page=0&size=20`  

**Response** `200 OK` → paginated list of interview objects

---

### `GET /interviews/{id}`
**Auth**: RECRUITER (their job) / CANDIDATE (their interview) / ADMIN  
**Response** `200 OK` → full interview object

---

### `PATCH /interviews/{id}`
**Auth**: RECRUITER  

**Request** (partial update)
```json
{
  "scheduledAt": "2025-01-26T10:00:00Z",
  "meetingLink": "https://meet.google.com/new-link",
  "status": "RESCHEDULED"
}
```

**Response** `200 OK` → updated interview object

---

### `DELETE /interviews/{id}`
**Auth**: RECRUITER  
**Response** `204 No Content` (sets status to CANCELLED, does not hard delete)

---

### `GET /interviews/{id}/questions`
**Auth**: RECRUITER  

**Response** `200 OK`
```json
[
  { "id": 1, "question": "Describe your experience with Spring Boot.", "category": "technical" },
  { "id": 2, "question": "Tell me about a challenging project.", "category": "behavioral" }
]
```

---

## 9. AI Endpoints (Internal — for Frontend to consume)

### `GET /ai/applications/{applicationId}/score`
**Auth**: RECRUITER (their job) / ADMIN  
**Note**: Returns cached result if available; triggers async scoring if not yet done.

**Response** `200 OK`
```json
{
  "applicationId": "uuid",
  "score": 87,
  "matchedSkills": ["Java", "Spring Boot", "PostgreSQL"],
  "missingSkills": ["Kubernetes", "Terraform"],
  "experienceGap": "Role requires 3+ years cloud; candidate has on-premise experience only.",
  "aiSummary": "Strong technical foundation. Recommended for technical round with focus on cloud skills.",
  "interviewQuestions": [
    { "question": "Walk me through a microservices project you've led.", "category": "technical" },
    { "question": "How do you handle production incidents?", "category": "behavioral" }
  ],
  "modelUsed": "gpt-4o-mini",
  "scoredAt": "2025-01-15T10:05:00Z"
}
```

**Response** `202 Accepted` (if scoring is still in progress)
```json
{
  "message": "Scoring in progress. Retry in a few seconds.",
  "status": "PROCESSING"
}
```

---

### `POST /ai/applications/{applicationId}/score/refresh`
**Auth**: RECRUITER (their job)  
**Note**: Force re-score (bypasses cache; costs AI tokens).  
**Response** `202 Accepted` → `{ "message": "Re-scoring triggered." }`

---

### `POST /ai/generate-questions`
**Auth**: RECRUITER  
**Note**: On-demand question generation without an existing application.

**Request**
```json
{
  "jobTitle": "Senior Java Developer",
  "skills": ["Java", "Spring Boot", "PostgreSQL"],
  "count": 10
}
```

**Response** `200 OK`
```json
{
  "questions": [
    { "question": "Explain JPA N+1 problem and solutions.", "category": "technical" },
    { "question": "How do you approach API versioning?", "category": "technical" },
    { "question": "Describe a time you improved system performance.", "category": "behavioral" }
  ]
}
```

---

### `GET /ai/jobs/{jobId}/applicant-ranking`
**Auth**: RECRUITER (their job)  
**Query**: `?status=SHORTLISTED&minScore=70`  

**Response** `200 OK`
```json
[
  {
    "rank": 1,
    "applicationId": "uuid",
    "candidateName": "Jane Doe",
    "matchScore": 92,
    "matchedSkills": ["Java", "Spring Boot", "PostgreSQL", "Docker"],
    "missingSkills": []
  },
  {
    "rank": 2,
    "applicationId": "uuid",
    "candidateName": "John Smith",
    "matchScore": 78,
    "matchedSkills": ["Java", "Spring Boot"],
    "missingSkills": ["PostgreSQL", "Docker"]
  }
]
```

---

## 10. Admin Endpoints

### `GET /admin/users`
**Auth**: ADMIN  
**Query**: `?role=RECRUITER&page=0&size=20`  
**Response** `200 OK` → paginated user list

### `PATCH /admin/users/{id}/deactivate`
**Auth**: ADMIN  
**Response** `200 OK` → `{ "isActive": false }`

### `GET /admin/ai/usage`
**Auth**: ADMIN  
**Query**: `?from=2025-01-01&to=2025-01-31`  
**Response** `200 OK`
```json
{
  "totalCalls": 1240,
  "totalTokensUsed": 1850000,
  "estimatedCostUsd": 1.85,
  "byModel": {
    "gpt-4o-mini": { "calls": 1000, "tokens": 1500000 },
    "gemini-1.5-flash": { "calls": 240, "tokens": 350000 }
  }
}
```
