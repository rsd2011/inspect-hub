# 📋 정책 관리 API

**Base URL:** `/api/v1/policies`

## GET /policies - 정책 목록 조회

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

## GET /policies/{id} - 정책 상세 조회

**Response:** `200 OK`

## POST /policies - 정책 생성 (스냅샷)

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

## PUT /policies/{id} - 정책 수정 (DRAFT만 가능)

**Response:** `200 OK`

## PUT /policies/{id}/activate - 정책 활성화

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

## POST /policies/{id}/rollback - 정책 롤백

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

## GET /policies/{id}/history - 정책 변경 이력

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

## DELETE /policies/{id} - 정책 삭제 (DRAFT만 가능)

**Response:** `200 OK`

---
