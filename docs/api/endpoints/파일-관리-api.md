# 📎 파일 관리 API

**Base URL:** `/api/v1/files`

## POST /files/upload - 파일 업로드

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

## GET /files/{id} - 파일 정보 조회

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

## GET /files/{id}/download - 파일 다운로드

**Response:** `200 OK` (Binary file)

## DELETE /files/{id} - 파일 삭제

**Response:** `200 OK`

---
