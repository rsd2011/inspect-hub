# 📞 API 호출 예시

## cURL Examples

**로그인:**
```bash
curl -X POST http://localhost:8090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

**사용자 목록 조회 (JWT 인증):**
```bash
curl -X GET http://localhost:8090/api/v1/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**정책 생성:**
```bash
curl -X POST http://localhost:8090/api/v1/policies \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "STR",
    "version": 1,
    "effectiveFrom": "2025-01-01T00:00:00Z",
    "configJson": {
      "threshold": 10000
    }
  }'
```

---
