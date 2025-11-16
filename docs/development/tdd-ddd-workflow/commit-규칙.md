# Commit 규칙

## Commit 조건

**다음 조건을 모두 만족할 때만 Commit:**

1. ✅ **모든 테스트가 통과** (`./gradlew test`)
2. ✅ **컴파일러/린터 경고 해결** (`./gradlew build`)
3. ✅ **단일 논리적 작업 단위** (하나의 기능 또는 하나의 리팩토링)
4. ✅ **구조적 변경 또는 행동적 변경 중 하나만** (Tidy First 원칙)

## Commit 메시지 규칙

**Conventional Commits 형식:**

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type:**
- `feat`: 새로운 기능 (행동적 변경)
- `fix`: 버그 수정 (행동적 변경)
- `refactor`: 리팩토링 (구조적 변경)
- `test`: 테스트 추가/수정
- `docs`: 문서 변경
- `style`: 코드 포맷팅 (세미콜론 누락 등)
- `chore`: 빌드/설정 변경

**예제:**

```bash
# 구조적 변경 (Tidy First)
git commit -m "refactor(policy): Extract validateUniqueness method

- PolicyDomainService에서 중복 검증 로직 추출
- 테스트는 변경 없음 (구조적 변경만)
"

# 행동적 변경 (TDD)
git commit -m "feat(policy): Add duplicate policy name validation

- Policy 생성 시 이름 중복 검사
- DuplicatePolicyException 추가
- 테스트: shouldRejectDuplicatePolicyName
"

# 버그 수정
git commit -m "fix(user): Fix email validation regex

- 이메일 도메인에 하이픈 허용
- 테스트: shouldAcceptEmailWithHyphenInDomain
"
```

## Commit 주기

**작고 자주 커밋:**

```
┌─────────────────────────────────────┐
│ Bad: 큰 덩어리 커밋                  │
│ - 10개 파일 변경                     │
│ - 300줄 추가                        │
│ - 구조적 + 행동적 변경 혼합          │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ Good: 작고 빈번한 커밋               │
│                                     │
│ Commit 1: refactor - 메서드 추출    │
│ Commit 2: test - 테스트 추가        │
│ Commit 3: feat - 기능 구현          │
│ Commit 4: refactor - 중복 제거      │
└─────────────────────────────────────┘
```

---
