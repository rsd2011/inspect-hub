# Tidy First 원칙

## 변경의 두 가지 유형

Kent Beck의 **Tidy First** 원칙은 모든 코드 변경을 두 가지로 분류합니다:

### 1. STRUCTURAL CHANGES (구조적 변경)

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

### 2. BEHAVIORAL CHANGES (행동적 변경)

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

## 원칙

1. **구조적 변경과 행동적 변경을 절대 혼합하지 않는다**
2. **구조적 변경이 필요하면 항상 먼저 수행한다**
3. **구조적 변경 후 테스트를 실행하여 행동 변경이 없음을 검증한다**

## 워크플로우

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

## 예제: Tidy First vs Behavioral Change

### ❌ 잘못된 방법 (혼합)

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

### ✅ 올바른 방법 (분리)

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
