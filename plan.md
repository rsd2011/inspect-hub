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
- [ ] EmployeeId 생성 테스트 - 유효한 사원ID 형식
- [ ] EmployeeId 형식 검증 테스트 - 잘못된 형식 거부
- [ ] Password 암호화 테스트 - BCrypt 해시
- [ ] Password 검증 테스트 - matches() 메서드

#### Aggregate Root: User
- [ ] User 생성 테스트 - 필수 필드 검증 (userId, employeeId)
- [ ] User 사원ID 변경 테스트 - 유효한 EmployeeId 객체
- [ ] User 비밀번호 변경 테스트 - 암호화된 Password 객체
- [ ] User 권한 부여 테스트 - Role 추가
- [ ] User 권한 제거 테스트 - Role 제거
- [ ] User hasRole 테스트 - 특정 역할 보유 여부
- [ ] User 활성화/비활성화 테스트 - 상태 변경
- [ ] User 도메인 이벤트 발행 테스트 - UserCreatedEvent

#### Domain Service: UserDomainService
- [ ] User 사원ID 중복 검사 테스트 - 같은 사원ID 존재 시 예외

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
- [ ] UserApplicationService.createUser 사원ID 중복 거부 테스트
- [ ] UserApplicationService.updateUser 테스트 - 정상 수정
- [ ] UserApplicationService.changePassword 테스트 - 비밀번호 변경
- [ ] UserApplicationService.assignRole 테스트 - 권한 부여
- [ ] UserApplicationService.revokeRole 테스트 - 권한 제거

#### Query Service
- [ ] UserQueryService.findById 테스트 - 존재하는 ID 조회
- [ ] UserQueryService.findByEmployeeId 테스트 - 사원ID로 조회
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
- [ ] UserRepositoryImpl.findByEmployeeId 테스트 - 사원ID로 조회
- [ ] UserRepositoryImpl.existsByEmployeeId 테스트 - 사원ID 존재 여부

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
- [ ] POST /api/v1/users - 사원ID 형식 오류 (400 Bad Request)
- [ ] POST /api/v1/users - 사원ID 중복 (409 Conflict)
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

#### Authentication - AD Login (Active Directory)

**정상 케이스:**
- [ ] AD 로그인 성공 - 유효한 사원ID + 비밀번호 → JWT 토큰 발급
- [ ] AD 로그인 성공 - 사용자 정보 포함 (id, employeeId, fullName, orgId, roles)
- [ ] AD 로그인 성공 - Access Token + Refresh Token 모두 발급
- [ ] AD 로그인 성공 - Token 만료시간 정확히 설정 (Access: 1시간, Refresh: 24시간)

**실패 케이스 - 인증 오류:**
- [ ] AD 로그인 실패 - 존재하지 않는 사원ID
- [ ] AD 로그인 실패 - 잘못된 비밀번호
- [ ] AD 로그인 실패 - 빈 사원ID (InvalidCredentialsException)
- [ ] AD 로그인 실패 - 빈 비밀번호 (InvalidCredentialsException)
- [ ] AD 로그인 실패 - null 사원ID (InvalidCredentialsException)
- [ ] AD 로그인 실패 - null 비밀번호 (InvalidCredentialsException)
- [ ] AD 로그인 실패 - 비활성화된 사용자 (AccountDisabledException)
- [ ] AD 로그인 실패 - 잠긴 계정 (AccountLockedException)
- [ ] AD 로그인 실패 - 만료된 계정 (AccountExpiredException)
- [ ] AD 로그인 실패 - 비밀번호 만료 (CredentialsExpiredException)

**실패 케이스 - 서버 오류:**
- [ ] AD 로그인 실패 - AD 서버 연결 불가 (Connection Timeout)
- [ ] AD 로그인 실패 - AD 서버 응답 없음 (Read Timeout 3초)
- [ ] AD 로그인 실패 - AD 서버 DNS 해석 실패
- [ ] AD 로그인 실패 - AD 서버 인증서 오류 (TLS/SSL)
- [ ] AD 로그인 실패 - AD 서버 5xx 에러 응답

**실패 케이스 - 입력 검증:**
- [ ] AD 로그인 실패 - 사원ID 형식 오류 (영문자 포함 등)
- [ ] AD 로그인 실패 - 사원ID 길이 초과 (50자 초과)
- [ ] AD 로그인 실패 - 비밀번호 길이 초과 (100자 초과)
- [ ] AD 로그인 실패 - SQL Injection 시도 차단
- [ ] AD 로그인 실패 - XSS 스크립트 입력 차단

**보안 케이스:**
- [ ] AD 로그인 시도 제한 - 5회 실패 시 계정 잠금 (5분)
- [ ] AD 로그인 시도 제한 - 10회 실패 시 계정 잠금 (30분)
- [ ] AD 로그인 Brute Force 방지 - IP별 요청 제한 (분당 10회)
- [ ] AD 로그인 감사 로그 - 성공 시 로그 기록 (사원ID, IP, 시간)
- [ ] AD 로그인 감사 로그 - 실패 시 로그 기록 (사원ID, 실패 사유, IP, 시간)

#### Authentication - SSO Login (Single Sign-On)

**정상 케이스:**
- [ ] SSO 로그인 성공 - Redirect URL 생성 (returnUrl 포함)
- [ ] SSO 로그인 성공 - Callback 처리 (SSO 토큰 → JWT 발급)
- [ ] SSO 로그인 성공 - State 파라미터 검증 (CSRF 방지)
- [ ] SSO 로그인 성공 - Frontend로 Redirect (JWT 포함)
- [ ] SSO 로그인 성공 - returnUrl 없을 때 기본 경로 (/)

**실패 케이스 - SSO 서버 오류:**
- [ ] SSO 로그인 실패 - SSO 서버 연결 불가 (Connection Timeout)
- [ ] SSO 로그인 실패 - SSO 서버 응답 없음 (Read Timeout 5초)
- [ ] SSO 로그인 실패 - SSO 서버 DNS 해석 실패
- [ ] SSO 로그인 실패 - SSO 서버 5xx 에러 응답
- [ ] SSO 로그인 실패 - SSO 서버 인증 거부 (401)

**실패 케이스 - Callback 처리:**
- [ ] SSO Callback 실패 - 잘못된 SSO 토큰 (InvalidSSOTokenException)
- [ ] SSO Callback 실패 - 만료된 SSO 토큰
- [ ] SSO Callback 실패 - 변조된 State 파라미터 (CSRF 공격)
- [ ] SSO Callback 실패 - State 파라미터 누락
- [ ] SSO Callback 실패 - SSO 토큰 누락

**입력 검증:**
- [ ] SSO 로그인 실패 - returnUrl XSS 시도 차단
- [ ] SSO 로그인 실패 - returnUrl Open Redirect 방지 (허용 도메인만)
- [ ] SSO 로그인 실패 - returnUrl 길이 초과 (500자)

**보안 케이스:**
- [ ] SSO 로그인 감사 로그 - Redirect 시도 기록
- [ ] SSO 로그인 감사 로그 - Callback 처리 성공/실패 기록
- [ ] SSO State 파라미터 - 1회용 토큰 검증 (재사용 불가)
- [ ] SSO State 파라미터 - 만료시간 검증 (10분)

#### Authentication - Local Login (Fallback)

**정상 케이스:**
- [ ] 일반 로그인 성공 - 유효한 사원ID + 비밀번호 → JWT 발급
- [ ] 일반 로그인 성공 - BCrypt 비밀번호 검증
- [ ] 일반 로그인 성공 - 사용자 정보 포함 (id, employeeId, fullName, orgId, roles)
- [ ] 일반 로그인 성공 - Access Token + Refresh Token 발급

**실패 케이스 - 인증 오류:**
- [ ] 일반 로그인 실패 - 존재하지 않는 사원ID
- [ ] 일반 로그인 실패 - 잘못된 비밀번호
- [ ] 일반 로그인 실패 - 빈 사원ID
- [ ] 일반 로그인 실패 - 빈 비밀번호
- [ ] 일반 로그인 실패 - null 사원ID
- [ ] 일반 로그인 실패 - null 비밀번호
- [ ] 일반 로그인 실패 - 비활성화된 사용자 (status=INACTIVE)
- [ ] 일반 로그인 실패 - 삭제된 사용자 (status=DELETED)

**입력 검증:**
- [ ] 일반 로그인 실패 - 사원ID 형식 오류
- [ ] 일반 로그인 실패 - 사원ID 길이 초과 (50자)
- [ ] 일반 로그인 실패 - 비밀번호 길이 초과 (100자)
- [ ] 일반 로그인 실패 - SQL Injection 시도 차단
- [ ] 일반 로그인 실패 - XSS 스크립트 입력 차단

**보안 케이스:**
- [ ] 일반 로그인 시도 제한 - 5회 실패 시 계정 잠금 (5분)
- [ ] 일반 로그인 시도 제한 - 10회 실패 시 계정 잠금 (30분)
- [ ] 일반 로그인 Brute Force 방지 - IP별 요청 제한 (분당 10회)
- [ ] 일반 로그인 감사 로그 - 성공 시 로그 기록
- [ ] 일반 로그인 감사 로그 - 실패 시 로그 기록

#### Authentication - Health Check & Fallback

**Health Check:**
- [ ] Health Check - 모든 서버 정상 (AD, SSO, Local)
- [ ] Health Check - AD 서버만 장애
- [ ] Health Check - SSO 서버만 장애
- [ ] Health Check - AD, SSO 모두 장애 (Local만 가능)
- [ ] Health Check - 응답 시간 측정 (ms 단위)
- [ ] Health Check - 권장 로그인 방식 반환 (recommendedMethod)

**Fallback 로직:**
- [ ] Fallback - SSO 장애 시 AD로 자동 전환
- [ ] Fallback - SSO, AD 장애 시 Local로 자동 전환
- [ ] Fallback - 장애 복구 시 원래 방식으로 복귀 (SSO 우선)
- [ ] Fallback - 사용자 명시적 선택 시 Fallback 무시
- [ ] Fallback - Fallback 시도 감사 로그 기록

#### Authentication - Concurrent & Performance

**동시성:**
- [ ] 동시 로그인 - 같은 사용자 여러 세션 허용
- [ ] 동시 로그인 - 세션별 독립적인 JWT 발급
- [ ] 동시 로그인 - 최대 세션 수 제한 (10개)
- [ ] 동시 로그인 - 초과 시 가장 오래된 세션 자동 만료

**성능:**
- [ ] 로그인 성능 - AD 인증 3초 이내 완료
- [ ] 로그인 성능 - SSO 인증 5초 이내 완료
- [ ] 로그인 성능 - Local 인증 1초 이내 완료
- [ ] 로그인 성능 - 동시 100건 요청 처리 (TPS 100+)

#### Authentication - Integration

**AD 연동:**
- [ ] AD 연동 - LDAP 연결 테스트 (ldap://ad.company.com:389)
- [ ] AD 연동 - 사용자 정보 조회 (displayName, mail, department)
- [ ] AD 연동 - 그룹 정보 조회 (memberOf)
- [ ] AD 연동 - 비밀번호 정책 준수 (복잡도, 만료일)

**SSO 연동:**
- [ ] SSO 연동 - OAuth2 흐름 테스트 (Authorization Code)
- [ ] SSO 연동 - 사용자 정보 조회 (UserInfo Endpoint)
- [ ] SSO 연동 - 토큰 갱신 테스트 (Refresh Token)
- [ ] SSO 연동 - 로그아웃 전파 (SLO - Single Logout)

#### JWT Token Management
- [ ] JWT 토큰 생성 테스트 - 유효한 클레임 포함
- [ ] JWT 토큰 검증 테스트 - 유효한 토큰
- [ ] JWT 토큰 검증 테스트 - 만료된 토큰 거부
- [ ] JWT 토큰 검증 테스트 - 잘못된 서명 거부

#### Password Management
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
