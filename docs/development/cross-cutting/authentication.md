### Security

**⚠️ 전체 시스템 인증 정책:**
- [x] 모든 기능 로그인 필수 - 사내 업무시스템으로 공개 API 없음
- [x] 비인증 사용자 접근 시 로그인 페이지로 자동 리다이렉트
- [ ] 로그인 리다이렉트 우선순위 - SSO > AD > LOCAL (모든 로그인 방식 사용 가정)
- [ ] 세션 만료 시 자동 로그아웃 후 최우선 로그인 방식으로 리다이렉트
- [x] 공개 API 예외 - 비밀번호 리셋 요청/검증 API만 인증 불필요
- [x] Health Check 엔드포인트 - 인증 불필요 (모니터링 목적)

**로그인 흐름 (Login Flow):**
- [ ] 비인증 사용자 → 정책 조회 → 최우선 방식으로 리다이렉트 (기본: SSO)
- [ ] SSO 페이지 접속 → SSO 인증 실패 시 자동 AD로 Fallback
- [ ] AD 페이지 접속 → AD 인증 실패 시 자동 LOCAL로 Fallback
- [ ] LOCAL 로그인 실패 시 → 에러 메시지 표시 (더 이상 Fallback 없음)
- [ ] 사용자 명시적 선택 → 선택한 방식으로만 로그인 (Fallback 무시)
- [ ] 로그인 성공 → 원래 접근하려던 페이지로 리다이렉트 (returnUrl 파라미터)

**인증 필수 API:**
- [x] 모든 /api/v1/** 엔드포인트 - JWT Access Token 필수
- [x] 인증 실패 시 401 Unauthorized + WWW-Authenticate 헤더
- [ ] 권한 부족 시 403 Forbidden + 필요 권한 정보 포함

**인증 불필요 API (예외):**
- [x] POST /api/v1/auth/request-reset - 비밀번호 리셋 요청 (이메일 기반)
- [x] GET /api/v1/auth/validate-reset-token - 리셋 토큰 검증
- [x] POST /api/v1/auth/reset-password - 비밀번호 리셋 실행
- [x] GET /actuator/health - Spring Boot Health Check
- [x] GET /actuator/info - 애플리케이션 정보

#### Authentication - AD Login (Active Directory)

**정상 케이스:**
- [x] AD 로그인 성공 - 유효한 사원ID + 비밀번호 → JWT 토큰 발급
- [x] AD 로그인 성공 - 사용자 정보 포함 (id, employeeId, fullName, orgId, roles)
- [x] AD 로그인 성공 - Access Token + Refresh Token 모두 발급
- [x] AD 로그인 성공 - Token 만료시간 정확히 설정 (Access: 1시간, Refresh: 24시간)

**실패 케이스 - 인증 오류:**
- [x] AD 로그인 실패 - 존재하지 않는 사원ID
- [x] AD 로그인 실패 - 잘못된 비밀번호
- [x] AD 로그인 실패 - 빈 사원ID (InvalidCredentialsException)
- [x] AD 로그인 실패 - 빈 비밀번호 (InvalidCredentialsException)
- [x] AD 로그인 실패 - null 사원ID (InvalidCredentialsException)
- [x] AD 로그인 실패 - null 비밀번호 (InvalidCredentialsException)
- [x] AD 로그인 실패 - 비활성화된 사용자 (AccountDisabledException)
- [x] AD 로그인 실패 - 잠긴 계정 (AccountLockedException)
- [x] AD 로그인 실패 - 만료된 계정 (AccountExpiredException)
- [x] AD 로그인 실패 - 비밀번호 만료 (CredentialsExpiredException)

**실패 케이스 - 서버 오류:**
- [x] AD 로그인 실패 - AD 서버 연결 불가 (Connection Timeout)
- [x] AD 로그인 실패 - AD 서버 응답 없음 (Read Timeout 3초)
- [ ] AD 로그인 실패 - AD 서버 DNS 해석 실패
- [ ] AD 로그인 실패 - AD 서버 인증서 오류 (TLS/SSL)
- [ ] AD 로그인 실패 - AD 서버 5xx 에러 응답

**실패 케이스 - 입력 검증:**
- [x] AD 로그인 실패 - 사원ID 형식 오류 (영문자 포함 등)
- [x] AD 로그인 실패 - 사원ID 길이 초과 (50자 초과)
- [x] AD 로그인 실패 - 비밀번호 길이 초과 (100자 초과)
- [x] AD 로그인 실패 - SQL Injection 시도 차단
- [x] AD 로그인 실패 - XSS 스크립트 입력 차단

**보안 케이스:**
- [x] AD 로그인 시도 제한 - 5회 실패 시 계정 잠금 (5분)
- [x] AD 로그인 시도 제한 - 10회 실패 시 계정 잠금 (30분)
- [ ] AD 로그인 Brute Force 방지 - IP별 요청 제한 (분당 10회)
- [x] AD 로그인 감사 로그 - 성공 시 로그 기록 (사원ID, IP, 시간) ⚠️ IP는 TODO
- [x] AD 로그인 감사 로그 - 실패 시 로그 기록 (사원ID, 실패 사유, IP, 시간) ⚠️ IP는 TODO

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
- [x] 일반 로그인 성공 - 유효한 사원ID + 비밀번호 → JWT 발급
- [x] 일반 로그인 성공 - BCrypt 비밀번호 검증
- [x] 일반 로그인 성공 - 사용자 정보 포함 (id, employeeId, fullName, orgId, roles)
- [x] 일반 로그인 성공 - Access Token + Refresh Token 발급

**실패 케이스 - 인증 오류:**
- [x] 일반 로그인 실패 - 존재하지 않는 사원ID
- [x] 일반 로그인 실패 - 잘못된 비밀번호
- [x] 일반 로그인 실패 - 빈 사원ID (Bean Validation)
- [x] 일반 로그인 실패 - 빈 비밀번호 (Bean Validation)
- [x] 일반 로그인 실패 - null 사원ID (Bean Validation)
- [x] 일반 로그인 실패 - null 비밀번호 (Bean Validation)
- [x] 일반 로그인 실패 - 비활성화된 사용자 (status=INACTIVE)
- [x] 일반 로그인 실패 - 삭제된 사용자 (status=DELETED) ⚠️ canLogin()으로 커버

**입력 검증:**
- [x] 일반 로그인 실패 - 사원ID 형식 오류 (Pattern Validation)
- [x] 일반 로그인 실패 - 사원ID 길이 초과 (50자) (Size Validation)
- [x] 일반 로그인 실패 - 비밀번호 길이 초과 (100자) (Size Validation)
- [x] 일반 로그인 실패 - SQL Injection 시도 차단 (Pattern Validation)
- [x] 일반 로그인 실패 - XSS 스크립트 입력 차단 (Pattern Validation)

**보안 케이스:**
- [x] 일반 로그인 시도 제한 - 5회 실패 시 계정 잠금 (5분)
- [x] 일반 로그인 시도 제한 - 10회 실패 시 계정 잠금 (30분)
- [ ] 일반 로그인 Brute Force 방지 - IP별 요청 제한 (분당 10회)
- [x] 일반 로그인 감사 로그 - 성공 시 로그 기록 ⚠️ IP는 TODO
- [x] 일반 로그인 감사 로그 - 실패 시 로그 기록 ⚠️ IP는 TODO

#### System Configuration Architecture (시스템 설정 아키텍처)

**⚠️ 전역 설정 원칙 (Global Configuration Principle):**
- [ ] **모든 설정은 시스템 전역(Global)으로 관리** - 조직/테넌트별 분리 없음
- [ ] 전체 시스템에 동일한 정책 적용 - 일관성 보장
- [ ] 이중 소스 지원 - DB 설정 + Properties 파일 Fallback
- [ ] 우선순위 - DB 설정 우선, DB 값이 NULL/빈값이면 Properties Fallback
- [ ] 관리자 UI 제공 - Jenkins 스타일 System Configuration 화면

**설정 소스 (Configuration Sources):**
- [ ] Primary Source - SYSTEM_CONFIG 테이블 (DB)
- [ ] Fallback Source - application.properties / application.yml
- [ ] Properties 로드 - 서버 부팅 시에만 로드 (Hot Reload 없음)
- [ ] DB 변경 - 관리자 UI에서 실시간 변경, 즉시 적용
- [ ] 캐시 전략 - Redis 캐싱으로 성능 최적화, 변경 시 캐시 무효화

**SYSTEM_CONFIG 테이블 구조:**
- [ ] 테이블 설계 - 키-값 저장소 패턴 (Key-Value Store)
- [ ] config_key (VARCHAR(100), PK) - 설정 키 (예: 'auth.login.local.enabled')
- [ ] config_value (TEXT) - 설정 값 (문자열 또는 JSON)
- [ ] value_type (VARCHAR(20)) - 값 타입 (BOOLEAN, INT, STRING, JSON, LIST)
- [ ] description (VARCHAR(500)) - 설정 설명 (UI 표시용)
- [ ] category (VARCHAR(50)) - 설정 카테고리 (LOGIN, PASSWORD, SESSION, IP, etc.)
- [ ] editable (BOOLEAN) - UI에서 수정 가능 여부 (일부 설정은 읽기 전용)
- [ ] updated_at (TIMESTAMP) - 마지막 수정 시간
- [ ] updated_by (VARCHAR(50)) - 수정한 관리자 ID
- [ ] version (INT) - 낙관적 락 (Optimistic Lock)

**설정 키 네이밍 규칙 (Config Key Convention):**
- [ ] 계층 구조 - 점(.) 구분자 사용 (예: auth.login.local.enabled)
- [ ] 일관성 - Properties 파일과 동일한 키 사용
- [ ] 로그인 방식 - auth.login.{method}.enabled (method: local, ad, sso)
- [ ] 로그인 우선순위 - auth.login.priority (예: "SSO,AD,LOCAL")
- [ ] 비밀번호 정책 - auth.password.{policy}.enabled, auth.password.{policy}.config
- [ ] 세션 정책 - auth.session.{policy}.enabled, auth.session.{policy}.config
- [ ] IP 정책 - auth.ip.{policy}.enabled, auth.ip.{policy}.config

**설정 우선순위 로직 (Configuration Priority Logic):**
- [ ] 1순위 - SYSTEM_CONFIG 테이블 조회
- [ ] DB 값 존재 확인 - config_key 조회
- [ ] DB 값 NULL/빈값 체크 - NULL 또는 빈 문자열이면 Fallback
- [ ] 2순위 - Properties 파일 조회 (@Value 또는 @ConfigurationProperties)
- [ ] 최종 값 반환 - DB 또는 Properties 중 유효한 값
- [ ] 캐싱 - Redis에 최종 값 캐싱 (키: "config:{config_key}", TTL: 5분)
- [ ] 캐시 무효화 - SYSTEM_CONFIG 수정 시 해당 키 캐시 삭제

**설정 조회 서비스 (ConfigurationService):**
- [ ] getConfig(key) - 설정 값 조회 (우선순위 로직 적용)
- [ ] getConfigAsBoolean(key) - Boolean 타입으로 변환
- [ ] getConfigAsInt(key) - Integer 타입으로 변환
- [ ] getConfigAsList(key) - 콤마 구분 문자열을 List로 변환
- [ ] getConfigAsJson(key) - JSON 문자열을 Object로 파싱
- [ ] updateConfig(key, value, adminId) - DB 설정 수정
- [ ] deleteConfig(key) - DB 설정 삭제 (Properties Fallback으로 전환)
- [ ] getAllConfigs(category) - 카테고리별 설정 목록 조회
- [ ] reloadCache() - 캐시 전체 무효화 및 재로드

**Properties 파일 예시 (application.properties):**
```properties
# 로그인 방식 설정
auth.login.local.enabled=true
auth.login.ad.enabled=true
auth.login.sso.enabled=false
auth.login.priority=SSO,AD,LOCAL

# 비밀번호 정책
auth.password.expiration.enabled=true
auth.password.expiration.days=90
auth.password.complexity.enabled=true
auth.password.complexity.minLength=8
auth.password.complexity.requireUppercase=true
auth.password.complexity.requireLowercase=true
auth.password.complexity.requireDigit=true
auth.password.complexity.requireSpecial=true
auth.password.history.enabled=true
auth.password.history.count=5

# 계정 잠금 정책
auth.account.inactive.enabled=true
auth.account.inactive.days=365
auth.account.failedAttempts.enabled=true
auth.account.failedAttempts.thresholds=[{"attempts":5,"lockMinutes":5},{"attempts":10,"lockMinutes":30},{"attempts":15,"lockMinutes":null}]

# IP 정책
auth.ip.blacklist.enabled=true
auth.ip.blacklist.failureThreshold=30
auth.ip.blacklist.blockHours=24
auth.ip.whitelist.enabled=false
auth.ip.rateLimit.enabled=true
auth.ip.rateLimit.maxRequestsPerMinute=10

# 세션 정책
auth.session.accessToken.expirationSeconds=3600
auth.session.refreshToken.expirationSeconds=86400
auth.session.maxConcurrentSessions=10

# 민감 정보 (환경 변수 필수)
auth.session.jwtSecret=${JWT_SECRET_KEY}
auth.ad.ldap.bindPassword=${AD_BIND_PASSWORD}
auth.sso.oauth2.clientSecret=${SSO_CLIENT_SECRET}

# AD (Active Directory) 연동 설정
auth.ad.ldap.url=ldap://ad.company.com:389
auth.ad.ldap.baseDn=dc=company,dc=com
auth.ad.ldap.bindDn=cn=admin,dc=company,dc=com
auth.ad.ldap.userDnPattern=uid={0},ou=people
auth.ad.ldap.searchFilter=(uid={0})

# SSO (Single Sign-On) 연동 설정
auth.sso.provider=azure
auth.sso.oauth2.clientId=your-client-id
auth.sso.oauth2.authorizationUri=https://login.microsoftonline.com/tenant-id/oauth2/v2.0/authorize
auth.sso.oauth2.tokenUri=https://login.microsoftonline.com/tenant-id/oauth2/v2.0/token
auth.sso.oauth2.userInfoUri=https://graph.microsoft.com/v1.0/me
auth.sso.oauth2.redirectUri=https://app.example.com/login/oauth2/callback
```

**SYSTEM_CONFIG 테이블 예시 데이터:**
```sql
INSERT INTO SYSTEM_CONFIG (config_key, config_value, value_type, description, category, editable)
VALUES
-- 로그인 방식
('auth.login.local.enabled', 'true', 'BOOLEAN', 'LOCAL 로그인 활성화 여부', 'LOGIN', true),
('auth.login.ad.enabled', 'true', 'BOOLEAN', 'AD 로그인 활성화 여부', 'LOGIN', true),
('auth.login.sso.enabled', 'false', 'BOOLEAN', 'SSO 로그인 활성화 여부', 'LOGIN', true),
('auth.login.priority', 'SSO,AD,LOCAL', 'LIST', '로그인 우선순위', 'LOGIN', true),

-- 비밀번호 만료 정책
('auth.password.expiration.enabled', 'true', 'BOOLEAN', '비밀번호 만료 정책 활성화', 'PASSWORD', true),
('auth.password.expiration.days', '90', 'INT', '비밀번호 만료 일수', 'PASSWORD', true),

-- 비밀번호 복잡도 정책
('auth.password.complexity.enabled', 'true', 'BOOLEAN', '비밀번호 복잡도 정책 활성화', 'PASSWORD', true),
('auth.password.complexity.config', '{"minLength":8,"requireUppercase":true,"requireLowercase":true,"requireDigit":true,"requireSpecial":true}', 'JSON', '비밀번호 복잡도 설정', 'PASSWORD', true),

-- IP 블랙리스트 정책
('auth.ip.blacklist.enabled', 'true', 'BOOLEAN', 'IP 블랙리스트 정책 활성화', 'IP', true),
('auth.ip.blacklist.config', '{"failureThreshold":30,"blockHours":24}', 'JSON', 'IP 블랙리스트 설정', 'IP', true);
```

**마이그레이션 전략 (기존 SecurityPolicy → SYSTEM_CONFIG):**
- [ ] 기존 테이블 - SecurityPolicy (orgId 기반 다중 정책)
- [ ] 마이그레이션 대상 - orgId = NULL인 글로벌 정책만 추출
- [ ] 변환 로직 - SecurityPolicy.policyType + config → SYSTEM_CONFIG.config_key + config_value
- [ ] 예시 변환:
  - SecurityPolicy(policyType=PASSWORD_EXPIRATION, config={days:90}) 
  → SYSTEM_CONFIG('auth.password.expiration.days', '90')
- [ ] 조직별 정책 처리 - 삭제 또는 아카이브 (더 이상 사용 안 함)
- [ ] 마이그레이션 스크립트 - SQL 스크립트 작성 (V2__migrate_to_global_config.sql)
- [ ] 롤백 계획 - 마이그레이션 전 백업 필수

**System Configuration UI 요구사항:**
- [ ] UI 위치 - 관리자 메뉴 > System Settings (시스템 설정)
- [ ] 접근 권한 - ROLE_ADMIN 필수
- [ ] UI 레이아웃 - Jenkins 스타일 (카테고리별 섹션 분리)
- [ ] 카테고리 탭 - LOGIN, PASSWORD, SESSION, IP, ACCOUNT, ADVANCED
- [ ] 설정 항목 표시 - 이름, 설명, 현재 값, 기본값(Properties), 수정 버튼
- [ ] 값 편집 - 타입별 입력 컴포넌트 (Boolean=Toggle, Int=Number Input, String=Text Input, JSON=JSON Editor)
- [ ] 즉시 적용 - 저장 버튼 클릭 시 즉시 DB 저장 및 캐시 무효화
- [ ] 변경 이력 - 설정 변경 이력 조회 (누가, 언제, 무엇을 변경)
- [ ] 리셋 기능 - 개별 설정 또는 전체 설정 초기화 (Properties 기본값으로)
- [ ] 검증 - 잘못된 값 입력 시 에러 메시지 표시 (예: days는 양수만)
- [ ] 미리보기 - 변경 전후 비교 (Optional)
- [ ] Export/Import - 설정 전체를 JSON 파일로 내보내기/가져오기 (Optional)

**System Configuration API:**
- [ ] GET /api/v1/admin/system-config - 전체 설정 조회 (카테고리별)
- [ ] GET /api/v1/admin/system-config/{key} - 개별 설정 조회
- [ ] PUT /api/v1/admin/system-config/{key} - 개별 설정 수정
- [ ] DELETE /api/v1/admin/system-config/{key} - 개별 설정 삭제 (Properties로 Fallback)
- [ ] POST /api/v1/admin/system-config/reset - 전체 설정 초기화
- [ ] POST /api/v1/admin/system-config/reload-cache - 캐시 재로드
- [ ] GET /api/v1/admin/system-config/history - 변경 이력 조회
- [ ] 인증 필수 - JWT Access Token + ROLE_ADMIN 권한
- [ ] Rate Limiting - 동일 관리자 1분당 30회 제한

**설정 변경 감사 로그 (Config Change Audit):**
- [ ] 테이블 - CONFIG_CHANGE_HISTORY
- [ ] 필드 - id, config_key, old_value, new_value, changed_by, changed_at, change_reason
- [ ] 로그 기록 - 모든 설정 변경 시 자동 기록
- [ ] 조회 기능 - 관리자가 변경 이력 조회 가능
- [ ] 보존 기간 - 최소 1년 이상 보관

**테스트 시나리오:**
- [ ] 시나리오 1: Properties만 있을 때 - Properties 값 사용
- [ ] 시나리오 2: DB 설정 추가 - DB 값으로 Override
- [ ] 시나리오 3: DB 값 NULL - Properties Fallback
- [ ] 시나리오 4: DB 설정 삭제 - Properties로 복귀
- [ ] 시나리오 5: UI에서 설정 변경 - 즉시 적용 확인
- [ ] 시나리오 6: 잘못된 값 입력 - 검증 에러 발생
- [ ] 시나리오 7: 캐시 동작 확인 - Redis 캐싱 및 무효화
- [ ] 시나리오 8: 마이그레이션 - 기존 SecurityPolicy → SYSTEM_CONFIG 변환
- [ ] 시나리오 9: 권한 검증 - ROLE_ADMIN 없는 사용자 접근 거부
- [ ] 시나리오 10: 변경 이력 조회 - 누가 언제 무엇을 변경했는지 확인

#### Account Security Policies (계정 보안 정책)

**⚠️ 정책 격리 원칙 (Policy Isolation by Login Method):**
- [ ] **모든 보안/잠금 정책은 LOCAL 로그인 전용** - AD, SSO는 각 시스템에서 정책 관리
- [ ] AD 로그인 사용자 - AD 도메인 정책으로 관리 (비밀번호, 계정 잠금 등)
- [ ] SSO 로그인 사용자 - SSO 시스템 정책으로 관리 (인증, 세션 등)
- [ ] LOCAL 로그인 사용자 - 내부 SecurityPolicy로 완전 제어
- [ ] 정책 적용 시 loginMethod 검증 - LOCAL이 아니면 정책 스킵
- [ ] User.loginMethod 필드 - LOCAL | AD | SSO (필수 저장)

**정책 관리 (Policy Management):**
- [ ] **전역 설정 기반** - SYSTEM_CONFIG 테이블 사용 (조직별 분리 없음)
- [ ] 정책 키 형식 - auth.{domain}.{policy}.enabled, auth.{domain}.{policy}.config
- [ ] 정책 활성화/비활성화 - {policy}.enabled 플래그로 관리
- [ ] 정책 설정 조회 - ConfigurationService.getConfig(key)
- [ ] 정책 활성 여부 확인 - getConfigAsBoolean("auth.{policy}.enabled") && isApplicableToLoginMethod(loginMethod)
- [ ] 정책 생성/수정 - System Configuration UI 또는 API 사용
- [ ] 정책 적용 범위 검증 - applicableLoginMethods 확인 후 적용 (LOCAL만)
- [ ] 정책 감사 로그 - CONFIG_CHANGE_HISTORY 테이블에 변경 이력 기록
- [ ] DB 우선 + Properties Fallback - DB 값 없으면 application.properties 사용
- [ ] 관리자 권한 필요 - ROLE_ADMIN만 설정 변경 가능

**정책 카테고리:**
- [ ] LOGIN - 로그인 방식 활성화, 우선순위
- [ ] PASSWORD - 비밀번호 만료, 복잡도, 히스토리
- [ ] ACCOUNT - 계정 잠금 (비활성, 실패 횟수)
- [ ] IP - IP 블랙리스트, 화이트리스트, Rate Limiting
- [ ] SESSION - JWT 만료시간, 동시 세션 수

**비활성 계정 잠금 정책 (LOCAL 전용, 선택적 적용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 사용자 제외
- [ ] 설정 키 - auth.account.inactive.enabled (BOOLEAN)
- [ ] 설정 키 - auth.account.inactive.days (INT, 기본값: 365)
- [ ] 비활성 계정 감지 - 최근 로그인 N일 경과 시 자동 잠금
- [ ] 비활성 기간 설정 - ConfigurationService에서 조회 (30/90/180/365일)
- [ ] 비활성 계정 잠금 - 계정 상태 ACTIVE → LOCKED_INACTIVE로 변경
- [ ] 비활성 계정 잠금 - lockedReason = "INACTIVE_{N}_DAYS" 기록
- [ ] 비활성 계정 잠금 - lockedAt 타임스탬프 기록
- [ ] 비활성 계정 잠금 해제 - 관리자 승인 필요 (Self-service 불가)
- [ ] 비활성 계정 잠금 해제 - 해제 시 비밀번호 즉시 변경 강제
- [ ] 비활성 계정 잠금 - 감사 로그 기록 (action: ACCOUNT_LOCKED, reason: INACTIVE)
- [ ] 정책 비활성화 시 - 기존 잠금된 계정 자동 해제하지 않음 (관리자 수동 처리)
- [ ] 배치 작업 - 매일 자정 실행, 비활성 계정 검사 및 잠금

**실패 로그인 횟수 잠금 정책 (LOCAL 전용, 선택적 적용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 사용자 제외
- [ ] 설정 키 - auth.account.failedAttempts.enabled (BOOLEAN)
- [ ] 설정 키 - auth.account.failedAttempts.thresholds (JSON)
- [ ] 정책 설정 - JSON 배열: [{"attempts":5,"lockMinutes":5},{"attempts":10,"lockMinutes":30},{"attempts":15,"lockMinutes":null}]
- [ ] 임계값 커스터마이징 - System Configuration UI에서 JSON 편집으로 설정 가능
- [ ] AD/SSO 로그인 실패 - 실패 카운트하지 않음 (각 시스템에서 관리)
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

**IP 블랙리스트 정책 (LOCAL 전용, 선택적 적용):**
- [ ] **적용 대상: LOCAL 로그인 IP만** - AD/SSO는 각 시스템에서 관리
- [ ] 설정 키 - auth.ip.blacklist.enabled (BOOLEAN)
- [ ] 설정 키 - auth.ip.blacklist.failureThreshold (INT, 기본값: 30)
- [ ] 설정 키 - auth.ip.blacklist.blockHours (INT, 기본값: 24)
- [ ] 정책 설정 - ConfigurationService에서 조회
- [ ] IP 블랙리스트 등록 - 동일 IP에서 LOCAL 로그인 N회 이상 실패 시 등록
- [ ] IP 블랙리스트 저장 - IP_BLACKLIST 테이블 (ip, blockedAt, expiresAt, reason, loginMethod)
- [ ] IP 블랙리스트 차단 - 블랙리스트 IP의 LOCAL 로그인 시도 즉시 403 응답
- [ ] IP 블랙리스트 해제 - expiresAt 시간 경과 후 자동 해제
- [ ] IP 블랙리스트 해제 - 관리자 수동 해제 가능
- [ ] IP 블랙리스트 감사 로그 - 등록/해제 시 감사 로그 기록
- [ ] Redis 캐싱 - 블랙리스트 IP 캐싱으로 성능 최적화
- [ ] AD/SSO 로그인 - IP 블랙리스트 검사하지 않음

**IP 화이트리스트 정책 (LOCAL 전용, 선택적 적용):**
- [ ] **적용 대상: LOCAL 로그인 IP만** - AD/SSO는 각 시스템에서 관리
- [ ] 설정 키 - auth.ip.whitelist.enabled (BOOLEAN)
- [ ] 설정 키 - auth.ip.whitelist.allowedIpRanges (LIST, CIDR 표기법)
- [ ] 정책 설정 - 콤마 구분 문자열 (예: "192.168.1.0/24,10.0.0.0/8")
- [ ] IP 화이트리스트 저장 - IP_WHITELIST 테이블 (ipRange, description, orgId, loginMethod)
- [ ] IP 화이트리스트 검증 - CIDR 범위 내 IP 확인 (LOCAL 로그인만)
- [ ] IP 화이트리스트 제외 - 화이트리스트 IP는 실패 잠금 정책 제외
- [ ] IP 화이트리스트 제외 - 화이트리스트 IP는 블랙리스트 등록 제외
- [ ] IP 화이트리스트 관리 - 관리자만 추가/수정/삭제 가능
- [ ] AD/SSO 로그인 - IP 화이트리스트 검사하지 않음

**IP Rate Limiting 정책 (LOCAL 전용, 선택적 적용):**
- [ ] **적용 대상: LOCAL 로그인 IP만** - AD/SSO는 각 시스템에서 관리
- [ ] 설정 키 - auth.ip.rateLimit.enabled (BOOLEAN)
- [ ] 설정 키 - auth.ip.rateLimit.maxRequestsPerMinute (INT, 기본값: 10)
- [ ] 정책 설정 - ConfigurationService에서 조회
- [ ] Rate Limit 카운터 - Redis 기반 카운터 (키: "rate_limit:local:ip:{ip}")
- [ ] Rate Limit TTL - 1분 자동 만료
- [ ] Rate Limit 초과 - 429 Too Many Requests 응답 (LOCAL 로그인만)
- [ ] Rate Limit 헤더 - X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset 헤더 포함
- [ ] Rate Limit 감사 로그 - 초과 시 감사 로그 기록
- [ ] AD/SSO 로그인 - Rate Limiting 적용하지 않음

#### Password Management Policies (비밀번호 관리 정책)

**⚠️ 비밀번호 정책 격리:**
- [ ] **모든 비밀번호 정책은 LOCAL 로그인 전용** - AD/SSO는 각 시스템에서 관리
- [ ] AD 로그인 사용자 - AD 도메인 비밀번호 정책 적용 (복잡도, 만료, 히스토리)
- [ ] SSO 로그인 사용자 - SSO 시스템 비밀번호 정책 적용
- [ ] LOCAL 로그인 사용자 - 내부 PasswordPolicy로 완전 제어
- [ ] 비밀번호 변경 API - LOCAL 사용자만 호출 가능 (AD/SSO는 403 Forbidden)

**비밀번호 정책 유형:**
- [ ] **SYSTEM_CONFIG 기반 관리** - DB + Properties 이중 소스 지원
- [ ] 정책별 독립 활성화 - 각 정책 개별적으로 enabled 설정 가능
- [ ] applicableLoginMethods - LOCAL만 적용 (AD, SSO 제외)
- [ ] 설정 키 - auth.password.{policyType}.enabled, auth.password.{policyType}.config

**비밀번호 만료 정책 (LOCAL 전용, 선택적 적용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 사용자 제외
- [ ] 설정 키 - auth.password.expiration.enabled (BOOLEAN)
- [ ] 설정 키 - auth.password.expiration.days (INT, 기본값: 90, 옵션: 30/60/90/180/365)
- [ ] 정책 설정 - ConfigurationService에서 조회
- [ ] 비밀번호 만료 - 비밀번호 생성/변경 후 N일 경과 시 만료
- [ ] 비밀번호 만료 - passwordExpiresAt 필드로 관리
- [ ] 비밀번호 만료 - 만료 후 로그인 시 PasswordExpiredException 예외
- [ ] 비밀번호 만료 - 만료 후 즉시 변경 페이지로 리다이렉트
- [ ] 비밀번호 만료 - 변경 완료 전 다른 페이지 접근 차단
- [ ] 비밀번호 만료 경고 - 로그인 시 만료까지 남은 일수 UI 표시
- [ ] 비밀번호 만료 경고 - 로그인 응답에 daysUntilExpiration 필드 포함
- [ ] 정책 비활성화 시 - passwordExpiresAt은 유지하지만 검증 스킵

**비밀번호 복잡도 정책 (LOCAL 전용, 선택적 적용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 사용자 제외
- [ ] 설정 키 - auth.password.complexity.enabled (BOOLEAN)
- [ ] 설정 키 - auth.password.complexity.minLength (INT, 기본값: 8)
- [ ] 설정 키 - auth.password.complexity.maxLength (INT, 기본값: 100)
- [ ] 설정 키 - auth.password.complexity.requireUppercase (BOOLEAN, 기본값: true)
- [ ] 설정 키 - auth.password.complexity.requireLowercase (BOOLEAN, 기본값: true)
- [ ] 설정 키 - auth.password.complexity.requireDigit (BOOLEAN, 기본값: true)
- [ ] 설정 키 - auth.password.complexity.requireSpecial (BOOLEAN, 기본값: true)
- [ ] 정책 설정 - ConfigurationService에서 개별 조회 또는 JSON으로 묶어서 관리
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

**비밀번호 히스토리 정책 (LOCAL 전용, 선택적 적용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 사용자 제외
- [ ] 설정 키 - auth.password.history.enabled (BOOLEAN)
- [ ] 설정 키 - auth.password.history.count (INT, 기본값: 5, 옵션: 3/5/10)
- [ ] 정책 설정 - ConfigurationService에서 조회
- [ ] 비밀번호 히스토리 - 최근 N개 비밀번호 재사용 금지
- [ ] 비밀번호 히스토리 - PASSWORD_HISTORY 테이블에 BCrypt 해시 저장
- [ ] 비밀번호 히스토리 - userId, passwordHash, createdAt 필드
- [ ] 비밀번호 히스토리 - 비밀번호 변경 시 자동 저장
- [ ] 비밀번호 히스토리 - 새 비밀번호 설정 시 최근 N개와 비교
- [ ] 비밀번호 히스토리 - 재사용 감지 시 PasswordReusedException 예외
- [ ] 비밀번호 히스토리 - N+1번째 이전 비밀번호는 재사용 허용
- [ ] 비밀번호 히스토리 - 파티셔닝 전략 (userId 기준)
- [ ] 정책 비활성화 시 - 히스토리 저장은 계속하지만 검증 스킵

**강제 비밀번호 변경 (LOCAL 전용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 사용자 제외
- [ ] 강제 변경 - 최초 로그인 시 비밀번호 변경 필수
- [ ] 강제 변경 - mustChangePassword = true 플래그
- [ ] 강제 변경 - 관리자가 비밀번호 리셋 시 mustChangePassword = true 설정
- [ ] 강제 변경 - 비밀번호 만료 후 로그인 시 강제 변경
- [ ] 강제 변경 - 보안 정책 변경 시 전체 LOCAL 사용자 강제 변경 (관리자 트리거)
- [ ] 강제 변경 - 데이터 유출 의심 시 특정 사용자 강제 변경
- [ ] 강제 변경 완료 전 - API 접근 차단 (비밀번호 변경 API 제외)
- [ ] 강제 변경 완료 전 - 로그인 성공하지만 즉시 변경 페이지로 리다이렉트
- [ ] 강제 변경 완료 후 - mustChangePassword = false 설정
- [ ] AD/SSO 사용자 - mustChangePassword 검사하지 않음

**비밀번호 변경 프로세스 (LOCAL 전용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO는 403 Forbidden
- [ ] 비밀번호 변경 API - loginMethod 검증, LOCAL이 아니면 거부
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

**비밀번호 변경 API (LOCAL 전용):**
- [ ] POST /api/v1/auth/change-password - 비밀번호 변경
- [ ] Request DTO - currentPassword, newPassword, newPasswordConfirm
- [ ] Response DTO - success, message, passwordExpiresAt
- [ ] 인증 필수 - JWT Access Token 필요
- [ ] loginMethod 검증 - LOCAL 사용자만 허용, AD/SSO는 403 Forbidden
- [ ] Rate Limiting - 동일 사용자 1분당 3회 제한

#### Session Management Policies (세션 관리 정책)

**⚠️ 세션 정책 적용 범위:**
- [ ] **모든 로그인 방식에 적용** - LOCAL, AD, SSO 모두 동일한 세션 정책 사용
- [ ] JWT 기반 통합 세션 관리 - 로그인 방식과 무관하게 동일한 토큰 구조
- [ ] 세션 만료, 갱신, 무효화 - 로그인 방식 구분 없이 통합 관리

**세션 정책 개요:**
- [ ] **전역 설정 기반** - SYSTEM_CONFIG 테이블 사용
- [ ] DB + Properties 이중 소스 지원
- [ ] JWT 기반 세션 관리 (Stateless)
- [ ] Refresh Token Rotation 지원
- [ ] 동시 세션 제어

**JWT Access Token 정책:**
- [ ] 설정 키 - auth.session.accessToken.expirationSeconds (INT, 기본값: 3600)
- [ ] 만료시간 - 1시간 (3600초), 조정 가능 범위: 300~86400초
- [ ] 토큰 갱신 - Refresh Token으로만 갱신 가능
- [ ] 토큰 무효화 - Redis 블랙리스트 (로그아웃, 비밀번호 변경 시)
- [ ] 클레임 포함 - userId, employeeId, roles, orgId, loginMethod, iat, exp, iss
- [ ] 서명 알고리즘 - HS256 (HMAC-SHA256)
- [ ] 비밀키 관리 - auth.session.jwtSecret (STRING, **환경 변수 필수**: ${JWT_SECRET_KEY}, 최소 256비트)
- [ ] 비밀키 길이 - 최소 256비트 (32자 이상)
- [ ] 비밀키 로테이션 - 정기적 변경 권장 (분기별)

**JWT Refresh Token 정책:**
- [ ] 설정 키 - auth.session.refreshToken.expirationSeconds (INT, 기본값: 86400)
- [ ] 만료시간 - 24시간 (86400초), 조정 가능 범위: 3600~604800초
- [ ] 토큰 Rotation - 갱신 시 새 Refresh Token 발급 및 기존 토큰 무효화
- [ ] 1회용 토큰 - 동일 Refresh Token 재사용 금지
- [ ] Family 탐지 - 탈취 감지 시 전체 토큰 무효화
- [ ] 저장 위치 - Pinia Store (메모리), SessionStorage (복원용)
- [ ] 전송 방식 - POST body (Authorization 헤더 아님)

**동시 세션 정책:**
- [ ] 설정 키 - auth.session.maxConcurrentSessions (INT, 기본값: 10)
- [ ] 동시 로그인 허용 - 같은 사용자 여러 세션 가능
- [ ] 최대 세션 수 - 초과 시 가장 오래된 세션 자동 만료
- [ ] 세션 추적 - Redis Set (키: "user_sessions:{userId}")
- [ ] 세션 정보 - sessionId, deviceInfo, ipAddress, lastAccessedAt
- [ ] 세션 만료 - Refresh Token 만료 시 자동 삭제
- [ ] 강제 로그아웃 - 관리자가 특정 세션 강제 종료 가능

**세션 타임아웃 정책:**
- [ ] 설정 키 - auth.session.idleTimeoutMinutes (INT, 기본값: 30)
- [ ] Idle Timeout - 마지막 활동 후 N분 경과 시 자동 로그아웃
- [ ] Activity 갱신 - 모든 API 요청 시 lastAccessedAt 업데이트
- [ ] 타임아웃 경고 - 만료 5분, 1분 전 UI 경고 표시
- [ ] 연장 옵션 - 사용자가 "연장" 버튼 클릭 시 세션 갱신

**세션 보안 정책:**
- [ ] 설정 키 - auth.session.enforceSingleDevice (BOOLEAN, 기본값: false)
- [ ] Single Device 모드 - 활성화 시 동일 사용자 1개 디바이스만 허용
- [ ] Device Fingerprinting - User-Agent, IP, Screen Resolution 해시
- [ ] IP 변경 감지 - 세션 중 IP 변경 시 재인증 요구 (옵션)
- [ ] 설정 키 - auth.session.requireReauthOnIpChange (BOOLEAN, 기본값: false)
- [ ] HTTPS 강제 - TLS 1.2+ 필수
- [ ] Secure Cookie - HttpOnly, Secure, SameSite=Strict (Refresh Token용, 옵션)

**세션 저장소:**
- [ ] Primary - Redis (분산 세션 관리)
- [ ] Fallback - In-Memory (개발 환경)
- [ ] TTL 설정 - Refresh Token 만료시간과 동일
- [ ] 클러스터 지원 - Redis Cluster 또는 Sentinel 구성

**세션 모니터링:**
- [ ] 활성 세션 조회 - 관리자가 전체 활성 세션 조회 가능
- [ ] 사용자별 세션 조회 - 특정 사용자의 모든 세션 조회
- [ ] 이상 세션 탐지 - 동일 사용자 다른 국가 동시 로그인 감지
- [ ] 세션 통계 - 평균 세션 시간, 동시 접속자 수, 피크 시간대

**세션 테스트 시나리오:**
- [ ] 시나리오 1: Access Token 만료 → Refresh Token으로 갱신 성공
- [ ] 시나리오 2: Refresh Token 만료 → 재로그인 필요
- [ ] 시나리오 3: Refresh Token 재사용 → TokenReusedException
- [ ] 시나리오 4: 동시 세션 초과 → 가장 오래된 세션 자동 만료
- [ ] 시나리오 5: Idle Timeout 경과 → 자동 로그아웃
- [ ] 시나리오 6: 비밀번호 변경 → 모든 세션 무효화
- [ ] 시나리오 7: 관리자 강제 로그아웃 → 특정 세션 즉시 종료
- [ ] 시나리오 8: IP 변경 감지 (설정 활성화) → 재인증 요구
- [ ] 시나리오 9: Single Device 모드 → 다른 디바이스 로그인 시 기존 세션 종료
- [ ] 시나리오 10: Refresh Token Family 탈취 → 전체 토큰 무효화

#### External Authentication Configuration (외부 인증 연동 설정)

**AD (Active Directory) 연동 설정:**
- [ ] **Properties 전용 설정** - 민감 정보로 DB 저장 금지
- [ ] 설정 활성화 - auth.login.ad.enabled (BOOLEAN, DB 또는 Properties)
- [ ] LDAP URL - auth.ad.ldap.url (STRING, 예: ldap://ad.company.com:389)
- [ ] LDAP Base DN - auth.ad.ldap.baseDn (STRING, 예: dc=company,dc=com)
- [ ] LDAP User DN Pattern - auth.ad.ldap.userDnPattern (STRING, 예: uid={0},ou=people)
- [ ] LDAP Bind DN - auth.ad.ldap.bindDn (STRING, 관리자 계정)
- [ ] LDAP Bind Password - auth.ad.ldap.bindPassword (STRING, **환경 변수 필수**: ${AD_BIND_PASSWORD})
- [ ] LDAP Search Filter - auth.ad.ldap.searchFilter (STRING, 예: (uid={0}))
- [ ] LDAP Attributes - auth.ad.ldap.attributes (LIST, 예: displayName,mail,department,memberOf)
- [ ] Connection Timeout - auth.ad.ldap.connectionTimeout (INT, 기본값: 5000ms)
- [ ] Read Timeout - auth.ad.ldap.readTimeout (INT, 기본값: 10000ms)
- [ ] Connection Pool - auth.ad.ldap.pooled (BOOLEAN, 기본값: true)
- [ ] Pool Size - auth.ad.ldap.poolSize (INT, 기본값: 10)
- [ ] SSL/TLS - auth.ad.ldap.useSsl (BOOLEAN, 기본값: true)
- [ ] Certificate Validation - auth.ad.ldap.validateCertificate (BOOLEAN, 기본값: true)

**AD 사용자 동기화:**
- [ ] 자동 동기화 - auth.ad.sync.enabled (BOOLEAN, 기본값: true)
- [ ] 동기화 주기 - auth.ad.sync.cronExpression (STRING, 예: 0 0 2 * * ?)
- [ ] 사용자 생성 - AD 로그인 성공 시 자동 User 레코드 생성
- [ ] 사용자 업데이트 - 로그인 시 displayName, email, department 등 최신화
- [ ] 그룹 매핑 - AD 그룹 → 애플리케이션 Role 매핑
- [ ] 매핑 설정 - auth.ad.groupMapping (JSON, 예: {"CN=Admins":"ROLE_ADMIN"})
- [ ] 비활성 처리 - AD에서 삭제된 사용자 자동 INACTIVE 처리

**AD Health Check:**
- [ ] Connection 테스트 - 주기적 LDAP 연결 확인
- [ ] Health Check 주기 - auth.ad.healthCheck.intervalSeconds (INT, 기본값: 60)
- [ ] Fallback - AD 장애 시 LOCAL 로그인으로 자동 전환
- [ ] 알림 - AD 장애 감지 시 관리자 알림 (선택적)

**SSO (Single Sign-On) 연동 설정:**
- [ ] **Properties 전용 설정** - OAuth2 Client 정보는 민감 정보
- [ ] 설정 활성화 - auth.login.sso.enabled (BOOLEAN, DB 또는 Properties)
- [ ] OAuth2 Provider - auth.sso.provider (STRING, 예: google, azure, okta, custom)
- [ ] Client ID - auth.sso.oauth2.clientId (STRING)
- [ ] Client Secret - auth.sso.oauth2.clientSecret (STRING, **환경 변수 필수**: ${SSO_CLIENT_SECRET})
- [ ] Authorization URI - auth.sso.oauth2.authorizationUri (STRING)
- [ ] Token URI - auth.sso.oauth2.tokenUri (STRING)
- [ ] User Info URI - auth.sso.oauth2.userInfoUri (STRING)
- [ ] Redirect URI - auth.sso.oauth2.redirectUri (STRING, 예: https://app.example.com/login/oauth2/callback)
- [ ] Scope - auth.sso.oauth2.scope (LIST, 예: openid,profile,email)
- [ ] JWKS URI - auth.sso.oauth2.jwksUri (STRING, JWT 서명 검증용)
- [ ] Issuer - auth.sso.oauth2.issuer (STRING, JWT 발급자 검증용)

**SSO 사용자 매핑:**
- [ ] Username Attribute - auth.sso.userMapping.usernameAttribute (STRING, 기본값: email)
- [ ] Email Attribute - auth.sso.userMapping.emailAttribute (STRING, 기본값: email)
- [ ] Name Attribute - auth.sso.userMapping.nameAttribute (STRING, 기본값: name)
- [ ] Role Attribute - auth.sso.userMapping.roleAttribute (STRING, 예: groups)
- [ ] Role Mapping - auth.sso.roleMapping (JSON, SSO 그룹 → 애플리케이션 Role)
- [ ] 자동 생성 - auth.sso.autoCreateUser (BOOLEAN, 기본값: true)
- [ ] 사용자 업데이트 - 로그인 시 SSO 정보로 최신화

**SSO Logout (SLO - Single Logout):**
- [ ] SLO 지원 - auth.sso.slo.enabled (BOOLEAN, 기본값: true)
- [ ] Logout URI - auth.sso.slo.logoutUri (STRING)
- [ ] Post Logout Redirect - auth.sso.slo.postLogoutRedirectUri (STRING)
- [ ] 세션 전파 - 애플리케이션 로그아웃 시 SSO 로그아웃도 호출

**SSO Health Check:**
- [ ] Token 검증 테스트 - 주기적 OAuth2 Token 발급 테스트
- [ ] Health Check 주기 - auth.sso.healthCheck.intervalSeconds (INT, 기본값: 300)
- [ ] Fallback - SSO 장애 시 AD 또는 LOCAL로 자동 전환
- [ ] 알림 - SSO 장애 감지 시 관리자 알림

**연동 설정 보안 (환경 변수 방식):**
- [ ] **환경 변수 사용** - 모든 민감 정보는 환경 변수로 관리
- [ ] 필수 환경 변수:
  - [ ] AD_BIND_PASSWORD - AD LDAP Bind 비밀번호
  - [ ] SSO_CLIENT_SECRET - OAuth2 Client Secret
  - [ ] JWT_SECRET_KEY - JWT 서명 비밀키 (최소 256비트/32자)
- [ ] Properties 파일 형식 - auth.ad.ldap.bindPassword=${AD_BIND_PASSWORD}
- [ ] 환경 변수 검증 - 서버 부팅 시 필수 환경 변수 존재 여부 확인
- [ ] 환경 변수 미설정 시 - 명확한 에러 메시지와 함께 부팅 실패
- [ ] 로깅 금지 - 환경 변수 값은 절대 로그에 기록하지 않음
- [ ] UI 마스킹 - System Configuration UI에서 환경 변수 값은 ********** 표시
- [ ] 변경 감사 로그 - 연동 설정 활성화/비활성화 변경만 기록 (비밀번호 제외)

**연동 테스트 도구:**
- [ ] AD Connection Test - System Configuration UI에서 LDAP 연결 테스트
- [ ] AD User Search Test - 특정 사용자 검색 테스트
- [ ] SSO Authorization Test - OAuth2 Authorization Code Flow 테스트
- [ ] SSO Token Validation - JWT 서명 및 클레임 검증 테스트
- [ ] 테스트 결과 표시 - 성공/실패, 에러 메시지, 소요 시간

#### Password Reset Policies (비밀번호 초기화 정책)

**⚠️ 비밀번호 리셋 정책 격리:**
- [ ] **모든 비밀번호 리셋은 LOCAL 로그인 전용** - AD/SSO는 각 시스템에서 처리
- [ ] AD 로그인 사용자 - AD 관리 콘솔에서 비밀번호 리셋
- [ ] SSO 로그인 사용자 - SSO 시스템에서 비밀번호 리셋
- [ ] LOCAL 로그인 사용자 - 이메일 기반 셀프 리셋 또는 관리자 리셋

**리셋 방식 (Reset Methods - LOCAL 전용):**
- [ ] 사용자 셀프 리셋 - 이메일 기반 토큰 리셋 (LOCAL 계정만)
- [ ] 관리자 리셋 - 관리자가 임시 비밀번호 발급 (LOCAL 계정만)
- [ ] 리셋 거부 - AD/SSO 계정 리셋 요청 시 403 Forbidden

**사용자 셀프 리셋 - 토큰 생성 (LOCAL 전용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 거부
- [ ] 리셋 요청 - 사용자 이메일 주소 입력 (사내 메일 주소)
- [ ] 이메일 검증 - User.email 존재 여부 확인
- [ ] 이메일 검증 - 계정 상태 확인 (ACTIVE 또는 LOCKED_* 상태만 리셋 가능)
- [ ] 이메일 검증 - loginMethod 확인 (LOCAL만 가능, AD/SSO는 거부)
- [ ] AD/SSO 계정 리셋 요청 - "AD/SSO 계정은 각 시스템에서 비밀번호를 관리합니다" 메시지 반환
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

**사용자 셀프 리셋 - Rate Limiting (LOCAL 전용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 사용자 제외
- [ ] AD/SSO 계정 이메일로 요청 - Rate Limit 카운트하지 않음 (거부됨)
- [ ] 이메일별 Rate Limit - 동일 이메일 1분당 1회 리셋 요청 제한
- [ ] 이메일별 Rate Limit - 동일 이메일 1시간당 3회 리셋 요청 제한
- [ ] 이메일별 Rate Limit - 동일 이메일 24시간당 5회 리셋 요청 제한
- [ ] IP별 Rate Limit - 동일 IP 1분당 3회 리셋 요청 제한
- [ ] IP별 Rate Limit - 동일 IP 1시간당 10회 리셋 요청 제한
- [ ] Rate Limit 저장 - Redis 카운터 사용 (키: "reset_limit:email:{email}", "reset_limit:ip:{ip}")
- [ ] Rate Limit 초과 - 429 Too Many Requests 반환
- [ ] Rate Limit 초과 - 재시도 가능 시간 헤더 포함 (Retry-After)

**사용자 셀프 리셋 API:**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 거부
- [ ] loginMethod 검증 - LOCAL 계정만 토큰 생성, AD/SSO는 거부 메시지
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

**관리자 비밀번호 리셋 (Admin Reset - LOCAL 전용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 사용자 제외
- [ ] AD/SSO 계정 리셋 시도 - 403 Forbidden (각 시스템에서 관리)
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
- [ ] POST /api/v1/admin/users/{userId}/reset-password - 비밀번호 리셋 (LOCAL 전용)
- [ ] loginMethod 검증 - LOCAL 사용자만 허용, AD/SSO는 403 Forbidden
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

**리셋 후 최초 로그인 (관리자 리셋 후 - LOCAL 전용):**
- [ ] **적용 대상: LOCAL 로그인 사용자만** - AD/SSO 사용자 제외
- [ ] AD/SSO 사용자 - mustChangePassword 검사하지 않음 (각 시스템에서 관리)
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

**리셋 테스트 시나리오 (LOCAL 전용):**
- [ ] **모든 리셋 시나리오는 LOCAL 계정 기준** - AD/SSO는 별도 시스템에서 관리
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
- [ ] 시나리오 11: SSO 계정으로 이메일 리셋 요청 → 거부 (LOCAL 계정만 가능)
- [ ] 시나리오 12: 관리자가 AD 사용자에게 임시 비밀번호 발급 시도 → 403 Forbidden
- [ ] 시나리오 13: 관리자가 SSO 사용자에게 임시 비밀번호 발급 시도 → 403 Forbidden

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

