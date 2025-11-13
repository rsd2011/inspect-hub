# Test Plan - Inspect-Hub (TDD + DDD)

> **Kent Beck's TDD + Domain-Driven Design 테스트 계획서**
>
> **Last Updated**: 2025-01-13
> **Status**: Active Development

---

## 📖 사용 방법

1. **"go" 명령어 입력** → 다음 체크되지 않은 테스트를 찾아 구현
2. **Red → Green → Refactor** 사이클 준수
3. **테스트 완료 시**: `[x]` 표시
4. **각 테스트는 독립적으로 실행 가능해야 함**

---

## 🏗️ Layer 1: Domain Layer (도메인 레이어)

### Aggregate: User (사용자)

#### Value Objects
- [ ] UserId 생성 테스트 - ULID 형식 검증
- [ ] Email 생성 테스트 - 유효한 이메일 형식
- [ ] Email 형식 검증 테스트 - 잘못된 형식 거부
- [ ] Password 암호화 테스트 - BCrypt 해시
- [ ] Password 검증 테스트 - matches() 메서드

#### Aggregate Root: User
- [ ] User 생성 테스트 - 필수 필드 검증 (userId, email)
- [ ] User 이메일 변경 테스트 - 유효한 Email 객체
- [ ] User 비밀번호 변경 테스트 - 암호화된 Password 객체
- [ ] User 권한 부여 테스트 - Role 추가
- [ ] User 권한 제거 테스트 - Role 제거
- [ ] User hasRole 테스트 - 특정 역할 보유 여부
- [ ] User 활성화/비활성화 테스트 - 상태 변경
- [ ] User 도메인 이벤트 발행 테스트 - UserCreatedEvent

#### Domain Service: UserDomainService
- [ ] User 이메일 중복 검사 테스트 - 같은 이메일 존재 시 예외

---

### Aggregate: Policy (정책 - 스냅샷 기반)

#### Value Objects
- [ ] PolicyId 생성 테스트 - ULID 형식 검증
- [ ] PolicyVersion 생성 테스트 - 버전 번호 양수
- [ ] PolicyVersion 증가 테스트 - increment() 메서드
- [ ] PolicyType enum 테스트 - KYC, STR, CTR, RBA, WLF, FIU

#### Aggregate Root: Policy (PolicySnapshot)
- [ ] PolicySnapshot 생성 테스트 - 필수 필드 검증
- [ ] PolicySnapshot 승인 테스트 - 상태가 APPROVED로 변경
- [ ] PolicySnapshot 승인 거부 시 사유 필수 테스트
- [ ] PolicySnapshot 배포 테스트 - effectiveFrom, effectiveTo 설정
- [ ] PolicySnapshot 활성 기간 중 수정 거부 테스트 - 불변성
- [ ] PolicySnapshot 이전 버전 연결 테스트 - prevId 설정
- [ ] PolicySnapshot 다음 버전 연결 테스트 - nextId 설정
- [ ] PolicySnapshot 도메인 이벤트 발행 테스트 - PolicyApprovedEvent

#### Domain Service: PolicyDomainService
- [ ] Policy 버전 체인 검증 테스트 - 올바른 prev/next 연결
- [ ] Policy 활성 버전 단일성 테스트 - 동시에 하나만 활성
- [ ] Policy 롤백 가능 여부 테스트 - 이전 버전 존재 시 가능

---

## 🔧 Layer 2: Application Layer (애플리케이션 레이어)

### User Application Service

#### Commands
- [ ] CreateUserCommand 검증 테스트 - 필수 필드 체크
- [ ] UpdateUserCommand 검증 테스트 - 필수 필드 체크
- [ ] AssignRoleCommand 검증 테스트 - 유효한 역할

#### Application Service
- [ ] UserApplicationService.createUser 테스트 - 정상 생성
- [ ] UserApplicationService.createUser 이메일 중복 거부 테스트
- [ ] UserApplicationService.updateUser 테스트 - 정상 수정
- [ ] UserApplicationService.changePassword 테스트 - 비밀번호 변경
- [ ] UserApplicationService.assignRole 테스트 - 권한 부여
- [ ] UserApplicationService.revokeRole 테스트 - 권한 제거

#### Query Service
- [ ] UserQueryService.findById 테스트 - 존재하는 ID 조회
- [ ] UserQueryService.findByEmail 테스트 - 이메일로 조회
- [ ] UserQueryService.findByRole 테스트 - 역할별 사용자 조회

---

### Policy Application Service

#### Commands
- [ ] CreatePolicySnapshotCommand 검증 테스트 - 필수 필드 체크
- [ ] ApprovePolicyCommand 검증 테스트 - 승인자 정보 필수
- [ ] DeployPolicyCommand 검증 테스트 - 배포 기간 설정

#### Application Service
- [ ] PolicyApplicationService.createSnapshot 테스트 - 스냅샷 생성
- [ ] PolicyApplicationService.approvePolicy 테스트 - 정책 승인
- [ ] PolicyApplicationService.deployPolicy 테스트 - 정책 배포
- [ ] PolicyApplicationService.rollbackPolicy 테스트 - 이전 버전으로 롤백
- [ ] PolicyApplicationService.deployPolicy 중복 활성 버전 거부 테스트

#### Query Service
- [ ] PolicyQueryService.findActivePolicy 테스트 - 활성 정책 조회
- [ ] PolicyQueryService.findPolicyHistory 테스트 - 버전 이력 조회
- [ ] PolicyQueryService.findPolicyByVersion 테스트 - 특정 버전 조회

---

## 🗄️ Layer 3: Infrastructure Layer (인프라 레이어)

### User Infrastructure

#### Repository Implementation
- [ ] UserRepositoryImpl.save 테스트 - INSERT 성공
- [ ] UserRepositoryImpl.save 테스트 - UPDATE 성공
- [ ] UserRepositoryImpl.findById 테스트 - 존재하는 ID 조회
- [ ] UserRepositoryImpl.findByEmail 테스트 - 이메일로 조회
- [ ] UserRepositoryImpl.existsByEmail 테스트 - 이메일 존재 여부

---

### Policy Infrastructure

#### Repository Implementation
- [ ] PolicyRepositoryImpl.save 테스트 - INSERT 성공
- [ ] PolicyRepositoryImpl.findActivePolicy 테스트 - 활성 정책 조회
- [ ] PolicyRepositoryImpl.findPolicyHistory 테스트 - 버전 이력 조회
- [ ] PolicyRepositoryImpl.findByVersion 테스트 - 특정 버전 조회

---

## 🌐 Layer 4: Interface Layer (인터페이스 레이어)

### User API

#### Controller Tests (MockMvc)
- [ ] POST /api/v1/users - 정상 생성 (201 Created)
- [ ] POST /api/v1/users - 이메일 형식 오류 (400 Bad Request)
- [ ] POST /api/v1/users - 이메일 중복 (409 Conflict)
- [ ] GET /api/v1/users/{id} - 존재하는 ID (200 OK)
- [ ] GET /api/v1/users/{id} - 존재하지 않는 ID (404 Not Found)
- [ ] PUT /api/v1/users/{id} - 정상 수정 (200 OK)
- [ ] PUT /api/v1/users/{id}/password - 비밀번호 변경 (200 OK)
- [ ] POST /api/v1/users/{id}/roles - 역할 추가 (200 OK)
- [ ] DELETE /api/v1/users/{id}/roles/{roleCode} - 역할 제거 (200 OK)

---

### Policy API

#### Controller Tests (MockMvc)
- [ ] POST /api/v1/policies - 스냅샷 생성 (201 Created)
- [ ] POST /api/v1/policies/{id}/approve - 승인 (200 OK)
- [ ] POST /api/v1/policies/{id}/deploy - 배포 (200 OK)
- [ ] POST /api/v1/policies/{id}/rollback - 롤백 (200 OK)
- [ ] GET /api/v1/policies/active - 활성 정책 조회 (200 OK)
- [ ] GET /api/v1/policies/{id}/history - 버전 이력 (200 OK)

---

## 🔐 Cross-Cutting Concerns (횡단 관심사)

### Security
- [ ] JWT 토큰 생성 테스트 - 유효한 클레임 포함
- [ ] JWT 토큰 검증 테스트 - 유효한 토큰
- [ ] JWT 토큰 검증 테스트 - 만료된 토큰 거부
- [ ] JWT 토큰 검증 테스트 - 잘못된 서명 거부
- [ ] Password 암호화 테스트 - BCrypt 해시
- [ ] Password 검증 테스트 - 평문과 해시 매칭

### Audit Logging
- [ ] AuditLog 생성 테스트 - 필수 필드 (who, when, what)
- [ ] AuditLog 저장 테스트 - Repository 호출
- [ ] AuditLog 조회 테스트 - 사용자별, 날짜별 조회

### Exception Handling
- [ ] GlobalExceptionHandler - MethodArgumentNotValidException (400)
- [ ] GlobalExceptionHandler - EntityNotFoundException (404)
- [ ] GlobalExceptionHandler - DuplicateEntityException (409)
- [ ] GlobalExceptionHandler - IllegalArgumentException (400)
- [ ] GlobalExceptionHandler - 기타 예외 (500)

---

## 📊 테스트 커버리지 목표

| Layer | Target Coverage | Priority |
|-------|-----------------|----------|
| Domain | 95% | Highest |
| Application | 90% | High |
| Infrastructure | 80% | Medium |
| Interface | 85% | High |
| **Overall** | **88%** | - |

---

## 🚀 실행 방법

### 단일 테스트 실행
```bash
./gradlew test --tests PolicyTest.shouldCreatePolicyWithValidVersion
```

### 특정 클래스 테스트 실행
```bash
./gradlew test --tests PolicyTest
```

### 전체 테스트 실행 (장기 실행 제외)
```bash
./gradlew test -x longRunningTests
```

### 커버리지 리포트 생성
```bash
./gradlew test jacocoTestReport
open backend/server/build/reports/jacoco/test/html/index.html
```

---

## 📝 Notes

- **각 테스트는 독립적으로 실행 가능해야 합니다** (테스트 간 의존성 없음)
- **Given-When-Then 패턴** 사용 권장
- **테스트 이름은 행동 중심**으로 작성 (예: `shouldRejectDuplicatePolicyName`)
- **Mockito 사용 최소화** - 실제 객체 우선, Mock은 외부 의존성에만 사용
- **Testcontainers 사용** - 통합 테스트에서 실제 DB 사용
- **@Transactional 주의** - 테스트 후 자동 롤백 확인

---

## 🔄 마지막 업데이트

- **2025-01-13**: 초기 테스트 계획 작성 (TDD + DDD)
