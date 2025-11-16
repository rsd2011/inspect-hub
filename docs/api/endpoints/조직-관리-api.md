# 🏢 조직 관리 API

**Base URL:** `/api/v1/organizations`

## GET /organizations - 조직 목록 조회

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

## GET /organizations/{id} - 조직 상세 조회

**Response:** `200 OK`

## POST /organizations - 조직 생성

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

## PUT /organizations/{id} - 조직 수정

**Response:** `200 OK`

## DELETE /organizations/{id} - 조직 삭제

**Response:** `200 OK`

---
