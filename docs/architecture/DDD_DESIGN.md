# DDD 설계 문서 - Inspect-Hub

## 레이어 구조

```
backend/server/src/main/java/com/inspecthub/server/
├── domain/                     # 도메인 레이어
│   ├── user/                  # 사용자 도메인
│   │   ├── model/
│   │   │   ├── User.java             # Aggregate Root
│   │   │   ├── UserId.java           # Value Object
│   │   │   ├── Email.java            # Value Object
│   │   │   └── Password.java         # Value Object
│   │   ├── repository/
│   │   │   └── UserRepository.java   # Repository Interface
│   │   └── service/
│   │       └── UserDomainService.java
│   └── policy/                # 정책 도메인
│       ├── model/
│       │   ├── Policy.java           # Aggregate Root
│       │   ├── PolicyId.java         # Value Object
│       │   ├── PolicyVersion.java    # Value Object
│       │   └── PolicyType.java       # Enum
│       ├── repository/
│       │   └── PolicyRepository.java
│       └── service/
│           └── PolicyDomainService.java
├── application/                # 애플리케이션 레이어
│   ├── user/
│   │   ├── UserApplicationService.java
│   │   ├── command/
│   │   └── query/
│   │       └── UserQueryService.java
│   └── policy/
│       ├── PolicyApplicationService.java
│       ├── command/
│       │   ├── CreatePolicySnapshotCommand.java
│       │   └── ApprovePolicyCommand.java
│       └── query/
│           └── PolicyQueryService.java
├── infrastructure/             # 인프라 레이어
│   ├── user/
│   │   └── UserRepositoryImpl.java
│   └── policy/
│       └── PolicyRepositoryImpl.java
└── interfaces/                 # 인터페이스 레이어
    ├── api/
    │   ├── user/
    │   │   └── UserController.java
    │   └── policy/
    │       └── PolicyController.java
    └── dto/
        ├── user/
        │   ├── CreateUserRequest.java
        │   └── UserResponse.java
        └── policy/
            ├── CreatePolicyRequest.java
            └── PolicyResponse.java
```

## 핵심 도메인 모델

### 1. User (사용자 - Aggregate Root)

**책임:**
- 사용자 정보 관리
- 권한 관리
- 인증 및 인가

**주요 비즈니스 규칙:**
- 이메일은 유일해야 함
- 비밀번호는 암호화 저장
- 권한은 역할 기반 (RBAC)

### 2. Policy (정책 - Aggregate Root)

**책임:**
- 정책 버전 관리
- 정책 활성화/비활성화
- 정책 적용 기간 관리

**주요 비즈니스 규칙:**
- 스냅샷 기반 버전 관리
- 승인 후 배포
- 동시에 하나의 활성 버전만 존재

## 테스트 전략

### 1. Unit Tests (단위 테스트)
- **Domain Model Tests**: 도메인 로직 테스트
- **Value Object Tests**: 불변성 및 동등성 테스트
- **Domain Service Tests**: 도메인 서비스 로직 테스트

### 2. Integration Tests (통합 테스트)
- **Repository Tests**: DB 연동 테스트
- **Application Service Tests**: 유스케이스 테스트
- **API Tests**: 컨트롤러 테스트

### 3. Coverage Target
- **최소 80% 커버리지**
- 도메인 레이어: 90% 이상
- 애플리케이션 레이어: 85% 이상
- 인프라 레이어: 75% 이상
- 인터페이스 레이어: 80% 이상

## TDD 개발 순서

1. **User Domain Model** (현재 진행)
   - UserId Value Object 테스트 & 구현
   - Email Value Object 테스트 & 구현
   - Password Value Object 테스트 & 구현
   - User Aggregate 테스트 & 구현
   - UserDomainService 테스트 & 구현

2. **User Infrastructure**
   - UserRepository 인터페이스
   - UserRepositoryImpl 테스트 & 구현

3. **User Application**
   - Commands/Queries 정의
   - UserApplicationService 테스트 & 구현

4. **User Interface**
   - DTOs 정의
   - UserController 테스트 & 구현

5. **Policy Domain** (다음 단계)
   - PolicyId Value Object 테스트 & 구현
   - PolicyVersion Value Object 테스트 & 구현
   - PolicyType Enum 테스트 & 구현
   - Policy Aggregate 테스트 & 구현
   - PolicyDomainService 테스트 & 구현
   - 관련 레이어 구현
   - 스냅샷 버전 관리 구현
