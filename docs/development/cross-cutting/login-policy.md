### Login Policy Management (로그인 정책 관리)

#### Login Policy - Domain Model (도메인 모델)

**정상 케이스:**
- [x] LoginPolicy 생성 - 필수 필드 (id, name, orgId, enabledMethods)
- [x] LoginPolicy 생성 - 활성화된 로그인 방식 리스트 (SSO, AD, LOCAL)
- [x] LoginPolicy 생성 - 기본값 설정 (모든 방식 활성화)
- [x] LoginPolicy 생성 - 우선순위 설정 (priority: SSO > AD > LOCAL)
- [ ] LoginPolicy 생성 - 조직별 정책 설정 (orgId 기준)
- [x] LoginPolicy 생성 - 글로벌 정책 설정 (orgId = null, 기본 정책)

**실패 케이스:**
- [x] LoginPolicy 생성 실패 - null name (IllegalArgumentException)
- [x] LoginPolicy 생성 실패 - 빈 enabledMethods (최소 1개 필요)
- [x] LoginPolicy 생성 실패 - null enabledMethods (IllegalArgumentException)
- [ ] LoginPolicy 생성 실패 - 잘못된 method 타입 (SSO, AD, LOCAL만 허용)
- [ ] LoginPolicy 생성 실패 - 중복된 method (Set 사용)

**비즈니스 로직:**
- [x] isMethodEnabled(method) - 특정 방식 활성화 여부 확인
- [ ] getEnabledMethods() - 활성화된 방식 리스트 반환
- [x] getPrimaryMethod() - 최우선 로그인 방식 반환 (priority 기준)
- [x] disableMethod(method) - 특정 방식 비활성화
- [x] enableMethod(method) - 특정 방식 활성화
- [x] updatePriority(newPriority) - 우선순위 변경

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

