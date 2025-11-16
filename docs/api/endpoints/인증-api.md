# 🔐 인증 API

**Base URL:** `/api/v1/auth`

## POST /login - 로그인

**Request:**
```json
{
  "username": "admin",
  "password": "SecurePassword123!"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "01REFRESH123...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "01ARZ3...",
      "username": "admin",
      "email": "admin@example.com",
      "roles": ["ROLE_ADMIN"]
    }
  }
}
```

## POST /refresh - 토큰 갱신

**Request:**
```json
{
  "refreshToken": "01REFRESH123..."
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "01REFRESH456...",
    "expiresIn": 3600
  }
}
```

## POST /logout - 로그아웃

**Request:**
```json
{
  "refreshToken": "01REFRESH123..."
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

## POST /change-password - 비밀번호 변경

**Request:**
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword456!"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

---
