# TDD 사이클

## Red → Green → Refactor

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

## TDD 방법론 가이드

### 1단계: Red (실패 테스트 작성)

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

### 2단계: Green (테스트 통과)

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

### 3단계: Refactor (리팩토링)

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

## 결함 수정 시 TDD

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
