# Backend Development Guide

> **Inspect-Hub AML 통합 컴플라이언스 모니터링 시스템 백엔드 가이드**

## 📚 목차

1. [프로젝트 개요](#-프로젝트-개요)
2. [기술 스택](#-기술-스택)
3. [프로젝트 구조](#-프로젝트-구조)
4. [개발 환경 설정](#-개발-환경-설정)
5. [모듈 설명](#-모듈-설명)
6. [코딩 규칙](#-코딩-규칙)
7. [데이터베이스 접근](#-데이터베이스-접근)
8. [API 개발](#-api-개발)
9. [테스트 전략](#-테스트-전략)
10. [빌드 및 배포](#-빌드-및-배포)
11. [참고 문서](#-참고-문서)

---

## 🚀 프로젝트 개요

Inspect-Hub 백엔드는 금융기관을 위한 AML(Anti-Money Laundering) 통합 컴플라이언스 모니터링 시스템입니다.

**핵심 기능:**
- 의심거래보고(STR) 탐지 및 관리
- 고액현금거래보고(CTR) 처리
- 감시대상명단(WLF) 필터링
- 스냅샷 기반 정책 버전 관리
- 승인 워크플로우
- 100% 감사 로깅
- FIU 보고서 생성

**현재 단계:** POC/MVP - PRD 완료, 코드 구현 진행 중

---

## 🛠 기술 스택

### Core Technologies

| 항목 | 기술 | 버전 | 용도 |
|------|------|------|------|
| **Language** | Java | 21 | 프로그래밍 언어 |
| **Framework** | Spring Boot | 3.3.2 | 애플리케이션 프레임워크 |
| **Build Tool** | Gradle | 8.8+ | 빌드 자동화 |
| **Database** | PostgreSQL | 14+ | 주 데이터베이스 |
| **Cache** | Redis | 7+ | 캐싱 및 세션 관리 |
| **Message Queue** | Apache Kafka | 3.x | 이벤트 스트리밍 |

### Spring Ecosystem

| 라이브러리 | 용도 |
|------------|------|
| **Spring Security** | 인증/인가 (JWT, OAuth2) |
| **Spring Data JPA** | JPA 추상화 (향후 고려, 현재 MyBatis 사용) |
| **MyBatis** | SQL Mapper (주 ORM) |
| **Spring Batch** | 배치 처리 (탐지 엔진) |
| **Spring AOP** | 감사 로깅, 트랜잭션 |
| **Spring Validation** | 입력 검증 |

### API & Documentation

| 라이브러리 | 용도 |
|------------|------|
| **Springdoc OpenAPI** | API 문서화 (Swagger UI) |
| **Swagger Annotations** | API 명세 작성 |

### Testing

| 라이브러리 | 용도 |
|------------|------|
| **JUnit 5** | 단위 테스트 프레임워크 |
| **Mockito** | Mocking 프레임워크 |
| **AssertJ** | Assertion 라이브러리 |
| **Testcontainers** | 통합 테스트 (DB 컨테이너) |
| **Rest Assured** | REST API 테스트 |

### Utilities

| 라이브러리 | 용도 |
|------------|------|
| **Lombok** | 보일러플레이트 코드 제거 |
| **MapStruct** | DTO ↔ Entity 매핑 |
| **ULID Creator** | ULID 생성 (식별자 전략) |

---

## 📁 프로젝트 구조

### Multi-Module Architecture

```
inspect-hub/
└── backend/
    ├── common/              # 공통 모듈 (인프라 레이어)
    ├── admin/               # 관리 도메인
    ├── policy/              # 정책 관리 도메인
    ├── detection/           # 탐지 엔진 도메인
    ├── investigation/       # 조사/사례 도메인
    ├── reporting/           # 보고 도메인
    ├── batch/               # 배치 처리 모듈
    ├── simulation/          # 리스크 시뮬레이션 도메인
    └── server/              # 메인 애플리케이션 (집합 모듈)
```

### Module Dependencies

```
server (Main Application)
  ├─→ common (공통)
  ├─→ admin (관리) └─→ common
  ├─→ policy (정책 관리) └─→ common
  ├─→ detection (탐지 엔진) └─→ common, policy
  ├─→ investigation (조사) └─→ common, detection
  ├─→ reporting (보고) └─→ common, investigation
  ├─→ batch (배치) └─→ common, detection, investigation
  └─→ simulation (시뮬레이션) └─→ common, policy, detection
```

**의존성 규칙:**
- ❌ 도메인 모듈은 `server`를 의존할 수 없음
- ❌ `common`은 도메인 모듈을 의존할 수 없음
- ✅ 도메인 모듈은 `common`을 의존 가능
- ✅ 도메인 모듈은 다른 도메인 모듈을 의존 가능
- ✅ `server`는 모든 도메인 모듈을 의존

### Module Structure (각 도메인 모듈)

```
policy/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/inspecthub/policy/
│   │   │       ├── controller/        # REST 컨트롤러
│   │   │       │   └── PolicyController.java
│   │   │       ├── service/           # 비즈니스 로직
│   │   │       │   └── PolicyService.java
│   │   │       ├── dto/               # 데이터 전송 객체
│   │   │       │   ├── PolicyRequest.java
│   │   │       │   └── PolicyResponse.java
│   │   │       ├── mapper/            # MyBatis Mapper 인터페이스
│   │   │       │   └── PolicyMapper.java
│   │   │       └── entity/            # 도메인 엔티티 (선택)
│   │   │           └── Policy.java
│   │   └── resources/
│   │       ├── mybatis/
│   │       │   └── mapper/
│   │       │       └── PolicyMapper.xml  # MyBatis SQL 매핑
│   │       └── application-policy.yml
│   └── test/
│       └── java/
│           └── com/inspecthub/policy/
│               ├── service/
│               │   └── PolicyServiceTest.java
│               └── controller/
│                   └── PolicyControllerTest.java
└── build.gradle
```

### Common Module Structure

```
common/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/inspecthub/common/
│       │       ├── entity/
│       │       │   └── BaseDomain.java   # 기본 도메인 (ID, 타임스탬프)
│       │       ├── dto/
│       │       │   ├── ApiResponse.java  # 통일된 API 응답
│       │       │   └── PageRequest.java  # 페이지네이션 요청
│       │       ├── exception/
│       │       │   ├── BusinessException.java
│       │       │   └── GlobalExceptionHandler.java
│       │       ├── util/
│       │       │   ├── UlidGenerator.java  # ULID 생성기
│       │       │   └── DateUtils.java
│       │       ├── mybatis/
│       │       │   ├── typehandler/
│       │       │   │   └── UlidTypeHandler.java
│       │       │   └── interceptor/
│       │       │       └── AuditInterceptor.java
│       │       ├── security/
│       │       │   ├── JwtTokenProvider.java
│       │       │   └── SecurityConfig.java
│       │       └── config/
│       │           ├── MyBatisConfig.java
│       │           ├── RedisConfig.java
│       │           └── KafkaConfig.java
│       └── resources/
│           ├── schema/
│           │   └── ulid_example.sql
│           └── application-common.yml
└── build.gradle
```

---

## 🔧 개발 환경 설정

### 필수 요구사항

| 항목 | 버전 | 확인 명령어 |
|------|------|-------------|
| **Java** | 21+ | `java --version` |
| **Gradle** | 8.8+ | `./gradlew --version` |
| **PostgreSQL** | 14+ | `psql --version` |
| **Redis** (선택) | 7+ | `redis-cli --version` |
| **Kafka** (선택) | 3.x | `kafka-topics.sh --version` |

### 로컬 개발 환경 설정

#### 1. 프로젝트 클론

```bash
git clone https://github.com/your-org/inspect-hub.git
cd inspect-hub/backend
```

#### 2. PostgreSQL 설정

```bash
# PostgreSQL 설치 (Ubuntu/Debian)
sudo apt-get install postgresql postgresql-contrib

# 데이터베이스 생성
sudo -u postgres psql
```

```sql
CREATE DATABASE inspecthub_dev;
CREATE USER inspecthub WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE inspecthub_dev TO inspecthub;
```

#### 3. 환경 변수 설정

`.env` 파일 생성 (또는 시스템 환경 변수):

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/inspecthub_dev
DB_USERNAME=inspecthub
DB_PASSWORD=password

# Redis (선택)
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key-here-must-be-at-least-256-bits
JWT_EXPIRATION=3600000

# Application
SPRING_PROFILES_ACTIVE=local
```

**IntelliJ IDEA 설정:**
1. Run → Edit Configurations
2. Environment Variables → `.env` 파일 경로 지정
3. 또는 직접 입력: `DB_URL=jdbc:...; DB_USERNAME=...`

#### 4. 빌드 및 실행

```bash
# 전체 빌드 (테스트 포함)
./gradlew clean build

# 테스트 제외 빌드
./gradlew clean build -x test

# 서버 실행
./gradlew :server:bootRun

# 특정 모듈만 테스트
./gradlew :policy:test

# 특정 테스트 클래스 실행
./gradlew test --tests PolicyServiceTest

# 특정 테스트 메서드 실행
./gradlew test --tests PolicyServiceTest.testCreatePolicy
```

#### 5. 서버 확인

```bash
# Health Check
curl http://localhost:8090/actuator/health

# Swagger UI 접속
http://localhost:8090/swagger-ui.html

# API Docs (JSON)
http://localhost:8090/v3/api-docs
```

---

## 📦 모듈 설명

### 1. common - 공통 모듈 (인프라)

**책임:**
- 모든 도메인 모듈이 공유하는 인프라 코드
- 데이터베이스 설정, 보안 설정, 유틸리티

**주요 컴포넌트:**
- `BaseDomain`: 모든 엔티티의 기본 클래스 (ID, 타임스탬프)
- `ApiResponse`: 통일된 API 응답 구조
- `GlobalExceptionHandler`: 전역 예외 처리
- `UlidGenerator`: ULID 식별자 생성
- `SecurityConfig`: Spring Security 설정
- `MyBatisConfig`: MyBatis 설정

**의존성:**
- 다른 모듈을 의존하지 않음

### 2. admin - 관리 도메인

**책임:**
- 사용자, 조직, 권한 관리
- 시스템 설정 관리
- 메뉴 및 코드 관리

**주요 API:**
- `POST /api/v1/users` - 사용자 생성
- `GET /api/v1/users` - 사용자 목록
- `PUT /api/v1/users/{id}` - 사용자 수정
- `GET /api/v1/organizations` - 조직 목록
- `POST /api/v1/permission-groups` - 권한 그룹 생성

**의존성:**
- `common`

### 3. policy - 정책 관리 도메인

**책임:**
- 정책 스냅샷 버전 관리
- KYC/STR/CTR/RBA/WLF 정책 기준 설정
- 정책 배포 및 승인 워크플로우

**핵심 개념: 스냅샷 기반 버전 관리**
- 불변성: 승인된 정책은 수정 불가
- 버전 체인: prev_id ↔ next_id 연결
- 롤백 지원: 이전 버전으로 되돌리기
- What-if 분석: 초안 스냅샷으로 시뮬레이션

**주요 테이블:**
- `standard_snapshot`: 정책 스냅샷 (type, version, effective_from/to)
- `standard_criteria`: 상세 기준 (JSON)

**주요 API:**
- `POST /api/v1/policies/snapshots` - 정책 스냅샷 생성
- `GET /api/v1/policies/snapshots/{id}` - 스냅샷 조회
- `PUT /api/v1/policies/snapshots/{id}/activate` - 정책 활성화
- `POST /api/v1/policies/snapshots/{id}/rollback` - 정책 롤백

**의존성:**
- `common`

### 4. detection - 탐지 엔진 도메인

**책임:**
- STR/CTR/WLF 탐지 규칙 실행
- 거래 데이터 분석
- 이벤트 생성

**핵심 개념: 배치 탐지 구조**
- `INSPECTION_INSTANCE`: 탐지 작업 인스턴스 (type, snapshot_ver, 생성 시간)
- `INSPECTION_EXECUTION`: 탐지 실행 이력 (status, start/end, 건수)
- `DETECTION_EVENT`: 탐지된 이벤트 (rule_id, severity)

**주요 API:**
- `POST /api/v1/detection/inspect` - 탐지 작업 시작
- `GET /api/v1/detection/events` - 탐지 이벤트 목록
- `GET /api/v1/detection/events/{id}` - 이벤트 상세

**의존성:**
- `common`, `policy`

### 5. investigation - 조사 도메인

**책임:**
- STR/CTR 사례 생성 및 관리
- 조사 활동 추적
- 승인 워크플로우 처리

**핵심 개념: 사례 생명주기**
- 이벤트 → 사례 생성
- 조사 → 증빙 자료 첨부
- 결재선 승인 → 보고
- 보관 → 폐기

**주요 테이블:**
- `alert_case`: 조사 사례
- `case_activity`: 활동 이력
- `case_attachment`: 첨부 파일

**주요 API:**
- `POST /api/v1/cases` - 사례 생성
- `GET /api/v1/cases` - 사례 목록
- `PUT /api/v1/cases/{id}/status` - 상태 변경
- `POST /api/v1/cases/{id}/activities` - 활동 기록

**의존성:**
- `common`, `detection`

### 6. reporting - 보고 도메인

**책임:**
- FIU 보고서 생성 (STR/CTR)
- 보고서 제출 이력 관리
- 통계 및 대시보드 데이터

**주요 테이블:**
- `report_str`: 의심거래보고서
- `report_ctr`: 고액현금거래보고서
- `submission_log`: 제출 이력

**주요 API:**
- `POST /api/v1/reports/str` - STR 보고서 생성
- `POST /api/v1/reports/ctr` - CTR 보고서 생성
- `GET /api/v1/reports` - 보고서 목록
- `POST /api/v1/reports/{id}/submit` - FIU 제출

**의존성:**
- `common`, `investigation`

### 7. batch - 배치 처리 모듈

**책임:**
- 야간 배치 탐지 작업
- 대량 데이터 처리 (Spring Batch)
- 통계 집계

**핵심 개념: Spring Batch 패턴**
- ItemReader → ItemProcessor → ItemWriter
- Chunk 기반 처리 (1000건씩)
- Skip/Retry 정책
- 병렬 처리 지원

**주요 Job:**
- `STR_DETECTION_JOB` - STR 탐지
- `CTR_DETECTION_JOB` - CTR 탐지
- `WLF_SCREENING_JOB` - WLF 필터링
- `DAILY_STATISTICS_JOB` - 일일 통계 집계

**의존성:**
- `common`, `detection`, `investigation`

### 8. simulation - 시뮬레이션 도메인

**책임:**
- What-if 분석
- 정책 백테스팅
- 리스크 시나리오 시뮬레이션

**주요 API:**
- `POST /api/v1/simulations` - 시뮬레이션 실행
- `GET /api/v1/simulations/{id}` - 시뮬레이션 결과
- `POST /api/v1/simulations/backtest` - 백테스트

**의존성:**
- `common`, `policy`, `detection`

### 9. server - 메인 애플리케이션

**책임:**
- 모든 도메인 모듈 통합
- Spring Boot 애플리케이션 부트스트랩
- 공통 설정 통합

**구성:**
- `InspectHubApplication.java` - 메인 클래스
- `application.yml` - 전역 설정
- 모든 도메인 모듈을 의존하여 하나의 애플리케이션으로 실행

**의존성:**
- 모든 도메인 모듈 (`admin`, `policy`, `detection`, `investigation`, `reporting`, `batch`, `simulation`)

---

## 📜 코딩 규칙

### 1. 명명 규칙

#### Package Naming
```
com.inspecthub.{module}.{layer}
```

**Examples:**
- `com.inspecthub.policy.controller`
- `com.inspecthub.policy.service`
- `com.inspecthub.policy.dto`
- `com.inspecthub.policy.mapper`

#### Class Naming

| 타입 | 명명 규칙 | 예시 |
|------|-----------|------|
| Controller | `*Controller` | `PolicyController` |
| Service | `*Service` | `PolicyService` |
| Mapper (Interface) | `*Mapper` | `PolicyMapper` |
| DTO (Request) | `*Request` | `CreatePolicyRequest` |
| DTO (Response) | `*Response` | `PolicyResponse` |
| Entity | PascalCase | `Policy`, `User` |
| Exception | `*Exception` | `PolicyNotFoundException` |

#### Method Naming

| 용도 | 명명 규칙 | 예시 |
|------|-----------|------|
| 조회 (단건) | `get*`, `find*` | `getUserById`, `findPolicyByVersion` |
| 조회 (목록) | `list*`, `findAll*` | `listPolicies`, `findAllActiveUsers` |
| 생성 | `create*` | `createPolicy`, `createUser` |
| 수정 | `update*` | `updatePolicy`, `updateStatus` |
| 삭제 | `delete*` | `deletePolicy`, `softDelete` |
| 검증 | `validate*`, `is*`, `has*` | `validatePolicy`, `isActive`, `hasPermission` |

#### Variable Naming

```java
// ✅ Good - camelCase, 명확한 의미
String userName;
LocalDateTime createdAt;
List<Policy> activePolicies;
boolean isApproved;

// ❌ Bad - 모호한 이름, 약어 남용
String un;
LocalDateTime dt;
List<Policy> pList;
boolean flag;
```

### 2. 코드 스타일

#### Indentation & Braces
```java
// ✅ Good - 4 spaces, braces on same line
public class PolicyService {
    public void createPolicy(PolicyRequest request) {
        if (request.isValid()) {
            // logic
        }
    }
}

// ❌ Bad - tabs, inconsistent braces
public class PolicyService 
{
	public void createPolicy(PolicyRequest request) 
	{
		if (request.isValid())
		{
			// logic
		}
	}
}
```

#### Line Length
- **최대 120자** (권장)
- 너무 긴 줄은 논리적 단위로 분리

```java
// ✅ Good
String result = someService
    .processData(param1, param2)
    .transform()
    .validate();

// ❌ Bad - 한 줄에 너무 많은 내용
String result = someService.processData(param1, param2).transform().validate().save();
```

#### Import Organization
```java
// ✅ Good - 그룹별 정렬
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.inspecthub.common.dto.ApiResponse;
import com.inspecthub.policy.dto.PolicyRequest;

// ❌ Bad - 정렬 없음, wildcard 사용
import java.util.*;
import com.inspecthub.policy.dto.PolicyRequest;
import org.springframework.stereotype.Service;
```

### 3. Annotation 순서

```java
// ✅ Good - 일관된 순서
@RestController
@RequestMapping("/api/v1/policies")
@Tag(name = "Policy", description = "Policy API")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {
    
    @GetMapping("/{id}")
    @Operation(summary = "Get Policy", description = "Retrieve policy by ID")
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicy(
        @PathVariable("id") String id
    ) {
        // ...
    }
}

// Class-level annotations order:
// 1. Stereotype (@RestController, @Service, @Repository)
// 2. Mapping (@RequestMapping)
// 3. Documentation (@Tag)
// 4. Lombok (@RequiredArgsConstructor, @Slf4j)
// 5. Other

// Method-level annotations order:
// 1. Mapping (@GetMapping, @PostMapping)
// 2. Documentation (@Operation)
// 3. Security (@PreAuthorize)
// 4. Transaction (@Transactional)
// 5. Other
```

### 4. 주석 규칙

#### JavaDoc (Public API)
```java
/**
 * Creates a new policy snapshot.
 * 
 * @param request the policy creation request
 * @return the created policy response
 * @throws PolicyValidationException if policy validation fails
 */
@PostMapping
public ResponseEntity<ApiResponse<PolicyResponse>> createPolicy(
    @Valid @RequestBody CreatePolicyRequest request
) {
    // implementation
}
```

#### Inline Comments
```java
// ✅ Good - Why, not what
// Use cached value to avoid expensive database query
Policy cached = cache.get(policyId);

// ❌ Bad - Obvious from code
// Get policy from cache
Policy cached = cache.get(policyId);
```

#### TODO/FIXME
```java
// TODO(username): Implement pagination for large result sets
// FIXME(username): Race condition when updating status concurrently
```

### 5. Exception Handling

```java
// ✅ Good - Specific exceptions, meaningful messages
@Service
@RequiredArgsConstructor
public class PolicyService {
    
    public Policy getPolicy(String id) {
        return policyMapper.findById(id)
            .orElseThrow(() -> new PolicyNotFoundException(
                "Policy not found with id: " + id
            ));
    }
    
    public void validatePolicy(Policy policy) {
        if (policy.getEffectiveTo().isBefore(policy.getEffectiveFrom())) {
            throw new PolicyValidationException(
                "Effective end date must be after start date"
            );
        }
    }
}

// ❌ Bad - Generic exceptions, no context
public Policy getPolicy(String id) {
    Policy policy = policyMapper.findById(id);
    if (policy == null) {
        throw new RuntimeException("Error");
    }
    return policy;
}
```

### 6. Logging

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyService {
    
    public Policy createPolicy(CreatePolicyRequest request) {
        log.info("Creating policy: type={}, version={}", 
            request.getType(), request.getVersion());
        
        try {
            Policy policy = // ... create policy
            log.info("Policy created successfully: id={}", policy.getId());
            return policy;
        } catch (Exception e) {
            log.error("Failed to create policy: request={}", request, e);
            throw e;
        }
    }
}

// Log Levels:
// - TRACE: Very detailed, rarely used
// - DEBUG: Development debugging
// - INFO: Important business events
// - WARN: Potential issues
// - ERROR: Error conditions
```

### 7. Constant Values

```java
// ✅ Good - Constants in dedicated class or interface
public class PolicyConstants {
    public static final String TYPE_STR = "STR";
    public static final String TYPE_CTR = "CTR";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_VERSION_HISTORY = 100;
}

// ❌ Bad - Magic numbers/strings
if (policy.getType().equals("STR")) { // What is "STR"?
    // ...
}
```

---

## 🗄 데이터베이스 접근

### MyBatis Mapper 패턴

#### 1. Mapper Interface

```java
package com.inspecthub.policy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PolicyMapper {
    
    // INSERT
    int insert(Policy policy);
    
    // SELECT (단건)
    Optional<Policy> findById(@Param("id") String id);
    
    // SELECT (목록)
    List<Policy> findAll(@Param("status") String status);
    
    // UPDATE
    int update(Policy policy);
    
    // DELETE (논리적 삭제)
    int softDelete(@Param("id") String id, @Param("deletedBy") String deletedBy);
    
    // Custom queries
    List<Policy> findActiveByType(@Param("type") String type);
    int countByStatus(@Param("status") String status);
}
```

#### 2. Mapper XML

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.inspecthub.policy.mapper.PolicyMapper">

    <!-- ResultMap -->
    <resultMap id="PolicyResultMap" type="com.inspecthub.policy.entity.Policy">
        <id property="id" column="id"/>
        <result property="type" column="type"/>
        <result property="version" column="version"/>
        <result property="status" column="status"/>
        <result property="effectiveFrom" column="effective_from"/>
        <result property="effectiveTo" column="effective_to"/>
        <result property="configJson" column="config_json" 
                typeHandler="org.apache.ibatis.type.JsonTypeHandler"/>
        <result property="createdAt" column="created_at"/>
        <result property="createdBy" column="created_by"/>
    </resultMap>

    <!-- INSERT -->
    <insert id="insert">
        INSERT INTO standard_snapshot (
            id, type, version, status,
            effective_from, effective_to, config_json,
            created_at, created_by
        ) VALUES (
            #{id}, #{type}, #{version}, #{status},
            #{effectiveFrom}, #{effectiveTo}, #{configJson}::jsonb,
            #{createdAt}, #{createdBy}
        )
    </insert>

    <!-- SELECT by ID -->
    <select id="findById" resultMap="PolicyResultMap">
        SELECT *
        FROM standard_snapshot
        WHERE id = #{id}
          AND deleted = FALSE
    </select>

    <!-- SELECT with dynamic WHERE -->
    <select id="findAll" resultMap="PolicyResultMap">
        SELECT *
        FROM standard_snapshot
        WHERE deleted = FALSE
        <if test="status != null">
            AND status = #{status}
        </if>
        ORDER BY created_at DESC
    </select>

    <!-- UPDATE -->
    <update id="update">
        UPDATE standard_snapshot
        SET status = #{status},
            effective_to = #{effectiveTo},
            updated_at = #{updatedAt},
            updated_by = #{updatedBy}
        WHERE id = #{id}
          AND deleted = FALSE
    </update>

    <!-- Soft DELETE -->
    <update id="softDelete">
        UPDATE standard_snapshot
        SET deleted = TRUE,
            deleted_at = NOW(),
            deleted_by = #{deletedBy}
        WHERE id = #{id}
          AND deleted = FALSE
    </update>

    <!-- Custom query with JOIN -->
    <select id="findActiveByType" resultMap="PolicyResultMap">
        SELECT s.*
        FROM standard_snapshot s
        WHERE s.type = #{type}
          AND s.status = 'ACTIVE'
          AND s.deleted = FALSE
          AND s.effective_from &lt;= NOW()
          AND (s.effective_to IS NULL OR s.effective_to &gt; NOW())
        ORDER BY s.version DESC
    </select>

</mapper>
```

#### 3. Service Layer Usage

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {
    
    private final PolicyMapper policyMapper;
    
    @Transactional
    public Policy createPolicy(CreatePolicyRequest request) {
        Policy policy = Policy.builder()
            .type(request.getType())
            .version(request.getVersion())
            .status("DRAFT")
            .effectiveFrom(request.getEffectiveFrom())
            .configJson(request.getConfigJson())
            .build();
        
        // Set ULID and timestamps
        policy.prePersist(getCurrentUserId());
        
        // Insert
        policyMapper.insert(policy);
        
        log.info("Policy created: id={}", policy.getId());
        return policy;
    }
    
    public Policy getPolicy(String id) {
        return policyMapper.findById(id)
            .orElseThrow(() -> new PolicyNotFoundException(
                "Policy not found: " + id
            ));
    }
    
    @Transactional
    public void deletePolicy(String id) {
        Policy policy = getPolicy(id);
        policyMapper.softDelete(id, getCurrentUserId());
        log.info("Policy soft deleted: id={}", id);
    }
}
```

### ULID 사용

모든 Primary Key는 **ULID (26자)**를 사용합니다.

```java
// BaseDomain을 상속받으면 자동 지원
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Policy extends BaseDomain {
    private String type;
    private Integer version;
    // ...
}

// Entity 생성 시
Policy policy = Policy.builder()
    .type("STR")
    .version(1)
    .build();

policy.prePersist(currentUserId); // ULID 자동 생성
System.out.println(policy.getId()); // "01ARZ3NDEKTSV4RRFFQ69G5FAV"
```

**상세 가이드:** [ULID.md](./ULID.md)

---

## 🌐 API 개발

### API 구조

```
/api/v1/{domain}/{resource}[/{id}][/{action}]
```

**Examples:**
- `GET /api/v1/policies` - 정책 목록
- `GET /api/v1/policies/{id}` - 정책 상세
- `POST /api/v1/policies` - 정책 생성
- `PUT /api/v1/policies/{id}` - 정책 수정
- `PUT /api/v1/policies/{id}/activate` - 정책 활성화
- `POST /api/v1/policies/{id}/rollback` - 정책 롤백

### Controller 패턴

```java
@RestController
@RequestMapping("/api/v1/policies")
@Tag(name = "Policy", description = "Policy Management API")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {
    
    private final PolicyService policyService;
    
    // ====================================================================
    // CREATE
    // ====================================================================
    
    @PostMapping
    @Operation(
        summary = "Create Policy",
        description = "Create a new policy snapshot"
    )
    public ResponseEntity<ApiResponse<PolicyResponse>> createPolicy(
        @Valid @RequestBody CreatePolicyRequest request
    ) {
        log.info("Creating policy: type={}, version={}", 
            request.getType(), request.getVersion());
        
        Policy policy = policyService.createPolicy(request);
        PolicyResponse response = PolicyMapper.INSTANCE.toResponse(policy);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }
    
    // ====================================================================
    // READ
    // ====================================================================
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Policy",
        description = "Retrieve policy snapshot by ID"
    )
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicy(
        @PathVariable("id") @Schema(description = "Policy ID") String id
    ) {
        Policy policy = policyService.getPolicy(id);
        PolicyResponse response = PolicyMapper.INSTANCE.toResponse(policy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @Operation(
        summary = "List Policies",
        description = "List all policy snapshots with optional filters"
    )
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> listPolicies(
        @RequestParam(required = false) 
        @Schema(description = "Policy type (STR, CTR, etc.)")
        String type,
        
        @RequestParam(required = false)
        @Schema(description = "Policy status (DRAFT, ACTIVE, etc.)")
        String status
    ) {
        List<Policy> policies = policyService.listPolicies(type, status);
        List<PolicyResponse> responses = policies.stream()
            .map(PolicyMapper.INSTANCE::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    // ====================================================================
    // UPDATE
    // ====================================================================
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Update Policy",
        description = "Update policy snapshot (only DRAFT status)"
    )
    public ResponseEntity<ApiResponse<PolicyResponse>> updatePolicy(
        @PathVariable("id") String id,
        @Valid @RequestBody UpdatePolicyRequest request
    ) {
        Policy policy = policyService.updatePolicy(id, request);
        PolicyResponse response = PolicyMapper.INSTANCE.toResponse(policy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}/activate")
    @Operation(
        summary = "Activate Policy",
        description = "Activate policy snapshot (DRAFT → ACTIVE)"
    )
    public ResponseEntity<ApiResponse<Void>> activatePolicy(
        @PathVariable("id") String id
    ) {
        policyService.activatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // ====================================================================
    // DELETE
    // ====================================================================
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Policy",
        description = "Soft delete policy snapshot"
    )
    public ResponseEntity<ApiResponse<Void>> deletePolicy(
        @PathVariable("id") String id
    ) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // ====================================================================
    // Custom Actions
    // ====================================================================
    
    @PostMapping("/{id}/rollback")
    @Operation(
        summary = "Rollback Policy",
        description = "Rollback to previous policy version"
    )
    public ResponseEntity<ApiResponse<PolicyResponse>> rollbackPolicy(
        @PathVariable("id") String id
    ) {
        Policy policy = policyService.rollbackPolicy(id);
        PolicyResponse response = PolicyMapper.INSTANCE.toResponse(policy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### DTO 패턴

#### Request DTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create Policy Request")
public class CreatePolicyRequest {
    
    @NotBlank(message = "Type is required")
    @Schema(description = "Policy type", example = "STR", 
            allowableValues = {"STR", "CTR", "WLF", "RBA", "KYC", "FIU"})
    private String type;
    
    @NotNull(message = "Version is required")
    @Min(value = 1, message = "Version must be at least 1")
    @Schema(description = "Policy version", example = "1")
    private Integer version;
    
    @NotNull(message = "Effective from date is required")
    @Schema(description = "Effective start date", example = "2025-01-13T00:00:00")
    private LocalDateTime effectiveFrom;
    
    @Schema(description = "Effective end date", example = "2025-12-31T23:59:59")
    private LocalDateTime effectiveTo;
    
    @NotNull(message = "Configuration is required")
    @Schema(description = "Policy configuration (JSON)")
    private Map<String, Object> configJson;
}
```

#### Response DTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Policy Response")
public class PolicyResponse {
    
    @Schema(description = "Policy ID (ULID)", example = "01ARZ3NDEKTSV4RRFFQ69G5FAV")
    private String id;
    
    @Schema(description = "Policy type", example = "STR")
    private String type;
    
    @Schema(description = "Policy version", example = "1")
    private Integer version;
    
    @Schema(description = "Policy status", example = "ACTIVE")
    private String status;
    
    @Schema(description = "Effective start date")
    private LocalDateTime effectiveFrom;
    
    @Schema(description = "Effective end date")
    private LocalDateTime effectiveTo;
    
    @Schema(description = "Policy configuration (JSON)")
    private Map<String, Object> configJson;
    
    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Created by (User ID)")
    private String createdBy;
}
```

### ApiResponse 래퍼

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .build();
    }
    
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
            .success(true)
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .build();
    }
}
```

### Swagger/OpenAPI 문서화

모든 API는 Swagger UI로 자동 문서화됩니다.

**접속:**
- Swagger UI: `http://localhost:8090/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8090/v3/api-docs`

**주요 Annotations:**
- `@Tag`: Controller 그룹 설명
- `@Operation`: API 엔드포인트 설명
- `@Schema`: DTO 필드 설명
- `@Parameter`: 파라미터 설명

**상세 가이드:** [AGENTS.md](./AGENTS.md) - Swagger/OpenAPI 섹션 참고

---

## 🧪 테스트 전략

### 테스트 피라미드

```
        ╱╲
       ╱E2E╲         10% - 통합 테스트 (느림)
      ╱━━━━━━╲
     ╱ Integration╲   20% - 통합 테스트 (중간)
    ╱━━━━━━━━━━━━━━╲
   ╱   Unit Tests   ╲ 70% - 단위 테스트 (빠름)
  ╱━━━━━━━━━━━━━━━━━━━╲
```

### 1. Unit Tests (단위 테스트)

**대상:** Service 레이어 비즈니스 로직

```java
@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {
    
    @Mock
    private PolicyMapper policyMapper;
    
    @InjectMocks
    private PolicyService policyService;
    
    @Test
    @DisplayName("정책 생성 - 성공")
    void createPolicy_Success() {
        // Given
        CreatePolicyRequest request = CreatePolicyRequest.builder()
            .type("STR")
            .version(1)
            .effectiveFrom(LocalDateTime.now())
            .configJson(Map.of("threshold", 10000))
            .build();
        
        // When
        Policy result = policyService.createPolicy(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull(); // ULID 생성 확인
        assertThat(result.getType()).isEqualTo("STR");
        
        verify(policyMapper, times(1)).insert(any(Policy.class));
    }
    
    @Test
    @DisplayName("정책 조회 - 존재하지 않음")
    void getPolicy_NotFound() {
        // Given
        String id = "01ARZ3NDEKTSV4RRFFQ69G5FAV";
        when(policyMapper.findById(id)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> policyService.getPolicy(id))
            .isInstanceOf(PolicyNotFoundException.class)
            .hasMessageContaining(id);
    }
}
```

### 2. Integration Tests (통합 테스트)

**대상:** Controller + Service + MyBatis + Database

```java
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql") // 테스트 데이터 초기화
@Transactional // 테스트 후 롤백
class PolicyControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("정책 생성 API - 성공")
    void createPolicy_Success() throws Exception {
        // Given
        CreatePolicyRequest request = CreatePolicyRequest.builder()
            .type("STR")
            .version(1)
            .effectiveFrom(LocalDateTime.now())
            .configJson(Map.of("threshold", 10000))
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.type").value("STR"))
            .andDo(print());
    }
    
    @Test
    @DisplayName("정책 조회 API - 존재하지 않음")
    void getPolicy_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/policies/INVALID_ID"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("POLICY_NOT_FOUND"))
            .andDo(print());
    }
}
```

### 3. Testcontainers (Database Tests)

```java
@SpringBootTest
@Testcontainers
class PolicyRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername());
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private PolicyMapper policyMapper;
    
    @Test
    @DisplayName("정책 저장 및 조회")
    void saveAndFind() {
        // Given
        Policy policy = Policy.builder()
            .type("STR")
            .version(1)
            .status("DRAFT")
            .build();
        policy.prePersist("TEST_USER");
        
        // When
        policyMapper.insert(policy);
        Policy found = policyMapper.findById(policy.getId()).orElse(null);
        
        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(policy.getId());
        assertThat(found.getType()).isEqualTo("STR");
    }
}
```

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :policy:test

# 특정 테스트 클래스
./gradlew test --tests PolicyServiceTest

# 테스트 커버리지 리포트
./gradlew test jacocoTestReport
# 리포트 위치: build/reports/jacoco/test/html/index.html

# 통합 테스트만 실행
./gradlew integrationTest

# 빠른 빌드 (테스트 제외)
./gradlew build -x test
```

---

## 🚀 빌드 및 배포

### 로컬 빌드

```bash
# Clean + Build
./gradlew clean build

# JAR 생성 위치
ls -lh server/build/libs/inspect-hub-server-*.jar

# 실행 (개발 환경)
java -jar -Dspring.profiles.active=local \
    server/build/libs/inspect-hub-server-1.0.0-SNAPSHOT.jar
```

### Docker 빌드

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY server/build/libs/inspect-hub-server-*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Docker 이미지 빌드
docker build -t inspect-hub-backend:latest .

# Docker 컨테이너 실행
docker run -d \
  -p 8090:8090 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://db:5432/inspecthub \
  -e DB_USERNAME=inspecthub \
  -e DB_PASSWORD=secret \
  inspect-hub-backend:latest
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  backend:
    build: .
    ports:
      - "8090:8090"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/inspecthub
      DB_USERNAME: inspecthub
      DB_PASSWORD: secret
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:14
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: inspecthub
      POSTGRES_USER: inspecthub
      POSTGRES_PASSWORD: secret
    volumes:
      - postgres-data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres-data:
```

```bash
# 전체 스택 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f backend

# 종료
docker-compose down
```

### 프로파일 구성

```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}

---
# application-local.yml
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:postgresql://localhost:5432/inspecthub_dev
    username: inspecthub
    password: password

---
# application-prod.yml
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

---

## 📖 참고 문서

### 프로젝트 문서

| 문서 | 위치 | 설명 |
|------|------|------|
| **API Contract** | [/docs/api/CONTRACT.md](../api/CONTRACT.md) | Frontend ↔ Backend API 계약 |
| **Database Design** | [/docs/architecture/DATABASE.md](../architecture/DATABASE.md) | 데이터베이스 스키마 및 설계 |
| **Architecture Overview** | [/docs/architecture/OVERVIEW.md](../architecture/OVERVIEW.md) | 시스템 아키텍처 개요 |
| **DDD Design** | [/docs/architecture/DDD_DESIGN.md](../architecture/DDD_DESIGN.md) | DDD 레이어 구조 및 도메인 모델 |
| **Backend Agents** | [/docs/backend/AGENTS.md](./AGENTS.md) | API Generator, Module Validator |
| **ULID Guide** | [/docs/backend/ULID.md](./ULID.md) | ULID 식별자 가이드 |
| **PRD** | [/docs/prd/index.md](../prd/index.md) | 제품 요구사항 문서 (15개 파일) |

### 외부 문서

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Springdoc OpenAPI](https://springdoc.org/)
- [ULID Specification](https://github.com/ulid/spec)

---

## 🔧 Troubleshooting

### 빌드 오류

**Problem:** `Could not find method compile()`

**Solution:** Gradle 7.0+ 사용. `compile` → `implementation` 변경

**Problem:** `Module 'common' not found`

**Solution:** 모듈 의존성 확인
```bash
./gradlew :common:build
./gradlew :server:dependencies
```

### 실행 오류

**Problem:** `Unable to connect to database`

**Solution:** 
1. PostgreSQL 실행 여부 확인
2. 연결 정보 확인 (.env 또는 환경 변수)
3. 데이터베이스 생성 확인

**Problem:** `Port 8090 is already in use`

**Solution:**
```bash
# 포트 사용 프로세스 확인
lsof -i :8090
# 또는
netstat -tulpn | grep 8090

# 프로세스 종료
kill -9 <PID>
```

### 테스트 오류

**Problem:** `No tests found`

**Solution:** Test 클래스/메서드에 `@Test` annotation 확인

**Problem:** `MockitoException: Cannot mock final class`

**Solution:** `build.gradle`에 추가:
```gradle
testImplementation 'org.mockito:mockito-inline:5.x.x'
```

---

## 📞 문의

문서 관련 문의:
- **Issues**: GitHub Issues
- **Email**: inspect-hub-team@example.com
- **Slack**: #inspect-hub-backend

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-13 | Backend README.md 초안 작성 | PM |

---

**Next Steps:**
1. API 엔드포인트 개발 시작 (각 도메인 모듈)
2. 통합 테스트 작성
3. CI/CD 파이프라인 구축
4. 성능 테스트 및 최적화

**See Also:**
- [Frontend README](../frontend/README.md) - 프론트엔드 가이드
- [API Contract](../api/CONTRACT.md) - API 계약서
- [Development Guide](../development/AGENTS.md) - 개발 규칙
