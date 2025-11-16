### Audit Logging

#### Audit Logging - Login Success (로그인 성공)

**정상 케이스:**
- [x] AD 로그인 성공 - 감사 로그 생성 (action: LOGIN_SUCCESS, method: AD)
- [x] SSO 로그인 성공 - 감사 로그 생성 (action: LOGIN_SUCCESS, method: SSO)
- [x] 일반 로그인 성공 - 감사 로그 생성 (action: LOGIN_SUCCESS, method: LOCAL)
- [x] 로그인 성공 감사 로그 - 필수 필드 포함 (userId, employeeId, username, orgId)
- [x] 로그인 성공 감사 로그 - IP 주소 기록 (clientIp, X-Forwarded-For 헤더)
- [x] 로그인 성공 감사 로그 - User-Agent 기록 (브라우저, OS 정보)
- [x] 로그인 성공 감사 로그 - 세션 ID 기록 (sessionId)
- [x] 로그인 성공 감사 로그 - 타임스탬프 기록 (loginAt, UTC)
- [ ] 로그인 성공 감사 로그 - 발급된 토큰 ID 기록 (accessTokenId, refreshTokenId) - TODO: JWT 토큰 발급 기능 구현 후
- [x] 로그인 성공 감사 로그 - Repository save 호출 확인 (모든 테스트에서 verify(auditLogMapper).insert() 검증 완료)

**로그 상세 정보:**
- [ ] 로그인 성공 - details JSON 포함 (roles, permissions, orgName)
- [ ] 로그인 성공 - 이전 로그인 시간 기록 (lastLoginAt)
- [ ] 로그인 성공 - 로그인 횟수 증가 (loginCount++)
- [ ] 로그인 성공 - 지역 정보 기록 (GeoIP lookup, 선택사항)
- [x] 로그인 성공 - Referer 헤더 기록 (loginReferrer)

**보안 정보:**
- [ ] 로그인 성공 - 이전 실패 횟수 리셋 (failedAttempts = 0)
- [ ] 로그인 성공 - 계정 잠금 해제 (lockedUntil = null)
- [ ] 로그인 성공 - 의심스러운 로그인 감지 (새로운 IP, 새로운 디바이스)
- [ ] 로그인 성공 - 비정상 시간대 로그인 감지 (근무시간 외)
- [ ] 로그인 성공 - 다중 지역 로그인 감지 (지리적으로 불가능한 위치)

#### Audit Logging - Login Failure (로그인 실패)

**정상 케이스:**
- [x] AD 로그인 실패 - 감사 로그 생성 (action: LOGIN_FAILURE, method: AD)
- [x] SSO 로그인 실패 - 감사 로그 생성 (action: LOGIN_FAILURE, method: SSO)
- [x] 일반 로그인 실패 - 감사 로그 생성 (action: LOGIN_FAILURE, method: LOCAL)
- [x] 로그인 실패 감사 로그 - 실패 사유 기록 (reason: INVALID_CREDENTIALS) - 기존 테스트에서 검증 완료
- [x] 로그인 실패 감사 로그 - 입력된 사원ID 기록 (attemptedEmployeeId) - 기존 테스트에서 검증 완료
- [x] 로그인 실패 감사 로그 - IP 주소 기록
- [x] 로그인 실패 감사 로그 - User-Agent 기록
- [x] 로그인 실패 감사 로그 - 타임스탬프 기록 - 기존 테스트에서 검증 완료
- [ ] 로그인 실패 감사 로그 - 실패 횟수 증가 (failedAttempts++) - TODO: UserService 책임
- [x] 로그인 실패 감사 로그 - Repository save 호출 확인 - 기존 테스트에서 검증 완료

**실패 사유 분류:**
- [x] 로그인 실패 - INVALID_CREDENTIALS (잘못된 사원ID 또는 비밀번호) - 기존 테스트에서 검증 완료
- [x] 로그인 실패 - ACCOUNT_DISABLED (비활성화된 계정)
- [x] 로그인 실패 - ACCOUNT_LOCKED (잠긴 계정)
- [x] 로그인 실패 - ACCOUNT_EXPIRED (만료된 계정)
- [x] 로그인 실패 - CREDENTIALS_EXPIRED (비밀번호 만료)
- [x] 로그인 실패 - AD_SERVER_UNAVAILABLE (AD 서버 장애)
- [x] 로그인 실패 - SSO_SERVER_UNAVAILABLE (SSO 서버 장애)
- [x] 로그인 실패 - INVALID_SSO_TOKEN (잘못된 SSO 토큰) - 기존 테스트에서 검증 완료
- [x] 로그인 실패 - BRUTE_FORCE_DETECTED (무차별 대입 공격 감지)

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

