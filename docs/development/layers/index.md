# DDD Layers - Test Cases

> **도메인 주도 설계 레이어별 테스트 케이스**

---

## 📖 개요

BDD 스타일(Given-When-Then)로 작성된 DDD 아키텍처 레이어별 테스트 계획입니다.

**상위 문서:** [Development Guide](../index.md) > [Test Plan](../plan.md)

---

## 🏗️ 레이어 구조

### Layer 1: Domain Layer (도메인 레이어)
**📄 [layer-1-domain.md](./layer-1-domain.md)**

**핵심 책임:** 비즈니스 로직과 규칙

**포함 요소:**
- **Value Objects** - UserId, EmployeeId, Password, PolicyId, PolicyVersion
- **Aggregate Roots** - User, PolicySnapshot
- **Domain Services** - UserDomainService, PolicyDomainService

**테스트 예시:**
```
- [ ] UserId 생성 테스트 - ULID 형식 검증
- [ ] User 생성 테스트 - 필수 필드 검증
- [ ] User 권한 부여 테스트 - Role 추가
- [ ] PolicySnapshot 승인 테스트 - 상태 APPROVED로 변경
```

---

### Layer 2: Application Layer (애플리케이션 레이어)
**📄 [layer-2-application.md](./layer-2-application.md)**

**핵심 책임:** 유스케이스 구현 및 트랜잭션 관리

**포함 요소:**
- **Commands** - CreateUserCommand, ApprovePolicyCommand
- **Application Services** - UserApplicationService, PolicyApplicationService
- **Query Services** - UserQueryService, PolicyQueryService

**테스트 예시:**
```
- [ ] CreateUserCommand 검증 테스트 - 필수 필드 체크
- [ ] UserApplicationService.createUser 테스트 - 정상 생성
- [ ] UserApplicationService.createUser 사원ID 중복 거부 테스트
- [ ] PolicyApplicationService.deployPolicy 중복 활성 버전 거부 테스트
```

---

### Layer 3: Infrastructure Layer (인프라 레이어)
**📄 [layer-3-infrastructure.md](./layer-3-infrastructure.md)**

**핵심 책임:** 데이터베이스, 외부 시스템 연동

**포함 요소:**
- **Repository Implementations** - UserRepositoryImpl, PolicyRepositoryImpl
- **MyBatis Mappers** - User, Policy SQL
- **External Adapters** - AD, SSO 연동

**테스트 예시:**
```
- [ ] UserRepositoryImpl.save 테스트 - INSERT 성공
- [ ] UserRepositoryImpl.save 테스트 - UPDATE 성공
- [ ] UserRepositoryImpl.findById 테스트 - 존재하는 ID 조회
- [ ] PolicyRepositoryImpl.findActivePolicy 테스트 - 활성 정책 조회
```

---

### Layer 4: Interface Layer (인터페이스 레이어)
**📄 [layer-4-interface.md](./layer-4-interface.md)**

**핵심 책임:** HTTP API 엔드포인트

**포함 요소:**
- **REST Controllers** - UserController, PolicyController
- **DTO Validation** - Request/Response 검증
- **Exception Handlers** - GlobalExceptionHandler

**테스트 예시:**
```
- [ ] POST /api/v1/users - 정상 생성 (201 Created)
- [ ] POST /api/v1/users - 사원ID 형식 오류 (400 Bad Request)
- [ ] POST /api/v1/users - 사원ID 중복 (409 Conflict)
- [ ] GET /api/v1/users/{id} - 존재하는 ID (200 OK)
- [ ] GET /api/v1/users/{id} - 존재하지 않는 ID (404 Not Found)
```

---

## 🎯 DDD 설계 원칙

### Aggregate 설계
- **Aggregate Root**: 트랜잭션 경계 단위
- **Value Object**: 불변 객체, equals/hashCode 구현
- **Entity**: 식별자 기반 동일성

### 의존성 규칙
```
Domain (순수 Java)
   ↑
Application (Use Cases)
   ↑
Infrastructure (DB, External)
   ↑
Interface (REST API)
```

- Domain Layer는 다른 레이어에 의존하지 않음
- Application Layer는 Domain에만 의존
- Infrastructure/Interface는 모든 레이어에 의존 가능

---

## 📝 테스트 작성 가이드

### BDD 스타일 (Given-When-Then)

```java
@Test
@DisplayName("사원ID 중복 시 User 생성이 거부된다")
void shouldRejectUserCreationWhenEmployeeIdDuplicated() {
    // Given (준비)
    EmployeeId employeeId = new EmployeeId("EMP001");
    when(userRepository.existsByEmployeeId(employeeId))
        .thenReturn(true);

    // When (실행)
    ThrowingCallable action = () -> userService.createUser(
        new CreateUserCommand(employeeId, "password")
    );

    // Then (검증)
    assertThatThrownBy(action)
        .isInstanceOf(DuplicateEmployeeIdException.class)
        .hasMessageContaining("EMP001");
}
```

### Lombok 패턴

```java
// DTO Pattern
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO { ... }

// Entity Pattern
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User { ... }

// Service Pattern (DI)
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService { ... }
```

---

## 🔗 관련 문서

- **[Test Plan](../plan.md)** - 전체 테스트 계획서
- **[TDD + DDD Workflow](../TDD_DDD_WORKFLOW.md)** - 상세 워크플로우
- **[DDD Design](../../architecture/DDD_DESIGN.md)** - 아키텍처 설계
- **[Backend README](../../backend/README.md)** - 백엔드 개발 가이드
