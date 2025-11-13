# TDD + DDD 워크플로우 가이드

> **Kent Beck의 Test-Driven Development + Domain-Driven Design 통합 개발 방법론**
>
> **Version**: 1.0
> **Last Updated**: 2025-01-13
> **Target**: Inspect-Hub AML Compliance System

---

## 📚 목차

1. [개요](#개요)
2. [핵심 원칙](#핵심-원칙)
3. [TDD 사이클](#tdd-사이클)
4. [Tidy First 원칙](#tidy-first-원칙)
5. [DDD 레이어별 TDD 적용](#ddd-레이어별-tdd-적용)
6. [Commit 규칙](#commit-규칙)
7. [코드 품질 기준](#코드-품질-기준)
8. [리팩토링 가이드라인](#리팩토링-가이드라인)
9. [실전 워크플로우](#실전-워크플로우)
10. [도구 및 설정](#도구-및-설정)

---

## 개요

### TDD (Test-Driven Development)란?

**테스트 주도 개발**은 Kent Beck이 정립한 소프트웨어 개발 방법론으로, **테스트를 먼저 작성하고 이를 통과시키기 위한 최소한의 코드를 구현**하는 접근법입니다.

### DDD (Domain-Driven Design)란?

**도메인 주도 설계**는 Eric Evans가 제안한 설계 방법론으로, **비즈니스 도메인을 중심으로 소프트웨어를 설계**하는 접근법입니다.

### 왜 TDD + DDD인가?

| TDD | DDD | 시너지 효과 |
|-----|-----|------------|
| 테스트 우선 작성 | 도메인 모델 중심 설계 | 도메인 로직의 정확성 보장 |
| 빠른 피드백 | 레이어 분리 | 각 레이어별 독립적 테스트 가능 |
| 리팩토링 안정성 | 명확한 책임 분리 | 안전한 구조 개선 |
| 최소 구현 | Aggregate 경계 명확화 | 불필요한 의존성 제거 |

---

## 핵심 원칙

### ROLE AND EXPERTISE

**당신은 시니어 소프트웨어 엔지니어입니다:**
- Kent Beck의 TDD 원칙을 정확히 따릅니다
- Tidy First 원칙으로 구조적 변경과 행동적 변경을 분리합니다
- 높은 코드 품질을 유지합니다

### CORE DEVELOPMENT PRINCIPLES

1. **TDD 사이클 준수**: Red → Green → Refactor
2. **가장 단순한 실패 테스트 먼저 작성**
3. **테스트를 통과시키기 위한 최소한의 코드 구현**
4. **테스트가 통과한 후에만 리팩토링**
5. **구조적 변경과 행동적 변경 분리** (Tidy First)
6. **높은 코드 품질 유지**

---

## TDD 사이클

### Red → Green → Refactor

```
┌─────────────────────────────────────────┐
│          1. Red (실패 테스트)            │
│  - 실패하는 테스트를 먼저 작성           │
│  - 구현하려는 기능의 작은 증분 정의      │
│  - 의미 있는 테스트 이름 사용            │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│         2. Green (테스트 통과)           │
│  - 테스트를 통과시키기 위한 최소 코드    │
│  - 코드 품질은 일단 무시                │
│  - "그냥 작동하게 만들기"                │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│        3. Refactor (리팩토링)            │
│  - 테스트는 계속 통과하는 상태 유지      │
│  - 중복 제거                            │
│  - 의도 명확화                          │
│  - 각 리팩토링 후 테스트 실행            │
└─────────────────┬───────────────────────┘
                  │
                  └─────► 다음 기능으로 반복
```

### TDD 방법론 가이드

#### 1단계: Red (실패 테스트 작성)

**DO:**
- ✅ 작은 기능 증분을 정의하는 테스트 작성
- ✅ 의미 있는 테스트 이름 사용 (예: `shouldRejectDuplicatePolicyName`)
- ✅ 명확하고 정보가 풍부한 실패 메시지 작성
- ✅ Given-When-Then 패턴 사용

**DON'T:**
- ❌ 여러 기능을 한 번에 테스트
- ❌ 구현 세부사항 테스트
- ❌ 모호한 테스트 이름 사용

**예제:**
```java
@Test
@DisplayName("중복된 이름의 Policy 생성 시 예외 발생")
void shouldRejectDuplicatePolicyName() {
    // Given
    String duplicateName = "KYC-Standard-v1";
    Policy existingPolicy = Policy.create(new PolicyId(), duplicateName, PolicyType.KYC);
    policyRepository.save(existingPolicy);

    // When & Then
    assertThatThrownBy(() -> {
        Policy newPolicy = Policy.create(new PolicyId(), duplicateName, PolicyType.KYC);
        policyDomainService.validateUniqueness(newPolicy);
    })
    .isInstanceOf(DuplicatePolicyException.class)
    .hasMessageContaining("이미 존재하는 정책 이름입니다");
}
```

#### 2단계: Green (테스트 통과)

**DO:**
- ✅ 테스트를 통과시키기 위한 **최소한**의 코드만 작성
- ✅ 하드코딩도 OK (나중에 리팩토링)
- ✅ 가장 단순한 해결책 사용

**DON'T:**
- ❌ 과도한 일반화
- ❌ 미래를 위한 코드 작성
- ❌ 불필요한 추상화

**예제:**
```java
// 최소 구현 (나중에 리팩토링)
public class PolicyDomainService {

    private final PolicyRepository policyRepository;

    public void validateUniqueness(Policy policy) {
        boolean exists = policyRepository.existsByName(policy.getName());
        if (exists) {
            throw new DuplicatePolicyException("이미 존재하는 정책 이름입니다");
        }
    }
}
```

#### 3단계: Refactor (리팩토링)

**DO:**
- ✅ 중복 제거
- ✅ 의도 명확화 (네이밍 개선)
- ✅ 작은 메서드로 분리
- ✅ 각 리팩토링 후 테스트 실행

**DON'T:**
- ❌ 테스트가 실패하는 상태에서 리팩토링
- ❌ 여러 리팩토링 동시 진행
- ❌ 행동 변경과 구조 변경 혼합

**예제:**
```java
// 리팩토링 후 (의도 명확화, 추출)
public class PolicyDomainService {

    private final PolicyRepository policyRepository;

    public void validateUniqueness(Policy policy) {
        if (isDuplicateName(policy.getName())) {
            throw new DuplicatePolicyException(
                createDuplicateErrorMessage(policy.getName())
            );
        }
    }

    private boolean isDuplicateName(String name) {
        return policyRepository.existsByName(name);
    }

    private String createDuplicateErrorMessage(String name) {
        return String.format("이미 존재하는 정책 이름입니다: %s", name);
    }
}
```

### 결함 수정 시 TDD

**결함 발견 시 프로세스:**

1. **API-level 실패 테스트 작성** (통합 테스트)
2. **문제를 재현하는 최소 단위 테스트 작성** (단위 테스트)
3. **두 테스트 모두 통과시키기**
4. **리팩토링**

**예제:**
```java
// 1. API-level 테스트 (통합 테스트)
@Test
void shouldReturn409WhenCreatingDuplicatePolicy() {
    // Given: 기존 Policy 존재
    CreatePolicyRequest request = new CreatePolicyRequest("KYC-Standard-v1", "KYC");
    policyController.createPolicy(request);

    // When: 중복 이름으로 생성 시도
    CreatePolicyRequest duplicateRequest = new CreatePolicyRequest("KYC-Standard-v1", "KYC");

    // Then: 409 Conflict
    mockMvc.perform(post("/api/v1/policies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateRequest)))
        .andExpect(status().isConflict());
}

// 2. 단위 테스트 (문제 재현)
@Test
void shouldThrowExceptionWhenDuplicateName() {
    // Given
    String name = "KYC-Standard-v1";
    when(policyRepository.existsByName(name)).thenReturn(true);
    Policy policy = Policy.create(new PolicyId(), name, PolicyType.KYC);

    // When & Then
    assertThatThrownBy(() -> policyDomainService.validateUniqueness(policy))
        .isInstanceOf(DuplicatePolicyException.class);
}
```

---

## Tidy First 원칙

### 변경의 두 가지 유형

Kent Beck의 **Tidy First** 원칙은 모든 코드 변경을 두 가지로 분류합니다:

#### 1. STRUCTURAL CHANGES (구조적 변경)

**정의**: 행동을 변경하지 않고 코드를 재배치하는 것

**예시:**
- 변수/메서드/클래스 이름 변경
- 메서드 추출 (Extract Method)
- 코드 이동 (Move Code)
- 파일/패키지 구조 변경
- Import 정리

**체크리스트:**
- [ ] 테스트가 변경 전/후 동일하게 통과하는가?
- [ ] 외부 행동에 변화가 없는가?
- [ ] 변경 전후 diff가 구조만 보여주는가?

#### 2. BEHAVIORAL CHANGES (행동적 변경)

**정의**: 실제 기능을 추가하거나 수정하는 것

**예시:**
- 새로운 기능 추가
- 버그 수정
- 알고리즘 변경
- 비즈니스 로직 수정

**체크리스트:**
- [ ] 새로운 테스트가 추가되었는가?
- [ ] 기존 테스트가 수정되었는가?
- [ ] 외부 행동에 변화가 있는가?

### 원칙

1. **구조적 변경과 행동적 변경을 절대 혼합하지 않는다**
2. **구조적 변경이 필요하면 항상 먼저 수행한다**
3. **구조적 변경 후 테스트를 실행하여 행동 변경이 없음을 검증한다**

### 워크플로우

```
새로운 기능 구현 필요
    ↓
기존 코드 구조가 불편한가?
    ↓
YES → Tidy First (구조적 변경)
    ↓
    1. 구조 개선 (이름 변경, 메서드 추출 등)
    2. 테스트 실행 (모두 통과 확인)
    3. Commit ("refactor: Extract validateUniqueness method")
    ↓
NO → Behavioral Change (행동적 변경)
    ↓
    1. Red: 실패 테스트 작성
    2. Green: 최소 구현
    3. Refactor: 리팩토링
    4. Commit ("feat: Add duplicate policy validation")
```

### 예제: Tidy First vs Behavioral Change

#### ❌ 잘못된 방법 (혼합)

```java
// Commit: "feat: Add duplicate check and refactor naming"
// 구조적 변경 + 행동적 변경 혼합 (BAD)

public class PolicyService {

    // 구조적 변경: 이름 변경 (old: createPolicy)
    public Policy createNewPolicy(CreatePolicyCommand command) {
        // 행동적 변경: 중복 체크 추가
        if (policyRepository.existsByName(command.getName())) {
            throw new DuplicatePolicyException();
        }

        Policy policy = new Policy(command.getName(), command.getType());
        return policyRepository.save(policy);
    }
}
```

#### ✅ 올바른 방법 (분리)

**Step 1: 구조적 변경 먼저 (Tidy First)**

```java
// Commit 1: "refactor: Rename createPolicy to createNewPolicy"

public class PolicyService {

    // 이름만 변경 (구조적 변경)
    public Policy createNewPolicy(CreatePolicyCommand command) {
        Policy policy = new Policy(command.getName(), command.getType());
        return policyRepository.save(policy);
    }
}

// 테스트 실행 → 모두 통과 확인
// Commit!
```

**Step 2: 행동적 변경 (TDD)**

```java
// Commit 2: "feat: Add duplicate policy name validation"

public class PolicyService {

    public Policy createNewPolicy(CreatePolicyCommand command) {
        // 행동적 변경: 중복 체크 로직 추가
        if (policyRepository.existsByName(command.getName())) {
            throw new DuplicatePolicyException();
        }

        Policy policy = new Policy(command.getName(), command.getType());
        return policyRepository.save(policy);
    }
}

// 새로운 테스트 추가 및 통과 확인
// Commit!
```

---

## DDD 레이어별 TDD 적용

### Domain Layer (도메인 레이어)

**특징:**
- 순수 비즈니스 로직
- 외부 의존성 없음
- 테스트 속도 가장 빠름

**테스트 전략:**
- ✅ **100% 단위 테스트**
- ✅ Mock 사용 최소화 (실제 객체 선호)
- ✅ Value Object 불변성 테스트
- ✅ Aggregate Root 비즈니스 규칙 테스트
- ✅ Domain Service 로직 테스트
- ✅ Domain Event 발행 테스트

**예제: Value Object 테스트**

```java
class PolicyIdTest {

    @Test
    @DisplayName("PolicyId는 ULID 형식으로 생성된다")
    void shouldCreatePolicyIdWithULIDFormat() {
        // When
        PolicyId policyId = new PolicyId();

        // Then
        assertThat(policyId.getValue()).matches("^[0-9A-HJKMNP-TV-Z]{26}$");
    }

    @Test
    @DisplayName("PolicyId는 불변이다")
    void shouldBeImmutable() {
        // Given
        PolicyId original = new PolicyId();

        // When
        String value = original.getValue();

        // Then
        assertThat(original.getValue()).isEqualTo(value); // 같은 값 유지
    }

    @Test
    @DisplayName("같은 값의 PolicyId는 동등하다")
    void shouldBeEqualWhenSameValue() {
        // Given
        String value = "01HN0Z8Q0G0Z8Q0G0Z8Q0G0Z8Q";
        PolicyId id1 = new PolicyId(value);
        PolicyId id2 = new PolicyId(value);

        // Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
```

**예제: Aggregate Root 테스트**

```java
class PolicyTest {

    @Test
    @DisplayName("Policy 생성 시 이름은 필수다")
    void shouldRequireNameWhenCreating() {
        // Given
        PolicyId policyId = new PolicyId();
        String nullName = null;

        // When & Then
        assertThatThrownBy(() -> Policy.create(policyId, nullName, PolicyType.KYC))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이름은 필수입니다");
    }

    @Test
    @DisplayName("Policy 승인 시 상태가 APPROVED로 변경된다")
    void shouldChangeStatusToApprovedWhenApprove() {
        // Given
        Policy policy = Policy.create(new PolicyId(), "KYC-Standard-v1", PolicyType.KYC);

        // When
        policy.approve("admin");

        // Then
        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.APPROVED);
    }

    @Test
    @DisplayName("Policy 승인 거부 시 사유가 필수다")
    void shouldRequireReasonWhenReject() {
        // Given
        Policy policy = Policy.create(new PolicyId(), "KYC-Standard-v1", PolicyType.KYC);
        String nullReason = null;

        // When & Then
        assertThatThrownBy(() -> policy.reject(nullReason))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("거부 사유는 필수입니다");
    }

    @Test
    @DisplayName("Policy 생성 시 PolicyCreatedEvent가 발행된다")
    void shouldPublishPolicyCreatedEventWhenCreated() {
        // Given
        PolicyId policyId = new PolicyId();
        String name = "KYC-Standard-v1";
        PolicyType type = PolicyType.KYC;

        // When
        Policy policy = Policy.create(policyId, name, type);

        // Then
        List<DomainEvent> events = policy.getDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(PolicyCreatedEvent.class);

        PolicyCreatedEvent event = (PolicyCreatedEvent) events.get(0);
        assertThat(event.getPolicyId()).isEqualTo(policyId);
        assertThat(event.getName()).isEqualTo(name);
    }
}
```

---

### Application Layer (애플리케이션 레이어)

**특징:**
- 유스케이스 조율
- 트랜잭션 경계
- Domain + Infrastructure 연결

**테스트 전략:**
- ✅ **통합 테스트 + 단위 테스트 혼합**
- ✅ Repository는 Mock 또는 실제 DB (Testcontainers)
- ✅ Command/Query 검증 테스트
- ✅ 트랜잭션 동작 테스트
- ✅ 도메인 이벤트 발행 확인

**예제: Application Service 테스트 (Mock 사용)**

```java
@ExtendWith(MockitoExtension.class)
class PolicyApplicationServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyDomainService policyDomainService;

    @InjectMocks
    private PolicyApplicationService policyApplicationService;

    @Test
    @DisplayName("Policy 생성 성공")
    void shouldCreatePolicySuccessfully() {
        // Given
        CreatePolicyCommand command = new CreatePolicyCommand("KYC-Standard-v1", "KYC");
        Policy expectedPolicy = Policy.create(new PolicyId(), "KYC-Standard-v1", PolicyType.KYC);

        when(policyRepository.save(any(Policy.class))).thenReturn(expectedPolicy);

        // When
        Policy result = policyApplicationService.createPolicy(command);

        // Then
        assertThat(result.getName()).isEqualTo("KYC-Standard-v1");
        assertThat(result.getStatus()).isEqualTo(PolicyStatus.DRAFT);

        verify(policyDomainService).validateUniqueness(any(Policy.class));
        verify(policyRepository).save(any(Policy.class));
    }

    @Test
    @DisplayName("중복된 Policy 생성 시 예외 발생")
    void shouldThrowExceptionWhenDuplicatePolicy() {
        // Given
        CreatePolicyCommand command = new CreatePolicyCommand("KYC-Standard-v1", "KYC");

        doThrow(new DuplicatePolicyException("이미 존재하는 정책 이름입니다"))
            .when(policyDomainService).validateUniqueness(any(Policy.class));

        // When & Then
        assertThatThrownBy(() -> policyApplicationService.createPolicy(command))
            .isInstanceOf(DuplicatePolicyException.class)
            .hasMessageContaining("이미 존재하는 정책 이름입니다");

        verify(policyRepository, never()).save(any(Policy.class));
    }
}
```

**예제: Query Service 테스트 (Testcontainers 사용)**

```java
@SpringBootTest
@Testcontainers
class PolicyQueryServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private PolicyQueryService policyQueryService;

    @Autowired
    private PolicyRepository policyRepository;

    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
    }

    @Test
    @DisplayName("ID로 Policy 조회 성공")
    void shouldFindPolicyById() {
        // Given
        Policy policy = Policy.create(new PolicyId(), "KYC-Standard-v1", PolicyType.KYC);
        Policy saved = policyRepository.save(policy);

        // When
        Optional<PolicyResponse> result = policyQueryService.findById(saved.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("KYC-Standard-v1");
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 빈 Optional 반환")
    void shouldReturnEmptyWhenPolicyNotFound() {
        // Given
        PolicyId nonExistentId = new PolicyId();

        // When
        Optional<PolicyResponse> result = policyQueryService.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }
}
```

---

### Infrastructure Layer (인프라 레이어)

**특징:**
- 외부 시스템 연동
- DB, 메시징, 파일 시스템 등

**테스트 전략:**
- ✅ **통합 테스트 중심**
- ✅ Testcontainers 사용 (실제 DB/Redis/Kafka)
- ✅ Repository CRUD 테스트
- ✅ 트랜잭션 경계 테스트
- ✅ MyBatis Mapper 테스트

**예제: Repository 테스트 (MyBatis + Testcontainers)**

```java
@SpringBootTest
@Testcontainers
@Transactional
class PolicyRepositoryImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private PolicyRepository policyRepository;

    @Test
    @DisplayName("Policy INSERT 성공")
    void shouldInsertPolicy() {
        // Given
        Policy policy = Policy.create(new PolicyId(), "KYC-Standard-v1", PolicyType.KYC);

        // When
        Policy saved = policyRepository.save(policy);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("KYC-Standard-v1");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Policy UPDATE 성공")
    void shouldUpdatePolicy() {
        // Given
        Policy policy = Policy.create(new PolicyId(), "Old Name", PolicyType.KYC);
        Policy saved = policyRepository.save(policy);

        // When
        saved.changeName("New Name");
        policyRepository.save(saved);

        // Then
        Policy updated = policyRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("이름으로 Policy 존재 여부 확인")
    void shouldCheckExistenceByName() {
        // Given
        Policy policy = Policy.create(new PolicyId(), "KYC-Standard-v1", PolicyType.KYC);
        policyRepository.save(policy);

        // When
        boolean exists = policyRepository.existsByName("KYC-Standard-v1");
        boolean notExists = policyRepository.existsByName("Non-existing Policy");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
```

---

### Interface Layer (인터페이스 레이어)

**특징:**
- REST API 엔드포인트
- 요청/응답 DTO 변환
- 입력 검증

**테스트 전략:**
- ✅ **MockMvc 통합 테스트**
- ✅ HTTP 상태 코드 검증
- ✅ 요청/응답 JSON 검증
- ✅ 입력 검증 테스트
- ✅ 예외 처리 테스트

**예제: Controller 테스트 (MockMvc)**

```java
@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyApplicationService policyApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/policies - 정상 생성 (201 Created)")
    void shouldCreatePolicySuccessfully() throws Exception {
        // Given
        CreatePolicyRequest request = new CreatePolicyRequest("KYC-Standard-v1", "KYC");
        Policy policy = Policy.create(new PolicyId(), "KYC-Standard-v1", PolicyType.KYC);

        when(policyApplicationService.createPolicy(any(CreatePolicyCommand.class)))
            .thenReturn(policy);

        // When & Then
        mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("KYC-Standard-v1"))
            .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("POST /api/v1/policies - 필수 필드 누락 (400 Bad Request)")
    void shouldReturn400WhenRequiredFieldMissing() throws Exception {
        // Given
        CreatePolicyRequest request = new CreatePolicyRequest(null, "KYC"); // name 누락

        // When & Then
        mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이름은 필수입니다"));
    }

    @Test
    @DisplayName("POST /api/v1/policies - 중복 이름 (409 Conflict)")
    void shouldReturn409WhenDuplicateName() throws Exception {
        // Given
        CreatePolicyRequest request = new CreatePolicyRequest("KYC-Standard-v1", "KYC");

        when(policyApplicationService.createPolicy(any(CreatePolicyCommand.class)))
            .thenThrow(new DuplicatePolicyException("이미 존재하는 정책 이름입니다"));

        // When & Then
        mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 존재하는 정책 이름입니다"));
    }
}
```

---

## Commit 규칙

### Commit 조건

**다음 조건을 모두 만족할 때만 Commit:**

1. ✅ **모든 테스트가 통과** (`./gradlew test`)
2. ✅ **컴파일러/린터 경고 해결** (`./gradlew build`)
3. ✅ **단일 논리적 작업 단위** (하나의 기능 또는 하나의 리팩토링)
4. ✅ **구조적 변경 또는 행동적 변경 중 하나만** (Tidy First 원칙)

### Commit 메시지 규칙

**Conventional Commits 형식:**

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type:**
- `feat`: 새로운 기능 (행동적 변경)
- `fix`: 버그 수정 (행동적 변경)
- `refactor`: 리팩토링 (구조적 변경)
- `test`: 테스트 추가/수정
- `docs`: 문서 변경
- `style`: 코드 포맷팅 (세미콜론 누락 등)
- `chore`: 빌드/설정 변경

**예제:**

```bash
# 구조적 변경 (Tidy First)
git commit -m "refactor(policy): Extract validateUniqueness method

- PolicyDomainService에서 중복 검증 로직 추출
- 테스트는 변경 없음 (구조적 변경만)
"

# 행동적 변경 (TDD)
git commit -m "feat(policy): Add duplicate policy name validation

- Policy 생성 시 이름 중복 검사
- DuplicatePolicyException 추가
- 테스트: shouldRejectDuplicatePolicyName
"

# 버그 수정
git commit -m "fix(user): Fix email validation regex

- 이메일 도메인에 하이픈 허용
- 테스트: shouldAcceptEmailWithHyphenInDomain
"
```

### Commit 주기

**작고 자주 커밋:**

```
┌─────────────────────────────────────┐
│ Bad: 큰 덩어리 커밋                  │
│ - 10개 파일 변경                     │
│ - 300줄 추가                        │
│ - 구조적 + 행동적 변경 혼합          │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ Good: 작고 빈번한 커밋               │
│                                     │
│ Commit 1: refactor - 메서드 추출    │
│ Commit 2: test - 테스트 추가        │
│ Commit 3: feat - 기능 구현          │
│ Commit 4: refactor - 중복 제거      │
└─────────────────────────────────────┘
```

---

## 코드 품질 기준

### 1. 중복 제거 (Ruthlessly Eliminate Duplication)

**DO:**
- ✅ 중복 코드를 발견하면 즉시 제거
- ✅ Extract Method, Extract Class 사용
- ✅ 공통 로직을 Domain Service로 추출

**예제:**

```java
// ❌ 중복 코드
public class PolicyService {
    public void approvePolicy(PolicyId id) {
        Policy policy = policyRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Policy not found"));
        policy.approve();
        policyRepository.save(policy);
    }

    public void rejectPolicy(PolicyId id, String reason) {
        Policy policy = policyRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Policy not found"));
        policy.reject(reason);
        policyRepository.save(policy);
    }
}

// ✅ 중복 제거
public class PolicyService {
    public void approvePolicy(PolicyId id) {
        Policy policy = findPolicyOrThrow(id);
        policy.approve();
        savePolicy(policy);
    }

    public void rejectPolicy(PolicyId id, String reason) {
        Policy policy = findPolicyOrThrow(id);
        policy.reject(reason);
        savePolicy(policy);
    }

    private Policy findPolicyOrThrow(PolicyId id) {
        return policyRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Policy not found"));
    }

    private void savePolicy(Policy policy) {
        policyRepository.save(policy);
    }
}
```

### 2. 의도 명확화 (Express Intent Clearly)

**DO:**
- ✅ 의미 있는 변수/메서드/클래스 이름 사용
- ✅ 주석 대신 코드로 의도 표현
- ✅ Magic Number 상수화

**예제:**

```java
// ❌ 의도 불명확
public class User {
    private int s; // 상태?

    public boolean check() {
        return s == 1;
    }
}

// ✅ 의도 명확
public class User {
    private static final int ACTIVE_STATUS = 1;
    private int status;

    public boolean isActive() {
        return status == ACTIVE_STATUS;
    }
}

// 더 나은 방법: Enum 사용
public class User {
    private UserStatus status;

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
```

### 3. 의존성 명시화 (Make Dependencies Explicit)

**DO:**
- ✅ 생성자 주입 사용 (필드 주입 금지)
- ✅ 인터페이스 기반 의존성
- ✅ 필요한 의존성만 주입

**예제:**

```java
// ❌ 필드 주입 (암묵적 의존성)
@Service
public class PolicyService {
    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyDomainService policyDomainService;
}

// ✅ 생성자 주입 (명시적 의존성)
@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final PolicyDomainService policyDomainService;

    // Lombok @RequiredArgsConstructor가 생성자 자동 생성
}
```

### 4. 단일 책임 (Single Responsibility)

**DO:**
- ✅ 메서드는 하나의 일만 수행
- ✅ 클래스는 하나의 책임만
- ✅ 메서드 길이 20줄 이하 권장

**예제:**

```java
// ❌ 여러 책임
public class UserService {
    public void registerUser(UserRequest request) {
        // 1. 검증
        if (request.getEmail() == null) throw new IllegalArgumentException();
        if (!request.getEmail().contains("@")) throw new IllegalArgumentException();

        // 2. 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException();
        }

        // 3. 비밀번호 암호화
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        // 4. 엔티티 생성
        User user = new User(request.getEmail(), hashedPassword);

        // 5. 저장
        userRepository.save(user);

        // 6. 이메일 전송
        emailService.sendWelcomeEmail(user.getEmail());
    }
}

// ✅ 단일 책임 (각 단계를 메서드로 분리)
public class UserService {
    public void registerUser(UserRequest request) {
        validateRequest(request);
        checkDuplicateEmail(request.getEmail());

        User user = createUser(request);
        userRepository.save(user);

        sendWelcomeEmail(user);
    }

    private void validateRequest(UserRequest request) {
        // 검증 로직
    }

    private void checkDuplicateEmail(String email) {
        // 중복 체크 로직
    }

    private User createUser(UserRequest request) {
        // 엔티티 생성 로직
    }

    private void sendWelcomeEmail(User user) {
        // 이메일 전송 로직
    }
}
```

### 5. 상태 최소화 (Minimize State)

**DO:**
- ✅ Immutable 객체 선호 (Value Object)
- ✅ 순수 함수 선호 (부작용 최소화)
- ✅ final 키워드 적극 사용

**예제:**

```java
// ❌ Mutable (변경 가능)
public class Email {
    private String value;

    public void setValue(String value) {
        this.value = value;
    }
}

// ✅ Immutable (불변)
public class Email {
    private final String value;

    public Email(String value) {
        validate(value);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private void validate(String value) {
        if (!value.contains("@")) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다");
        }
    }
}
```

### 6. 가장 단순한 해결책 (Simplest Solution)

**DO:**
- ✅ YAGNI (You Aren't Gonna Need It)
- ✅ 과도한 일반화 지양
- ✅ 미래를 위한 코드 작성 금지

**예제:**

```java
// ❌ 과도한 일반화 (현재 필요 없음)
public interface PolicyValidator {
    boolean validate(Policy policy);
}

public class UniqueNameValidator implements PolicyValidator {
    public boolean validate(Policy policy) { ... }
}

public class StatusValidator implements PolicyValidator {
    public boolean validate(Policy policy) { ... }
}

// ✅ 단순한 해결책 (현재 필요한 것만)
public class PolicyDomainService {
    public void validateUniqueness(Policy policy) {
        // 현재 필요한 검증만 구현
    }
}
```

---

## 리팩토링 가이드라인

### 리팩토링 시점

**리팩토링은 Green 단계에서만 수행:**

```
Red → Green → Refactor
          ↑       ↑
      테스트 통과 후에만 리팩토링
```

### 리팩토링 규칙

1. **한 번에 하나의 리팩토링만**
2. **각 리팩토링 후 테스트 실행**
3. **기존 리팩토링 패턴 이름 사용**
4. **중복 제거와 가독성 개선 우선**

### 주요 리팩토링 패턴

#### 1. Extract Method (메서드 추출)

**언제:** 메서드가 너무 길거나, 주석이 필요한 코드 블록

```java
// Before
public void processOrder(Order order) {
    // 재고 확인
    if (inventory.getQuantity(order.getProductId()) < order.getQuantity()) {
        throw new InsufficientStockException();
    }

    // 가격 계산
    BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
    if (order.hasDiscount()) {
        price = price.multiply(BigDecimal.valueOf(0.9));
    }

    // 주문 저장
    orderRepository.save(order);
}

// After
public void processOrder(Order order) {
    validateStock(order);
    BigDecimal price = calculatePrice(order);
    saveOrder(order);
}

private void validateStock(Order order) {
    if (inventory.getQuantity(order.getProductId()) < order.getQuantity()) {
        throw new InsufficientStockException();
    }
}

private BigDecimal calculatePrice(Order order) {
    BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
    if (order.hasDiscount()) {
        price = price.multiply(BigDecimal.valueOf(0.9));
    }
    return price;
}

private void saveOrder(Order order) {
    orderRepository.save(order);
}
```

#### 2. Introduce Parameter Object (매개변수 객체화)

**언제:** 메서드 매개변수가 3개 이상

```java
// Before
public User createUser(String name, String email, String phone, String address) {
    // ...
}

// After
public class UserRegistrationInfo {
    private final String name;
    private final String email;
    private final String phone;
    private final String address;

    // constructor, getters
}

public User createUser(UserRegistrationInfo info) {
    // ...
}
```

#### 3. Replace Conditional with Polymorphism (조건문을 다형성으로)

**언제:** 타입별 분기가 반복됨

```java
// Before
public BigDecimal calculateDiscount(Customer customer, BigDecimal amount) {
    if (customer.getType() == CustomerType.REGULAR) {
        return amount.multiply(BigDecimal.valueOf(0.05));
    } else if (customer.getType() == CustomerType.PREMIUM) {
        return amount.multiply(BigDecimal.valueOf(0.10));
    } else if (customer.getType() == CustomerType.VIP) {
        return amount.multiply(BigDecimal.valueOf(0.20));
    }
    return BigDecimal.ZERO;
}

// After
public interface DiscountPolicy {
    BigDecimal calculateDiscount(BigDecimal amount);
}

public class RegularDiscountPolicy implements DiscountPolicy {
    public BigDecimal calculateDiscount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.05));
    }
}

public class PremiumDiscountPolicy implements DiscountPolicy {
    public BigDecimal calculateDiscount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.10));
    }
}

public class VIPDiscountPolicy implements DiscountPolicy {
    public BigDecimal calculateDiscount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.20));
    }
}

// Customer 클래스
public class Customer {
    private DiscountPolicy discountPolicy;

    public BigDecimal calculateDiscount(BigDecimal amount) {
        return discountPolicy.calculateDiscount(amount);
    }
}
```

---

## 실전 워크플로우

### "go" 명령어 워크플로우

**사용자가 "go" 입력 시:**

1. **plan.md 열기**
2. **다음 체크되지 않은 테스트 찾기**
3. **TDD 사이클 시작:**
   - **Red**: 실패 테스트 작성
   - **Green**: 최소 구현
   - **Refactor**: 리팩토링
4. **plan.md 업데이트** (`[x]` 표시)
5. **Commit**
6. **다음 테스트 대기**

### 예제 세션

```
User: go