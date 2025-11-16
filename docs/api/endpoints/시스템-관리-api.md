# 🔧 시스템 관리 API

**Base URL:** `/api/v1/system`

## GET /health - Health Check

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

## GET /info - 시스템 정보

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
