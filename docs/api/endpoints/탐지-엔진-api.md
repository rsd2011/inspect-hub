# 🔍 탐지 엔진 API

**Base URL:** `/api/v1/detection`

## POST /inspect - 탐지 작업 시작

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

## GET /jobs/{jobId} - 탐지 작업 상태 조회

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

## GET /events - 탐지 이벤트 목록

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

## GET /events/{id} - 탐지 이벤트 상세

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

## PUT /events/{id}/review - 이벤트 검토

**Request:**
```json
{
  "reviewed": true,
  "reviewComment": "False positive - legitimate transaction",
  "action": "DISMISS"
}
```

**Response:** `200 OK`

## POST /events/{id}/convert-to-case - 이벤트를 사례로 전환

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
