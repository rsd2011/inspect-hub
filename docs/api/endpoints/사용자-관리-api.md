# 👤 사용자 관리 API

**Base URL:** `/api/v1/users`

## GET /users - 사용자 목록 조회

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

## GET /users/{id} - 사용자 상세 조회

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

## POST /users - 사용자 생성

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

## PUT /users/{id} - 사용자 수정

**Request:**
```json
{
  "email": "updated@example.com",
  "roles": ["ROLE_USER", "ROLE_REVIEWER"],
  "permissionGroupId": "01PERM..."
}
```

**Response:** `200 OK`

## PATCH /users/{id}/status - 사용자 상태 변경

**Request:**
```json
{
  "status": "inactive"
}
```

**Response:** `200 OK`

## DELETE /users/{id} - 사용자 삭제 (Soft Delete)

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "User deleted successfully"
}
```

---
