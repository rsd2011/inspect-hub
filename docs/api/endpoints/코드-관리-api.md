# 🗂️ 코드 관리 API

**Base URL:** `/api/v1/codes`

## GET /codes/groups - 코드 그룹 목록

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "groupCode": "USER_STATUS",
      "groupName": "사용자 상태",
      "description": "사용자 계정 상태 코드"
    },
    {
      "groupCode": "CASE_STATUS",
      "groupName": "사례 상태",
      "description": "사례 처리 상태 코드"
    }
  ]
}
```

## GET /codes/{groupCode} - 코드 값 목록

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "groupCode": "USER_STATUS",
    "groupName": "사용자 상태",
    "codes": [
      {
        "codeValue": "active",
        "codeName": "활성",
        "codeNameEn": "Active",
        "order": 1
      },
      {
        "codeValue": "inactive",
        "codeName": "비활성",
        "codeNameEn": "Inactive",
        "order": 2
      },
      {
        "codeValue": "locked",
        "codeName": "잠김",
        "codeNameEn": "Locked",
        "order": 3
      }
    ]
  }
}
```

---
