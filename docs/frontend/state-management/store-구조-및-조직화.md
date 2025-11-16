# Store 구조 및 조직화

## FSD 기반 Store 조직화

```
frontend/
├── features/
│   ├── auth/
│   │   └── model/
│   │       ├── auth.store.ts          # 인증/인가
│   │       └── auth.types.ts
│   ├── notification/
│   │   └── model/
│   │       ├── notification.store.ts  # 알림
│   │       └── notification.types.ts
│   └── theme/
│       └── model/
│           └── theme.store.ts         # 테마 설정
├── entities/
│   ├── user/
│   │   └── model/
│   │       ├── user.store.ts          # 사용자 엔티티
│   │       └── user.types.ts
│   ├── case/
│   │   └── model/
│   │       ├── case.store.ts          # 사례 엔티티
│   │       └── case.types.ts
│   └── detection/
│       └── model/
│           ├── detection.store.ts     # 탐지 이벤트
│           └── detection.types.ts
└── shared/
    └── lib/
        ├── ui-store/                  # UI 전역 상태
        │   └── ui.store.ts
        └── app-store/                 # 앱 설정
            └── app.store.ts
```

## Store 명명 규칙

**파일명:**
- `{domain}.store.ts` (예: `auth.store`, `user.store`)

**Store ID:**
- kebab-case 사용
- 도메인 이름 사용 (예: `'auth'`, `'user'`, `'case-management'`)

**State 변수:**
- camelCase 사용
- 명확한 이름 (예: `isLoading`, `currentUser`, `selectedCase`)

---
