#### JWT Token Management - Token Generation (생성)

**정상 케이스:**
- [x] JWT Access Token 생성 - 유효한 사용자 정보로 생성
- [x] JWT Refresh Token 생성 - 유효한 사용자 정보로 생성
- [x] JWT 클레임 포함 - subject (employeeId), userId, name, email
- [ ] JWT 클레임 포함 - iat (발급시간), exp (만료시간), iss (발급자)
- [ ] JWT 헤더 포함 - alg (HS256), typ (JWT)
- [x] JWT 만료시간 설정 - Access Token 1시간 (3600초)
- [x] JWT 만료시간 설정 - Refresh Token 24시간 (86400초)
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
- [x] JWT 검증 성공 - 유효한 Access Token
- [x] JWT 검증 성공 - 유효한 Refresh Token
- [x] JWT 검증 성공 - 올바른 서명
- [ ] JWT 검증 성공 - 만료시간 이전
- [ ] JWT 검증 성공 - 올바른 발급자 (iss)
- [ ] JWT 클레임 추출 - subject (userId) 정확히 추출
- [x] JWT 클레임 추출 - employeeId 정확히 추출
- [ ] JWT 클레임 추출 - roles 리스트 정확히 추출
- [ ] JWT 클레임 추출 - orgId 정확히 추출
- [x] JWT 클레임 추출 - 만료시간 (exp) 정확히 추출

**실패 케이스 - 만료:**
- [x] JWT 검증 실패 - 만료된 Access Token (ExpiredJwtException)
- [x] JWT 검증 실패 - 만료된 Refresh Token (ExpiredJwtException)
- [ ] JWT 검증 실패 - 만료시간이 과거 (exp < now)
- [ ] JWT 검증 실패 - 만료 1초 후 (경계값 테스트)

**실패 케이스 - 서명:**
- [x] JWT 검증 실패 - 잘못된 서명 (SignatureException)
- [ ] JWT 검증 실패 - 다른 비밀키로 생성된 토큰
- [ ] JWT 검증 실패 - 서명 부분 변조된 토큰
- [ ] JWT 검증 실패 - 페이로드 변조 후 재서명 안 된 토큰
- [ ] JWT 검증 실패 - 알고리즘 변조 (alg: none)

**실패 케이스 - 형식:**
- [x] JWT 검증 실패 - null 토큰 (IllegalArgumentException)
- [x] JWT 검증 실패 - 빈 문자열 토큰 (MalformedJwtException)
- [x] JWT 검증 실패 - 잘못된 형식 (점 2개 미만)
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

**Token Type 검증:**
- [x] Access Token 타입이 맞는지 검증한다
- [x] Refresh Token 타입이 맞는지 검증한다
- [x] Access Token을 Refresh Token으로 사용할 수 없다
- [x] Refresh Token을 Access Token으로 사용할 수 없다

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
