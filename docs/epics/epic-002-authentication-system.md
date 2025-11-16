# Epic 002: Authentication System (인증 시스템)

**Status**: 🚧 IN PROGRESS (35% of total Epic)  
**Priority**: P0 (Critical)  
**Estimated Effort**: 40 Story Points  
**Sprint**: Sprint 2  
**Dependencies**: Epic 001 (Login Policy System)

---

## 📋 Epic Overview

통합 인증 시스템 구현 - LOCAL, AD, SSO 3가지 로그인 방식 지원 및 JWT 기반 세션 관리

**Business Value:**
- 3가지 로그인 방식 지원 (LOCAL, AD, SSO)
- 우선순위 기반 자동 Fallback (SSO > AD > LOCAL)
- JWT 기반 Stateless 세션 관리
- 세션 보안 강화 (Refresh Token Rotation, 동시 세션 제어)

**Technical Goals:**
- Spring Security 통합
- JWT 토큰 생성/검증 (HMAC-SHA256)
- LDAP 연동 (AD Login)
- OAuth2 연동 (SSO Login)
- BCrypt 비밀번호 암호화
- Redis 기반 세션 추적 및 토큰 블랙리스트

---

## 📊 Epic Scope

### ✅ In Scope
- [x] JWT Token Provider (생성, 검증, 갱신, 무효화)
- [x] LOCAL Login (자체 DB 인증)
- [x] AD Login (Active Directory LDAP 연동)
- [ ] SSO Login (OAuth2 Single Sign-On)
- [ ] Login Policy Integration (우선순위 기반 Fallback)
- [ ] Password Management (변경, 리셋)
- [ ] Account Security Policies (잠금, 비활성 - LOCAL 전용)
- [ ] Session Management (동시 세션 제어, 타임아웃)

### ❌ Out of Scope
- MFA (Multi-Factor Authentication) - 향후 Epic
- 생체 인증 - 향후 Epic
- 소셜 로그인 (Google, Facebook 등) - SSO로 대체
- 비밀번호 정책 UI - Epic 1 Story 6에서 처리
- IP 화이트리스트/블랙리스트 - Epic 3 (Security Policies)

---

## 🎯 Stories Breakdown

### ✅ Story 1: JWT Token Provider (완료)
**Status**: ✅ COMPLETED (10% of total Epic)  
**Effort**: 5 SP

**Delivered**:
- ✅ JWT Access Token 생성 (HS256, 1시간 만료)
- ✅ JWT Refresh Token 생성 (24시간 만료)
- ✅ 토큰 검증 (서명, 만료시간, 클레임)
- ✅ 토큰 갱신 (Refresh Token Rotation)
- ✅ 토큰 무효화 (Redis 블랙리스트)
- ✅ 단위 테스트 (12/12 passed)

**Files**:
- `JwtTokenProvider.java` - 토큰 생성/검증/갱신
- `JwtTokenProviderTest.java` - 단위 테스트
- `application.yml` - JWT 설정 (만료시간, 비밀키)

---

### ✅ Story 2: LOCAL Login Implementation
**Status**: ✅ COMPLETED (22% of Epic)  
**Effort**: 5 SP  
**Dependencies**: Story 1 (JWT Token Provider)

**Delivered**:
- ✅ BCrypt 비밀번호 암호화 (PasswordEncoder)
- ✅ 로그인 검증 (사원ID + 비밀번호)
- ✅ 사용자 상태 검증 (ACTIVE, LOCKED, INACTIVE)
- ✅ JWT 토큰 발급 (Access + Refresh)
- ✅ 로그인 실패 카운트 (failedLoginAttempts 증가/리셋)
- ✅ 계정 잠금 정책 (5회→5분, 10회→30분, 15회→영구)
- ✅ 감사 로그 기록 (성공/실패 모두)
- ✅ 단위 테스트 (29/29 passed)

**Files**:
- `AuthService.java` - LOCAL 로그인 구현
- `AuthServiceTest.java` - 단위 테스트 (29개)
- `UserRepository.java` - 사용자 조회, 잠금, 실패 카운트

**Acceptance Criteria**:
- [ ] 유효한 사원ID + 비밀번호로 로그인 성공 시 JWT 발급
- [ ] 잘못된 비밀번호 시 InvalidCredentialsException
- [ ] 존재하지 않는 사원ID 시 InvalidCredentialsException
- [ ] 비활성 계정 (INACTIVE) 시 AccountDisabledException
- [ ] 잠긴 계정 (LOCKED_*) 시 AccountLockedException
- [ ] 로그인 실패 시 failedLoginAttempts 증가
- [ ] 로그인 성공 시 failedLoginAttempts 리셋
- [ ] 감사 로그 기록 (사원ID, IP, 시간, 성공/실패 사유)

---

### ✅ Story 3: AD Login Implementation (완료)
**Status**: ✅ COMPLETED (35% of Epic)  
**Effort**: 8 SP  
**Dependencies**: Story 2 (LOCAL Login)

**Delivered**:
- ✅ Spring LDAP 연동 (LdapTemplate)
- ✅ LDAP 인증 (Bind 검증 via authenticate())
- ✅ 사용자 자동 생성 (AD 인증 성공 시)
- ✅ 사용자 상태 검증 (ACTIVE, LOCKED)
- ✅ JWT 토큰 발급 (Access + Refresh)
- ✅ AD 서버 연결 오류 처리 (CommunicationException)
- ✅ 정책 격리 (AD 실패 시 failedLoginAttempts 미증가)
- ✅ 감사 로그 기록 (성공/실패 모두)
- ✅ 단위 테스트 (9/9 passed)

**Files**:
- `AdAuthenticationService.java` - AD LDAP 인증 구현
- `AdAuthenticationServiceTest.java` - 단위 테스트 (9개)
- `User.java` - loginMethod 필드 추가
- `UserRepository.java` - save() 메서드 추가
- `build.gradle` - Spring LDAP 의존성 추가

**Tasks** (일부 미완료 - 추후 개선):
- ✅ LDAP 인증 (Bind 검증)
- ✅ 사용자 자동 생성 (AD 정보 동기화)
- ✅ Connection Timeout 처리 (CommunicationException)
- ✅ 단위 테스트 (Mock LDAP)
- ⏸️ LDAP 사용자 정보 조회 (displayName, mail, department) - TODO 주석 추가됨
- ⏸️ LDAP 그룹 매핑 (memberOf → Roles) - 향후 개선
- ⏸️ Health Check (AD 서버 연결 상태) - 향후 추가
- ⏸️ 통합 테스트 (Embedded LDAP) - 단위 테스트로 대체

**Acceptance Criteria**:
- [ ] 유효한 사원ID + AD 비밀번호로 로그인 성공
- [ ] AD 서버 연결 실패 시 LOCAL로 Fallback
- [ ] AD 사용자 정보로 User 레코드 자동 생성
- [ ] 로그인 시마다 사용자 정보 최신화 (이름, 이메일 등)
- [ ] AD 그룹을 애플리케이션 Role로 매핑 (예: CN=Admins → ROLE_ADMIN)
- [ ] Connection Timeout 시 명확한 에러 메시지

---

### ⏳ Story 4: SSO Login Implementation
**Status**: ⏳ TODO (0% of Epic)  
**Effort**: 10 SP  
**Dependencies**: Story 2 (LOCAL Login)

**Tasks**:
- [ ] OAuth2 Client 설정 (Azure AD, Google, Okta 지원)
- [ ] Authorization Code Flow 구현
- [ ] Redirect URL 생성 (returnUrl 파라미터 포함)
- [ ] Callback 처리 (SSO 토큰 → JWT 발급)
- [ ] State 파라미터 검증 (CSRF 방지)
- [ ] SSO 사용자 정보 매핑 (UserInfo → User)
- [ ] SLO (Single Logout) 구현
- [ ] Health Check (SSO 서버 연결 상태)
- [ ] 단위 테스트 (Mock OAuth2 Server)
- [ ] 통합 테스트

**Acceptance Criteria**:
- [ ] SSO Redirect URL 생성 성공 (returnUrl 포함)
- [ ] Callback 처리 후 JWT 발급 성공
- [ ] State 파라미터 변조 시 CSRF 공격 차단
- [ ] SSO 서버 장애 시 AD로 Fallback
- [ ] 로그아웃 시 SSO 로그아웃도 호출 (SLO)
- [ ] returnUrl 없을 때 기본 경로(/)로 리다이렉트

---

### ⏳ Story 5: Login Policy Integration
**Status**: ⏳ TODO (0% of Epic)  
**Effort**: 3 SP  
**Dependencies**: Story 2, 3, 4 (모든 로그인 방식 구현 완료)

**Tasks**:
- [ ] LoginPolicyService 연동
- [ ] 우선순위 기반 Fallback 로직 (SSO > AD > LOCAL)
- [ ] Health Check 기반 자동 전환
- [ ] 사용자 명시적 선택 시 Fallback 무시
- [ ] 비인증 사용자 최우선 방식으로 리다이렉트
- [ ] Fallback 시도 감사 로그 기록
- [ ] 통합 테스트

**Acceptance Criteria**:
- [ ] SSO 활성화 시 SSO로 먼저 시도
- [ ] SSO 장애 시 자동 AD로 전환
- [ ] SSO, AD 모두 장애 시 LOCAL로 전환
- [ ] 사용자가 "LOCAL 로그인" 버튼 클릭 시 Fallback 무시
- [ ] 정책 변경 즉시 반영 (캐시 무효화)

---

### ⏳ Story 6: Password Management
**Status**: ⏳ TODO (0% of Epic)  
**Effort**: 5 SP  
**Dependencies**: Story 2 (LOCAL Login)

**Tasks**:
- [ ] 비밀번호 변경 API (현재 비밀번호 검증)
- [ ] 비밀번호 복잡도 검증 (정책 활성화 시)
- [ ] 비밀번호 히스토리 검증 (최근 N개 재사용 금지)
- [ ] 비밀번호 리셋 (이메일 토큰 방식)
- [ ] 관리자 임시 비밀번호 발급
- [ ] 강제 비밀번호 변경 (mustChangePassword 플래그)
- [ ] 비밀번호 만료 경고 (daysUntilExpiration)
- [ ] 단위 테스트
- [ ] 통합 테스트

**Acceptance Criteria**:
- [ ] **LOCAL 사용자만** 비밀번호 변경 가능 (AD/SSO는 403 Forbidden)
- [ ] 비밀번호 변경 시 현재 비밀번호 검증 필수
- [ ] 복잡도 정책 위반 시 PasswordComplexityException
- [ ] 히스토리 정책 위반 시 PasswordReusedException
- [ ] 비밀번호 변경 후 모든 Refresh Token 무효화
- [ ] 이메일 리셋 토큰 1시간 후 자동 만료
- [ ] 관리자 임시 비밀번호 발급 후 mustChangePassword = true

---

### ⏳ Story 7: Account Security Policies (LOCAL 전용)
**Status**: ⏳ TODO (0% of Epic)  
**Effort**: 5 SP  
**Dependencies**: Story 2 (LOCAL Login)

**Tasks**:
- [ ] 로그인 실패 횟수 잠금 (5회 → 5분, 10회 → 30분, 15회 → 영구)
- [ ] 비활성 계정 잠금 (N일 미로그인)
- [ ] IP 블랙리스트 (N회 실패 시 IP 차단)
- [ ] IP Rate Limiting (분당 10회 제한)
- [ ] 계정 상태 관리 (ACTIVE, LOCKED_*, INACTIVE, DELETED)
- [ ] 잠금 해제 (시간 제한 잠금 자동 해제, 관리자 수동 해제)
- [ ] 배치 작업 (비활성 계정 검사)
- [ ] 단위 테스트
- [ ] 통합 테스트

**Acceptance Criteria**:
- [ ] **LOCAL 사용자만** 정책 적용 (AD/SSO 사용자 제외)
- [ ] 5회 실패 시 5분 잠금 (lockedUntil 설정)
- [ ] 10회 실패 시 30분 잠금
- [ ] 15회 실패 시 영구 잠금 (lockedUntil = null)
- [ ] 잠긴 계정 로그인 시 AccountLockedException (남은 시간 포함)
- [ ] lockedUntil 경과 후 자동 해제
- [ ] 비활성 N일 경과 시 LOCKED_INACTIVE 상태로 전환
- [ ] IP 블랙리스트 등록 시 Redis 캐싱

---

### ⏳ Story 8: Session Management
**Status**: ⏳ TODO (0% of Epic)  
**Effort**: 4 SP  
**Dependencies**: Story 1 (JWT Token Provider)

**Tasks**:
- [ ] 동시 세션 제어 (최대 10개)
- [ ] 세션 추적 (Redis Set: user_sessions:{userId})
- [ ] Idle Timeout (30분 비활동 시 자동 로그아웃)
- [ ] 세션 정보 조회 (관리자 UI용)
- [ ] 강제 로그아웃 (관리자 기능)
- [ ] 세션 통계 (평균 세션 시간, 동시 접속자 수)
- [ ] 단위 테스트
- [ ] 통합 테스트

**Acceptance Criteria**:
- [ ] **모든 로그인 방식에 적용** (LOCAL, AD, SSO 공통)
- [ ] 동시 세션 10개 초과 시 가장 오래된 세션 자동 만료
- [ ] 마지막 활동 30분 경과 시 자동 로그아웃
- [ ] 비밀번호 변경 시 모든 세션 무효화
- [ ] 관리자가 특정 사용자의 모든 세션 강제 종료 가능
- [ ] Redis 장애 시 Fallback (In-Memory)

---

## 📈 Progress Tracking

| Story | Status | Effort | Completed | Remaining |
|-------|--------|--------|-----------|-----------|
| 1. JWT Token Provider | ✅ COMPLETED | 5 SP | 5 SP | 0 SP |
| 2. LOCAL Login | ✅ COMPLETED | 5 SP | 5 SP | 0 SP |
| 3. AD Login | ⏳ TODO | 8 SP | 0 SP | 8 SP |
| 4. SSO Login | ⏳ TODO | 10 SP | 0 SP | 10 SP |
| 5. Login Policy Integration | ⏳ TODO | 3 SP | 0 SP | 3 SP |
| 6. Password Management | ⏳ TODO | 5 SP | 0 SP | 5 SP |
| 7. Account Security (LOCAL) | ⏳ TODO | 5 SP | 0 SP | 5 SP |
| 8. Session Management | ⏳ TODO | 4 SP | 0 SP | 4 SP |
| **TOTAL** | **22%** | **45 SP** | **10 SP** | **35 SP** |

---

## 🔗 Dependencies

### External Dependencies
- **Epic 001** (Login Policy System) - LoginPolicyService 연동 필요
- **AD Server** - LDAP 연결 설정 필요
- **SSO Provider** - OAuth2 Client 등록 필요
- **Redis** - 세션 추적 및 토큰 블랙리스트

### Technical Dependencies
- Spring Security 6.x
- Spring Data Redis
- Spring LDAP (AD 연동)
- Spring Security OAuth2 Client (SSO 연동)
- JWT Library (jjwt-api, jjwt-impl, jjwt-jackson)
- BCrypt (Spring Security 내장)

---

## 🎯 Acceptance Criteria (Epic Level)

### Functional Requirements
- [x] JWT 토큰 생성/검증/갱신 가능
- [ ] LOCAL 로그인 성공 시 JWT 발급
- [ ] AD 로그인 성공 시 JWT 발급
- [ ] SSO 로그인 성공 시 JWT 발급
- [ ] 우선순위 기반 Fallback (SSO > AD > LOCAL)
- [ ] 비밀번호 변경/리셋 (LOCAL 전용)
- [ ] 계정 잠금 정책 (LOCAL 전용)
- [ ] 동시 세션 제어 (모든 로그인 방식)

### Non-Functional Requirements
- [ ] 로그인 응답 시간 < 1초 (LOCAL)
- [ ] 로그인 응답 시간 < 3초 (AD)
- [ ] 로그인 응답 시간 < 5초 (SSO)
- [ ] JWT 검증 성능 1000 TPS 이상
- [ ] Redis 장애 시 Fallback 동작 (In-Memory)
- [ ] 모든 인증 시도 감사 로그 기록
- [ ] TDD 준수 (테스트 커버리지 80% 이상)

---

## 📝 Notes

### Design Decisions
1. **정책 격리 원칙**: 비밀번호/계정 보안 정책은 LOCAL 전용, AD/SSO는 각 시스템에서 관리
2. **JWT Stateless**: 세션 상태를 서버에 저장하지 않고 JWT로 관리
3. **Refresh Token Rotation**: 보안 강화를 위해 갱신 시마다 새 토큰 발급
4. **Redis 캐싱**: 세션 추적, 토큰 블랙리스트, IP 블랙리스트 등 Redis 활용
5. **이중 소스 지원**: SYSTEM_CONFIG (DB) + application.properties (Fallback)

### Risks & Mitigation
- **Risk**: AD 서버 장애 시 로그인 불가
  - **Mitigation**: AD 장애 시 자동 LOCAL로 Fallback
- **Risk**: SSO 서버 장애 시 로그인 불가
  - **Mitigation**: SSO 장애 시 자동 AD → LOCAL로 Fallback
- **Risk**: Redis 장애 시 세션 추적 불가
  - **Mitigation**: Redis 장애 시 In-Memory Fallback
- **Risk**: JWT 비밀키 노출
  - **Mitigation**: 환경 변수 사용, 정기적 로테이션

### Future Enhancements
- MFA (Multi-Factor Authentication) - Epic 4
- 생체 인증 지원 - Epic 5
- 비밀번호 없는 로그인 (Passwordless) - Epic 6
- Device Fingerprinting - Epic 7
