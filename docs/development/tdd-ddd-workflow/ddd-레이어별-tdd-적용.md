# DDD 레이어별 TDD 적용

## Domain Layer (도메인 레이어)

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

## Application Layer (애플리케이션 레이어)

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

## Infrastructure Layer (인프라 레이어)

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

## Interface Layer (인터페이스 레이어)

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
