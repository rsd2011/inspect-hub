# 🔄 HTTP 메서드

## 표준 CRUD

| 메서드 | 용도 | 멱등성 | Request Body | Response Body |
|--------|------|--------|--------------|---------------|
| **GET** | 조회 | O | ❌ | ✅ (리소스 데이터) |
| **POST** | 생성 | ❌ | ✅ (생성 데이터) | ✅ (생성된 리소스) |
| **PUT** | 전체 수정 | O | ✅ (전체 데이터) | ✅ (수정된 리소스) |
| **PATCH** | 부분 수정 | O | ✅ (수정할 필드만) | ✅ (수정된 리소스) |
| **DELETE** | 삭제 | O | ❌ | ✅ (성공 메시지) |

## GET - 조회

```http
# 목록 조회
GET /api/v1/users?status=active&page=1&size=20
```

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
        "username": "admin",
        "email": "admin@example.com",
        "status": "active"
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

```http
# 단건 조회
GET /api/v1/users/01ARZ3NDEKTSV4RRFFQ69G5FAV
```

```json
{
  "success": true,
  "data": {
    "id": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "username": "admin",
    "email": "admin@example.com",
    "status": "active",
    "createdAt": "2025-01-13T10:00:00Z"
  }
}
```

## POST - 생성

```http
POST /api/v1/users
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "SecurePassword123!"
}
```

```json
// 201 Created
// Location: /api/v1/users/01H2X3Y4Z5A6B7C8D9E0F1G2H3
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "01H2X3Y4Z5A6B7C8D9E0F1G2H3",
    "username": "newuser",
    "email": "newuser@example.com",
    "status": "active",
    "createdAt": "2025-01-13T10:05:00Z"
  }
}
```

## PUT - 전체 수정

**전체 필드를 제공해야 함 (누락 시 null/default로 처리)**

```http
PUT /api/v1/users/01ARZ3NDEKTSV4RRFFQ69G5FAV
Content-Type: application/json

{
  "username": "admin-updated",
  "email": "admin-new@example.com",
  "status": "active"
}
```

## PATCH - 부분 수정

**수정할 필드만 제공 (JSON Merge Patch)**

```http
PATCH /api/v1/users/01ARZ3NDEKTSV4RRFFQ69G5FAV
Content-Type: application/json

{
  "email": "admin-new@example.com"
}
```

```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "username": "admin",  // 변경되지 않음
    "email": "admin-new@example.com",  // 변경됨
    "status": "active"
  }
}
```

## DELETE - 삭제

```http
DELETE /api/v1/users/01ARZ3NDEKTSV4RRFFQ69G5FAV
```

```json
// 200 OK (soft delete) 또는 204 No Content (hard delete)
{
  "success": true,
  "message": "User deleted successfully"
}
```

**Note:** Inspect-Hub는 기본적으로 **Soft Delete** 사용

---
