# 📊 통계 및 대시보드 API

**Base URL:** `/api/v1/dashboard`

## GET /dashboard/summary - 대시보드 요약

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

## GET /dashboard/charts/cases-by-status - 상태별 사례 통계

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

## GET /dashboard/charts/detection-trend - 탐지 추세

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
