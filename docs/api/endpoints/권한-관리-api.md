# 🔑 권한 관리 API

**Base URL:** `/api/v1/permissions`

## GET /permission-groups - 권한 그룹 목록

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01PERM...",
        "code": "PG.ADMIN",
        "name": "관리자 권한 그룹",
        "description": "시스템 관리자",
        "permissions": [
          { "code": "user:read", "name": "사용자 조회" },
          { "code": "user:write", "name": "사용자 생성/수정" }
        ]
      }
    ]
  }
}
```

## POST /permission-groups - 권한 그룹 생성

**Request:**
```json
{
  "code": "PG.INVESTIGATOR",
  "name": "조사자 권한 그룹",
  "description": "STR/CTR 사례 조사",
  "permissions": [
    "case:read",
    "case:write",
    "case:investigate"
  ]
}
```

**Response:** `201 Created`

## GET /menus - 메뉴 목록 (권한별)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "01MENU...",
      "code": "MENU.DASHBOARD",
      "name": "대시보드",
      "path": "/dashboard",
      "icon": "dashboard",
      "order": 1,
      "children": []
    },
    {
      "id": "01MENU...",
      "code": "MENU.USER_MANAGEMENT",
      "name": "사용자 관리",
      "path": "/users",
      "icon": "users",
      "order": 2,
      "children": [
        {
          "id": "01MENU...",
          "code": "MENU.USER_LIST",
          "name": "사용자 목록",
          "path": "/users/list"
        }
      ]
    }
  ]
}
```

---
