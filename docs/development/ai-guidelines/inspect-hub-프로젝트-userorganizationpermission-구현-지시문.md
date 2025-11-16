# 🎯 Inspect-Hub 프로젝트: User/Organization/Permission 구현 지시문

> **AI 에이전트가 Inspect-Hub 프로젝트의 User/Organization/Permission/Policy 기능을 구현할 때 반드시 따라야 할 실행 규칙**
>
> **중요**: 이 섹션의 모든 규칙은 **MUST (필수)** 준수사항입니다.

---

## 📐 아키텍처 설계 원칙

### 1. 엔티티 구조 (MUST)

**User/Organization/Permission 엔티티를 구현할 때 다음 구조를 반드시 따르십시오:**

```java
/**
 * ✅ REQUIRED: User 엔티티
 * 
 * 필수 필드:
 * - userId (ULID)
 * - employeeId (사원번호, UNIQUE)
 * - orgId (조직 ID, FK to Organization)
 * - status (ACTIVE/INACTIVE/LOCKED)
 * 
 * 금지:
 * - ❌ role 필드 직접 저장 (PermissionGroup 사용)
 * - ❌ permissions List 필드 (별도 테이블)
 */
@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @Column(name = "user_id", length = 26)
    private String userId;                     // ULID (MUST)
    
    @Column(name = "employee_id", unique = true, nullable = false, length = 50)
    private String employeeId;                 // 사원번호 (MUST)
    
    @Column(name = "org_id", nullable = false, length = 26)
    private String orgId;                      // 조직 ID (MUST)
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;                     // ACTIVE/INACTIVE/LOCKED (MUST)
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;                   // BCrypt 암호화 (MUST)
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // ❌ FORBIDDEN: 직접 role 필드
    // private String role;  // 사용 금지!
}

/**
 * ✅ REQUIRED: Organization 엔티티
 * 
 * 필수 필드:
 * - orgId (ULID)
 * - orgPath (계층 경로, Materialized Path Pattern)
 * - level (계층 레벨)
 * - parentOrgId (상위 조직)
 */
@Entity
@Table(name = "organization")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Organization {
    
    @Id
    @Column(name = "org_id", length = 26)
    private String orgId;                      // ULID (MUST)
    
    @Column(name = "org_name", nullable = false, length = 100)
    private String orgName;
    
    @Column(name = "org_code", unique = true, nullable = false, length = 50)
    private String orgCode;                    // 조직 코드 (MUST)
    
    @Column(name = "parent_org_id", length = 26)
    private String parentOrgId;                // 상위 조직 (MUST for hierarchy)
    
    @Column(name = "org_path", nullable = false, length = 500)
    private String orgPath;                    // "/본사/서울지점/준법감시팀" (MUST)
    
    @Column(name = "level", nullable = false)
    private Integer level;                     // 1: 본사, 2: 지점, 3: 팀 (MUST)
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

/**
 * ✅ REQUIRED: Permission 엔티티 (Feature-Action 기반)
 * 
 * 필수 규칙:
 * - feature + action 조합으로 권한 정의
 * - feature: "user", "case", "policy", "report" 등
 * - action: "read", "write", "approve", "delete" 등
 */
@Entity
@Table(name = "permission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Permission {
    
    @Id
    @Column(name = "permission_id", length = 26)
    private String permissionId;               // ULID (MUST)
    
    @Column(name = "feature", nullable = false, length = 50)
    private String feature;                    // "case", "policy", "user" (MUST)
    
    @Column(name = "action", nullable = false, length = 50)
    private String action;                     // "read", "write", "approve" (MUST)
    
    @Column(name = "description", length = 200)
    private String description;
    
    /**
     * 권한 코드 생성: feature:action
     */
    public String getCode() {
        return feature + ":" + action;
    }
}

/**
 * ✅ REQUIRED: PermissionGroup 엔티티 (역할 대체)
 * 
 * 역할(Role) 대신 PermissionGroup 사용
 */
@Entity
@Table(name = "permission_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PermissionGroup {
    
    @Id
    @Column(name = "group_id", length = 26)
    private String groupId;                    // ULID (MUST)
    
    @Column(name = "group_name", nullable = false, unique = true, length = 100)
    private String groupName;                  // "INVESTIGATOR", "APPROVER_ORG" (MUST)
    
    @Column(name = "description", length = 500)
    private String description;
}

/**
 * ✅ REQUIRED: DataPolicy 엔티티 (RowScope, FieldMask 정책)
 */
@Entity
@Table(name = "data_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DataPolicy {
    
    @Id
    @Column(name = "policy_id", length = 26)
    private String policyId;                   // ULID (MUST)
    
    @Column(name = "group_id", nullable = false, length = 26)
    private String groupId;                    // FK to PermissionGroup (MUST)
    
    @Column(name = "feature", nullable = false, length = 50)
    private String feature;                    // "case", "customer" (MUST)
    
    @Column(name = "row_scope", nullable = false, length = 20)
    private String rowScope;                   // OWN/ORG/ORG_HIERARCHY/ALL (MUST)
    
    @Column(name = "field_mask_json", columnDefinition = "TEXT")
    private String fieldMaskJson;              // JSON: {"ssn": "PARTIAL", "email": "EMAIL"}
}
```

---

## 🔧 구현 규칙

### 규칙 1: Feature-Action 권한 체계 필수 사용

**❌ 금지: 단일 역할 문자열 저장**
```java
// ❌ BAD: 이렇게 하지 마십시오
@Column(name = "role")
private String role;  // "ADMIN", "USER" 등

@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() { ... }
```

**✅ 필수: Feature-Action 기반 권한**
```java
// ✅ GOOD: 이렇게 구현하십시오
@RequirePermission(feature = "user", action = "delete")
public void deleteUser(String userId) {
    // 구현...
}

@RequirePermission(feature = "case", action = "approve")
public void approveCase(String caseId) {
    // 구현...
}

@RequirePermission(feature = "policy", action = "deploy")
public void deployPolicy(String policyId) {
    // 구현...
}
```

---

### 규칙 2: @RequirePermission 어노테이션 사용

**모든 비즈니스 로직 메서드에 @RequirePermission 필수 적용:**

```java
/**
 * ✅ REQUIRED: @RequirePermission 어노테이션 정의
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String feature();
    String action();
}

/**
 * ✅ REQUIRED: AOP로 권한 검증
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    
    private final PermissionService permissionService;
    
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String feature = requirePermission.feature();
        String action = requirePermission.action();
        
        boolean hasPermission = permissionService.hasPermission(
            userId, 
            feature, 
            action
        );
        
        if (!hasPermission) {
            throw new AccessDeniedException(
                String.format("권한 없음: %s:%s", feature, action)
            );
        }
        
        // 감사 로그 기록 (MUST)
        auditLogger.log(userId, feature, action, "PERMISSION_CHECK", "SUCCESS");
    }
}
```

---

### 규칙 3: 조직 계층 구현 시 orgPath 사용 필수

**❌ 금지: 재귀 쿼리로 조직 계층 탐색**
```java
// ❌ BAD: 성능 저하
public List<Organization> getChildOrganizations(String orgId) {
    // 재귀로 하위 조직 찾기 - O(n) 복잡도
    List<Organization> children = new ArrayList<>();
    // ... 재귀 로직
    return children;
}
```

**✅ 필수: orgPath 기반 단일 쿼리**
```java
// ✅ GOOD: Materialized Path Pattern
@Mapper
public interface OrganizationMapper {
    
    /**
     * 하위 조직 조회 (단일 쿼리)
     */
    @Select("""
        SELECT * FROM organization
        WHERE org_path LIKE CONCAT(#{orgPath}, '%')
          AND org_id != #{orgId}
        ORDER BY level ASC
    """)
    List<Organization> findChildOrganizations(
        @Param("orgId") String orgId,
        @Param("orgPath") String orgPath
    );
    
    /**
     * 상위 조직 조회 (단일 쿼리)
     */
    @Select("""
        SELECT * FROM organization
        WHERE #{orgPath} LIKE CONCAT(org_path, '%')
          AND org_id != #{orgId}
        ORDER BY level ASC
    """)
    List<Organization> findParentOrganizations(
        @Param("orgId") String orgId,
        @Param("orgPath") String orgPath
    );
}
```

---

### 규칙 4: RowScope 정책 적용 필수

**모든 목록 조회 API는 RowScope 정책을 적용해야 합니다:**

```java
/**
 * ✅ REQUIRED: RowScope 기반 데이터 필터링
 */
@Service
@RequiredArgsConstructor
public class CaseQueryService {
    
    private final CaseMapper caseMapper;
    private final DataPolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    
    /**
     * 사례 목록 조회 (RowScope 적용 필수)
     */
    public List<Case> listCases(String userId, CaseSearchCriteria criteria) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId).orElseThrow();
        
        // 2. 데이터 정책 조회
        DataPolicy policy = policyRepository.findByUserAndFeature(userId, "case");
        
        // 3. RowScope에 따른 필터링
        List<Case> cases;
        switch (policy.getRowScope()) {
            case "OWN":
                // 본인이 생성한 사례만
                cases = caseMapper.findWithRowLevelSecurity(
                    "OWN", userId, null, null
                );
                break;
            
            case "ORG":
                // 동일 조직 사례만
                cases = caseMapper.findWithRowLevelSecurity(
                    "ORG", userId, user.getOrgId(), null
                );
                break;
            
            case "ORG_HIERARCHY":
                // 본인 조직 + 하위 조직 사례
                Organization org = organizationRepository.findById(user.getOrgId()).orElseThrow();
                List<String> orgIds = getOrgHierarchyIds(org.getOrgPath());
                cases = caseMapper.findWithRowLevelSecurity(
                    "ORG_HIERARCHY", userId, null, orgIds
                );
                break;
            
            case "ALL":
                // 모든 사례 (관리자)
                cases = caseMapper.findAll();
                break;
            
            default:
                throw new IllegalStateException("Unknown RowScope: " + policy.getRowScope());
        }
        
        // 4. Field Masking 적용 (MUST)
        return cases.stream()
            .map(c -> maskSensitiveFields(c, userId))
            .collect(Collectors.toList());
    }
}
```

---

### 규칙 5: Field Masking 적용 필수

**모든 응답 DTO는 Field Masking을 거쳐야 합니다:**

```java
/**
 * ✅ REQUIRED: @Sensitive 어노테이션으로 마스킹 대상 표시
 */
@Getter
@Builder
public class CustomerResponse {
    
    private String customerId;
    private String name;
    
    @Sensitive(maskType = MaskType.PARTIAL)
    private String ssn;                        // 주민등록번호
    
    @Sensitive(maskType = MaskType.EMAIL)
    private String email;
    
    @Sensitive(maskType = MaskType.PHONE)
    private String phoneNumber;
}

/**
 * ✅ REQUIRED: Service 레이어에서 마스킹 적용
 */
@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final FieldMaskingService maskingService;
    
    public CustomerResponse getCustomer(String customerId, String viewerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        
        CustomerResponse response = CustomerResponse.builder()
            .customerId(customer.getCustomerId())
            .name(customer.getName())
            .ssn(customer.getSsn())
            .email(customer.getEmail())
            .phoneNumber(customer.getPhoneNumber())
            .build();
        
        // Field Masking 적용 (MUST)
        return maskingService.maskSensitiveFields(response, viewerId);
    }
}
```

---

### 규칙 6: Separation of Duties (SoD) 검증 필수

**모든 승인 로직에는 SoD 검증이 필수입니다:**

```java
/**
 * ✅ REQUIRED: SoD 검증 로직
 */
@Service
@RequiredArgsConstructor
public class CaseApprovalService {
    
    private final SeparationOfDutiesService sodService;
    private final AuditLogRepository auditLogRepository;
    
    @RequirePermission(feature = "case", action = "approve")
    @Transactional
    public void approveCase(String caseId, String approverId) {
        // 1. SoD 검증 (MUST)
        sodService.validateSoD(caseId, approverId, "APPROVE");
        
        // 2. 승인 체인 검증 (MUST)
        sodService.validateApprovalChain(caseId, approverId);
        
        // 3. 비즈니스 로직
        Case caseObj = caseRepository.findById(caseId).orElseThrow();
        caseObj.approve(approverId);
        caseRepository.update(caseObj);
        
        // 4. 감사 로그 (MUST)
        auditLogRepository.insert(AuditLog.builder()
            .userId(approverId)
            .action("APPROVE")
            .resource("CASE")
            .resourceId(caseId)
            .result("SUCCESS")
            .timestamp(LocalDateTime.now())
            .build()
        );
    }
}
```

---

### 규칙 7: 권한 캐싱 필수

**권한 계산 결과는 반드시 캐싱해야 합니다:**

```java
/**
 * ✅ REQUIRED: Redis 기반 권한 캐싱
 */
@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final RedisTemplate<String, Set<Permission>> redisTemplate;
    private static final String CACHE_PREFIX = "permission:user:";
    private static final long CACHE_TTL = 300; // 5분 (MUST)
    
    /**
     * 유효 권한 조회 (캐싱 적용)
     */
    @Cacheable(value = "userPermissions", key = "#userId")
    public Set<Permission> getEffectivePermissions(String userId) {
        String cacheKey = CACHE_PREFIX + userId;
        
        // 1. 캐시 조회
        Set<Permission> cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 2. 계산 (복잡한 조직 계층 순회)
        Set<Permission> permissions = calculatePermissions(userId);
        
        // 3. 캐시 저장 (MUST)
        redisTemplate.opsForValue().set(
            cacheKey, 
            permissions, 
            CACHE_TTL, 
            TimeUnit.SECONDS
        );
        
        return permissions;
    }
    
    /**
     * 캐시 무효화 (권한 변경 시 MUST)
     */
    @CacheEvict(value = "userPermissions", key = "#userId")
    public void invalidateUserPermissions(String userId) {
        redisTemplate.delete(CACHE_PREFIX + userId);
    }
}
```

---

### 규칙 8: 감사 로그 100% 기록 필수

**권한 관련 모든 작업은 감사 로그에 기록해야 합니다:**

```java
/**
 * ✅ REQUIRED: 감사 로그 AOP
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLoggingAspect {
    
    private final AuditLogRepository auditLogRepository;
    
    /**
     * 권한 체크 후 로그 기록 (MUST)
     */
    @AfterReturning(
        pointcut = "@annotation(requirePermission)",
        returning = "result"
    )
    public void logPermissionCheck(
        JoinPoint joinPoint, 
        RequirePermission requirePermission,
        Object result
    ) {
        String userId = getCurrentUserId();
        
        auditLogRepository.insert(AuditLog.builder()
            .userId(userId)
            .action("PERMISSION_CHECK")
            .resource(requirePermission.feature())
            .resourceId(requirePermission.action())
            .result("SUCCESS")
            .timestamp(LocalDateTime.now())
            .build()
        );
    }
    
    /**
     * 권한 체크 실패 로그 (MUST)
     */
    @AfterThrowing(
        pointcut = "@annotation(requirePermission)",
        throwing = "ex"
    )
    public void logPermissionDenied(
        JoinPoint joinPoint,
        RequirePermission requirePermission,
        Exception ex
    ) {
        String userId = getCurrentUserId();
        
        auditLogRepository.insert(AuditLog.builder()
            .userId(userId)
            .action("PERMISSION_CHECK")
            .resource(requirePermission.feature())
            .resourceId(requirePermission.action())
            .result("FAILURE")
            .errorMessage(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build()
        );
    }
}
```

---

## 🧪 테스트 작성 규칙

### 규칙 9: 권한 시스템 테스트 필수 작성

**모든 권한 관련 기능은 다음 테스트를 반드시 작성해야 합니다:**

```java
/**
 * ✅ REQUIRED: 권한 검증 테스트
 */
@SpringBootTest
@Transactional
class PermissionSystemTest {
    
    @Test
    @DisplayName("Feature-Action 권한이 없으면 접근이 거부된다")
    void shouldDenyAccessWhenNoPermission() {
        // Given
        User user = createUser("john", "org1");
        // john에게는 "case:read" 권한만 있음
        assignPermission(user, "case", "read");
        
        // When & Then
        assertThatThrownBy(() -> 
            caseService.approveCase("case123", user.getUserId()))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("case:approve");
    }
    
    @Test
    @DisplayName("RowScope=OWN 정책 적용 시 본인 데이터만 조회된다")
    void shouldReturnOnlyOwnDataWhenRowScopeIsOwn() {
        // Given
        User john = createUser("john", "org1");
        User jane = createUser("jane", "org1");
        
        Case johnCase = createCase("case1", john.getUserId());
        Case janeCase = createCase("case2", jane.getUserId());
        
        DataPolicy policy = createPolicy(john, "case", "OWN");
        
        // When
        List<Case> cases = caseQueryService.listCases(john.getUserId(), new CaseSearchCriteria());
        
        // Then
        assertThat(cases)
            .hasSize(1)
            .extracting(Case::getCaseId)
            .containsExactly("case1");
    }
    
    @Test
    @DisplayName("Field Masking 적용 시 민감 정보가 마스킹된다")
    void shouldMaskSensitiveFieldsWhenPolicyApplies() {
        // Given
        Customer customer = createCustomer("123-45-6789", "john@example.com");
        User viewer = createUser("viewer", "org1");
        DataPolicy policy = createPolicyWithMasking(viewer, "customer", 
            Map.of("ssn", "PARTIAL", "email", "EMAIL"));
        
        // When
        CustomerResponse response = customerService.getCustomer(
            customer.getCustomerId(), 
            viewer.getUserId()
        );
        
        // Then
        assertThat(response.getSsn()).isEqualTo("***-**-6789");
        assertThat(response.getEmail()).isEqualTo("j***n@example.com");
    }
    
    @Test
    @DisplayName("Maker-Checker 원칙: 생성자와 승인자가 같으면 예외가 발생한다")
    void shouldThrowExceptionWhenMakerAndCheckerAreSame() {
        // Given
        String caseId = createCase("john");
        
        // When & Then
        assertThatThrownBy(() -> 
            caseApprovalService.approveCase(caseId, "john"))
            .isInstanceOf(SeparationOfDutiesViolationException.class)
            .hasMessageContaining("cannot both create and APPROVE");
    }
    
    @Test
    @DisplayName("조직 계층 기반 RLS: 하위 조직 데이터도 조회된다")
    void shouldReturnChildOrgDataWhenRowScopeIsOrgHierarchy() {
        // Given
        Organization hq = createOrg("본사", null, "/본사", 1);
        Organization branch = createOrg("지점", hq, "/본사/지점", 2);
        Organization team = createOrg("팀", branch, "/본사/지점/팀", 3);
        
        User manager = createUser("manager", hq.getOrgId());
        
        Case hqCase = createCase("case1", hq.getOrgId());
        Case branchCase = createCase("case2", branch.getOrgId());
        Case teamCase = createCase("case3", team.getOrgId());
        
        DataPolicy policy = createPolicy(manager, "case", "ORG_HIERARCHY");
        
        // When
        List<Case> cases = caseQueryService.listCases(manager.getUserId(), new CaseSearchCriteria());
        
        // Then: 본사 + 지점 + 팀 모든 사례 조회
        assertThat(cases).hasSize(3);
    }
}
```

---

## 📋 구현 체크리스트

**AI 에이전트는 User/Organization/Permission 기능을 구현할 때 다음을 확인하십시오:**

### ✅ 엔티티 설계
- [ ] User 엔티티에 userId (ULID), employeeId, orgId, status 필드 포함
- [ ] Organization 엔티티에 orgPath, level, parentOrgId 필드 포함
- [ ] Permission 엔티티를 Feature-Action 기반으로 설계
- [ ] PermissionGroup 엔티티로 역할(Role) 대체
- [ ] DataPolicy 엔티티에 rowScope, fieldMaskJson 필드 포함
- [ ] ❌ User 엔티티에 role 문자열 필드 사용 금지

### ✅ 권한 검증
- [ ] @RequirePermission(feature, action) 어노테이션 정의
- [ ] AOP로 권한 검증 로직 구현
- [ ] 모든 비즈니스 메서드에 @RequirePermission 적용
- [ ] 권한 없을 시 AccessDeniedException 발생

### ✅ 조직 계층
- [ ] orgPath 필드로 Materialized Path Pattern 구현
- [ ] 단일 쿼리로 하위/상위 조직 조회
- [ ] ❌ 재귀 쿼리 사용 금지

### ✅ Row-Level Security
- [ ] RowScope 정책 정의 (OWN/ORG/ORG_HIERARCHY/ALL)
- [ ] 모든 목록 조회 API에 RowScope 필터링 적용
- [ ] MyBatis Dynamic SQL로 조건부 WHERE 절 구현

### ✅ Field-Level Masking
- [ ] @Sensitive 어노테이션 정의
- [ ] MaskType별 마스킹 로직 구현 (FULL/PARTIAL/EMAIL/PHONE)
- [ ] 모든 응답 DTO에 마스킹 적용

### ✅ Separation of Duties
- [ ] Maker-Checker 검증 로직 구현
- [ ] 승인 체인 검증 로직 구현
- [ ] 중복 승인 방지

### ✅ 성능 최적화
- [ ] Redis로 권한 계산 결과 캐싱 (TTL: 5분)
- [ ] 권한 변경 시 캐시 무효화
- [ ] @Cacheable 어노테이션 사용

### ✅ 감사 로깅
- [ ] 모든 권한 체크에 감사 로그 기록
- [ ] 권한 변경 시 감사 로그 기록
- [ ] 성공/실패 모두 로그에 기록

### ✅ 테스트
- [ ] 권한 검증 테스트 작성
- [ ] RowScope 필터링 테스트 작성
- [ ] Field Masking 테스트 작성
- [ ] SoD 검증 테스트 작성
- [ ] 조직 계층 RLS 테스트 작성

---

## 🚨 금지 사항 (MUST NOT)

**절대로 다음과 같이 구현하지 마십시오:**

### ❌ 금지 1: Role 문자열 직접 저장
```java
// ❌ FORBIDDEN
@Column(name = "role")
private String role;  // "ADMIN", "USER"
```

### ❌ 금지 2: hasRole() 직접 사용
```java
// ❌ FORBIDDEN
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() { ... }
```

### ❌ 금지 3: 재귀 쿼리로 조직 계층 탐색
```java
// ❌ FORBIDDEN: O(n) 성능 저하
public List<Organization> getChildren(String orgId) {
    // 재귀 로직...
}
```

### ❌ 금지 4: RowScope 정책 미적용
```java
// ❌ FORBIDDEN: 모든 데이터 노출
public List<Case> listCases() {
    return caseRepository.findAll();  // 보안 위험!
}
```

### ❌ 금지 5: Field Masking 미적용
```java
// ❌ FORBIDDEN: 민감 정보 노출
public CustomerResponse getCustomer(String customerId) {
    return customerRepository.findById(customerId);  // 마스킹 없음!
}
```

### ❌ 금지 6: SoD 검증 생략
```java
// ❌ FORBIDDEN: 내부 부정 위험
public void approveCase(String caseId, String approverId) {
    // SoD 검증 없이 바로 승인 - 위험!
    caseRepository.approve(caseId, approverId);
}
```

### ❌ 금지 7: 권한 캐싱 생략
```java
// ❌ FORBIDDEN: 성능 저하
public Set<Permission> getPermissions(String userId) {
    // 매번 복잡한 계산 - 비효율적!
    return calculatePermissionsWithHierarchy(userId);
}
```

### ❌ 금지 8: 감사 로그 생략
```java
// ❌ FORBIDDEN: 추적 불가
public void changePermission(String userId, String permission) {
    // 로그 없이 권한 변경 - 감사 불가!
    userRepository.updatePermission(userId, permission);
}
```

---

## 📝 코드 생성 템플릿

**AI 에이전트는 다음 템플릿을 사용하여 코드를 생성하십시오:**

### 템플릿 1: Service 메서드 구현

```java
/**
 * ✅ TEMPLATE: 권한 검증이 포함된 Service 메서드
 */
@Service
@RequiredArgsConstructor
public class XxxService {
    
    private final XxxRepository xxxRepository;
    private final RowLevelSecurityService rlsService;
    private final FieldMaskingService maskingService;
    private final AuditLogRepository auditLogRepository;
    
    @RequirePermission(feature = "xxx", action = "read")
    public List<XxxResponse> listXxx(String userId, XxxSearchCriteria criteria) {
        // 1. RowScope 필터링 적용 (MUST)
        List<Xxx> items = rlsService.filterByRowScope(userId, "xxx", criteria);
        
        // 2. DTO 변환
        List<XxxResponse> responses = items.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        // 3. Field Masking 적용 (MUST)
        responses = responses.stream()
            .map(r -> maskingService.maskSensitiveFields(r, userId))
            .collect(Collectors.toList());
        
        // 4. 감사 로그 기록 (MUST)
        auditLogRepository.insert(AuditLog.builder()
            .userId(userId)
            .action("LIST")
            .resource("XXX")
            .result("SUCCESS")
            .timestamp(LocalDateTime.now())
            .build()
        );
        
        return responses;
    }
    
    @RequirePermission(feature = "xxx", action = "approve")
    @Transactional
    public void approveXxx(String xxxId, String approverId) {
        // 1. SoD 검증 (MUST)
        sodService.validateSoD(xxxId, approverId, "APPROVE");
        
        // 2. 승인 체인 검증 (MUST)
        sodService.validateApprovalChain(xxxId, approverId);
        
        // 3. 비즈니스 로직
        Xxx xxx = xxxRepository.findById(xxxId).orElseThrow();
        xxx.approve(approverId);
        xxxRepository.update(xxx);
        
        // 4. 감사 로그 (MUST)
        auditLogRepository.insert(AuditLog.builder()
            .userId(approverId)
            .action("APPROVE")
            .resource("XXX")
            .resourceId(xxxId)
            .result("SUCCESS")
            .timestamp(LocalDateTime.now())
            .build()
        );
    }
}
```

### 템플릿 2: MyBatis Mapper (RowScope 적용)

```xml
<!-- ✅ TEMPLATE: RowScope 기반 Dynamic SQL -->
<mapper namespace="com.inspecthub.xxx.infrastructure.XxxMapper">
    
    <select id="findWithRowLevelSecurity" resultType="Xxx">
        SELECT * FROM xxx
        WHERE deleted = FALSE
        
        <!-- RowScope 기반 필터링 (MUST) -->
        <if test="rowScope == 'OWN'">
            AND created_by = #{userId}
        </if>
        
        <if test="rowScope == 'ORG'">
            AND org_id = #{orgId}
        </if>
        
        <if test="rowScope == 'ORG_HIERARCHY'">
            AND org_id IN
            <foreach item="id" collection="orgIds" open="(" separator="," close=")">
                #{id}
            </foreach>
        </if>
        
        <!-- Additional criteria -->
        <if test="criteria.status != null">
            AND status = #{criteria.status}
        </if>
        
        ORDER BY created_at DESC
    </select>
</mapper>
```

---

## 🎓 요약: AI 에이전트 실행 지침

**User/Organization/Permission 기능 구현 시:**

1. **엔티티 설계** → Feature-Action 기반 Permission, orgPath 기반 Organization
2. **권한 검증** → @RequirePermission + AOP
3. **데이터 보호** → RowScope 필터링 + Field Masking
4. **보안 강화** → SoD 검증 + 권한 캐싱
5. **감사 추적** → 100% 감사 로깅
6. **테스트** → 권한/RLS/Masking/SoD 테스트 필수

**절대 금지:**
- ❌ Role 문자열 직접 저장
- ❌ hasRole() 사용
- ❌ 재귀 쿼리
- ❌ RowScope 미적용
- ❌ Field Masking 생략
- ❌ SoD 검증 생략
- ❌ 캐싱 생략
- ❌ 감사 로그 생략

---
