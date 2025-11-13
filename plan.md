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

#### JWT Token Management - Token Generation (생성)

**정상 케이스:**
- [ ] JWT Access Token 생성 - 유효한 사용자 정보로 생성
- [ ] JWT Refresh Token 생성 - 유효한 사용자 정보로 생성
- [ ] JWT 클레임 포함 - subject (userId), employeeId, roles, orgId
- [ ] JWT 클레임 포함 - iat (발급시간), exp (만료시간), iss (발급자)
- [ ] JWT 헤더 포함 - alg (HS256), typ (JWT)
- [ ] JWT 만료시간 설정 - Access Token 1시간 (3600초)
- [ ] JWT 만료시간 설정 - Refresh Token 24시간 (86400초)
- [ ] JWT 서명 생성 - 설정된 비밀키로 HMAC-SHA256 서명

**실패 케이스:**
- [ ] JWT 생성 실패 - null 사용자 정보 (IllegalArgumentException)
- [ ] JWT 생성 실패 - 빈 userId (IllegalArgumentException)
- [ ] JWT 생성 실패 - null employeeId (IllegalArgumentException)
- [ ] JWT 생성 실패 - 빈 roles 리스트 (IllegalArgumentException)
- [ ] JWT 생성 실패 - null 비밀키 (JwtException)
- [ ] JWT 생성 실패 - 빈 비밀키 (JwtException)
- [ ] JWT 생성 실패 - 너무 짧은 비밀키 (최소 256비트 미만)

**엣지 케이스:**
- [ ] JWT 생성 - 매우 긴 클레임 값 (1KB 사용자명)
- [ ] JWT 생성 - 특수문자 포함 클레임 (이름에 한글, 공백)
- [ ] JWT 생성 - 다중 역할 (roles: [ADMIN, MANAGER, USER])
- [ ] JWT 생성 - 동일 사용자 연속 생성 시 다른 토큰 반환 (iat 차이)

#### JWT Token Management - Token Validation (검증)

**정상 케이스:**
- [ ] JWT 검증 성공 - 유효한 Access Token
- [ ] JWT 검증 성공 - 유효한 Refresh Token
- [ ] JWT 검증 성공 - 올바른 서명
- [ ] JWT 검증 성공 - 만료시간 이전
- [ ] JWT 검증 성공 - 올바른 발급자 (iss)
- [ ] JWT 클레임 추출 - subject (userId) 정확히 추출
- [ ] JWT 클레임 추출 - employeeId 정확히 추출
- [ ] JWT 클레임 추출 - roles 리스트 정확히 추출
- [ ] JWT 클레임 추출 - orgId 정확히 추출
- [ ] JWT 클레임 추출 - 만료시간 (exp) 정확히 추출

**실패 케이스 - 만료:**
- [ ] JWT 검증 실패 - 만료된 Access Token (ExpiredJwtException)
- [ ] JWT 검증 실패 - 만료된 Refresh Token (ExpiredJwtException)
- [ ] JWT 검증 실패 - 만료시간이 과거 (exp < now)
- [ ] JWT 검증 실패 - 만료 1초 후 (경계값 테스트)

**실패 케이스 - 서명:**
- [ ] JWT 검증 실패 - 잘못된 서명 (SignatureException)
- [ ] JWT 검증 실패 - 다른 비밀키로 생성된 토큰
- [ ] JWT 검증 실패 - 서명 부분 변조된 토큰
- [ ] JWT 검증 실패 - 페이로드 변조 후 재서명 안 된 토큰
- [ ] JWT 검증 실패 - 알고리즘 변조 (alg: none)

**실패 케이스 - 형식:**
- [ ] JWT 검증 실패 - null 토큰 (IllegalArgumentException)
- [ ] JWT 검증 실패 - 빈 문자열 토큰 (MalformedJwtException)
- [ ] JWT 검증 실패 - 잘못된 형식 (점 2개 미만)
- [ ] JWT 검증 실패 - Base64 디코딩 실패
- [ ] JWT 검증 실패 - JSON 파싱 실패 (잘못된 페이로드)

**실패 케이스 - 클레임:**
- [ ] JWT 검증 실패 - subject(userId) 누락 (MissingClaimException)
- [ ] JWT 검증 실패 - employeeId 클레임 누락
- [ ] JWT 검증 실패 - roles 클레임 누락
- [ ] JWT 검증 실패 - 잘못된 발급자 (iss 불일치)
- [ ] JWT 검증 실패 - iat(발급시간) 미래 시간 (NotBeforeException)

**보안 케이스:**
- [ ] JWT 검증 - 토큰 재사용 공격 방지 (Refresh Token 1회용)
- [ ] JWT 검증 - 타임스탬프 조작 방지 (exp, iat 서버 시간 기준)
- [ ] JWT 검증 - 알고리즘 혼동 공격 방지 (alg: none 거부)
- [ ] JWT 검증 - 비밀키 노출 시 영향 범위 (토큰 무효화 필요)

#### JWT Token Management - Token Refresh (갱신)

**정상 케이스:**
- [ ] Refresh Token으로 새 Access Token 발급
- [ ] Refresh Token으로 새 Refresh Token 발급 (Rotation)
- [ ] Refresh 후 새 토큰 만료시간 정확히 설정
- [ ] Refresh 후 기존 Refresh Token 무효화 (1회용)
- [ ] Refresh 시 사용자 정보 재검증 (DB 조회)
- [ ] Refresh 시 사용자 상태 확인 (ACTIVE만 허용)

**실패 케이스:**
- [ ] Refresh 실패 - 만료된 Refresh Token (ExpiredJwtException)
- [ ] Refresh 실패 - Access Token으로 시도 (InvalidTokenTypeException)
- [ ] Refresh 실패 - 이미 사용된 Refresh Token (TokenReusedException)
- [ ] Refresh 실패 - 잘못된 서명 Refresh Token
- [ ] Refresh 실패 - 사용자 없음 (UserNotFoundException)
- [ ] Refresh 실패 - 비활성 사용자 (status=INACTIVE)
- [ ] Refresh 실패 - 삭제된 사용자 (status=DELETED)
- [ ] Refresh 실패 - 잠긴 계정 (AccountLockedException)

**보안 케이스:**
- [ ] Refresh Token Rotation - 이전 토큰 즉시 무효화
- [ ] Refresh Token Family - 탈취 감지 시 전체 토큰 무효화
- [ ] Refresh 시도 제한 - 동일 토큰으로 재시도 거부
- [ ] Refresh 감사 로그 - 모든 갱신 시도 기록

#### JWT Token Management - Token Revocation (무효화)

**정상 케이스:**
- [ ] 로그아웃 시 Refresh Token 무효화
- [ ] 로그아웃 시 Redis 블랙리스트 등록
- [ ] 사용자 상태 변경 시 모든 토큰 무효화 (INACTIVE)
- [ ] 비밀번호 변경 시 모든 토큰 무효화
- [ ] 관리자 강제 로그아웃 시 토큰 무효화

**실패 케이스:**
- [ ] 무효화 실패 - 이미 무효화된 토큰
- [ ] 무효화 실패 - 존재하지 않는 토큰
- [ ] 무효화 실패 - Redis 연결 실패 (Fallback 처리)

**보안 케이스:**
- [ ] 블랙리스트 - Access Token 만료까지 유지 (1시간)
- [ ] 블랙리스트 - Refresh Token 만료까지 유지 (24시간)
- [ ] 블랙리스트 - TTL 자동 설정 (exp - now)
- [ ] 블랙리스트 - 무효화 감사 로그 기록

#### JWT Token Management - Token Storage & Transport (저장 및 전송)

**클라이언트 저장:**
- [ ] Access Token - Pinia Store (메모리)에 저장
- [ ] Refresh Token - Pinia Store (메모리)에 저장
- [ ] 페이지 새로고침 시 토큰 복원 (SessionStorage)
- [ ] 브라우저 종료 시 토큰 삭제

**HTTP 전송:**
- [ ] Access Token - Authorization 헤더 전송 (Bearer)
- [ ] Authorization 헤더 형식 - "Bearer {token}"
- [ ] Refresh Token - 별도 엔드포인트로 전송 (POST body)
- [ ] HTTPS 강제 (TLS 1.2+)

**보안 케이스:**
- [ ] XSS 방지 - HttpOnly 쿠키 사용 안 함 (SPA)
- [ ] CSRF 방지 - SameSite 쿠키 설정 (사용 시)
- [ ] 토큰 노출 방지 - URL 파라미터 사용 금지
- [ ] 로컬스토리지 사용 금지 - XSS 공격 취약

#### JWT Token Management - Claims & Payload (클레임 및 페이로드)

**표준 클레임:**
- [ ] sub (Subject) - 사용자 ID (ULID)
- [ ] iss (Issuer) - "inspect-hub"
- [ ] iat (Issued At) - Unix timestamp (초)
- [ ] exp (Expiration) - Unix timestamp (초)
- [ ] jti (JWT ID) - 토큰 고유 ID (선택사항)

**커스텀 클레임:**
- [ ] employeeId - 사원ID (문자열)
- [ ] roles - 역할 리스트 (문자열 배열)
- [ ] orgId - 조직 ID (ULID)
- [ ] orgName - 조직명 (문자열, 선택사항)
- [ ] fullName - 사용자 이름 (문자열)
- [ ] tokenType - "access" | "refresh"

**페이로드 크기:**
- [ ] 표준 페이로드 크기 - 512 바이트 이하
- [ ] 최대 페이로드 크기 - 8KB 이하
- [ ] 큰 페이로드 거부 - 8KB 초과 시 예외

**클레임 검증:**
- [ ] 필수 클레임 존재 확인 - sub, iss, iat, exp, employeeId, roles
- [ ] 클레임 타입 검증 - roles는 배열, exp는 숫자
- [ ] 클레임 값 검증 - employeeId 형식, orgId ULID 형식

#### JWT Token Management - Performance & Caching (성능 및 캐싱)

**성능:**
- [ ] 토큰 생성 성능 - 100개 생성 1초 이내
- [ ] 토큰 검증 성능 - 1000개 검증 1초 이내
- [ ] 서명 검증 최적화 - 캐싱된 비밀키 사용

**캐싱:**
- [ ] 사용자 정보 캐싱 - 토큰 검증 시 DB 조회 최소화
- [ ] 캐시 키 - "jwt:user:{userId}"
- [ ] 캐시 TTL - Access Token 만료시간과 동일 (1시간)
- [ ] 캐시 무효화 - 사용자 정보 변경 시

**동시성:**
- [ ] 동시 토큰 생성 - Thread-safe
- [ ] 동시 토큰 검증 - Thread-safe
- [ ] Redis 동시 접근 - Lettuce Connection Pool

#### JWT Token Management - Error Handling (오류 처리)

**예외 타입:**
- [ ] ExpiredJwtException - 만료된 토큰
- [ ] SignatureException - 잘못된 서명
- [ ] MalformedJwtException - 잘못된 형식
- [ ] UnsupportedJwtException - 지원하지 않는 JWT
- [ ] IllegalArgumentException - 잘못된 인자
- [ ] JwtException - 기타 JWT 오류

**예외 응답:**
- [ ] 401 Unauthorized - 만료/잘못된 토큰
- [ ] 403 Forbidden - 권한 없는 접근
- [ ] 400 Bad Request - 잘못된 형식
- [ ] 500 Internal Server Error - 서버 내부 오류

**에러 메시지:**
- [ ] 클라이언트 친화적 메시지 - "토큰이 만료되었습니다"
- [ ] 개발자용 상세 로그 - 스택 트레이스 포함
- [ ] 에러 코드 - TOKEN_EXPIRED, INVALID_TOKEN, etc.

#### JWT Token Management - Testing Utilities (테스트 유틸리티)

**테스트 헬퍼:**
- [ ] JwtTestHelper.createValidToken() - 유효한 토큰 생성
- [ ] JwtTestHelper.createExpiredToken() - 만료된 토큰 생성
- [ ] JwtTestHelper.createInvalidSignatureToken() - 잘못된 서명 토큰
- [ ] JwtTestHelper.createMalformedToken() - 잘못된 형식 토큰
- [ ] JwtTestHelper.extractClaims() - 클레임 추출

**Mock 데이터:**
- [ ] Mock 사용자 - 테스트용 고정 사용자 정보
- [ ] Mock 비밀키 - 테스트용 고정 비밀키
- [ ] Mock 시간 - Clock.fixed() 사용 (만료 테스트)

**통합 테스트:**
- [ ] 전체 플로우 - 로그인 → 토큰 발급 → API 호출 → Refresh → 로그아웃
- [ ] 멀티 스레드 - 동시 토큰 생성/검증 (100 threads)
- [ ] 부하 테스트 - 1000 TPS 토큰 검증

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
