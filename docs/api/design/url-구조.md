# 🔗 URL 구조

## 기본 패턴

```
https://{domain}/api/{version}/{resource}[/{id}][/{sub-resource}][/{action}]
```

**Components:**
- `{domain}`: `api.inspecthub.example.com` (프로덕션)
- `{version}`: API 버전 (예: `v1`, `v2`)
- `{resource}`: 리소스 이름 (복수형 명사)
- `{id}`: 리소스 식별자 (ULID 26자)
- `{sub-resource}`: 하위 리소스
- `{action}`: 특수 동작 (동사)

## Examples

```
# 리소스 CRUD
GET    /api/v1/users
POST   /api/v1/users
GET    /api/v1/users/{id}
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}

# 하위 리소스
GET    /api/v1/users/{userId}/permissions
POST   /api/v1/users/{userId}/permissions
DELETE /api/v1/users/{userId}/permissions/{permissionId}

# 특수 동작 (Action)
POST   /api/v1/policies/{id}/activate      # 정책 활성화
POST   /api/v1/policies/{id}/rollback      # 정책 롤백
POST   /api/v1/cases/{id}/assign           # 사례 할당
PUT    /api/v1/cases/{id}/approve          # 사례 승인

# 검색/필터링 (Query Parameters)
GET    /api/v1/users?status=active&role=admin
GET    /api/v1/policies?type=STR&version=1

# 페이지네이션
GET    /api/v1/users?page=1&size=20&sort=createdAt,desc
```

## 명명 규칙

| 항목 | 규칙 | 예시 |
|------|------|------|
| **리소스** | 복수형 명사, kebab-case | `users`, `policy-snapshots` |
| **리소스 ID** | ULID (26자) | `01ARZ3NDEKTSV4RRFFQ69G5FAV` |
| **Action** | 동사, kebab-case | `activate`, `send-email` |
| **Query Parameter** | camelCase | `userId`, `createdAfter` |

**❌ 금지:**
- 동사로 시작하는 URL (`/getUser`, `/createPolicy`)
- 밑줄 사용 (`/user_profiles` → `/user-profiles`)
- 단수형 리소스 (`/user` → `/users`)
- URL에 파일 확장자 (`.json`, `.xml`)

---
