# API Endpoints Reference

> **Inspect-Hub 전체 API 엔드포인트 목록**

## 📚 목차

1. [인증 API](#-인증-api)
2. [사용자 관리 API](#-사용자-관리-api)
3. [조직 관리 API](#-조직-관리-api)
4. [권한 관리 API](#-권한-관리-api)
5. [정책 관리 API](#-정책-관리-api)
6. [탐지 엔진 API](#-탐지-엔진-api)
7. [사례 관리 API](#-사례-관리-api)
8. [보고 API](#-보고-api)
9. [시뮬레이션 API](#-시뮬레이션-api)
10. [코드 관리 API](#-코드-관리-api)
11. [파일 관리 API](#-파일-관리-api)

---

## 🔐 인증 API

**Base URL:** `/api/v1/auth`

### POST /login - 로그인

**Request:**
```json
{
  "username": "admin",
  "password": "SecurePassword123!"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "01REFRESH123...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "01ARZ3...",
      "username": "admin",
      "email": "admin@example.com",
      "roles": ["ROLE_ADMIN"]
    }
  }
}
```

### POST /refresh - 토큰 갱신

**Request:**
```json
{
  "refreshToken": "01REFRESH123..."
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "01REFRESH456...",
    "expiresIn": 3600
  }
}
```

### POST /logout - 로그아웃

**Request:**
```json
{
  "refreshToken": "01REFRESH123..."
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

### POST /change-password - 비밀번호 변경

**Request:**
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword456!"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

---

## 👤 사용자 관리 API

**Base URL:** `/api/v1/users`

### GET /users - 사용자 목록 조회

**Query Parameters:**
- `status` (optional): `active`, `inactive`, `locked`
- `role` (optional): Role name
- `orgId` (optional): Organization ID
- `search` (optional): Search term
- `page` (optional): Page number (default: 1)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field,direction

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01ARZ3...",
        "username": "admin",
        "email": "admin@example.com",
        "status": "active",
        "roles": ["ROLE_ADMIN"],
        "organization": {
          "id": "01ORG...",
          "name": "ABC Bank"
        },
        "createdAt": "2025-01-13T10:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 45,
      "totalPages": 3
    }
  }
}
```

### GET /users/{id} - 사용자 상세 조회

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "01ARZ3...",
    "username": "admin",
    "email": "admin@example.com",
    "status": "active",
    "roles": ["ROLE_ADMIN"],
    "permissions": ["user:read", "user:write"],
    "organization": {
      "id": "01ORG...",
      "name": "ABC Bank"
    },
    "createdAt": "2025-01-13T10:00:00Z",
    "lastLoginAt": "2025-01-13T15:30:00Z"
  }
}
```

### POST /users - 사용자 생성

**Request:**
```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "SecurePassword123!",
  "roles": ["ROLE_USER"],
  "organizationId": "01ORG...",
  "permissionGroupId": "01PERM..."
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "01USER...",
    "username": "newuser",
    "email": "newuser@example.com",
    "status": "active",
    "createdAt": "2025-01-13T10:00:00Z"
  }
}
```

### PUT /users/{id} - 사용자 수정

**Request:**
```json
{
  "email": "updated@example.com",
  "roles": ["ROLE_USER", "ROLE_REVIEWER"],
  "permissionGroupId": "01PERM..."
}
```

**Response:** `200 OK`

### PATCH /users/{id}/status - 사용자 상태 변경

**Request:**
```json
{
  "status": "inactive"
}
```

**Response:** `200 OK`

### DELETE /users/{id} - 사용자 삭제 (Soft Delete)

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "User deleted successfully"
}
```

---

## 🏢 조직 관리 API

**Base URL:** `/api/v1/organizations`

### GET /organizations - 조직 목록 조회

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01ORG...",
        "name": "ABC Bank",
        "code": "ABC",
        "status": "active",
        "createdAt": "2025-01-01T00:00:00Z"
      }
    ]
  }
}
```

### GET /organizations/{id} - 조직 상세 조회

**Response:** `200 OK`

### POST /organizations - 조직 생성

**Request:**
```json
{
  "name": "XYZ Bank",
  "code": "XYZ",
  "address": "Seoul, Korea",
  "contactEmail": "contact@xyzbank.com"
}
```

**Response:** `201 Created`

### PUT /organizations/{id} - 조직 수정

**Response:** `200 OK`

### DELETE /organizations/{id} - 조직 삭제

**Response:** `200 OK`

---

## 🔑 권한 관리 API

**Base URL:** `/api/v1/permissions`

### GET /permission-groups - 권한 그룹 목록

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01PERM...",
        "code": "PG.ADMIN",
        "name": "관리자 권한 그룹",
        "description": "시스템 관리자",
        "permissions": [
          { "code": "user:read", "name": "사용자 조회" },
          { "code": "user:write", "name": "사용자 생성/수정" }
        ]
      }
    ]
  }
}
```

### POST /permission-groups - 권한 그룹 생성

**Request:**
```json
{
  "code": "PG.INVESTIGATOR",
  "name": "조사자 권한 그룹",
  "description": "STR/CTR 사례 조사",
  "permissions": [
    "case:read",
    "case:write",
    "case:investigate"
  ]
}
```

**Response:** `201 Created`

### GET /menus - 메뉴 목록 (권한별)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "01MENU...",
      "code": "MENU.DASHBOARD",
      "name": "대시보드",
      "path": "/dashboard",
      "icon": "dashboard",
      "order": 1,
      "children": []
    },
    {
      "id": "01MENU...",
      "code": "MENU.USER_MANAGEMENT",
      "name": "사용자 관리",
      "path": "/users",
      "icon": "users",
      "order": 2,
      "children": [
        {
          "id": "01MENU...",
          "code": "MENU.USER_LIST",
          "name": "사용자 목록",
          "path": "/users/list"
        }
      ]
    }
  ]
}
```

---

## 📋 정책 관리 API

**Base URL:** `/api/v1/policies`

### GET /policies - 정책 목록 조회

**Query Parameters:**
- `type` (optional): `STR`, `CTR`, `WLF`, `RBA`, `KYC`, `FIU`
- `status` (optional): `DRAFT`, `ACTIVE`, `INACTIVE`, `ARCHIVED`
- `version` (optional): Version number
- `page`, `size`, `sort`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01POLICY...",
        "type": "STR",
        "version": 1,
        "status": "ACTIVE",
        "effectiveFrom": "2025-01-01T00:00:00Z",
        "effectiveTo": null,
        "configJson": {
          "threshold": 10000,
          "rules": []
        },
        "createdAt": "2024-12-01T00:00:00Z",
        "createdBy": "admin"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 15
    }
  }
}
```

### GET /policies/{id} - 정책 상세 조회

**Response:** `200 OK`

### POST /policies - 정책 생성 (스냅샷)

**Request:**
```json
{
  "type": "STR",
  "version": 2,
  "effectiveFrom": "2025-02-01T00:00:00Z",
  "effectiveTo": null,
  "configJson": {
    "threshold": 15000,
    "rules": [
      {
        "ruleId": "STR_001",
        "name": "High Amount Transaction",
        "condition": "amount >= 15000",
        "severity": "HIGH"
      }
    ]
  }
}
```

**Response:** `201 Created`

### PUT /policies/{id} - 정책 수정 (DRAFT만 가능)

**Response:** `200 OK`

### PUT /policies/{id}/activate - 정책 활성화

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Policy activated successfully",
  "data": {
    "id": "01POLICY...",
    "status": "ACTIVE",
    "activatedAt": "2025-01-13T10:00:00Z"
  }
}
```

### POST /policies/{id}/rollback - 정책 롤백

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Policy rolled back to previous version",
  "data": {
    "currentVersion": {
      "id": "01POLICY...",
      "version": 1,
      "status": "ACTIVE"
    },
    "previousVersion": {
      "id": "01PREV...",
      "version": 2,
      "status": "INACTIVE"
    }
  }
}
```

### GET /policies/{id}/history - 정책 변경 이력

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "01POLICY...",
      "version": 2,
      "status": "ACTIVE",
      "effectiveFrom": "2025-01-01T00:00:00Z",
      "createdAt": "2024-12-15T00:00:00Z"
    },
    {
      "id": "01PREV...",
      "version": 1,
      "status": "INACTIVE",
      "effectiveFrom": "2024-01-01T00:00:00Z",
      "effectiveTo": "2024-12-31T23:59:59Z",
      "createdAt": "2023-12-01T00:00:00Z"
    }
  ]
}
```

### DELETE /policies/{id} - 정책 삭제 (DRAFT만 가능)

**Response:** `200 OK`

---

## 🔍 탐지 엔진 API

**Base URL:** `/api/v1/detection`

### POST /inspect - 탐지 작업 시작

**Request:**
```json
{
  "type": "STR",
  "snapshotId": "01POLICY...",
  "dateRange": {
    "from": "2025-01-01T00:00:00Z",
    "to": "2025-01-31T23:59:59Z"
  }
}
```

**Response:** `202 Accepted`
```json
{
  "success": true,
  "message": "Inspection job started",
  "data": {
    "jobId": "01JOB...",
    "status": "PROCESSING",
    "type": "STR",
    "startedAt": "2025-01-13T10:00:00Z"
  }
}
```

### GET /jobs/{jobId} - 탐지 작업 상태 조회

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "jobId": "01JOB...",
    "type": "STR",
    "status": "COMPLETED",
    "progress": 100,
    "startedAt": "2025-01-13T10:00:00Z",
    "completedAt": "2025-01-13T10:30:00Z",
    "result": {
      "totalProcessed": 10000,
      "eventsGenerated": 45,
      "casesCreated": 12
    }
  }
}
```

### GET /events - 탐지 이벤트 목록

**Query Parameters:**
- `type` (optional): `STR`, `CTR`, `WLF`
- `severity` (optional): `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `status` (optional): `NEW`, `REVIEWED`, `CONVERTED_TO_CASE`
- `dateFrom`, `dateTo`
- `cursor` (optional): Cursor for pagination
- `size` (default: 20)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01EVENT...",
        "type": "STR",
        "severity": "HIGH",
        "status": "NEW",
        "ruleId": "STR_001",
        "ruleName": "High Amount Transaction",
        "transactionId": "01TX...",
        "amount": 25000,
        "detectedAt": "2025-01-13T09:30:00Z"
      }
    ],
    "cursor": {
      "next": "01CRZ3...",
      "hasMore": true
    }
  }
}
```

### GET /events/{id} - 탐지 이벤트 상세

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "01EVENT...",
    "type": "STR",
    "severity": "HIGH",
    "status": "NEW",
    "ruleId": "STR_001",
    "ruleName": "High Amount Transaction",
    "transactionId": "01TX...",
    "transaction": {
      "id": "01TX...",
      "amount": 25000,
      "currency": "KRW",
      "sender": "John Doe",
      "receiver": "Jane Smith",
      "transactionDate": "2025-01-13T08:00:00Z"
    },
    "detectedAt": "2025-01-13T09:30:00Z",
    "reviewed": false
  }
}
```

### PUT /events/{id}/review - 이벤트 검토

**Request:**
```json
{
  "reviewed": true,
  "reviewComment": "False positive - legitimate transaction",
  "action": "DISMISS"
}
```

**Response:** `200 OK`

### POST /events/{id}/convert-to-case - 이벤트를 사례로 전환

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Event converted to case",
  "data": {
    "caseId": "01CASE...",
    "eventId": "01EVENT..."
  }
}
```

---

## 📁 사례 관리 API

**Base URL:** `/api/v1/cases`

### GET /cases - 사례 목록 조회

**Query Parameters:**
- `type` (optional): `STR`, `CTR`
- `status` (optional): `NEW`, `INVESTIGATING`, `PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `CLOSED`
- `assignedTo` (optional): User ID
- `priority` (optional): `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- `dateFrom`, `dateTo`
- `page`, `size`, `sort`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01CASE...",
        "type": "STR",
        "status": "INVESTIGATING",
        "priority": "HIGH",
        "subject": "High amount cash withdrawal",
        "assignedTo": {
          "id": "01USER...",
          "username": "investigator1"
        },
        "createdAt": "2025-01-13T10:00:00Z",
        "dueDate": "2025-01-20T23:59:59Z"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 35
    }
  }
}
```

### GET /cases/{id} - 사례 상세 조회

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "01CASE...",
    "type": "STR",
    "status": "INVESTIGATING",
    "priority": "HIGH",
    "subject": "High amount cash withdrawal",
    "description": "Customer withdrew 50,000,000 KRW in cash",
    "assignedTo": {
      "id": "01USER...",
      "username": "investigator1"
    },
    "events": [
      {
        "id": "01EVENT...",
        "type": "STR",
        "severity": "HIGH"
      }
    ],
    "activities": [
      {
        "id": "01ACT...",
        "type": "COMMENT",
        "content": "Started investigation",
        "createdBy": "investigator1",
        "createdAt": "2025-01-13T10:30:00Z"
      }
    ],
    "attachments": [
      {
        "id": "01FILE...",
        "filename": "evidence.pdf",
        "size": 1048576,
        "uploadedAt": "2025-01-13T11:00:00Z"
      }
    ],
    "createdAt": "2025-01-13T10:00:00Z",
    "dueDate": "2025-01-20T23:59:59Z"
  }
}
```

### POST /cases - 사례 생성

**Request:**
```json
{
  "type": "STR",
  "subject": "Suspicious transaction pattern",
  "description": "Multiple high-value transactions detected",
  "priority": "HIGH",
  "eventIds": ["01EVENT1...", "01EVENT2..."]
}
```

**Response:** `201 Created`

### PUT /cases/{id} - 사례 수정

**Request:**
```json
{
  "subject": "Updated subject",
  "description": "Updated description",
  "priority": "URGENT"
}
```

**Response:** `200 OK`

### PUT /cases/{id}/assign - 사례 할당

**Request:**
```json
{
  "assignedTo": "01USER..."
}
```

**Response:** `200 OK`

### PUT /cases/{id}/status - 사례 상태 변경

**Request:**
```json
{
  "status": "PENDING_APPROVAL",
  "comment": "Investigation completed, ready for approval"
}
```

**Response:** `200 OK`

### POST /cases/{id}/activities - 활동 기록 추가

**Request:**
```json
{
  "type": "COMMENT",
  "content": "Contacted customer for additional information"
}
```

**Response:** `201 Created`

### POST /cases/{id}/attachments - 첨부 파일 추가

**Request:** `multipart/form-data`
```
files: [file1.pdf, file2.jpg]
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Files uploaded successfully",
  "data": [
    {
      "id": "01FILE1...",
      "filename": "file1.pdf",
      "size": 1048576
    },
    {
      "id": "01FILE2...",
      "filename": "file2.jpg",
      "size": 524288
    }
  ]
}
```

### DELETE /cases/{id}/attachments/{attachmentId} - 첨부 파일 삭제

**Response:** `200 OK`

### POST /cases/{id}/approve - 사례 승인

**Request:**
```json
{
  "comment": "Approved for FIU submission"
}
```

**Response:** `200 OK`

### POST /cases/{id}/reject - 사례 거부

**Request:**
```json
{
  "reason": "Insufficient evidence",
  "comment": "Need more investigation"
}
```

**Response:** `200 OK`

---

## 📊 보고 API

**Base URL:** `/api/v1/reports`

### GET /reports - 보고서 목록

**Query Parameters:**
- `type` (optional): `STR`, `CTR`
- `status` (optional): `DRAFT`, `SUBMITTED`, `ACCEPTED`, `REJECTED`
- `dateFrom`, `dateTo`
- `page`, `size`, `sort`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01REPORT...",
        "type": "STR",
        "reportNumber": "STR-2025-001",
        "status": "SUBMITTED",
        "caseId": "01CASE...",
        "submittedAt": "2025-01-13T15:00:00Z",
        "submittedBy": "admin"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 10
    }
  }
}
```

### GET /reports/{id} - 보고서 상세

**Response:** `200 OK`

### POST /reports/str - STR 보고서 생성

**Request:**
```json
{
  "caseId": "01CASE...",
  "reportData": {
    "suspiciousActivity": "Large cash withdrawal",
    "amount": 50000000,
    "currency": "KRW",
    "customerInfo": {
      "name": "John Doe",
      "idNumber": "******-*******"
    }
  }
}
```

**Response:** `201 Created`

### POST /reports/ctr - CTR 보고서 생성

**Request:**
```json
{
  "caseId": "01CASE...",
  "reportData": {
    "transactionType": "CASH_WITHDRAWAL",
    "amount": 10000000,
    "currency": "KRW"
  }
}
```

**Response:** `201 Created`

### POST /reports/{id}/submit - FIU 제출

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Report submitted to FIU",
  "data": {
    "reportId": "01REPORT...",
    "submissionId": "FIU-2025-001",
    "submittedAt": "2025-01-13T15:00:00Z"
  }
}
```

### GET /reports/{id}/download - 보고서 다운로드

**Response:** `200 OK` (PDF/Excel file)

---

## 🧪 시뮬레이션 API

**Base URL:** `/api/v1/simulations`

### POST /simulations - 시뮬레이션 실행

**Request:**
```json
{
  "type": "WHAT_IF",
  "policySnapshotId": "01POLICY...",
  "scenario": {
    "threshold": 20000,
    "dateRange": {
      "from": "2024-01-01T00:00:00Z",
      "to": "2024-12-31T23:59:59Z"
    }
  }
}
```

**Response:** `202 Accepted`
```json
{
  "success": true,
  "message": "Simulation started",
  "data": {
    "simulationId": "01SIM...",
    "status": "RUNNING"
  }
}
```

### GET /simulations/{id} - 시뮬레이션 결과 조회

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "simulationId": "01SIM...",
    "type": "WHAT_IF",
    "status": "COMPLETED",
    "result": {
      "eventsGenerated": 120,
      "comparison": {
        "baseline": {
          "events": 150,
          "cases": 45
        },
        "simulated": {
          "events": 120,
          "cases": 38
        },
        "difference": {
          "events": -30,
          "cases": -7,
          "percentChange": -20
        }
      }
    },
    "completedAt": "2025-01-13T16:00:00Z"
  }
}
```

### POST /simulations/backtest - 백테스트 실행

**Request:**
```json
{
  "policySnapshotId": "01POLICY...",
  "historicalDateRange": {
    "from": "2024-01-01T00:00:00Z",
    "to": "2024-12-31T23:59:59Z"
  }
}
```

**Response:** `202 Accepted`

---

## 🗂️ 코드 관리 API

**Base URL:** `/api/v1/codes`

### GET /codes/groups - 코드 그룹 목록

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "groupCode": "USER_STATUS",
      "groupName": "사용자 상태",
      "description": "사용자 계정 상태 코드"
    },
    {
      "groupCode": "CASE_STATUS",
      "groupName": "사례 상태",
      "description": "사례 처리 상태 코드"
    }
  ]
}
```

### GET /codes/{groupCode} - 코드 값 목록

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "groupCode": "USER_STATUS",
    "groupName": "사용자 상태",
    "codes": [
      {
        "codeValue": "active",
        "codeName": "활성",
        "codeNameEn": "Active",
        "order": 1
      },
      {
        "codeValue": "inactive",
        "codeName": "비활성",
        "codeNameEn": "Inactive",
        "order": 2
      },
      {
        "codeValue": "locked",
        "codeName": "잠김",
        "codeNameEn": "Locked",
        "order": 3
      }
    ]
  }
}
```

---

## 📎 파일 관리 API

**Base URL:** `/api/v1/files`

### POST /files/upload - 파일 업로드

**Request:** `multipart/form-data`
```
files: [file1.pdf, file2.jpg]
category: "case_attachment"
refId: "01CASE..."
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Files uploaded successfully",
  "data": [
    {
      "id": "01FILE1...",
      "filename": "evidence.pdf",
      "originalFilename": "evidence.pdf",
      "size": 1048576,
      "mimeType": "application/pdf",
      "uploadedAt": "2025-01-13T10:00:00Z",
      "downloadUrl": "/api/v1/files/01FILE1.../download"
    }
  ]
}
```

### GET /files/{id} - 파일 정보 조회

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "01FILE...",
    "filename": "evidence.pdf",
    "originalFilename": "evidence.pdf",
    "size": 1048576,
    "mimeType": "application/pdf",
    "category": "case_attachment",
    "refId": "01CASE...",
    "uploadedBy": "01USER...",
    "uploadedAt": "2025-01-13T10:00:00Z"
  }
}
```

### GET /files/{id}/download - 파일 다운로드

**Response:** `200 OK` (Binary file)

### DELETE /files/{id} - 파일 삭제

**Response:** `200 OK`

---

## 📊 통계 및 대시보드 API

**Base URL:** `/api/v1/dashboard`

### GET /dashboard/summary - 대시보드 요약

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "statistics": {
      "totalCases": 350,
      "pendingCases": 45,
      "approvedCases": 280,
      "rejectedCases": 25
    },
    "recentActivity": [
      {
        "type": "CASE_CREATED",
        "description": "New STR case created",
        "timestamp": "2025-01-13T15:30:00Z"
      }
    ],
    "alerts": [
      {
        "type": "WARNING",
        "message": "5 cases approaching due date",
        "count": 5
      }
    ]
  }
}
```

### GET /dashboard/charts/cases-by-status - 상태별 사례 통계

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "labels": ["NEW", "INVESTIGATING", "PENDING_APPROVAL", "APPROVED"],
    "values": [45, 120, 85, 280]
  }
}
```

### GET /dashboard/charts/detection-trend - 탐지 추세

**Query Parameters:**
- `period`: `daily`, `weekly`, `monthly`
- `dateFrom`, `dateTo`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "labels": ["2025-01-01", "2025-01-02", "2025-01-03"],
    "datasets": [
      {
        "label": "STR Events",
        "data": [15, 22, 18]
      },
      {
        "label": "CTR Events",
        "data": [8, 12, 10]
      }
    ]
  }
}
```

---

## 🔧 시스템 관리 API

**Base URL:** `/api/v1/system`

### GET /health - Health Check

**Response:** `200 OK`
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "kafka": { "status": "UP" }
  }
}
```

### GET /info - 시스템 정보

**Response:** `200 OK`
```json
{
  "application": {
    "name": "Inspect-Hub",
    "version": "1.0.0"
  },
  "build": {
    "time": "2025-01-13T00:00:00Z"
  }
}
```

---

## 📞 API 호출 예시

### cURL Examples

**로그인:**
```bash
curl -X POST http://localhost:8090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

**사용자 목록 조회 (JWT 인증):**
```bash
curl -X GET http://localhost:8090/api/v1/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**정책 생성:**
```bash
curl -X POST http://localhost:8090/api/v1/policies \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "STR",
    "version": 1,
    "effectiveFrom": "2025-01-01T00:00:00Z",
    "configJson": {
      "threshold": 10000
    }
  }'
```

---

## 📖 참고 문서

- [API Design Guidelines](./DESIGN.md) - API 설계 원칙
- [API Contract](./CONTRACT.md) - Frontend ↔ Backend 계약
- [Security](../architecture/SECURITY.md) - 보안 구현
- [Backend README](../backend/README.md) - 백엔드 개발 가이드

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-13 | API Endpoints 초안 작성 | PM |

---

**Swagger UI:**
- 실시간 API 문서: `http://localhost:8090/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8090/v3/api-docs`

**Postman Collection:**
- Postman 컬렉션 다운로드: `/docs/api/postman/Inspect-Hub.postman_collection.json`
