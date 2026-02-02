# Postman API Testing Guide - OKR Tracking System

A complete cheatsheet for testing all API endpoints in Postman.

---

## Table of Contents

1. [Setup](#1-setup)
2. [Environment Variables](#2-environment-variables)
3. [Authentication](#3-authentication)
4. [Users](#4-users)
5. [Divisions](#5-divisions)
6. [Departments](#6-departments)
7. [Objectives](#7-objectives)
8. [Key Results](#8-key-results)
9. [Evaluations](#9-evaluations)
10. [Score Levels](#10-score-levels)
11. [Export & Demo](#11-export--demo)
12. [Testing Workflow](#12-testing-workflow)

---

## 1. Setup

### Prerequisites
- Postman installed
- Backend running on `http://localhost:8080`

### Create Collection
1. Open Postman
2. Create new Collection: **"OKR Tracking System"**
3. Add folders for each category (Auth, Users, Divisions, etc.)

### Import Environment
Create a new Environment called **"OKR Local"** with the variables listed below.

---

## 2. Environment Variables

Create these variables in your Postman environment:

| Variable | Initial Value | Description |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8080/api` | API base URL |
| `token` | (empty) | JWT token (auto-set after login) |
| `user_id` | (empty) | Current user ID |
| `admin_id` | (empty) | Admin user ID |
| `division_id` | (empty) | Test division ID |
| `department_id` | (empty) | Test department ID |
| `objective_id` | (empty) | Test objective ID |
| `key_result_id` | (empty) | Test key result ID |
| `evaluation_id` | (empty) | Test evaluation ID |

### Auto-Set Token Script
Add this to your Login request's **Tests** tab:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    pm.environment.set("user_id", jsonData.user.id);
}
```

---

## 3. Authentication

### 3.1 Login (Get Token)

```
POST {{base_url}}/auth/login
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
    "username": "admin",
    "password": "admin123"
}
```

**Expected Response (200 OK):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "user": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "username": "admin",
        "email": "admin@okr-tracker.com",
        "fullName": "System Administrator",
        "role": "ADMIN"
    }
}
```

**Tests Script:**
```javascript
pm.test("Login successful", function () {
    pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    pm.environment.set("user_id", jsonData.user.id);
    pm.environment.set("admin_id", jsonData.user.id);
}
```

---

### 3.2 Get Current User

```
GET {{base_url}}/auth/me
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response (200 OK):**
```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin",
    "email": "admin@okr-tracker.com",
    "fullName": "System Administrator",
    "role": "ADMIN",
    "isActive": true,
    "assignedDepartments": []
}
```

---

### 3.3 Register New User (Admin Only)

```
POST {{base_url}}/auth/register
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "username": "director1",
    "email": "director@company.com",
    "password": "password123",
    "fullName": "John Director",
    "role": "DIRECTOR"
}
```

**Expected Response (200 OK):**
```json
{
    "id": "...",
    "username": "director1",
    "email": "director@company.com",
    "fullName": "John Director",
    "role": "DIRECTOR"
}
```

---

## 4. Users

### 4.1 Get All Users

```
GET {{base_url}}/users
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 4.2 Get Users with Scores

```
GET {{base_url}}/users/with-scores
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
[
    {
        "id": "...",
        "username": "admin",
        "fullName": "System Administrator",
        "overallScore": 0.75,
        "scoreLevel": "Very Good",
        "scoreColor": "#28a745",
        "scorePercentage": 75.0
    }
]
```

---

### 4.3 Get User by ID

```
GET {{base_url}}/users/{{user_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 4.4 Create User

```
POST {{base_url}}/users
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "username": "hruser",
    "email": "hr@company.com",
    "password": "password123",
    "fullName": "HR Manager",
    "role": "HR",
    "jobTitle": "HR Director",
    "phoneNumber": "+998901234567",
    "bio": "Human Resources Department Head"
}
```

**Tests Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("hr_user_id", jsonData.id);
}
```

---

### 4.5 Update User

```
PUT {{base_url}}/users/{{user_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "fullName": "Updated Name",
    "jobTitle": "Senior Administrator",
    "phoneNumber": "+998909876543",
    "bio": "Updated bio information"
}
```

---

### 4.6 Delete User

```
DELETE {{base_url}}/users/{{user_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:** `204 No Content`

---

### 4.7 Assign Departments to User

```
POST {{base_url}}/users/{{user_id}}/departments
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "departmentIds": ["{{department_id}}"]
}
```

---

### 4.8 Remove Department from User

```
DELETE {{base_url}}/users/{{user_id}}/departments/{{department_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 4.9 Upload Profile Photo

```
POST {{base_url}}/users/{{user_id}}/photo
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Body (form-data):**
| Key | Type | Value |
|-----|------|-------|
| photo | File | (select image file) |

---

### 4.10 Get Users by Department

```
GET {{base_url}}/users/by-department/{{department_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 4.11 Get Current User Profile

```
GET {{base_url}}/users/me/profile
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

## 5. Divisions

### 5.1 Get All Divisions

```
GET {{base_url}}/divisions
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
[
    {
        "id": "div-001",
        "name": "Technology Division",
        "divisionLeader": {
            "id": "...",
            "fullName": "John Director"
        },
        "departments": [...]
    }
]
```

---

### 5.2 Get Division by ID

```
GET {{base_url}}/divisions/{{division_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 5.3 Create Division

```
POST {{base_url}}/divisions
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "name": "Technology Division",
    "leaderId": "{{user_id}}"
}
```

**Tests Script:**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("division_id", jsonData.id);
}
```

---

### 5.4 Update Division

```
PUT {{base_url}}/divisions/{{division_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "name": "Updated Division Name",
    "leaderId": "{{user_id}}"
}
```

---

### 5.5 Delete Division

```
DELETE {{base_url}}/divisions/{{division_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:** `204 No Content`

> **Note:** Division can only be deleted if it has no departments.

---

### 5.6 Get Departments in Division

```
GET {{base_url}}/divisions/{{division_id}}/departments
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

## 6. Departments

### 6.1 Get All Departments

```
GET {{base_url}}/departments
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
[
    {
        "id": "dept-001",
        "name": "Software Development",
        "divisionId": "div-001",
        "objectives": [...],
        "score": 0.72,
        "finalScore": 0.75
    }
]
```

---

### 6.2 Get Department by ID

```
GET {{base_url}}/departments/{{department_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 6.3 Create Department

```
POST {{base_url}}/departments
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "name": "Software Development",
    "divisionId": "{{division_id}}"
}
```

**Tests Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("department_id", jsonData.id);
}
```

---

### 6.4 Update Department

```
PUT {{base_url}}/departments/{{department_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "id": "{{department_id}}",
    "name": "Updated Department Name",
    "divisionId": "{{division_id}}"
}
```

---

### 6.5 Delete Department

```
DELETE {{base_url}}/departments/{{department_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:** `204 No Content`

---

### 6.6 Get Department Scores

```
GET {{base_url}}/departments/{{department_id}}/scores
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
{
    "automaticOkrScore": 0.72,
    "automaticOkrPercentage": 72.0,
    "directorEvaluation": 4.625,
    "directorStars": 3,
    "directorComment": "Good progress",
    "hasDirectorEvaluation": true,
    "hrEvaluationLetter": "B",
    "hrEvaluationNumeric": 4.75,
    "hrComment": "Meeting expectations",
    "hasHrEvaluation": true,
    "businessBlockEvaluation": 4.25,
    "businessBlockStars": 2,
    "hasBusinessBlockEvaluation": true,
    "finalCombinedScore": 0.75,
    "finalPercentage": 75.0,
    "scoreLevel": "Very Good",
    "color": "#28a745"
}
```

---

## 7. Objectives

### 7.1 Create Objective

```
POST {{base_url}}/departments/{{department_id}}/objectives
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "name": "Increase System Performance",
    "weight": 40,
    "departmentId": "{{department_id}}",
    "keyResults": []
}
```

**Tests Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("objective_id", jsonData.id);
}
```

---

### 7.2 Update Objective

```
PUT {{base_url}}/objectives/{{objective_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "id": "{{objective_id}}",
    "name": "Updated Objective Name",
    "weight": 50,
    "departmentId": "{{department_id}}"
}
```

---

### 7.3 Delete Objective

```
DELETE {{base_url}}/objectives/{{objective_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:** `204 No Content`

---

## 8. Key Results

### 8.1 Create Key Result (Quantitative - Higher Better)

```
POST {{base_url}}/objectives/{{objective_id}}/key-results
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "name": "Response Time Reduction",
    "description": "Reduce API response time",
    "metricType": "HIGHER_BETTER",
    "unit": "ms improvement",
    "weight": 30,
    "thresholds": {
        "below": 0,
        "meets": 50,
        "good": 100,
        "veryGood": 150,
        "exceptional": 200
    },
    "actualValue": null
}
```

**Tests Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("key_result_id", jsonData.id);
}
```

---

### 8.2 Create Key Result (Quantitative - Lower Better)

```
POST {{base_url}}/objectives/{{objective_id}}/key-results
```

**Body:**
```json
{
    "name": "Bug Count",
    "description": "Reduce production bugs",
    "metricType": "LOWER_BETTER",
    "unit": "bugs",
    "weight": 25,
    "thresholds": {
        "below": 50,
        "meets": 40,
        "good": 30,
        "veryGood": 20,
        "exceptional": 10
    },
    "actualValue": null
}
```

---

### 8.3 Create Key Result (Qualitative)

```
POST {{base_url}}/objectives/{{objective_id}}/key-results
```

**Body:**
```json
{
    "name": "Code Quality Assessment",
    "description": "External audit score",
    "metricType": "QUALITATIVE",
    "unit": "grade",
    "weight": 20,
    "actualValue": null
}
```

> **Note:** Qualitative KRs use grades: A, B, C, D, E

---

### 8.4 Update Key Result

```
PUT {{base_url}}/key-results/{{key_result_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "id": "{{key_result_id}}",
    "name": "Updated Key Result Name",
    "description": "Updated description",
    "metricType": "HIGHER_BETTER",
    "unit": "ms",
    "weight": 35,
    "thresholds": {
        "below": 0,
        "meets": 60,
        "good": 120,
        "veryGood": 180,
        "exceptional": 240
    }
}
```

---

### 8.5 Update Key Result Actual Value

```
PUT {{base_url}}/key-results/{{key_result_id}}/actual-value
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body (for quantitative):**
```json
{
    "actualValue": "125"
}
```

**Body (for qualitative):**
```json
{
    "actualValue": "B"
}
```

---

### 8.6 Delete Key Result

```
DELETE {{base_url}}/key-results/{{key_result_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:** `204 No Content`

---

## 9. Evaluations

### 9.1 Create Director Evaluation (Stars)

```
POST {{base_url}}/evaluations
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "targetType": "DEPARTMENT",
    "targetId": "{{department_id}}",
    "evaluatorType": "DIRECTOR",
    "starRating": 4,
    "comment": "Good progress this quarter"
}
```

**Tests Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("evaluation_id", jsonData.id);
}
```

> **Note:** Stars: 1-5, converted to 4.25-5.0 internally

---

### 9.2 Create HR Evaluation (Letter Grade)

```
POST {{base_url}}/evaluations
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "targetType": "DEPARTMENT",
    "targetId": "{{department_id}}",
    "evaluatorType": "HR",
    "letterRating": "B",
    "comment": "Team collaboration is strong"
}
```

> **Note:** Letter grades: A, B, C, D

---

### 9.3 Create Business Block Evaluation

```
POST {{base_url}}/evaluations
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "targetType": "DEPARTMENT",
    "targetId": "{{department_id}}",
    "evaluatorType": "BUSINESS_BLOCK",
    "starRating": 3,
    "comment": "Meeting business requirements"
}
```

---

### 9.4 Submit Evaluation (Draft → Submitted)

```
POST {{base_url}}/evaluations/{{evaluation_id}}/submit
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
{
    "id": "...",
    "status": "SUBMITTED",
    ...
}
```

---

### 9.5 Update Evaluation

```
PUT {{base_url}}/evaluations/{{evaluation_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
    "targetType": "DEPARTMENT",
    "targetId": "{{department_id}}",
    "evaluatorType": "DIRECTOR",
    "starRating": 5,
    "comment": "Updated: Excellent performance!"
}
```

---

### 9.6 Get Evaluations for Target

```
GET {{base_url}}/evaluations/target/DEPARTMENT/{{department_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
[
    {
        "id": "...",
        "evaluatorId": "...",
        "evaluatorName": "Admin User",
        "evaluatorType": "DIRECTOR",
        "targetType": "DEPARTMENT",
        "targetId": "...",
        "numericRating": 4.8125,
        "letterRating": null,
        "comment": "Good progress",
        "status": "SUBMITTED"
    }
]
```

---

### 9.7 Get My Evaluations

```
GET {{base_url}}/evaluations/my
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 9.8 Get All Evaluations (Admin Only)

```
GET {{base_url}}/evaluations/all
```

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 9.9 Delete Evaluation

```
DELETE {{base_url}}/evaluations/{{evaluation_id}}
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:** `204 No Content`

> **Note:** Only DRAFT evaluations can be deleted

---

## 10. Score Levels

### 10.1 Get All Score Levels

```
GET {{base_url}}/score-levels
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
[
    {
        "id": "...",
        "name": "Below",
        "scoreValue": 0.0,
        "color": "#d9534f",
        "displayOrder": 0
    },
    {
        "id": "...",
        "name": "Meets",
        "scoreValue": 0.25,
        "color": "#f0ad4e",
        "displayOrder": 1
    },
    {
        "id": "...",
        "name": "Good",
        "scoreValue": 0.5,
        "color": "#5cb85c",
        "displayOrder": 2
    },
    {
        "id": "...",
        "name": "Very Good",
        "scoreValue": 0.75,
        "color": "#28a745",
        "displayOrder": 3
    },
    {
        "id": "...",
        "name": "Exceptional",
        "scoreValue": 1.0,
        "color": "#1e7b34",
        "displayOrder": 4
    }
]
```

---

### 10.2 Update Score Levels

```
PUT {{base_url}}/score-levels
```

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
[
    {
        "id": "existing-id-1",
        "name": "Needs Improvement",
        "scoreValue": 0.0,
        "color": "#dc3545",
        "displayOrder": 0
    },
    {
        "id": "existing-id-2",
        "name": "Satisfactory",
        "scoreValue": 0.25,
        "color": "#ffc107",
        "displayOrder": 1
    },
    {
        "id": "existing-id-3",
        "name": "Good",
        "scoreValue": 0.5,
        "color": "#28a745",
        "displayOrder": 2
    },
    {
        "id": "existing-id-4",
        "name": "Excellent",
        "scoreValue": 0.75,
        "color": "#20c997",
        "displayOrder": 3
    },
    {
        "id": "existing-id-5",
        "name": "Outstanding",
        "scoreValue": 1.0,
        "color": "#007bff",
        "displayOrder": 4
    }
]
```

---

### 10.3 Reset Score Levels to Defaults

```
POST {{base_url}}/score-levels/reset
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:** `200 OK`

---

## 11. Export & Demo

### 11.1 Export to Excel

```
GET {{base_url}}/export/excel
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:** Binary file (`.xlsx`)

> **Postman Tip:** Click "Save Response" → "Save to a file" to download the Excel file.

---

### 11.2 Load Demo Data (Admin Only)

```
POST {{base_url}}/demo/load
```

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
[
    {
        "id": "...",
        "name": "Demo Department 1",
        "objectives": [...]
    }
]
```

---

## 12. Testing Workflow

### Recommended Testing Order

Follow this sequence for complete testing:

```
1. AUTHENTICATION
   └── Login as admin
   └── Verify token works (GET /auth/me)

2. SCORE LEVELS (Optional)
   └── Get default levels
   └── Customize if needed

3. USERS
   └── Create users with different roles:
       - DIRECTOR
       - HR
       - BUSINESS_BLOCK
       - EMPLOYEE

4. DIVISIONS
   └── Create division
   └── Assign leader

5. DEPARTMENTS
   └── Create department in division
   └── Assign users to department

6. OBJECTIVES
   └── Create objectives for department
   └── Set weights (should sum to 100%)

7. KEY RESULTS
   └── Create KRs for each objective
   └── Set weights (should sum to 100% per objective)
   └── Update actual values

8. EVALUATIONS
   └── Login as DIRECTOR → Create director evaluation
   └── Login as HR → Create HR evaluation
   └── Login as BUSINESS_BLOCK → Create business evaluation
   └── Submit all evaluations

9. VERIFY SCORES
   └── GET /departments/{id}/scores
   └── GET /users/with-scores

10. EXPORT
    └── Download Excel report
```

---

## Quick Reference Card

### Headers Template
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

### Common HTTP Status Codes
| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 204 | No Content (Delete success) |
| 400 | Bad Request |
| 401 | Unauthorized (Invalid/missing token) |
| 403 | Forbidden (No permission) |
| 404 | Not Found |
| 413 | File Too Large |
| 500 | Server Error |

### Role Permissions Quick Reference
| Endpoint | ADMIN | DIRECTOR | HR | BUSINESS_BLOCK | USER |
|----------|-------|----------|-----|----------------|------|
| Create User | Yes | No | No | No | No |
| Delete User | Yes | No | No | No | No |
| Create Division | Yes | Yes | No | No | No |
| Create Department | Yes | Yes | No | No | No |
| Create Evaluation | Yes | Director only | HR only | BB only | No |
| View Scores | Yes | Yes | Yes | Yes | Yes |

### Evaluation Types
| Type | Input | Internal Value |
|------|-------|----------------|
| DIRECTOR | 1-5 stars | 4.25-5.0 |
| HR | A/B/C/D | 5.0/4.75/4.5/4.25 |
| BUSINESS_BLOCK | 1-5 stars | 4.25-5.0 |

### Score Formula
```
Final = (OKR × 60%) + (Director × 20%) + (HR × 20%)
```

---

## Postman Collection Variables Script

Add this to your collection's **Pre-request Script** for automatic token refresh:

```javascript
// Check if token exists and set auth header
if (pm.environment.get("token")) {
    pm.request.headers.add({
        key: "Authorization",
        value: "Bearer " + pm.environment.get("token")
    });
}
```

---

## Troubleshooting

### "401 Unauthorized"
- Token expired → Re-login
- Token not set → Check environment variable
- Wrong header format → Use `Bearer {{token}}`

### "403 Forbidden"
- User role doesn't have permission
- Try logging in as ADMIN

### "404 Not Found"
- Check entity ID exists
- Verify URL path is correct

### "400 Bad Request"
- Check JSON body format
- Verify required fields are present
- Check data types (string vs number)

### Score Not Updating
- Ensure evaluations are SUBMITTED (not DRAFT)
- Check that actual values are set on Key Results
- Verify weights sum to 100%

---

## Sample Test Data

### Complete Department Setup
```json
// 1. Create Division
{
    "name": "IT Division",
    "leaderId": "{{admin_id}}"
}

// 2. Create Department
{
    "name": "Backend Team",
    "divisionId": "{{division_id}}"
}

// 3. Create Objective
{
    "name": "Improve API Performance",
    "weight": 100,
    "departmentId": "{{department_id}}"
}

// 4. Create Key Results
{
    "name": "Reduce Response Time",
    "metricType": "HIGHER_BETTER",
    "weight": 50,
    "thresholds": {
        "below": 0, "meets": 20, "good": 40,
        "veryGood": 60, "exceptional": 80
    }
}

{
    "name": "Code Coverage",
    "metricType": "HIGHER_BETTER",
    "weight": 50,
    "unit": "%",
    "thresholds": {
        "below": 0, "meets": 60, "good": 70,
        "veryGood": 80, "exceptional": 90
    }
}

// 5. Set Actual Values
{ "actualValue": "45" }  // Response time improvement
{ "actualValue": "75" }  // Code coverage %

// 6. Create & Submit Evaluations
// Director: 4 stars
// HR: "B" grade
// Business Block: 3 stars
```

---

**Happy Testing!**
