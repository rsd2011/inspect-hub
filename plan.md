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

### Login Policy Management (로그인 정책 관리)

#### Login Policy - Domain Model (도메인 모델)

**정상 케이스:**
- [ ] LoginPolicy 생성 - 필수 필드 (id, name, orgId, enabledMethods)
- [ ] LoginPolicy 생성 - 활성화된 로그인 방식 리스트 (SSO, AD, LOCAL)
- [ ] LoginPolicy 생성 - 기본값 설정 (모든 방식 활성화)
- [ ] LoginPolicy 생성 - 우선순위 설정 (priority: SSO > AD > LOCAL)
- [ ] LoginPolicy 생성 - 조직별 정책 설정 (orgId 기준)
- [ ] LoginPolicy 생성 - 글로벌 정책 설정 (orgId = null, 기본 정책)

**실패 케이스:**
- [ ] LoginPolicy 생성 실패 - null name (IllegalArgumentException)
- [ ] LoginPolicy 생성 실패 - 빈 enabledMethods (최소 1개 필요)
- [ ] LoginPolicy 생성 실패 - 잘못된 method 타입 (SSO, AD, LOCAL만 허용)
- [ ] LoginPolicy 생성 실패 - 중복된 method (Set 사용)

**비즈니스 로직:**
- [ ] isMethodEnabled(method) - 특정 방식 활성화 여부 확인
- [ ] getEnabledMethods() - 활성화된 방식 리스트 반환
- [ ] getPrimaryMethod() - 최우선 로그인 방식 반환 (priority 기준)
- [ ] disableMethod(method) - 특정 방식 비활성화
- [ ] enableMethod(method) - 특정 방식 활성화
- [ ] updatePriority(newPriority) - 우선순위 변경

#### Login Policy - Application Service (응용 서비스)

**정책 조회:**
- [ ] LoginPolicyService.getPolicyByOrg(orgId) - 조직별 정책 조회
- [ ] LoginPolicyService.getPolicyByOrg(null) - 글로벌 정책 조회 (기본값)
- [ ] LoginPolicyService.getPolicyByOrg(orgId) - 조직 정책 없으면 글로벌 반환 (Fallback)
- [ ] LoginPolicyService.getAvailableMethods(orgId) - 사용 가능한 로그인 방식 리스트
- [ ] LoginPolicyService.getPrimaryMethod(orgId) - 최우선 로그인 방식

**정책 생성:**
- [ ] LoginPolicyService.createPolicy(request) - 새 정책 생성
- [ ] LoginPolicyService.createPolicy(request) - orgId 중복 확인
- [ ] LoginPolicyService.createPolicy(request) - 최소 1개 방식 활성화 검증
- [ ] LoginPolicyService.createGlobalPolicy(request) - 글로벌 정책 생성 (orgId = null)

**정책 수정:**
- [ ] LoginPolicyService.updatePolicy(id, request) - 정책 수정
- [ ] LoginPolicyService.updateEnabledMethods(id, methods) - 활성화 방식 변경
- [ ] LoginPolicyService.updatePriority(id, priority) - 우선순위 변경
- [ ] LoginPolicyService.disableMethod(id, method) - 특정 방식 비활성화
- [ ] LoginPolicyService.enableMethod(id, method) - 특정 방식 활성화

**정책 삭제:**
- [ ] LoginPolicyService.deletePolicy(id) - 정책 삭제
- [ ] LoginPolicyService.deletePolicy(globalPolicy) - 글로벌 정책 삭제 금지 (예외)
- [ ] LoginPolicyService.deletePolicy(id) - 사용 중인 정책 삭제 금지

#### Login Policy - Data Structure (데이터 구조)

**LoginPolicy Entity:**
- [ ] LoginPolicy.id - ULID (고유 식별자)
- [ ] LoginPolicy.name - 정책 이름
- [ ] LoginPolicy.orgId - 조직 ID (null = 글로벌 정책)
- [ ] LoginPolicy.enabledMethods - 활성화된 방식 (Set<LoginMethod>)
- [ ] LoginPolicy.priority - 우선순위 리스트 (List<LoginMethod>)
- [ ] LoginPolicy.active - 활성 상태 (기본값: true)
- [ ] LoginPolicy.createdBy - 생성자 ID
- [ ] LoginPolicy.createdAt - 생성 일시
- [ ] LoginPolicy.updatedBy - 수정자 ID
- [ ] LoginPolicy.updatedAt - 수정 일시

**LoginMethod Enum:**
- [ ] LoginMethod.SSO - Single Sign-On
- [ ] LoginMethod.AD - Active Directory
- [ ] LoginMethod.LOCAL - 일반 로그인 (DB)

**LoginPolicyRequest DTO:**
- [ ] name - 정책 이름 (필수, 최대 100자)
- [ ] orgId - 조직 ID (선택, null = 글로벌)
- [ ] enabledMethods - 활성화 방식 (필수, 최소 1개)
- [ ] priority - 우선순위 (선택, 기본값: [SSO, AD, LOCAL])

#### Login Policy - Repository & Queries (저장소 및 쿼리)

**저장:**
- [ ] LoginPolicyRepository.save(policy) - 정책 저장
- [ ] LoginPolicyRepository.save(policy) - INSERT 성공
- [ ] LoginPolicyRepository.save(policy) - UPDATE 성공

**조회:**
- [ ] LoginPolicyRepository.findByOrgId(orgId) - 조직별 정책 조회
- [ ] LoginPolicyRepository.findGlobalPolicy() - 글로벌 정책 조회 (orgId = null)
- [ ] LoginPolicyRepository.findByOrgIdOrGlobal(orgId) - 조직 정책 또는 글로벌 (Fallback)
- [ ] LoginPolicyRepository.existsByOrgId(orgId) - 조직 정책 존재 여부
- [ ] LoginPolicyRepository.findAll() - 모든 정책 조회
- [ ] LoginPolicyRepository.findAllActive() - 활성 정책만 조회

**삭제:**
- [ ] LoginPolicyRepository.delete(id) - 정책 삭제 (Soft delete)
- [ ] LoginPolicyRepository.delete(globalPolicy) - 글로벌 정책 삭제 방지

#### Login Policy - Integration with Authentication (인증 통합)

**로그인 프로세스 통합:**
- [ ] AuthenticationService - 조직별 정책 조회 후 로그인 방식 결정
- [ ] AuthenticationService - 비활성화된 방식 사용 시 예외 (MethodNotAllowedException)
- [ ] AuthenticationService - 우선순위 기반 자동 Fallback (기본: SSO → AD → LOCAL)
- [ ] AuthenticationService - 비인증 사용자 → 최우선 방식(SSO)으로 리다이렉트
- [ ] AuthenticationService - 명시적 방식 지정 시 정책 검증 (사용자가 AD 선택 시)
- [ ] AuthenticationService - 정책 없으면 글로벌 정책 사용 (기본: SSO > AD > LOCAL)
- [ ] AuthenticationService - 모든 로그인 방식 사용 가정 (우선순위만 다름)

**Health Check 통합:**
- [ ] AuthHealthService - 정책 기반 사용 가능 방식만 체크
- [ ] AuthHealthService - 비활성화된 방식은 건강 체크 제외
- [ ] AuthHealthService - 권장 방식 반환 시 정책 우선순위 반영

**Frontend 통합:**
- [ ] Login Page - 모든 로그인 방식 표시 (SSO, AD, LOCAL 탭)
- [ ] Login Page - 우선순위 순서대로 탭 배치 (SSO > AD > LOCAL)
- [ ] Login Page - 기본 선택 탭 = SSO (최우선 방식)
- [ ] Login Page - 비인증 접근 시 자동 리다이렉트 (returnUrl 파라미터 포함)
- [ ] Login Page - SSO 실패 시 자동 AD 탭으로 전환
- [ ] Login Page - AD 실패 시 자동 LOCAL 탭으로 전환

#### Login Policy - API Endpoints (API 엔드포인트)

**조회 API:**
- [ ] GET /api/v1/login-policies - 모든 정책 조회 (관리자)
- [ ] GET /api/v1/login-policies/global - 글로벌 정책 조회
- [ ] GET /api/v1/login-policies/org/{orgId} - 조직별 정책 조회
- [ ] GET /api/v1/login-policies/{id} - 특정 정책 조회
- [ ] GET /api/v1/auth/available-methods - 현재 사용자 조직의 사용 가능 방식 (Public)

**생성/수정 API:**
- [ ] POST /api/v1/login-policies - 정책 생성 (관리자)
- [ ] PUT /api/v1/login-policies/{id} - 정책 수정 (관리자)
- [ ] PATCH /api/v1/login-policies/{id}/methods - 활성화 방식 변경
- [ ] PATCH /api/v1/login-policies/{id}/priority - 우선순위 변경
- [ ] PATCH /api/v1/login-policies/{id}/toggle/{method} - 방식 활성화/비활성화

**삭제 API:**
- [ ] DELETE /api/v1/login-policies/{id} - 정책 삭제 (관리자)

#### Login Policy - Validation Rules (검증 규칙)

**필수 검증:**
- [ ] 최소 1개 로그인 방식 활성화 필수
- [ ] enabledMethods가 빈 리스트면 예외 (BadRequestException)
- [ ] 마지막 남은 방식 비활성화 시도 시 예외

**우선순위 검증:**
- [ ] priority 리스트는 enabledMethods와 일치해야 함
- [ ] priority에 없는 방식은 자동으로 끝에 추가
- [ ] priority에 중복 방식 있으면 예외

**조직 정책 검증:**
- [ ] 동일 orgId로 중복 정책 생성 불가 (UNIQUE 제약)
- [ ] 글로벌 정책은 1개만 존재 (orgId = null)
- [ ] orgId는 존재하는 조직이어야 함 (FK 제약)

**보안 검증:**
- [ ] LOCAL 방식만 활성화 시 경고 로그 (보안상 위험)
- [ ] SSO 비활성화 시 관리자 알림
- [ ] 정책 변경 시 감사 로그 기록

#### Login Policy - Default Policies (기본 정책)

**시스템 기본 정책:**
- [ ] 시스템 초기화 시 글로벌 정책 자동 생성
- [ ] 글로벌 정책 기본값: enabledMethods = [SSO, AD, LOCAL] (모든 방식 사용)
- [ ] 글로벌 정책 기본 우선순위: [SSO, AD, LOCAL] (SSO 최우선)
- [ ] 글로벌 정책 name: "기본 로그인 정책"
- [ ] 비인증 사용자 자동 리다이렉트: /login?returnUrl={originalUrl}&method=SSO

**조직 생성 시:**
- [ ] 새 조직 생성 시 별도 정책 생성 안 함 (글로벌 사용)
- [ ] 조직별 커스텀 정책은 명시적으로 생성

**마이그레이션:**
- [ ] 기존 시스템 마이그레이션 시 모든 조직에 글로벌 정책 적용
- [ ] 특정 조직만 다른 정책 필요 시 개별 생성

#### Login Policy - Caching (캐싱)

**캐시 전략:**
- [ ] LoginPolicy 조회 시 Redis 캐싱
- [ ] 캐시 키: "login-policy:org:{orgId}"
- [ ] 글로벌 정책 캐시 키: "login-policy:global"
- [ ] 캐시 TTL: 1시간
- [ ] 정책 변경 시 캐시 무효화

**캐시 무효화:**
- [ ] 정책 생성 시 해당 조직 캐시 무효화
- [ ] 정책 수정 시 해당 조직 캐시 무효화
- [ ] 정책 삭제 시 해당 조직 캐시 무효화
- [ ] 글로벌 정책 변경 시 모든 캐시 무효화

#### Login Policy - Audit Logging (감사 로그)

**정책 변경 감사:**
- [ ] 정책 생성 - 감사 로그 (action: LOGIN_POLICY_CREATED)
- [ ] 정책 수정 - 감사 로그 (action: LOGIN_POLICY_UPDATED)
- [ ] 정책 삭제 - 감사 로그 (action: LOGIN_POLICY_DELETED)
- [ ] 방식 활성화 - 감사 로그 (action: LOGIN_METHOD_ENABLED)
- [ ] 방식 비활성화 - 감사 로그 (action: LOGIN_METHOD_DISABLED)
- [ ] 우선순위 변경 - 감사 로그 (action: LOGIN_PRIORITY_UPDATED)

**로그 상세 정보:**
- [ ] details.policyId - 정책 ID
- [ ] details.orgId - 조직 ID
- [ ] details.beforeMethods - 변경 전 활성화 방식
- [ ] details.afterMethods - 변경 후 활성화 방식
- [ ] details.beforePriority - 변경 전 우선순위
- [ ] details.afterPriority - 변경 후 우선순위
- [ ] details.changedBy - 변경자 사원ID

#### Login Policy - Error Handling (오류 처리)

**예외 타입:**
- [ ] PolicyNotFoundException - 정책을 찾을 수 없음
- [ ] DuplicatePolicyException - 중복 정책 (orgId)
- [ ] MethodNotAllowedException - 비활성화된 방식 사용 시도
- [ ] InvalidMethodException - 잘못된 로그인 방식
- [ ] EmptyMethodsException - 활성화된 방식이 없음
- [ ] GlobalPolicyDeletionException - 글로벌 정책 삭제 시도

**HTTP 응답:**
- [ ] 404 Not Found - 정책 없음
- [ ] 409 Conflict - 중복 정책
- [ ] 400 Bad Request - 유효하지 않은 요청
- [ ] 403 Forbidden - 권한 없음 (관리자 전용)

#### Login Policy - Testing Scenarios (테스트 시나리오)

**시나리오 1: 기본 로그인 흐름 (모든 방식 활성화)**
- [ ] enabledMethods = [SSO, AD, LOCAL] (기본값)
- [ ] priority = [SSO, AD, LOCAL]
- [ ] 비인증 사용자 접근 → /login?method=SSO로 리다이렉트
- [ ] SSO 로그인 성공 → 원래 페이지로 이동
- [ ] SSO 실패 → 자동 AD 탭으로 전환
- [ ] AD 실패 → 자동 LOCAL 탭으로 전환
- [ ] LOCAL 실패 → 에러 메시지 표시 (Fallback 종료)

**시나리오 2: 사용자 명시적 방식 선택**
- [ ] 사용자가 AD 탭 직접 선택
- [ ] AD 로그인 시도
- [ ] AD 실패 시 → 자동 Fallback 없음 (에러 메시지만 표시)
- [ ] 사용자가 다른 탭으로 수동 전환 가능

**시나리오 3: 세션 만료 후 재로그인**
- [ ] 세션 만료 감지 (JWT 만료)
- [ ] 자동 로그아웃 처리
- [ ] 최우선 방식(SSO)으로 리다이렉트
- [ ] returnUrl에 만료 전 페이지 URL 포함

**시나리오 4: API 인증 실패**
- [ ] 비인증 API 호출 → 401 Unauthorized
- [ ] 프론트엔드에서 로그인 페이지로 리다이렉트
- [ ] returnUrl 파라미터로 원래 페이지 정보 전달

**시나리오 5: Health Check 및 공개 API**
- [ ] GET /actuator/health → 인증 없이 200 OK
- [ ] POST /api/v1/auth/request-reset → 인증 없이 200 OK
- [ ] GET /api/v1/users → 인증 필요, 401 Unauthorized (비인증 시)

**시나리오 6: 정책 변경 중 로그인**
- [ ] 정책 변경 중에도 기존 세션 유지
- [ ] 캐시 무효화 후 새로운 로그인부터 적용
- [ ] 정책 변경 감사 로그 기록

### Security

**⚠️ 전체 시스템 인증 정책:**
- [ ] 모든 기능 로그인 필수 - 사내 업무시스템으로 공개 API 없음
- [ ] 비인증 사용자 접근 시 로그인 페이지로 자동 리다이렉트
- [ ] 로그인 리다이렉트 우선순위 - SSO > AD > LOCAL (모든 로그인 방식 사용 가정)
- [ ] 세션 만료 시 자동 로그아웃 후 최우선 로그인 방식으로 리다이렉트
- [ ] 공개 API 예외 - 비밀번호 리셋 요청/검증 API만 인증 불필요
- [ ] Health Check 엔드포인트 - 인증 불필요 (모니터링 목적)

**로그인 흐름 (Login Flow):**
- [ ] 비인증 사용자 → 정책 조회 → 최우선 방식으로 리다이렉트 (기본: SSO)
- [ ] SSO 페이지 접속 → SSO 인증 실패 시 자동 AD로 Fallback
- [ ] AD 페이지 접속 → AD 인증 실패 시 자동 LOCAL로 Fallback
- [ ] LOCAL 로그인 실패 시 → 에러 메시지 표시 (더 이상 Fallback 없음)
- [ ] 사용자 명시적 선택 → 선택한 방식으로만 로그인 (Fallback 무시)
- [ ] 로그인 성공 → 원래 접근하려던 페이지로 리다이렉트 (returnUrl 파라미터)

**인증 필수 API:**
- [ ] 모든 /api/v1/** 엔드포인트 - JWT Access Token 필수
- [ ] 인증 실패 시 401 Unauthorized + WWW-Authenticate 헤더
- [ ] 권한 부족 시 403 Forbidden + 필요 권한 정보 포함

**인증 불필요 API (예외):**
- [ ] POST /api/v1/auth/request-reset - 비밀번호 리셋 요청 (이메일 기반)
- [ ] GET /api/v1/auth/validate-reset-token - 리셋 토큰 검증
- [ ] POST /api/v1/auth/reset-password - 비밀번호 리셋 실행
- [ ] GET /actuator/health - Spring Boot Health Check
- [ ] GET /actuator/info - 애플리케이션 정보

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

#### Account Security Policies (계정 보안 정책)

**정책 관리 (Policy Management):**
- [ ] SecurityPolicy 엔티티 - id, policyType, enabled, config (JSON), orgId, active
- [ ] SecurityPolicyType enum - INACTIVE_LOCKOUT, FAILED_ATTEMPTS_LOCKOUT, IP_BLACKLIST, IP_WHITELIST, IP_RATE_LIMIT
- [ ] 정책 활성화/비활성화 - enabled 플래그로 관리
- [ ] 조직별 정책 설정 - orgId 기준 (null = 글로벌 정책)
- [ ] 정책 우선순위 - 조직 정책 > 글로벌 정책
- [ ] 정책 설정 조회 - getPolicyConfig(policyType, orgId)
- [ ] 정책 활성 여부 확인 - isPolicyEnabled(policyType, orgId)
- [ ] 정책 생성/수정 - 관리자 권한 필요 (ROLE_ADMIN)
- [ ] 정책 적용 범위 - LOCAL 로그인 방식에만 적용 (AD, SSO는 제외)
- [ ] 정책 감사 로그 - 정책 생성/수정/삭제 시 감사 로그 기록

**비활성 계정 잠금 정책 (선택적 적용):**
- [ ] 비활성 계정 감지 - 최근 로그인 N일 경과 시 자동 잠금 (기본값: 365일)
- [ ] 비활성 기간 설정 - config.inactiveDays 필드 (30/90/180/365일)
- [ ] 비활성 계정 잠금 - 계정 상태 ACTIVE → LOCKED_INACTIVE로 변경
- [ ] 비활성 계정 잠금 - lockedReason = "INACTIVE_{N}_DAYS" 기록
- [ ] 비활성 계정 잠금 - lockedAt 타임스탬프 기록
- [ ] 비활성 계정 잠금 해제 - 관리자 승인 필요 (Self-service 불가)
- [ ] 비활성 계정 잠금 해제 - 해제 시 비밀번호 즉시 변경 강제
- [ ] 비활성 계정 잠금 - 감사 로그 기록 (action: ACCOUNT_LOCKED, reason: INACTIVE)
- [ ] 정책 비활성화 시 - 기존 잠금된 계정 자동 해제하지 않음 (관리자 수동 처리)
- [ ] 배치 작업 - 매일 자정 실행, 비활성 계정 검사 및 잠금

**실패 로그인 횟수 잠금 정책 (선택적 적용):**
- [ ] 정책 설정 - config.thresholds 배열: [{attempts: 5, lockMinutes: 5}, {attempts: 10, lockMinutes: 30}, {attempts: 15, lockMinutes: null}]
- [ ] 임계값 커스터마이징 - 조직별 실패 횟수 및 잠금 시간 설정 가능
- [ ] 로그인 실패 카운트 - 실패 시 failedLoginAttempts 증가
- [ ] 로그인 실패 카운트 - 성공 시 failedLoginAttempts 리셋 (0으로)
- [ ] 로그인 실패 카운트 - lastFailedLoginAt 타임스탬프 업데이트
- [ ] 계정 잠금 (임계값 도달) - 계정 상태 ACTIVE → LOCKED_FAILED_ATTEMPTS로 변경
- [ ] 계정 잠금 (임계값 도달) - lockedUntil = now + lockMinutes 설정
- [ ] 계정 잠금 (임계값 도달) - lockedReason = "FAILED_ATTEMPTS_{N}" 기록
- [ ] 계정 잠금 (영구 잠금) - lockMinutes = null인 임계값 도달 시 영구 잠금
- [ ] 계정 잠금 (영구 잠금) - lockedUntil = null 설정
- [ ] 계정 잠금 해제 - lockedUntil 시간 경과 후 자동 해제 (시간 제한 잠금)
- [ ] 계정 잠금 해제 - 해제 시 failedLoginAttempts 리셋하지 않음 (추적 유지)
- [ ] 계정 잠금 해제 - 관리자 수동 해제 가능 (강제 해제 버튼)
- [ ] 계정 잠금 - 감사 로그 기록 (action: ACCOUNT_LOCKED, reason: FAILED_ATTEMPTS, attempts: N)
- [ ] 정책 비활성화 시 - 로그인 실패 카운트는 계속 유지 (추적 목적)

**계정 상태 관리:**
- [ ] AccountStatus.ACTIVE - 정상 활성 계정
- [ ] AccountStatus.LOCKED_INACTIVE - 비활성으로 인한 잠금
- [ ] AccountStatus.LOCKED_FAILED_ATTEMPTS - 로그인 실패로 인한 잠금
- [ ] AccountStatus.LOCKED_ADMIN - 관리자에 의한 수동 잠금
- [ ] AccountStatus.EXPIRED - 계정 만료 (유효기간 경과)
- [ ] AccountStatus.DISABLED - 비활성화 (퇴사자 등)
- [ ] AccountStatus.DELETED - 삭제됨 (Soft delete)
- [ ] 상태 전이 - ACTIVE → LOCKED_* (잠금 이벤트)
- [ ] 상태 전이 - LOCKED_* → ACTIVE (잠금 해제)
- [ ] 상태 전이 - ACTIVE → DISABLED (관리자 비활성화)
- [ ] 상태 전이 - ACTIVE → EXPIRED (만료일 경과)
- [ ] 상태 전이 - * → DELETED (삭제, 되돌릴 수 없음)
- [ ] 잠금된 계정 로그인 시도 - AccountLockedException 예외 발생
- [ ] 잠금된 계정 로그인 시도 - 남은 잠금 시간 응답 (lockedUntil - now)
- [ ] 비활성화된 계정 로그인 시도 - AccountDisabledException 예외 발생
- [ ] 만료된 계정 로그인 시도 - AccountExpiredException 예외 발생

**IP 블랙리스트 정책 (선택적 적용):**
- [ ] 정책 설정 - config.failureThreshold (기본값: 30), config.blockHours (기본값: 24)
- [ ] IP 블랙리스트 등록 - 동일 IP에서 N회 이상 실패 시 등록
- [ ] IP 블랙리스트 저장 - IP_BLACKLIST 테이블 (ip, blockedAt, expiresAt, reason)
- [ ] IP 블랙리스트 차단 - 블랙리스트 IP 로그인 시도 즉시 403 응답
- [ ] IP 블랙리스트 해제 - expiresAt 시간 경과 후 자동 해제
- [ ] IP 블랙리스트 해제 - 관리자 수동 해제 가능
- [ ] IP 블랙리스트 감사 로그 - 등록/해제 시 감사 로그 기록
- [ ] Redis 캐싱 - 블랙리스트 IP 캐싱으로 성능 최적화

**IP 화이트리스트 정책 (선택적 적용):**
- [ ] 정책 설정 - config.allowedIpRanges 배열 (CIDR 표기법)
- [ ] IP 화이트리스트 저장 - IP_WHITELIST 테이블 (ipRange, description, orgId)
- [ ] IP 화이트리스트 검증 - CIDR 범위 내 IP 확인
- [ ] IP 화이트리스트 제외 - 화이트리스트 IP는 실패 잠금 정책 제외
- [ ] IP 화이트리스트 제외 - 화이트리스트 IP는 블랙리스트 등록 제외
- [ ] IP 화이트리스트 관리 - 관리자만 추가/수정/삭제 가능

**IP Rate Limiting 정책 (선택적 적용):**
- [ ] 정책 설정 - config.maxRequestsPerMinute (기본값: 10)
- [ ] Rate Limit 카운터 - Redis 기반 카운터 (키: "rate_limit:ip:{ip}")
- [ ] Rate Limit TTL - 1분 자동 만료
- [ ] Rate Limit 초과 - 429 Too Many Requests 응답
- [ ] Rate Limit 헤더 - X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset 헤더 포함
- [ ] Rate Limit 감사 로그 - 초과 시 감사 로그 기록

#### Password Management Policies (비밀번호 관리 정책)

**비밀번호 정책 유형:**
- [ ] PasswordPolicyType enum - EXPIRATION, COMPLEXITY, HISTORY
- [ ] 정책별 독립 활성화 - 각 정책 개별적으로 enabled/disabled 설정 가능
- [ ] LOCAL 로그인 전용 - 비밀번호 정책은 LOCAL 로그인에만 적용

**비밀번호 만료 정책 (선택적 적용):**
- [ ] 정책 설정 - config.expirationDays (기본값: 90, 옵션: 30/60/90/180/365)
- [ ] 비밀번호 만료 - 비밀번호 생성/변경 후 N일 경과 시 만료
- [ ] 비밀번호 만료 - passwordExpiresAt 필드로 관리
- [ ] 비밀번호 만료 - 만료 후 로그인 시 PasswordExpiredException 예외
- [ ] 비밀번호 만료 - 만료 후 즉시 변경 페이지로 리다이렉트
- [ ] 비밀번호 만료 - 변경 완료 전 다른 페이지 접근 차단
- [ ] 비밀번호 만료 경고 - 로그인 시 만료까지 남은 일수 UI 표시
- [ ] 비밀번호 만료 경고 - 로그인 응답에 daysUntilExpiration 필드 포함
- [ ] 정책 비활성화 시 - passwordExpiresAt은 유지하지만 검증 스킵

**비밀번호 복잡도 정책 (선택적 적용):**
- [ ] 정책 설정 - config: {minLength, maxLength, requireUppercase, requireLowercase, requireDigit, requireSpecial}
- [ ] 비밀번호 복잡도 - 최소 길이 (기본값: 8, 조정 가능)
- [ ] 비밀번호 복잡도 - 최대 길이 (기본값: 100)
- [ ] 비밀번호 복잡도 - 영문 대문자 포함 여부 (기본값: true)
- [ ] 비밀번호 복잡도 - 영문 소문자 포함 여부 (기본값: true)
- [ ] 비밀번호 복잡도 - 숫자 포함 여부 (기본값: true)
- [ ] 비밀번호 복잡도 - 특수문자 포함 여부 (기본값: true, 허용 문자: !@#$%^&*()_+-=[]{}|;:,.<>?)
- [ ] 비밀번호 복잡도 - 연속된 문자 3개 이상 금지 (abc, 123, aaa)
- [ ] 비밀번호 복잡도 - 사원ID 포함 금지
- [ ] 비밀번호 복잡도 - 사용자 이름 포함 금지
- [ ] 비밀번호 복잡도 - 일반적인 단어 금지 (password, admin, 123456 등)
- [ ] 비밀번호 복잡도 - 공백 문자 금지
- [ ] 비밀번호 복잡도 검증 - PasswordComplexityValidator 클래스
- [ ] 비밀번호 복잡도 검증 - 검증 실패 시 구체적인 오류 메시지 반환
- [ ] 정책 비활성화 시 - 복잡도 검증 스킵

**비밀번호 히스토리 정책 (선택적 적용):**
- [ ] 정책 설정 - config.historyCount (기본값: 5, 옵션: 3/5/10)
- [ ] 비밀번호 히스토리 - 최근 N개 비밀번호 재사용 금지
- [ ] 비밀번호 히스토리 - PASSWORD_HISTORY 테이블에 BCrypt 해시 저장
- [ ] 비밀번호 히스토리 - userId, passwordHash, createdAt 필드
- [ ] 비밀번호 히스토리 - 비밀번호 변경 시 자동 저장
- [ ] 비밀번호 히스토리 - 새 비밀번호 설정 시 최근 N개와 비교
- [ ] 비밀번호 히스토리 - 재사용 감지 시 PasswordReusedException 예외
- [ ] 비밀번호 히스토리 - N+1번째 이전 비밀번호는 재사용 허용
- [ ] 비밀번호 히스토리 - 파티셔닝 전략 (userId 기준)
- [ ] 정책 비활성화 시 - 히스토리 저장은 계속하지만 검증 스킵

**강제 비밀번호 변경:**
- [ ] 강제 변경 - 최초 로그인 시 비밀번호 변경 필수
- [ ] 강제 변경 - mustChangePassword = true 플래그
- [ ] 강제 변경 - 관리자가 비밀번호 리셋 시 mustChangePassword = true 설정
- [ ] 강제 변경 - 비밀번호 만료 후 로그인 시 강제 변경
- [ ] 강제 변경 - 보안 정책 변경 시 전체 사용자 강제 변경 (관리자 트리거)
- [ ] 강제 변경 - 데이터 유출 의심 시 특정 사용자 강제 변경
- [ ] 강제 변경 완료 전 - API 접근 차단 (비밀번호 변경 API 제외)
- [ ] 강제 변경 완료 전 - 로그인 성공하지만 즉시 변경 페이지로 리다이렉트
- [ ] 강제 변경 완료 후 - mustChangePassword = false 설정

**비밀번호 변경 프로세스:**
- [ ] 비밀번호 변경 - 현재 비밀번호 검증 필수
- [ ] 비밀번호 변경 - 활성화된 정책에 따라 검증 (복잡도, 히스토리, 만료)
- [ ] 비밀번호 변경 - 새 비밀번호 현재 비밀번호와 동일 금지
- [ ] 비밀번호 변경 - BCrypt로 해시 (strength=10)
- [ ] 비밀번호 변경 - User.password 업데이트
- [ ] 비밀번호 변경 - User.passwordChangedAt 타임스탬프 업데이트
- [ ] 비밀번호 변경 - User.passwordExpiresAt = now + expirationDays 설정 (만료 정책 활성화 시)
- [ ] 비밀번호 변경 - PASSWORD_HISTORY 테이블에 이전 비밀번호 저장 (히스토리 정책 활성화 시)
- [ ] 비밀번호 변경 - 모든 Refresh Token 무효화 (재로그인 필요)
- [ ] 비밀번호 변경 - 감사 로그 기록 (action: PASSWORD_CHANGED, userId)
- [ ] 비밀번호 변경 실패 - 현재 비밀번호 불일치 시 InvalidPasswordException
- [ ] 비밀번호 변경 실패 - 복잡도 미달 시 PasswordComplexityException (정책 활성화 시)
- [ ] 비밀번호 변경 실패 - 재사용 감지 시 PasswordReusedException (정책 활성화 시)

**비밀번호 변경 API:**
- [ ] POST /api/v1/auth/change-password - 비밀번호 변경
- [ ] Request DTO - currentPassword, newPassword, newPasswordConfirm
- [ ] Response DTO - success, message, passwordExpiresAt
- [ ] 인증 필수 - JWT Access Token 필요
- [ ] Rate Limiting - 동일 사용자 1분당 3회 제한

#### Password Reset Policies (비밀번호 초기화 정책)

**리셋 방식 (Reset Methods):**
- [ ] 사용자 셀프 리셋 - 이메일 기반 토큰 리셋 (LOCAL 계정만 해당)
- [ ] 관리자 리셋 - 관리자가 임시 비밀번호 발급 (모든 계정 유형)
- [ ] 리셋 적용 범위 - LOCAL 계정만 비밀번호 리셋 가능 (AD, SSO는 각 시스템에서 처리)

**사용자 셀프 리셋 - 토큰 생성 (Email-Based Reset Token):**
- [ ] 리셋 요청 - 사용자 이메일 주소 입력 (사내 메일 주소)
- [ ] 이메일 검증 - User.email 존재 여부 확인
- [ ] 이메일 검증 - 계정 상태 확인 (ACTIVE 또는 LOCKED_* 상태만 리셋 가능)
- [ ] 이메일 검증 - 계정 유형 확인 (LOCAL 계정만 리셋 가능, AD/SSO는 거부)
- [ ] 토큰 생성 - SecureRandom 64자 (영문 대소문자 + 숫자)
- [ ] 토큰 해시 - SHA-256 해시 후 PASSWORD_RESET_TOKEN 테이블 저장
- [ ] 토큰 저장 - userId, tokenHash, expiresAt (1시간), createdAt, used (false)
- [ ] 토큰 만료 - 1시간 후 자동 만료
- [ ] 이메일 발송 - 리셋 링크 포함 (https://app.example.com/reset-password?token=xxx)
- [ ] 이메일 발송 - 유효 기간 명시 (1시간)
- [ ] 이메일 발송 - 요청하지 않았을 경우 무시하라는 안내 포함
- [ ] 이메일 발송 실패 - 사용자에게 에러 메시지 표시 (이메일 발송 실패)
- [ ] 존재하지 않는 이메일 - 보안상 동일한 성공 메시지 반환 (이메일 유무 노출 방지)

**사용자 셀프 리셋 - 토큰 검증 (Token Validation):**
- [ ] 토큰 검증 - 토큰 존재 여부 확인 (PASSWORD_RESET_TOKEN 테이블 조회)
- [ ] 토큰 검증 - 토큰 해시 일치 확인 (SHA-256 해시 비교)
- [ ] 토큰 검증 - 토큰 만료 확인 (expiresAt < now → ExpiredResetTokenException)
- [ ] 토큰 검증 - 토큰 사용 여부 확인 (used = true → UsedResetTokenException)
- [ ] 토큰 검증 - 사용자 계정 상태 확인 (DELETED, SUSPENDED → InvalidResetTokenException)
- [ ] 토큰 검증 성공 - 비밀번호 변경 페이지로 이동
- [ ] 토큰 검증 실패 - 에러 페이지 표시 (토큰 무효/만료/이미 사용됨)

**사용자 셀프 리셋 - 비밀번호 변경 (Password Reset Execution):**
- [ ] 비밀번호 변경 - 유효한 토큰으로만 변경 가능
- [ ] 비밀번호 변경 - 새 비밀번호 입력 및 확인
- [ ] 비밀번호 변경 - 비밀번호 복잡도 검증 (복잡도 정책 활성화 시)
- [ ] 비밀번호 변경 - 비밀번호 히스토리 검증 (히스토리 정책 활성화 시)
- [ ] 비밀번호 변경 - BCrypt로 해시 후 User.password 업데이트
- [ ] 비밀번호 변경 - User.passwordChangedAt 타임스탬프 업데이트
- [ ] 비밀번호 변경 - User.passwordExpiresAt = now + expirationDays 설정 (만료 정책 활성화 시)
- [ ] 비밀번호 변경 - User.failedLoginAttempts = 0 리셋
- [ ] 비밀번호 변경 - 계정 잠금 상태면 해제 (LOCKED_* → ACTIVE)
- [ ] 비밀번호 변경 - PASSWORD_HISTORY 테이블에 이전 비밀번호 저장 (히스토리 정책 활성화 시)
- [ ] 비밀번호 변경 - 토큰 사용 처리 (PASSWORD_RESET_TOKEN.used = true, usedAt = now)
- [ ] 비밀번호 변경 - 모든 Refresh Token 무효화 (보안 강화)
- [ ] 비밀번호 변경 - 감사 로그 기록 (action: PASSWORD_RESET, userId)
- [ ] 비밀번호 변경 성공 - 로그인 페이지로 리다이렉트

**사용자 셀프 리셋 - Rate Limiting:**
- [ ] 이메일별 Rate Limit - 동일 이메일 1분당 1회 리셋 요청 제한
- [ ] 이메일별 Rate Limit - 동일 이메일 1시간당 3회 리셋 요청 제한
- [ ] 이메일별 Rate Limit - 동일 이메일 24시간당 5회 리셋 요청 제한
- [ ] IP별 Rate Limit - 동일 IP 1분당 3회 리셋 요청 제한
- [ ] IP별 Rate Limit - 동일 IP 1시간당 10회 리셋 요청 제한
- [ ] Rate Limit 저장 - Redis 카운터 사용 (키: "reset_limit:email:{email}", "reset_limit:ip:{ip}")
- [ ] Rate Limit 초과 - 429 Too Many Requests 반환
- [ ] Rate Limit 초과 - 재시도 가능 시간 헤더 포함 (Retry-After)

**사용자 셀프 리셋 API:**
- [ ] POST /api/v1/auth/request-reset - 비밀번호 리셋 요청
- [ ] Request DTO - email (사내 메일 주소)
- [ ] Response DTO - success, message ("이메일을 확인하세요")
- [ ] 인증 불필요 - 공개 API (누구나 호출 가능)
- [ ] GET /api/v1/auth/validate-reset-token?token=xxx - 토큰 검증
- [ ] Response DTO - valid (true/false), userId (토큰 유효 시만), errorCode
- [ ] 인증 불필요 - 공개 API
- [ ] POST /api/v1/auth/reset-password - 비밀번호 변경
- [ ] Request DTO - token, newPassword, confirmPassword
- [ ] Response DTO - success, message
- [ ] 인증 불필요 - 공개 API (토큰으로 인증)

**관리자 비밀번호 리셋 (Admin Reset):**
- [ ] 관리자 리셋 - 관리자 권한 확인 (ROLE_ADMIN 또는 ROLE_USER_MANAGER)
- [ ] 관리자 리셋 - 대상 사용자 선택 (userId 또는 employeeId 기준)
- [ ] 관리자 리셋 - 임시 비밀번호 생성 (SecureRandom, 12자)
- [ ] 관리자 리셋 - 임시 비밀번호 복잡도 자동 만족 (대문자+소문자+숫자+특수문자)
- [ ] 관리자 리셋 - BCrypt로 해시 후 User.password 업데이트
- [ ] 관리자 리셋 - User.mustChangePassword = true 설정
- [ ] 관리자 리셋 - User.passwordChangedAt 타임스탬프 업데이트
- [ ] 관리자 리셋 - User.passwordExpiresAt = now + expirationDays 설정 (만료 정책 활성화 시)
- [ ] 관리자 리셋 - User.failedLoginAttempts = 0 리셋
- [ ] 관리자 리셋 - 계정 잠금 상태면 해제 (LOCKED_* → ACTIVE)
- [ ] 관리자 리셋 - PASSWORD_HISTORY 테이블에 이전 비밀번호 저장 (히스토리 정책 활성화 시)
- [ ] 관리자 리셋 - 모든 Refresh Token 무효화
- [ ] 관리자 리셋 - 감사 로그 기록 (action: ADMIN_PASSWORD_RESET, adminId, targetUserId)
- [ ] 관리자 리셋 - Self-reset 금지 (관리자 자신 리셋 불가)

**관리자 리셋 API:**
- [ ] POST /api/v1/admin/users/{userId}/reset-password - 비밀번호 리셋
- [ ] Request DTO - (없음, userId는 path parameter)
- [ ] Response DTO - success, temporaryPassword (평문, 1회만 표시)
- [ ] 인증 필수 - JWT Access Token + ROLE_ADMIN 권한
- [ ] Rate Limiting - 동일 관리자 1분당 5회 제한

**임시 비밀번호 전달:**
- [ ] 임시 비밀번호 표시 - API 응답에만 1회 포함
- [ ] 임시 비밀번호 표시 - UI에서 복사 버튼 제공
- [ ] 임시 비밀번호 표시 - 관리자가 사용자에게 직접 전달 (오프라인 또는 메신저)
- [ ] 임시 비밀번호 보안 - DB에 평문 저장 금지 (BCrypt 해시만 저장)
- [ ] 임시 비밀번호 보안 - 로그에 평문 기록 금지

**리셋 후 최초 로그인 (관리자 리셋 후):**
- [ ] 최초 로그인 - mustChangePassword = true 확인
- [ ] 최초 로그인 - 임시 비밀번호로 로그인 성공
- [ ] 최초 로그인 - 로그인 후 즉시 비밀번호 변경 페이지로 리다이렉트
- [ ] 최초 로그인 - 비밀번호 변경 완료 전 다른 API 호출 차단 (401 Unauthorized)
- [ ] 최초 로그인 - 비밀번호 변경 완료 후 mustChangePassword = false 설정
- [ ] 최초 로그인 - 비밀번호 변경 후 정상 시스템 접근 가능

**PASSWORD_RESET_TOKEN 테이블:**
- [ ] 테이블 구조 - id (ULID), userId (FK), tokenHash (SHA-256), expiresAt, createdAt, used, usedAt
- [ ] 인덱스 - userId, tokenHash (조회 성능)
- [ ] 정리 작업 - 만료된 토큰 정기 삭제 (배치 작업, 7일 이상 경과)
- [ ] 사용 제한 - 1회 사용 후 즉시 used = true 설정

**리셋 테스트 시나리오:**
- [ ] 시나리오 1: 사용자 이메일로 리셋 요청 → 이메일 수신 → 토큰 검증 → 새 비밀번호 설정 → 로그인 성공
- [ ] 시나리오 2: 만료된 토큰으로 리셋 시도 → ExpiredResetTokenException
- [ ] 시나리오 3: 이미 사용된 토큰으로 재시도 → UsedResetTokenException
- [ ] 시나리오 4: 존재하지 않는 이메일로 요청 → 동일한 성공 메시지 반환 (보안)
- [ ] 시나리오 5: Rate Limit 초과 (동일 이메일 1분 내 2회 요청) → 429 Too Many Requests
- [ ] 시나리오 6: 관리자 임시 비밀번호 발급 → 사용자 최초 로그인 → 강제 변경 → 정상 접근
- [ ] 시나리오 7: 잠긴 계정 리셋 (이메일/관리자) → 계정 잠금 해제 → 로그인 성공
- [ ] 시나리오 8: Self-reset 시도 (관리자 리셋) → 403 Forbidden (관리자가 자신 리셋 불가)
- [ ] 시나리오 9: 권한 없는 사용자 관리자 리셋 시도 → 403 Forbidden
- [ ] 시나리오 10: AD 계정으로 이메일 리셋 요청 → 거부 (LOCAL 계정만 가능)

#### Authentication - Health Check & Fallback

**Health Check:**
- [ ] Health Check - 모든 서버 정상 (SSO, AD, Local)
- [ ] Health Check - SSO 서버만 장애
- [ ] Health Check - AD 서버만 장애
- [ ] Health Check - SSO, AD 모두 장애 (Local만 가능)
- [ ] Health Check - 응답 시간 측정 (ms 단위)
- [ ] Health Check - 권장 로그인 방식 반환 (기본: SSO, 장애 시 다음 우선순위)
- [ ] Health Check - 인증 불필요 (GET /actuator/health)

**Fallback 로직 (모든 로그인 방식 사용 가정):**
- [ ] Fallback - 기본 우선순위: SSO > AD > LOCAL
- [ ] Fallback - SSO 장애 시 AD로 자동 전환
- [ ] Fallback - SSO, AD 장애 시 LOCAL로 자동 전환
- [ ] Fallback - 장애 복구 시 원래 우선순위로 복귀 (SSO 최우선)
- [ ] Fallback - 사용자 명시적 선택 시 Fallback 무시 (선택한 방식만 시도)
- [ ] Fallback - Fallback 시도 감사 로그 기록
- [ ] Fallback - 비인증 사용자는 항상 SSO로 먼저 리다이렉트

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

#### Audit Logging - Login Success (로그인 성공)

**정상 케이스:**
- [ ] AD 로그인 성공 - 감사 로그 생성 (action: LOGIN_SUCCESS, method: AD)
- [ ] SSO 로그인 성공 - 감사 로그 생성 (action: LOGIN_SUCCESS, method: SSO)
- [ ] 일반 로그인 성공 - 감사 로그 생성 (action: LOGIN_SUCCESS, method: LOCAL)
- [ ] 로그인 성공 감사 로그 - 필수 필드 포함 (userId, employeeId, username, orgId)
- [ ] 로그인 성공 감사 로그 - IP 주소 기록 (clientIp, X-Forwarded-For 헤더)
- [ ] 로그인 성공 감사 로그 - User-Agent 기록 (브라우저, OS 정보)
- [ ] 로그인 성공 감사 로그 - 세션 ID 기록 (sessionId)
- [ ] 로그인 성공 감사 로그 - 타임스탬프 기록 (loginAt, UTC)
- [ ] 로그인 성공 감사 로그 - 발급된 토큰 ID 기록 (accessTokenId, refreshTokenId)
- [ ] 로그인 성공 감사 로그 - Repository save 호출 확인

**로그 상세 정보:**
- [ ] 로그인 성공 - details JSON 포함 (roles, permissions, orgName)
- [ ] 로그인 성공 - 이전 로그인 시간 기록 (lastLoginAt)
- [ ] 로그인 성공 - 로그인 횟수 증가 (loginCount++)
- [ ] 로그인 성공 - 지역 정보 기록 (GeoIP lookup, 선택사항)
- [ ] 로그인 성공 - Referer 헤더 기록 (loginReferrer)

**보안 정보:**
- [ ] 로그인 성공 - 이전 실패 횟수 리셋 (failedAttempts = 0)
- [ ] 로그인 성공 - 계정 잠금 해제 (lockedUntil = null)
- [ ] 로그인 성공 - 의심스러운 로그인 감지 (새로운 IP, 새로운 디바이스)
- [ ] 로그인 성공 - 비정상 시간대 로그인 감지 (근무시간 외)
- [ ] 로그인 성공 - 다중 지역 로그인 감지 (지리적으로 불가능한 위치)

#### Audit Logging - Login Failure (로그인 실패)

**정상 케이스:**
- [ ] AD 로그인 실패 - 감사 로그 생성 (action: LOGIN_FAILURE, method: AD)
- [ ] SSO 로그인 실패 - 감사 로그 생성 (action: LOGIN_FAILURE, method: SSO)
- [ ] 일반 로그인 실패 - 감사 로그 생성 (action: LOGIN_FAILURE, method: LOCAL)
- [ ] 로그인 실패 감사 로그 - 실패 사유 기록 (reason: INVALID_CREDENTIALS)
- [ ] 로그인 실패 감사 로그 - 입력된 사원ID 기록 (attemptedEmployeeId)
- [ ] 로그인 실패 감사 로그 - IP 주소 기록
- [ ] 로그인 실패 감사 로그 - User-Agent 기록
- [ ] 로그인 실패 감사 로그 - 타임스탬프 기록
- [ ] 로그인 실패 감사 로그 - 실패 횟수 증가 (failedAttempts++)
- [ ] 로그인 실패 감사 로그 - Repository save 호출 확인

**실패 사유 분류:**
- [ ] 로그인 실패 - INVALID_CREDENTIALS (잘못된 사원ID 또는 비밀번호)
- [ ] 로그인 실패 - ACCOUNT_DISABLED (비활성화된 계정)
- [ ] 로그인 실패 - ACCOUNT_LOCKED (잠긴 계정)
- [ ] 로그인 실패 - ACCOUNT_EXPIRED (만료된 계정)
- [ ] 로그인 실패 - CREDENTIALS_EXPIRED (비밀번호 만료)
- [ ] 로그인 실패 - AD_SERVER_UNAVAILABLE (AD 서버 장애)
- [ ] 로그인 실패 - SSO_SERVER_UNAVAILABLE (SSO 서버 장애)
- [ ] 로그인 실패 - INVALID_SSO_TOKEN (잘못된 SSO 토큰)
- [ ] 로그인 실패 - BRUTE_FORCE_DETECTED (무차별 대입 공격 감지)

**보안 알림:**
- [ ] 로그인 실패 - 5회 실패 시 경고 로그 (WARNING level)
- [ ] 로그인 실패 - 10회 실패 시 위험 로그 (ERROR level)
- [ ] 로그인 실패 - 계정 잠금 시 알림 로그 (accountLocked: true)
- [ ] 로그인 실패 - IP 블랙리스트 등록 시 알림
- [ ] 로그인 실패 - 관리자 알림 이벤트 발행 (10회 이상)

#### Audit Logging - Token Operations (토큰 작업)

**Token Refresh:**
- [ ] Token Refresh 성공 - 감사 로그 생성 (action: TOKEN_REFRESH_SUCCESS)
- [ ] Token Refresh 성공 - 이전 토큰 ID 기록 (oldRefreshTokenId)
- [ ] Token Refresh 성공 - 새 토큰 ID 기록 (newAccessTokenId, newRefreshTokenId)
- [ ] Token Refresh 성공 - IP 주소 및 User-Agent 기록
- [ ] Token Refresh 실패 - 감사 로그 생성 (action: TOKEN_REFRESH_FAILURE)
- [ ] Token Refresh 실패 - 실패 사유 기록 (reason: EXPIRED_TOKEN, INVALID_TOKEN, TOKEN_REUSED)

**Token Revocation:**
- [ ] 로그아웃 - 감사 로그 생성 (action: LOGOUT)
- [ ] 로그아웃 - 무효화된 토큰 ID 기록 (revokedTokenIds)
- [ ] 로그아웃 - 로그아웃 사유 기록 (reason: USER_REQUESTED, FORCED, PASSWORD_CHANGED)
- [ ] 강제 로그아웃 - 감사 로그 생성 (action: FORCED_LOGOUT)
- [ ] 강제 로그아웃 - 관리자 정보 기록 (adminUserId, adminEmployeeId)
- [ ] 강제 로그아웃 - 강제 사유 기록 (reason)

#### Audit Logging - Security Events (보안 이벤트)

**의심스러운 활동:**
- [ ] 의심스러운 로그인 - 새로운 IP 주소 (action: SUSPICIOUS_LOGIN_NEW_IP)
- [ ] 의심스러운 로그인 - 새로운 디바이스 (action: SUSPICIOUS_LOGIN_NEW_DEVICE)
- [ ] 의심스러운 로그인 - 비정상 시간대 (action: SUSPICIOUS_LOGIN_UNUSUAL_TIME)
- [ ] 의심스러운 로그인 - 다중 지역 (action: SUSPICIOUS_LOGIN_IMPOSSIBLE_TRAVEL)
- [ ] 의심스러운 활동 - details JSON에 이전 로그인 정보 포함

**공격 감지:**
- [ ] Brute Force 공격 - 감사 로그 생성 (action: BRUTE_FORCE_DETECTED)
- [ ] Brute Force 공격 - 공격 패턴 기록 (attemptCount, timeWindow)
- [ ] SQL Injection 시도 - 감사 로그 생성 (action: SQL_INJECTION_ATTEMPT)
- [ ] SQL Injection 시도 - 입력값 기록 (sanitized)
- [ ] XSS 시도 - 감사 로그 생성 (action: XSS_ATTEMPT)
- [ ] XSS 시도 - 입력값 기록 (sanitized)
- [ ] CSRF 공격 - 감사 로그 생성 (action: CSRF_ATTACK_DETECTED)
- [ ] CSRF 공격 - State 파라미터 변조 기록

**계정 보안:**
- [ ] 계정 잠금 - 감사 로그 생성 (action: ACCOUNT_LOCKED)
- [ ] 계정 잠금 - 잠금 사유 기록 (reason: FAILED_ATTEMPTS, ADMIN_ACTION)
- [ ] 계정 잠금 - 잠금 기간 기록 (lockedUntil)
- [ ] 계정 해제 - 감사 로그 생성 (action: ACCOUNT_UNLOCKED)
- [ ] 비밀번호 변경 - 감사 로그 생성 (action: PASSWORD_CHANGED)
- [ ] 비밀번호 변경 - 변경 사유 기록 (reason: USER_REQUESTED, EXPIRED, ADMIN_RESET)

#### Audit Logging - Data Structure (데이터 구조)

**필수 필드:**
- [ ] AuditLog.id - ULID (고유 식별자)
- [ ] AuditLog.action - 작업 타입 (LOGIN_SUCCESS, LOGIN_FAILURE, etc.)
- [ ] AuditLog.userId - 사용자 ID (로그인 성공 시, nullable)
- [ ] AuditLog.employeeId - 사원ID (항상 기록)
- [ ] AuditLog.username - 사용자명 (nullable)
- [ ] AuditLog.timestamp - UTC 타임스탬프
- [ ] AuditLog.clientIp - 클라이언트 IP 주소
- [ ] AuditLog.success - 성공 여부 (true/false)

**선택 필드:**
- [ ] AuditLog.method - 인증 방법 (AD, SSO, LOCAL)
- [ ] AuditLog.sessionId - 세션 ID
- [ ] AuditLog.userAgent - User-Agent 문자열
- [ ] AuditLog.referer - Referer 헤더
- [ ] AuditLog.reason - 실패/특이 사유
- [ ] AuditLog.details - JSON 상세 정보
- [ ] AuditLog.orgId - 조직 ID
- [ ] AuditLog.orgName - 조직명

**Details JSON 구조:**
- [ ] details.roles - 역할 리스트
- [ ] details.permissions - 권한 리스트
- [ ] details.lastLoginAt - 이전 로그인 시간
- [ ] details.loginCount - 로그인 횟수
- [ ] details.failedAttempts - 실패 횟수
- [ ] details.geoLocation - 지역 정보 (country, city, lat, lng)
- [ ] details.device - 디바이스 정보 (browser, os, device)
- [ ] details.tokenIds - 토큰 ID 리스트

#### Audit Logging - Repository & Queries (저장소 및 쿼리)

**저장:**
- [ ] AuditLog 저장 - Repository.save() 호출
- [ ] AuditLog 저장 - 비동기 처리 (@Async)
- [ ] AuditLog 저장 - 실패 시 로그만 출력 (메인 플로우 방해 안 함)
- [ ] AuditLog 저장 - 배치 저장 최적화 (10초마다 flush)

**조회:**
- [ ] AuditLog 조회 - 사용자별 조회 (userId)
- [ ] AuditLog 조회 - 사원ID별 조회 (employeeId)
- [ ] AuditLog 조회 - 날짜 범위 조회 (startDate, endDate)
- [ ] AuditLog 조회 - 작업 타입별 조회 (action)
- [ ] AuditLog 조회 - IP 주소별 조회 (clientIp)
- [ ] AuditLog 조회 - 성공/실패 조회 (success)
- [ ] AuditLog 조회 - 페이징 지원 (page, size)
- [ ] AuditLog 조회 - 정렬 지원 (timestamp DESC)

**통계:**
- [ ] AuditLog 통계 - 일별 로그인 성공 횟수
- [ ] AuditLog 통계 - 일별 로그인 실패 횟수
- [ ] AuditLog 통계 - 사용자별 로그인 횟수
- [ ] AuditLog 통계 - IP별 로그인 시도 횟수
- [ ] AuditLog 통계 - 실패율 계산 (failedCount / totalCount)
- [ ] AuditLog 통계 - 피크 시간대 분석 (시간별 분포)

#### Audit Logging - Retention & Compliance (보관 및 규정 준수)

**보관 정책:**
- [ ] AuditLog 보관 - 최소 5년 (금융 규정)
- [ ] AuditLog 보관 - 삭제 금지 (Soft delete도 금지)
- [ ] AuditLog 보관 - 파티셔닝 (월별 또는 분기별)
- [ ] AuditLog 보관 - 아카이빙 (1년 이상 오래된 로그)
- [ ] AuditLog 보관 - 압축 저장 (아카이브 시)

**무결성:**
- [ ] AuditLog 무결성 - 수정 불가 (Immutable)
- [ ] AuditLog 무결성 - 체크섬 검증 (선택사항)
- [ ] AuditLog 무결성 - 디지털 서명 (선택사항)
- [ ] AuditLog 무결성 - 변조 감지 메커니즘

**규정 준수:**
- [ ] 개인정보보호법 - 비밀번호 평문 기록 금지
- [ ] 개인정보보호법 - IP 주소 가명 처리 (선택사항)
- [ ] FATF 권고사항 - 금융거래 감사 추적
- [ ] 내부감사 - 로그 접근 이력 기록 (AuditLog의 AuditLog)

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
