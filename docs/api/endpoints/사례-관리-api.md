# 📁 사례 관리 API

**Base URL:** `/api/v1/cases`

## GET /cases - 사례 목록 조회

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

## GET /cases/{id} - 사례 상세 조회

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

## POST /cases - 사례 생성

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

## PUT /cases/{id} - 사례 수정

**Request:**
```json
{
  "subject": "Updated subject",
  "description": "Updated description",
  "priority": "URGENT"
}
```

**Response:** `200 OK`

## PUT /cases/{id}/assign - 사례 할당

**Request:**
```json
{
  "assignedTo": "01USER..."
}
```

**Response:** `200 OK`

## PUT /cases/{id}/status - 사례 상태 변경

**Request:**
```json
{
  "status": "PENDING_APPROVAL",
  "comment": "Investigation completed, ready for approval"
}
```

**Response:** `200 OK`

## POST /cases/{id}/activities - 활동 기록 추가

**Request:**
```json
{
  "type": "COMMENT",
  "content": "Contacted customer for additional information"
}
```

**Response:** `201 Created`

## POST /cases/{id}/attachments - 첨부 파일 추가

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

## DELETE /cases/{id}/attachments/{attachmentId} - 첨부 파일 삭제

**Response:** `200 OK`

## POST /cases/{id}/approve - 사례 승인

**Request:**
```json
{
  "comment": "Approved for FIU submission"
}
```

**Response:** `200 OK`

## POST /cases/{id}/reject - 사례 거부

**Request:**
```json
{
  "reason": "Insufficient evidence",
  "comment": "Need more investigation"
}
```

**Response:** `200 OK`

---
