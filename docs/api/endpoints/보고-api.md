# 📊 보고 API

**Base URL:** `/api/v1/reports`

## GET /reports - 보고서 목록

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

## GET /reports/{id} - 보고서 상세

**Response:** `200 OK`

## POST /reports/str - STR 보고서 생성

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

## POST /reports/ctr - CTR 보고서 생성

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

## POST /reports/{id}/submit - FIU 제출

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

## GET /reports/{id}/download - 보고서 다운로드

**Response:** `200 OK` (PDF/Excel file)

---
