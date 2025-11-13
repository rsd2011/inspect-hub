# DDD 설계 문서 - Inspect-Hub

## 레이어 구조

```
backend/server/src/main/java/com/inspecthub/server/
├── domain/                     # 도메인 레이어
│   ├── tenant/                # 테넌트 도메인
│   │   ├── model/
│   │   │   ├── Tenant.java           # Aggregate Root
│   │   │   ├── TenantId.java         # Value Object
│   │   │   └── TenantStatus.java     # Enum
│   │   ├── repository/
│   │   │   └── TenantRepository.java # Repository Interface
│   │   └── service/
│   │       └── TenantDomainService.java
│   ├── user/                  # 사용자 도메인
│   └── policy/                # 정책 도메인
├── application/                # 애플리케이션 레이어
│   ├── tenant/
│   │   ├── TenantApplicationService.java
│   │   ├── command/
│   │   │   ├── CreateTenantCommand.java
│   │   │   └── UpdateTenantCommand.java
│   │   └── query/
│   │       └── TenantQueryService.java
│   └── user/
├── infrastructure/             # 인프라 레이어
│   ├── tenant/
│   │   └── TenantRepositoryImpl.java
│   └── user/
└── interfaces/                 # 인터페이스 레이어
    ├── api/
    │   └── tenant/
    │       └── TenantController.java
    └── dto/
        └── tenant/
            ├── CreateTenantRequest.java
            └── TenantResponse.java
```

## 핵심 도메인 모델

### 1. Tenant (테넌트 - Aggregate Root)

**책임:**
- 회원사/기관 정보 관리
- 테넌트 상태 관리 (활성/비활성/정지)
- 테넌트 설정 관리

**주요 비즈니스 규칙:**
- 테넌트 ID는 변경 불가능
- 활성 상태일 때만 시스템 사용 가능
- 테넌트 이름은 필수
- 테넌트 정지 시 사유 필요

### 2. User (사용자 - Aggregate Root)

**책임:**
- 사용자 정보 관리
- 권한 관리
- 테넌트 소속 관리

**주요 비즈니스 규칙:**
- 사용자는 하나의 테넌트에만 소속
- 이메일은 유일해야 함
- 비밀번호는 암호화 저장
- 권한은 역할 기반 (RBAC)

### 3. Policy (정책 - Aggregate Root)

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

1. **Tenant Domain Model** (현재 진행)
   - TenantId Value Object 테스트 & 구현
   - TenantStatus Enum 테스트 & 구현
   - Tenant Aggregate 테스트 & 구현
   - TenantDomainService 테스트 & 구현

2. **Tenant Infrastructure**
   - TenantRepository 인터페이스
   - TenantRepositoryImpl 테스트 & 구현

3. **Tenant Application**
   - Commands/Queries 정의
   - TenantApplicationService 테스트 & 구현

4. **Tenant Interface**
   - DTOs 정의
   - TenantController 테스트 & 구현

5. **User Domain** (다음 단계)
   - User 도메인 모델
   - 관련 레이어 구현

6. **Policy Domain** (향후 진행)
   - Policy 도메인 모델
   - 스냅샷 버전 관리 구현
