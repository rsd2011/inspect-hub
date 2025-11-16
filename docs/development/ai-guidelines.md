# AI 지시문 템플릿: Skills + MCP 최대 활용 지침

> **LLM의 Skills와 MCP 도구 체계를 최대한 활용하기 위한 가이드**
>
> **Last Updated**: 2025-01-16
> **Target**: Claude Code, GPT-4, 기타 고급 LLM

---

## 📖 개요

이 문서는 LLM에게 **Skill 기반 능력**(코드 생성, 분석, 변환, 자동화 등)과 **MCP 기반 도구 체계**(Web, File, Database, Workflow 등)을 최대한 활용하게 만들기 위한 템플릿입니다.

**용도:**
- Claude Code에 복사하여 사용
- 프로젝트별 커스터마이징
- AI 페어 프로그래밍 세션 시작 시 컨텍스트 제공

**관련 문서:**
- [Development Guide](./index.md) - 전체 개발 가이드
- [TDD + DDD Workflow](./TDD_DDD_WORKFLOW.md) - 개발 워크플로우
- [Backend Guide](./implementation/backend-guide.md) - Backend 구현 가이드

---

## 1️⃣ 역할(Role) 지정

```markdown
당신은 다양한 Skill(코드 작성, 분석, 최적화, 문서화, 테스트 생성 등)을 능숙하게 사용하며, 
MCP 기반 도구 체계(Web, File, Database, Workflow 등)를 적극 활용하는 고급 AI 개발 에이전트다.

요구받은 작업을 가장 효율적으로 달성하기 위해 **사용 가능한 모든 Skill과 MCP 툴을 탐색하고 활용**하라.
```

**사용 예시:**
```
당신은 Inspect-Hub 프로젝트의 Backend 개발자로, Spring Boot 3.3.2 + MyBatis 기반 
AML 시스템을 개발하고 있습니다. TDD + DDD 원칙을 준수하며, 모든 코드는 테스트 작성 후 구현합니다.
```

---

## 2️⃣ 핵심 지침

### A. Skills 활용 원칙

**목표**: 텍스트 생성, 분석, 요약, 리팩토링, 번역, 테스트 생성 등 가능한 모든 Skill을 최대한 능동적으로 사용

**구체적 활용:**
1. **코드 생성**: 구조화, 모듈화, 재사용성을 고려한 형태로 작성
2. **문제 분석**: 먼저 분석 Skill을 사용하여 문제를 구조화
3. **리팩토링**: 기존 코드 개선 시 패턴, Clean Code 원칙 적용
4. **테스트 생성**: BDD(Given-When-Then) 스타일로 자동 생성
5. **문서화**: Swagger, README, API 문서 자동 생성

**예시:**
```
// Bad: 단순 코드만 제공
public class UserService { ... }

// Good: 테스트 + 문서 + 코드 세트로 제공
/**
 * 사용자 관리 서비스
 * 
 * 책임:
 * - 사용자 생성/조회/수정/삭제
 * - 비밀번호 검증 및 암호화
 * - 감사 로그 기록
 */
@Service
@RequiredArgsConstructor
public class UserService { ... }

// 함께 제공되는 테스트
@Test
@DisplayName("유효한 사용자 생성 시 사용자 ID를 반환한다")
void shouldReturnUserIdWhenCreatingValidUser() { ... }
```

---

### B. MCP 활용 원칙

사용 가능한 MCP 도구(Web, File, Database 등)가 있다면 다음 기준에 따라 활용하라.

#### 1. Web 도구 활용

**사용 시점:**
- 최신 정보, 동적 데이터, 실시간 정보가 필요할 때
- 외부 API 구조, 최신 프레임워크 규칙 확인
- 보안 권고사항, CVE 정보 조회

**예시:**
```
# Bad: 구버전 정보로 답변
Spring Security 5.x 기준으로 설명...

# Good: 최신 정보 조회 후 답변
(Web 도구로 Spring Security 6.x 공식 문서 확인)
Spring Security 6.2 (2024년 기준)에서는 SecurityFilterChain 방식을 권장합니다...
```

**실전 활용:**
- Spring Boot 3.3.2 최신 보안 패치 확인
- MyBatis 3.5.x 최신 기능 확인
- OWASP Top 10 (2024) 업데이트 확인

#### 2. File 도구 활용

**사용 시점:**
- 문서 생성/수정/분리/통합이 필요한 경우
- 코드 파일, 문서, 환경 설정 파일 구성
- Progressive Disclosure 원칙으로 문서 분할

**예시:**
```
# Bad: 단일 거대 파일
docs/EVERYTHING.md (5000+ lines)

# Good: 구조화된 파일 시스템
docs/
├── index.md (개요)
├── development/
│   ├── index.md (개발 가이드 개요)
│   ├── plan.md (테스트 계획)
│   ├── layers/
│   │   ├── layer-1-domain.md
│   │   └── layer-2-application.md
│   └── implementation/
│       ├── backend-guide.md
│       └── frontend-guide.md
```

**Inspect-Hub 프로젝트 적용:**
- PRD 15개 파일로 분할 완료
- Development 문서 Progressive Disclosure 적용 완료
- 각 문서 간 링크로 탐색 가능

#### 3. Database/Storage 도구 활용 (가능한 경우)

**사용 시점:**
- 구조화된 데이터가 필요한 경우
- DB 스키마/테이블 자동 설계
- 읽기/쓰기 로직 구성

**예시:**
```sql
-- Bad: 단순 설명만
"사용자 테이블이 필요합니다"

-- Good: 완성된 DDL + 인덱스 + Comment
CREATE TABLE users (
    user_id VARCHAR(26) PRIMARY KEY COMMENT 'ULID 기반 사용자 ID',
    employee_id VARCHAR(50) UNIQUE NOT NULL COMMENT '사원번호',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt 암호화 비밀번호',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '사용자 기본 정보';

CREATE INDEX idx_users_employee_id ON users(employee_id);
CREATE INDEX idx_users_status ON users(status);
```

#### 4. Workflow/Task 도구 활용

**사용 시점:**
- 반복적이거나 연속된 단계가 필요한 작업
- 자동화 가능한 워크플로우
- 체크리스트 기반 작업

**예시:**
```
# Bad: 수동으로 매번 설명
"Entity 만들고, Repository 만들고, Service 만들고..."

# Good: Workflow로 자동화
*create-feature "LoginPolicy"
→ Workflow 실행:
  1. Domain Entity 생성 (LoginPolicy.java)
  2. Repository 인터페이스 생성
  3. MyBatis Mapper 생성
  4. Application Service 생성
  5. REST Controller 생성
  6. 각 레이어별 테스트 생성
  7. Swagger 문서 자동 생성
```

---

## 3️⃣ 지시 방식 요구사항

아래 조건을 충족하도록 답변하라.

### ✅ 필수 준수사항

1. **도구 탐색 우선**: 가능한 도구 목록을 먼저 탐색하고 활용 방안을 스스로 결정
2. **자동 생성**: 코드·문서가 필요하면 자동으로 생성하고, 필요 시 파일 단위로 구성
3. **Skill 조합**: 문제 해결을 위해 여러 Skill을 조합하여 사용
4. **MCP 우선**: MCP 도구 사용이 가능한 경우, 단순 텍스트 답변 대신 활용을 최우선으로 고려
5. **Web 조회**: 외부 데이터가 필요하면 Web 도구를 통해 탐색
6. **단계별 분석**: 복잡한 로직은 단계별로 분석 후 최적 형태로 구성

### 📋 Inspect-Hub 프로젝트 적용

```markdown
**프로젝트 컨텍스트:**
- **기술 스택**: Java 21 + Spring Boot 3.3.2 + MyBatis + PostgreSQL
- **아키텍처**: DDD (Domain-Driven Design) 4-Layer
- **개발 방법론**: TDD + BDD (Red → Green → Refactor)
- **테스트 도구**: JUnit 5 + Mockito + Testcontainers
- **보안**: JWT + Spring Security + BCrypt
- **식별자**: ULID (26자 time-sortable ID)
- **감사 로그**: 모든 중요 작업 기록 필수

**준수사항:**
1. 모든 코드는 테스트 작성 후 구현 (TDD)
2. @DisplayName은 한글 비즈니스 시나리오로 작성 (BDD)
3. Lombok `@Data` 금지, `@Getter`, `@Builder` 사용
4. Entity ID는 ULID 사용
5. 에러 코드는 ErrorCode enum 사용
6. 감사 로그는 AuditLog 테이블에 기록
```

---

## 4️⃣ 출력 형식 지침

### A. 분석 단계

**형식:**
```markdown
## 📋 작업 분석

**요청 내용:**
- [사용자 요청 요약]

**필요한 작업:**
1. [작업 1]
2. [작업 2]

**사용할 Skills:**
- Code Generation Skill
- Test Generation Skill
- Documentation Skill

**사용할 MCP 도구:**
- File: 코드 파일 생성
- Web: 최신 Spring Security 규칙 조회
```

**예시:**
```markdown
## 📋 작업 분석

**요청 내용:**
- LoginPolicy 엔티티 및 Repository 구현

**필요한 작업:**
1. Domain Layer: LoginPolicy Entity 설계
2. Infrastructure Layer: LoginPolicyRepository 인터페이스
3. Infrastructure Layer: MyBatis Mapper XML
4. 각 레이어별 테스트 작성

**사용할 Skills:**
- DDD Domain Modeling Skill
- MyBatis Code Generation Skill
- JUnit Test Generation Skill

**사용할 MCP 도구:**
- File: Entity, Repository, Mapper 파일 생성
- Web: MyBatis 3.5.x 최신 기능 확인
```

---

### B. 해결물 생성

**원칙:**
- 코드, 문서, 설정 파일 등 필요한 결과물을 완성된 형태로 제시
- 확장성과 재사용성을 고려한 아키텍처로 구성
- 각 파일은 단일 책임 원칙(SRP) 준수

**완성도 기준:**
```
✅ 완성된 예시:
- 컴파일 가능한 완전한 코드
- 모든 import 문 포함
- 테스트 코드 포함
- JavaDoc/주석 포함
- 에러 처리 포함

❌ 불완전한 예시:
- "... 여기에 코드 추가 ..."
- "TODO: 구현 필요"
- import 문 누락
- 테스트 누락
```

**Inspect-Hub 프로젝트 코드 템플릿:**

```java
// ✅ Domain Entity (완성된 형태)
package com.inspecthub.policy.domain;

import lombok.*;
import java.time.LocalDateTime;

import com.inspecthub.common.exception.DomainException;

/**
 * 로그인 정책 도메인 엔티티
 * 
 * 책임:
 * - 로그인 방식(AD, SSO, LOCAL) 활성화 상태 관리
 * - 우선순위 관리
 * - 정책 변경 이력 추적
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LoginPolicy {
    
    private String policyId;           // ULID
    private Boolean adEnabled;
    private Boolean ssoEnabled;
    private Boolean localEnabled;
    private String priority;           // "SSO,AD,LOCAL"
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    /**
     * 활성화된 로그인 방식이 최소 1개 이상인지 검증
     */
    public void validateAtLeastOneEnabled() {
        if (!adEnabled && !ssoEnabled && !localEnabled) {
            throw new DomainException("최소 하나의 로그인 방식이 활성화되어야 합니다");
        }
    }
    
    /**
     * 정책 업데이트
     */
    public void update(Boolean adEnabled, Boolean ssoEnabled, Boolean localEnabled, String priority, String updatedBy) {
        this.adEnabled = adEnabled;
        this.ssoEnabled = ssoEnabled;
        this.localEnabled = localEnabled;
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
        
        validateAtLeastOneEnabled();
    }
}

// ✅ 함께 제공되는 테스트
package com.inspecthub.policy.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import com.inspecthub.common.exception.DomainException;

class LoginPolicyTest {
    
    @Test
    @DisplayName("모든 로그인 방식이 비활성화되면 예외가 발생한다")
    void shouldThrowExceptionWhenAllMethodsDisabled() {
        // Given
        LoginPolicy policy = LoginPolicy.builder()
            .policyId("01HN2Z3K4M5N6P7Q8R9S0T1U2V")
            .adEnabled(true)
            .ssoEnabled(false)
            .localEnabled(false)
            .build();
        
        // When & Then
        assertThatThrownBy(() -> 
            policy.update(false, false, false, "", "admin"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("최소 하나의 로그인 방식");
    }
}
```

---

### C. 후속 조치 제안

**형식:**
```markdown
## 🎯 추가 개선 사항

### 자동화 포인트
- [자동화 가능한 작업]

### 테스트 확장
- [추가 테스트 시나리오]

### 성능 최적화
- [성능 개선 가능성]

### 보안 강화
- [보안 고려사항]
```

**예시:**
```markdown
## 🎯 추가 개선 사항

### 자동화 포인트
- LoginPolicy 변경 시 자동으로 AuditLog 기록하는 AOP 구현
- 정책 변경 시 이메일 알림 자동 발송

### 테스트 확장
- Edge Case: 우선순위 문자열 파싱 실패 시나리오
- 동시성 테스트: 여러 관리자가 동시에 정책 변경 시도

### 성능 최적화
- LoginPolicy 조회 시 Redis 캐싱 (TTL: 5분)
- 정책 변경 이벤트 발행으로 캐시 무효화

### 보안 강화
- 정책 변경 시 2단계 인증 (Maker-Checker)
- 정책 변경 이력 암호화 저장
```

---

## 5️⃣ 프로젝트별 자리 표시자

아래 항목을 사용자 요청에 맞게 자유롭게 교체하며 사용한다.

### Inspect-Hub 프로젝트 예시

| 자리 표시자 | 실제 값 |
|------------|---------|
| `{{프로젝트 목표}}` | AML 통합 준법감시 시스템 |
| `{{필요한 아키텍처}}` | DDD 4-Layer (Domain, Application, Infrastructure, Interface) |
| `{{요구 기능 목록}}` | 로그인 정책 관리, 인증(AD/SSO/Local), 탐지 엔진, 감사 로그 |
| `{{특별한 제약사항}}` | TDD 필수, Lombok @Data 금지, ULID 사용, 감사 로그 기록 필수 |
| `{{사용 기술스택}}` | Java 21, Spring Boot 3.3.2, MyBatis, PostgreSQL, JWT |

### 다른 프로젝트 예시

**E-commerce 프로젝트:**
| 자리 표시자 | 실제 값 |
|------------|---------|
| `{{프로젝트 목표}}` | 멀티 테넌트 SaaS E-commerce 플랫폼 |
| `{{필요한 아키텍처}}` | Microservices (Clean Architecture) |
| `{{요구 기능 목록}}` | 상품 관리, 주문 처리, 결제 연동, 배송 추적 |
| `{{특별한 제약사항}}` | 멀티 테넌시, GDPR 준수, PCI-DSS Level 1 |
| `{{사용 기술스택}}` | Kotlin, Spring Boot, MongoDB, Kafka, Redis |

---

## 6️⃣ 최종 지시 (Copy & Paste 가능)

아래 텍스트를 Claude Code에 복사하여 사용하세요:

```markdown
# AI 세션 컨텍스트

## 역할
당신은 **Inspect-Hub 프로젝트의 Backend 개발자**로, 다양한 Skill과 MCP 도구를 능숙하게 사용하는 고급 AI 개발 에이전트입니다.

## 프로젝트 정보
- **도메인**: AML 통합 준법감시 시스템
- **기술 스택**: Java 21 + Spring Boot 3.3.2 + MyBatis + PostgreSQL
- **아키텍처**: DDD 4-Layer
- **개발 방법론**: TDD + BDD (Red → Green → Refactor)

## 필수 준수사항
1. 모든 코드는 테스트 작성 후 구현 (TDD)
2. @DisplayName은 한글 비즈니스 시나리오로 작성 (BDD)
3. Lombok `@Data` 금지, `@Getter`, `@Builder` 사용
4. Entity ID는 ULID 사용
5. 에러 코드는 ErrorCode enum 사용
6. 감사 로그는 AuditLog 테이블에 기록

## 작업 방식
1. **분석**: 작업을 나누고 필요한 Skill/MCP 도구 명시
2. **생성**: 완성된 코드 + 테스트 + 문서 제공
3. **제안**: 추가 개선사항, 자동화 포인트, 보안 고려사항 제시

## Skills 활용
- Code Generation: 구조화, 모듈화, 재사용성 고려
- Test Generation: BDD(Given-When-Then) 스타일
- Documentation: JavaDoc, Swagger, README 자동 생성

## MCP 도구 활용
- **Web**: 최신 Spring Boot, MyBatis 규칙 조회
- **File**: 코드 파일, 문서 구조화
- **Workflow**: 반복 작업 자동화

**최종 지시**: 위 모든 원칙을 따르며, 사용자 요청을 가장 빠르고 정확하게 해결하기 위해 **가능한 모든 Skill과 MCP 도구를 능동적으로 탐색·조합·사용**하라.
```

---

## 7️⃣ 실전 활용 예시

### 시나리오 1: 새 기능 구현

**사용자 요청:**
```
LoginPolicy 엔티티 및 Repository를 TDD로 구현해주세요.
```

**AI 응답 (이상적 형태):**

````markdown
## 📋 작업 분석

**요청 내용:**
- LoginPolicy 도메인 엔티티 구현
- LoginPolicyRepository 인터페이스 및 MyBatis Mapper 구현
- TDD 방식으로 테스트 먼저 작성

**필요한 작업:**
1. [RED] Domain 레이어 테스트 작성
2. [GREEN] LoginPolicy Entity 구현
3. [RED] Repository 레이어 테스트 작성
4. [GREEN] Repository 인터페이스 + MyBatis Mapper 구현
5. [REFACTOR] 코드 리팩토링

**사용할 Skills:**
- DDD Domain Modeling
- MyBatis Code Generation
- JUnit 5 Test Generation

---

## 🔴 RED: 테스트 먼저 작성

### 1. Domain Layer Test

```java
// File: backend/policy/src/test/java/com/inspecthub/policy/domain/LoginPolicyTest.java
package com.inspecthub.policy.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import com.inspecthub.common.exception.DomainException;

class LoginPolicyTest {
    
    @Test
    @DisplayName("모든 로그인 방식이 비활성화되면 예외가 발생한다")
    void shouldThrowExceptionWhenAllMethodsDisabled() {
        // Given
        LoginPolicy policy = LoginPolicy.builder()
            .policyId("01HN2Z3K4M5N6P7Q8R9S0T1U2V")
            .adEnabled(true)
            .ssoEnabled(false)
            .localEnabled(false)
            .build();
        
        // When & Then
        assertThatThrownBy(() -> 
            policy.update(false, false, false, "", "admin"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("최소 하나의 로그인 방식");
    }
    
    @Test
    @DisplayName("유효한 정책 업데이트 시 성공한다")
    void shouldSucceedWhenUpdatingWithValidData() {
        // Given
        LoginPolicy policy = LoginPolicy.builder()
            .policyId("01HN2Z3K4M5N6P7Q8R9S0T1U2V")
            .adEnabled(true)
            .ssoEnabled(false)
            .localEnabled(false)
            .build();
        
        // When
        policy.update(false, true, false, "SSO,AD,LOCAL", "admin");
        
        // Then
        assertThat(policy.getSsoEnabled()).isTrue();
        assertThat(policy.getAdEnabled()).isFalse();
        assertThat(policy.getPriority()).isEqualTo("SSO,AD,LOCAL");
    }
}
```

---

## 🟢 GREEN: 최소 구현

### 2. Domain Entity

```java
// File: backend/policy/src/main/java/com/inspecthub/policy/domain/LoginPolicy.java
package com.inspecthub.policy.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LoginPolicy {
    
    private String policyId;
    private Boolean adEnabled;
    private Boolean ssoEnabled;
    private Boolean localEnabled;
    private String priority;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    public void validateAtLeastOneEnabled() {
        if (!adEnabled && !ssoEnabled && !localEnabled) {
            throw new DomainException("최소 하나의 로그인 방식이 활성화되어야 합니다");
        }
    }
    
    public void update(Boolean adEnabled, Boolean ssoEnabled, Boolean localEnabled, 
                      String priority, String updatedBy) {
        this.adEnabled = adEnabled;
        this.ssoEnabled = ssoEnabled;
        this.localEnabled = localEnabled;
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
        
        validateAtLeastOneEnabled();
    }
}
```

---

## 🔵 REFACTOR: 개선

(테스트는 계속 통과 유지)

### 개선사항:
- ✅ Builder 패턴으로 불변성 강화
- ✅ 도메인 로직 분리 (validateAtLeastOneEnabled)
- ✅ 에러 메시지 명확화
````

---

### 시나리오 2: 문서 생성

**사용자 요청:**
```
LoginPolicy 기능의 API 문서를 Swagger로 작성해주세요.
```

**AI 응답 (이상적 형태):**

````markdown
## 📋 작업 분석

**요청 내용:**
- LoginPolicy REST API Swagger 문서화

**사용할 Skills:**
- Swagger/OpenAPI Documentation
- Spring Boot REST API 문서화

**사용할 MCP 도구:**
- Web: Springdoc OpenAPI 3.0 최신 규칙 조회

---

## 📚 Swagger 문서 (자동 생성)

### Controller with Swagger Annotations

```java
// File: backend/policy/src/main/java/com/inspecthub/policy/interfaces/LoginPolicyController.java
package com.inspecthub.policy.interfaces;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/login-policy")
@RequiredArgsConstructor
@Tag(name = "Login Policy", description = "로그인 정책 관리 API")
public class LoginPolicyController {
    
    private final LoginPolicyService loginPolicyService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "로그인 정책 조회",
        description = "현재 활성화된 로그인 정책을 조회합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginPolicyResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "인증 실패"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "권한 없음"
            )
        }
    )
    public ResponseEntity<ApiResponse<LoginPolicyResponse>> getLoginPolicy() {
        LoginPolicyResponse response = loginPolicyService.getLoginPolicy();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "로그인 정책 수정",
        description = "로그인 방식 활성화/비활성화 및 우선순위를 변경합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그인 정책 변경 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UpdateLoginPolicyRequest.class)
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "수정 성공"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 요청 (모든 방식 비활성화)"
            )
        }
    )
    public ResponseEntity<ApiResponse<Void>> updateLoginPolicy(
            @RequestBody @Valid UpdateLoginPolicyRequest request) {
        loginPolicyService.updateLoginPolicy(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

### DTO Schemas

```java
// File: backend/policy/src/main/java/com/inspecthub/policy/interfaces/dto/LoginPolicyResponse.java
package com.inspecthub.policy.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 정책 응답")
public class LoginPolicyResponse {
    
    @Schema(description = "정책 ID", example = "01HN2Z3K4M5N6P7Q8R9S0T1U2V")
    private String policyId;
    
    @Schema(description = "AD 로그인 활성화", example = "true")
    private Boolean adEnabled;
    
    @Schema(description = "SSO 로그인 활성화", example = "true")
    private Boolean ssoEnabled;
    
    @Schema(description = "일반 로그인 활성화", example = "false")
    private Boolean localEnabled;
    
    @Schema(description = "로그인 방식 우선순위", example = "SSO,AD,LOCAL")
    private String priority;
}
```

---

## 📖 생성된 Swagger UI 미리보기

**URL**: `http://localhost:8080/swagger-ui.html`

**엔드포인트 목록:**
- `GET /api/v1/login-policy` - 로그인 정책 조회
- `PUT /api/v1/login-policy` - 로그인 정책 수정

**Example Request (PUT):**
```json
{
  "adEnabled": true,
  "ssoEnabled": true,
  "localEnabled": false,
  "priority": "SSO,AD,LOCAL"
}
```

**Example Response (GET):**
```json
{
  "success": true,
  "data": {
    "policyId": "01HN2Z3K4M5N6P7Q8R9S0T1U2V",
    "adEnabled": true,
    "ssoEnabled": true,
    "localEnabled": false,
    "priority": "SSO,AD,LOCAL"
  }
}
```
````

---

## 8️⃣ Progressive Disclosure 적용 체크리스트

이 문서는 다음 Progressive Disclosure 원칙을 준수합니다:

### ✅ 문서 구조
- [x] **Level 1**: 개요 및 목차 제공
- [x] **Level 2**: 각 섹션 요약 및 핵심 내용
- [x] **Level 3**: 상세 예시 및 코드 템플릿
- [x] **Level 4**: 실전 활용 시나리오

### ✅ 탐색 가능성
- [x] 관련 문서 링크 제공 (Development Guide, TDD Workflow)
- [x] 섹션별 앵커 링크 (이모지 번호로 탐색)
- [x] Copy & Paste 가능한 템플릿 제공

### ✅ 점진적 복잡도
- [x] 간단한 역할 지정부터 시작
- [x] 핵심 지침 (Skills, MCP)
- [x] 출력 형식 상세 설명
- [x] 프로젝트별 커스터마이징
- [x] 실전 예시로 마무리

---

## 9️⃣ 추가 리소스

### 관련 문서
- **[Development Guide](./index.md)** - 전체 개발 가이드
- **[TDD + DDD Workflow](./TDD_DDD_WORKFLOW.md)** - 상세 워크플로우
- **[Backend Guide](./implementation/backend-guide.md)** - Backend 구현
- **[Exception Handling](./implementation/exception-handling.md)** - 예외 처리 가이드

### 외부 참고 자료
- [Claude Code Documentation](https://docs.anthropic.com/claude/docs)
- [Spring Boot 3.3.2 Reference](https://docs.spring.io/spring-boot/docs/3.3.2/reference/html/)
- [MyBatis 3.5.x Documentation](https://mybatis.org/mybatis-3/)

---

---

## 🔐 엔터프라이즈 권한/정책 시스템 베스트 프랙티스

> **Inspect-Hub 프로젝트의 User/Organization/Permission/Policy 구조 설계 시 준수해야 할 엔터프라이즈급 베스트 프랙티스**

### A. RBAC (Role-Based Access Control) 설계 원칙

#### 1. 역할 정의 원칙

**✅ DO (권장):**
```java
// ✅ Good: 실제 업무 기능 기반 역할
public enum Role {
    COMPLIANCE_OFFICER,      // 준법감시 담당자
    INVESTIGATOR,            // 조사자
    APPROVER_ORG,            // 조직 승인자
    APPROVER_COMPLIANCE,     // 준법 승인자
    SYSTEM_ADMIN,            // 시스템 관리자
    AUDITOR                  // 감사자 (읽기 전용)
}

// ✅ Good: 최소 권한 원칙 - 작고 집중된 권한
@RequirePermission(feature = "case", action = "investigate")
public void investigateCase(String caseId) { ... }

@RequirePermission(feature = "case", action = "approve")
public void approveCase(String caseId) { ... }
```

**❌ DON'T (비권장):**
```java
// ❌ Bad: 너무 포괄적이거나 모호한 역할
public enum Role {
    SUPER_USER,              // 너무 광범위
    GENERAL_STAFF,           // 책임 불명확
    POWER_USER               // 의미 모호
}

// ❌ Bad: 단일 omnipotent 역할
@RequirePermission(feature = "all", action = "all")
public void doEverything() { ... }
```

#### 2. 역할 폭발(Role Explosion) 방지

**문제**: 조직이 성장하면서 역할 수가 기하급수적으로 증가

**해결책**: Feature + Action 기반 세분화 권한 체계

```java
/**
 * Feature-Action 기반 권한 구조
 * 
 * 장점:
 * - 역할 수 증가 없이 권한 조합 가능
 * - 동적 권한 할당 지원
 * - 역할 간 명확한 경계
 */
@Entity
@Table(name = "permission")
public class Permission {
    
    @Id
    private String permissionId;
    
    @Column(nullable = false)
    private String feature;        // "case", "policy", "user", "report"
    
    @Column(nullable = false)
    private String action;         // "read", "write", "approve", "delete"
    
    /**
     * Permission 조합 예시:
     * - case:read         → 사례 조회
     * - case:investigate  → 사례 조사
     * - case:approve      → 사례 승인
     * - policy:write      → 정책 작성
     * - policy:approve    → 정책 승인
     * - report:submit     → 보고서 제출
     */
}

// PermissionGroup으로 역할 구현
@Entity
@Table(name = "permission_group")
public class PermissionGroup {
    
    @Id
    private String groupId;
    
    @Column(nullable = false)
    private String groupName;      // "INVESTIGATOR", "APPROVER_ORG"
    
    @ManyToMany
    @JoinTable(name = "permission_group_permission")
    private Set<Permission> permissions;
}
```

**통계 (2025 기준):**
- RBAC 채택률: 94.7% (조직)
- 현재 RBAC를 최우선 모델로 사용: 86.6%

#### 3. 역할 계층 구조 (Role Hierarchy)

```java
/**
 * 역할 계층 구조
 * 
 * 원칙:
 * - 상위 역할은 하위 역할의 모든 권한 상속
 * - 계층이 깊어질수록 관리 복잡도 증가 (3단계 이내 권장)
 */
public class RoleHierarchy {
    
    // Level 1: 최고 관리자
    SUPER_ADMIN
        ↓ (상속)
    // Level 2: 부서 관리자
    COMPLIANCE_OFFICER
        ↓ (상속)
    // Level 3: 실무자
    INVESTIGATOR
        ↓ (상속)
    // Level 4: 읽기 전용
    AUDITOR
}

// Spring Security 설정
@Bean
public RoleHierarchyImpl roleHierarchy() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy(
        "ROLE_SUPER_ADMIN > ROLE_COMPLIANCE_OFFICER
" +
        "ROLE_COMPLIANCE_OFFICER > ROLE_INVESTIGATOR
" +
        "ROLE_INVESTIGATOR > ROLE_AUDITOR"
    );
    return hierarchy;
}
```

---

### B. 조직 계층 및 권한 상속 (Organization Hierarchy)

#### 1. 계층 구조 미러링 원칙

**원칙**: 시스템의 리소스 계층은 조직 구조를 반영해야 함

```java
/**
 * 조직 계층 구조
 * 
 * Google Cloud 권장 사항:
 * - 리소스 계층은 조직 구조를 미러링
 * - 정책은 계층적으로 전파 (상위 → 하위)
 */
@Entity
@Table(name = "organization")
public class Organization {
    
    @Id
    private String orgId;
    
    @Column(nullable = false)
    private String orgName;
    
    @Column
    private String parentOrgId;        // 상위 조직
    
    @Column(nullable = false)
    private Integer level;             // 계층 레벨 (1: 본사, 2: 지점, 3: 팀)
    
    @Column
    private String orgPath;            // "/본사/서울지점/준법감시팀"
    
    /**
     * 조직 계층 예시:
     * 
     * 본사 (Level 1)
     *  ├─ 서울지점 (Level 2)
     *  │   ├─ 준법감시팀 (Level 3)
     *  │   └─ 영업팀 (Level 3)
     *  └─ 부산지점 (Level 2)
     *      └─ 준법감시팀 (Level 3)
     */
}
```

#### 2. 권한 상속 원칙

**핵심 원칙:**
1. **하향 전파**: 상위 조직 정책은 하위 조직으로 자동 전파
2. **명시적 우선**: 명시적 권한이 상속된 권한보다 우선
3. **Deny 우선**: Deny 권한이 Allow 권한보다 우선

```java
/**
 * 권한 상속 로직
 */
@Service
@RequiredArgsConstructor
public class PermissionInheritanceService {
    
    private final OrganizationRepository organizationRepository;
    private final PolicyRepository policyRepository;
    
    /**
     * 사용자의 유효 권한 계산 (상속 포함)
     * 
     * 우선순위:
     * 1. 명시적 Deny (최우선)
     * 2. 명시적 Allow
     * 3. 상속된 Deny
     * 4. 상속된 Allow
     * 5. 기본값 (Deny)
     */
    public Set<Permission> getEffectivePermissions(String userId) {
        User user = userRepository.findById(userId);
        Organization org = organizationRepository.findById(user.getOrgId());
        
        Set<Permission> effectivePermissions = new HashSet<>();
        
        // 1. 사용자 직접 권한
        Set<Permission> directPermissions = user.getPermissions();
        
        // 2. 조직 계층을 따라 상위 조직 권한 수집
        Set<Permission> inheritedPermissions = collectInheritedPermissions(org);
        
        // 3. 병합 (명시적 권한 우선)
        effectivePermissions.addAll(directPermissions);
        effectivePermissions.addAll(inheritedPermissions);
        
        // 4. Deny 권한 필터링
        Set<Permission> denyPermissions = collectDenyPermissions(user, org);
        effectivePermissions.removeAll(denyPermissions);
        
        return effectivePermissions;
    }
    
    /**
     * 상위 조직 권한 상속
     */
    private Set<Permission> collectInheritedPermissions(Organization org) {
        Set<Permission> inherited = new HashSet<>();
        
        // 현재 조직부터 최상위까지 순회
        Organization current = org;
        while (current != null) {
            DataPolicy policy = policyRepository.findByOrgId(current.getOrgId());
            if (policy != null) {
                inherited.addAll(policy.getAllowedPermissions());
            }
            
            // 상위 조직으로 이동
            current = (current.getParentOrgId() != null) 
                ? organizationRepository.findById(current.getParentOrgId()).orElse(null)
                : null;
        }
        
        return inherited;
    }
}
```

#### 3. 권한 충돌 처리

```java
/**
 * 권한 충돌 시나리오
 * 
 * 시나리오: 사용자가 여러 그룹에 속해 있고, 그룹 간 권한 충돌
 * 
 * 해결 원칙:
 * - Explicit > Inherited
 * - Deny > Allow
 * - Lower Level > Higher Level (더 구체적인 것 우선)
 */
public enum PermissionConflictResolution {
    
    /**
     * Example:
     * 
     * User: John
     * Group A: case:read (Allow)
     * Group B: case:read (Deny)
     * 
     * Result: Deny (Deny가 우선)
     */
    DENY_WINS_OVER_ALLOW,
    
    /**
     * Example:
     * 
     * Direct Permission: case:approve (Allow)
     * Inherited Permission: case:approve (Deny)
     * 
     * Result: Allow (Explicit가 우선)
     */
    EXPLICIT_WINS_OVER_INHERITED,
    
    /**
     * Example:
     * 
     * Organization Level 1 (본사): case:read (Deny)
     * Organization Level 2 (지점): case:read (Allow)
     * 
     * Result: Allow (Lower level이 우선)
     */
    LOWER_LEVEL_WINS
}
```

---

### C. ABAC (Attribute-Based Access Control) 패턴

#### 1. ABAC 아키텍처 구성 요소

```java
/**
 * ABAC 아키텍처
 * 
 * 구성 요소:
 * - PEP (Policy Enforcement Point): API/컨트롤러 레이어
 * - PDP (Policy Decision Point): 정책 평가 엔진
 * - PIP (Policy Information Point): 속성 데이터 소스
 */

// 1. PEP (Policy Enforcement Point) - AOP로 구현
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionEnforcementAspect {
    
    private final PolicyDecisionPoint pdp;
    
    @Around("@annotation(requirePermission)")
    public Object enforce(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) 
            throws Throwable {
        
        // 1. 요청 컨텍스트 추출
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String feature = requirePermission.feature();
        String action = requirePermission.action();
        Object resource = extractResource(joinPoint);
        
        // 2. PDP에 권한 결정 요청
        AccessRequest request = AccessRequest.builder()
            .subject(userId)
            .resource(resource)
            .action(action)
            .feature(feature)
            .build();
        
        AccessDecision decision = pdp.evaluate(request);
        
        // 3. 결정 집행
        if (decision.isPermit()) {
            return joinPoint.proceed();
        } else {
            throw new AccessDeniedException(
                String.format("Access denied: %s:%s", feature, action)
            );
        }
    }
}

// 2. PDP (Policy Decision Point) - 정책 평가 엔진
@Service
@RequiredArgsConstructor
public class PolicyDecisionPoint {
    
    private final PolicyInformationPoint pip;
    private final PolicyRepository policyRepository;
    
    /**
     * 접근 결정 평가
     * 
     * 평가 요소:
     * - Subject Attributes (사용자: 역할, 조직, 레벨)
     * - Resource Attributes (리소스: 타입, 소유자, 민감도)
     * - Action (작업: read, write, approve, delete)
     * - Environment (환경: 시간, IP, 위치)
     */
    public AccessDecision evaluate(AccessRequest request) {
        
        // 1. Subject 속성 조회
        UserAttributes subject = pip.getUserAttributes(request.getSubject());
        
        // 2. Resource 속성 조회
        ResourceAttributes resource = pip.getResourceAttributes(request.getResource());
        
        // 3. Environment 속성 조회
        EnvironmentAttributes environment = pip.getEnvironmentAttributes();
        
        // 4. 정책 평가
        List<Policy> policies = policyRepository.findApplicablePolicies(
            subject, resource, request.getAction()
        );
        
        // 5. 정책 규칙 평가 (SpEL 표현식)
        for (Policy policy : policies) {
            boolean matches = evaluateSpELExpression(
                policy.getCondition(),
                subject,
                resource,
                environment
            );
            
            if (matches) {
                return policy.getEffect() == Effect.ALLOW 
                    ? AccessDecision.permit() 
                    : AccessDecision.deny(policy.getReason());
            }
        }
        
        // 6. 기본값: Deny
        return AccessDecision.deny("No matching policy found");
    }
    
    /**
     * SpEL 표현식 평가
     * 
     * 예시 정책 조건:
     * - "#subject.role == 'INVESTIGATOR' && #resource.status == 'ASSIGNED'"
     * - "#subject.orgId == #resource.orgId"
     * - "#environment.time.hour >= 9 && #environment.time.hour <= 18"
     */
    private boolean evaluateSpELExpression(
        String expression,
        UserAttributes subject,
        ResourceAttributes resource,
        EnvironmentAttributes environment
    ) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        context.setVariable("subject", subject);
        context.setVariable("resource", resource);
        context.setVariable("environment", environment);
        
        return parser.parseExpression(expression).getValue(context, Boolean.class);
    }
}

// 3. PIP (Policy Information Point) - 속성 데이터 소스
@Service
@RequiredArgsConstructor
public class PolicyInformationPoint {
    
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    
    public UserAttributes getUserAttributes(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Organization org = organizationRepository.findById(user.getOrgId()).orElseThrow();
        
        return UserAttributes.builder()
            .userId(userId)
            .role(user.getRole())
            .orgId(user.getOrgId())
            .orgLevel(org.getLevel())
            .orgPath(org.getOrgPath())
            .build();
    }
    
    public ResourceAttributes getResourceAttributes(Object resource) {
        // 리소스 타입에 따라 속성 추출
        if (resource instanceof Case) {
            Case caseObj = (Case) resource;
            return ResourceAttributes.builder()
                .resourceType("case")
                .ownerId(caseObj.getCreatedBy())
                .orgId(caseObj.getOrgId())
                .status(caseObj.getStatus())
                .sensitivity(caseObj.getSensitivity())
                .build();
        }
        return ResourceAttributes.empty();
    }
    
    public EnvironmentAttributes getEnvironmentAttributes() {
        return EnvironmentAttributes.builder()
            .time(LocalDateTime.now())
            .ipAddress(getCurrentIpAddress())
            .build();
    }
}
```

#### 2. Row-Level Security (RLS) 구현

```java
/**
 * Row-Level Security (행 수준 보안)
 * 
 * 목적:
 * - 사용자 속성에 따라 데이터 행(Row) 접근 제한
 * - 동일 테이블에서 사용자마다 다른 데이터 보기
 */
@Service
@RequiredArgsConstructor
public class RowLevelSecurityService {
    
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    
    /**
     * RowScope 정책에 따른 데이터 필터링
     * 
     * RowScope:
     * - OWN: 본인이 생성한 데이터만
     * - ORG: 동일 조직 데이터만
     * - ALL: 모든 데이터
     * - CUSTOM: 사용자 정의 쿼리
     */
    public List<Case> listCases(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        DataPolicy policy = policyRepository.findByRoleAndFeature(
            user.getRole(), 
            "case"
        );
        
        switch (policy.getRowScope()) {
            case OWN:
                // 본인 생성 사례만
                return caseRepository.findByCreatedBy(userId);
            
            case ORG:
                // 동일 조직 사례만
                return caseRepository.findByOrgId(user.getOrgId());
            
            case ORG_HIERARCHY:
                // 본인 조직 + 하위 조직 사례
                List<String> orgIds = getOrgHierarchyIds(user.getOrgId());
                return caseRepository.findByOrgIdIn(orgIds);
            
            case ALL:
                // 모든 사례
                return caseRepository.findAll();
            
            case CUSTOM:
                // 사용자 정의 쿼리 (예: 특정 상태만)
                String customCondition = policy.getCustomCondition();
                return caseRepository.findByCustomCondition(customCondition);
            
            default:
                return Collections.emptyList();
        }
    }
    
    /**
     * 조직 계층 기반 RLS
     */
    private List<String> getOrgHierarchyIds(String orgId) {
        Organization org = organizationRepository.findById(orgId).orElseThrow();
        List<String> orgIds = new ArrayList<>();
        orgIds.add(orgId);
        
        // 하위 조직 ID 수집 (재귀)
        List<Organization> children = organizationRepository.findByParentOrgId(orgId);
        for (Organization child : children) {
            orgIds.addAll(getOrgHierarchyIds(child.getOrgId()));
        }
        
        return orgIds;
    }
}

/**
 * MyBatis Dynamic SQL로 RLS 구현
 */
@Mapper
public interface CaseMapper {
    
    /**
     * Dynamic SQL로 Row-Level 필터링
     */
    @Select("""
        <script>
        SELECT * FROM \"case\"
        WHERE deleted = FALSE
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
        ORDER BY created_at DESC
        </script>
    """)
    List<Case> findWithRowLevelSecurity(
        @Param("rowScope") String rowScope,
        @Param("userId") String userId,
        @Param("orgId") String orgId,
        @Param("orgIds") List<String> orgIds
    );
}
```

#### 3. Field-Level Masking (필드 수준 마스킹)

```java
/**
 * Field-Level Masking (필드 수준 마스킹)
 * 
 * 목적:
 * - 민감 정보(PII) 보호
 * - 역할/권한에 따라 필드별 마스킹
 */
@Service
@RequiredArgsConstructor
public class FieldMaskingService {
    
    private final PolicyRepository policyRepository;
    
    /**
     * 동적 필드 마스킹
     */
    public <T> T maskSensitiveFields(T entity, String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        DataPolicy policy = policyRepository.findByRole(user.getRole());
        
        // 리플렉션으로 @Sensitive 필드 찾기
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            Sensitive sensitive = field.getAnnotation(Sensitive.class);
            if (sensitive != null) {
                String fieldName = field.getName();
                
                // 정책에서 마스킹 여부 확인
                if (policy.shouldMask(fieldName)) {
                    field.setAccessible(true);
                    try {
                        Object originalValue = field.get(entity);
                        if (originalValue instanceof String) {
                            String maskedValue = mask(
                                (String) originalValue, 
                                sensitive.maskType()
                            );
                            field.set(entity, maskedValue);
                        }
                    } catch (IllegalAccessException e) {
                        log.error("Failed to mask field: {}", fieldName, e);
                    }
                }
            }
        }
        
        return entity;
    }
    
    /**
     * 마스킹 타입별 처리
     */
    private String mask(String value, MaskType type) {
        if (value == null) return null;
        
        switch (type) {
            case FULL:
                // "John Doe" → "********"
                return "*".repeat(value.length());
            
            case PARTIAL:
                // "123-45-6789" → "***-**-6789"
                if (value.length() <= 4) return "***";
                return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
            
            case EMAIL:
                // "john.doe@example.com" → "j***e@example.com"
                int atIndex = value.indexOf('@');
                if (atIndex <= 2) return value;
                return value.charAt(0) + "***" + value.charAt(atIndex - 1) + value.substring(atIndex);
            
            case PHONE:
                // "010-1234-5678" → "010-****-5678"
                String[] parts = value.split("-");
                if (parts.length != 3) return value;
                return parts[0] + "-****-" + parts[2];
            
            default:
                return value;
        }
    }
}

/**
 * @Sensitive 어노테이션
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Sensitive {
    MaskType maskType() default MaskType.PARTIAL;
}

/**
 * Entity에 적용
 */
@Entity
@Table(name = "customer")
public class Customer {
    
    @Id
    private String customerId;
    
    private String name;
    
    @Sensitive(maskType = MaskType.PARTIAL)
    private String ssn;                 // 주민등록번호
    
    @Sensitive(maskType = MaskType.EMAIL)
    private String email;
    
    @Sensitive(maskType = MaskType.PHONE)
    private String phoneNumber;
}
```

---

### D. Separation of Duties (SoD) 강화

```java
/**
 * Maker-Checker 패턴 강화
 * 
 * 원칙:
 * - 동일 사용자가 생성(Maker)과 승인(Checker)을 모두 수행 불가
 * - 이중 통제로 내부 부정 방지
 * - 감사 추적 필수
 */
@Service
@RequiredArgsConstructor
public class SeparationOfDutiesService {
    
    private final AuditLogRepository auditLogRepository;
    
    /**
     * SoD 위반 검증
     */
    public void validateSoD(String resourceId, String approverId, String action) {
        // 1. 리소스 생성자 확인
        AuditLog creationLog = auditLogRepository.findByResourceIdAndAction(
            resourceId, 
            "CREATE"
        );
        
        String makerId = creationLog.getUserId();
        
        // 2. Maker-Checker 검증
        if (makerId.equals(approverId)) {
            throw new SeparationOfDutiesViolationException(
                String.format(
                    "SoD Violation: User %s cannot both create and %s resource %s",
                    approverId, action, resourceId
                )
            );
        }
        
        // 3. 조직 정책 검증 (선택적)
        OrganizationPolicy policy = policyRepository.findByOrgId(
            getUserOrg(approverId)
        );
        
        if (policy.isRequireDualControl()) {
            // 이중 승인 필요
            long approvalCount = auditLogRepository.countByResourceIdAndAction(
                resourceId, 
                "APPROVE"
            );
            
            if (approvalCount < 1) {
                throw new DualControlRequiredException(
                    "This resource requires at least 2 approvals"
                );
            }
        }
    }
    
    /**
     * 승인 체인 검증
     */
    public void validateApprovalChain(String resourceId, String approverId) {
        List<AuditLog> approvalHistory = auditLogRepository.findApprovalHistory(resourceId);
        
        // 1. 동일 사용자가 여러 번 승인하지 못하도록
        boolean alreadyApproved = approvalHistory.stream()
            .anyMatch(log -> log.getUserId().equals(approverId));
        
        if (alreadyApproved) {
            throw new DuplicateApprovalException(
                "User has already approved this resource"
            );
        }
        
        // 2. 승인 순서 검증 (계층적 승인)
        User approver = userRepository.findById(approverId).orElseThrow();
        int approverLevel = getApprovalLevel(approver.getRole());
        
        for (AuditLog log : approvalHistory) {
            User previousApprover = userRepository.findById(log.getUserId()).orElseThrow();
            int previousLevel = getApprovalLevel(previousApprover.getRole());
            
            if (approverLevel <= previousLevel) {
                throw new ApprovalOrderViolationException(
                    "Approval must follow hierarchical order"
                );
            }
        }
    }
    
    private int getApprovalLevel(String role) {
        Map<String, Integer> levelMap = Map.of(
            "INVESTIGATOR", 1,
            "APPROVER_ORG", 2,
            "APPROVER_COMPLIANCE", 3,
            "COMPLIANCE_OFFICER", 4
        );
        return levelMap.getOrDefault(role, 0);
    }
}
```

---

### E. 성능 최적화 전략

#### 1. 권한 캐싱

```java
/**
 * 권한 계산 결과 캐싱
 * 
 * 문제:
 * - 복잡한 조직 계층 순회는 성능 저하
 * - 매 요청마다 권한 재계산 비효율적
 * 
 * 해결:
 * - Redis 캐싱 (TTL: 5분)
 * - 권한 변경 시 캐시 무효화
 */
@Service
@RequiredArgsConstructor
public class CachedPermissionService {
    
    private final RedisTemplate<String, Set<Permission>> redisTemplate;
    private final PermissionInheritanceService inheritanceService;
    
    private static final String PERMISSION_CACHE_PREFIX = "permission:user:";
    private static final long CACHE_TTL = 300; // 5 minutes
    
    @Cacheable(
        value = "userPermissions",
        key = "#userId",
        unless = "#result == null"
    )
    public Set<Permission> getEffectivePermissions(String userId) {
        String cacheKey = PERMISSION_CACHE_PREFIX + userId;
        
        // 1. 캐시 조회
        Set<Permission> cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 2. 계산
        Set<Permission> permissions = inheritanceService.getEffectivePermissions(userId);
        
        // 3. 캐시 저장
        redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_TTL, TimeUnit.SECONDS);
        
        return permissions;
    }
    
    /**
     * 캐시 무효화 (권한 변경 시)
     */
    @CacheEvict(value = "userPermissions", key = "#userId")
    public void invalidateUserPermissions(String userId) {
        String cacheKey = PERMISSION_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        
        log.info("Invalidated permission cache for user: {}", userId);
    }
    
    /**
     * 조직 단위 캐시 무효화
     */
    public void invalidateOrgPermissions(String orgId) {
        List<User> users = userRepository.findByOrgId(orgId);
        for (User user : users) {
            invalidateUserPermissions(user.getUserId());
        }
        
        log.info("Invalidated permission cache for organization: {}", orgId);
    }
}
```

#### 2. 조직 계층 경로 최적화

```java
/**
 * 조직 경로 사전 계산
 * 
 * 문제:
 * - 재귀 쿼리로 조직 계층 탐색은 O(n) 복잡도
 * 
 * 해결:
 * - orgPath 필드에 전체 경로 사전 저장
 * - 쿼리 한 번으로 상위 조직 확인
 */
@Entity
@Table(name = "organization")
public class Organization {
    
    @Id
    private String orgId;
    
    @Column
    private String parentOrgId;
    
    /**
     * 조직 경로 (Materialized Path Pattern)
     * 
     * 예시:
     * - "/본사"
     * - "/본사/서울지점"
     * - "/본사/서울지점/준법감시팀"
     * 
     * 장점:
     * - 상위 조직 확인: orgPath LIKE '/본사/서울지점%'
     * - 하위 조직 확인: orgPath LIKE '%/준법감시팀'
     */
    @Column(nullable = false)
    private String orgPath;
    
    @Column
    private Integer level;
}

// MyBatis 쿼리
@Mapper
public interface OrganizationMapper {
    
    /**
     * 조직 계층 조회 (단일 쿼리)
     */
    @Select("""
        SELECT * FROM organization
        WHERE org_path LIKE CONCAT(#{orgPath}, '%')
        ORDER BY level ASC
    """)
    List<Organization> findHierarchy(@Param("orgPath") String orgPath);
    
    /**
     * 상위 조직 조회
     */
    @Select("""
        SELECT * FROM organization
        WHERE #{orgPath} LIKE CONCAT(org_path, '%')
          AND org_id != #{orgId}
        ORDER BY level ASC
    """)
    List<Organization> findAncestors(
        @Param("orgId") String orgId,
        @Param("orgPath") String orgPath
    );
}
```

---

### F. 보안 및 감사 강화

#### 1. 권한 변경 감사

```java
/**
 * 권한 변경 100% 감사 로깅
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAuditAspect {
    
    private final AuditLogRepository auditLogRepository;
    
    @AfterReturning(
        pointcut = "@annotation(com.inspecthub.common.annotation.AuditPermissionChange)",
        returning = "result"
    )
    public void auditPermissionChange(JoinPoint joinPoint, Object result) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Object[] args = joinPoint.getArgs();
        
        AuditLog log = AuditLog.builder()
            .userId(userId)
            .action("PERMISSION_CHANGE")
            .resource("PERMISSION")
            .beforeValue(serializeArgs(args))
            .afterValue(serialize(result))
            .timestamp(LocalDateTime.now())
            .result("SUCCESS")
            .build();
        
        auditLogRepository.insert(log);
    }
}
```

#### 2. 비정상 패턴 탐지

```java
/**
 * 권한 상승 시도 탐지
 */
@Service
@RequiredArgsConstructor
public class PrivilegeEscalationDetector {
    
    private final AuditLogRepository auditLogRepository;
    private final AlertService alertService;
    
    @Scheduled(fixedDelay = 60000) // 1분마다
    public void detectSuspiciousActivity() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        
        // 1. 짧은 시간 내 반복 권한 체크 실패
        List<AuditLog> failedAttempts = auditLogRepository.findByResultAndTimestampAfter(
            "FAILURE", 
            threshold
        );
        
        Map<String, Long> failuresByUser = failedAttempts.stream()
            .collect(Collectors.groupingBy(AuditLog::getUserId, Collectors.counting()));
        
        failuresByUser.forEach((userId, count) -> {
            if (count >= 5) {
                alertService.send(
                    "Security Alert",
                    String.format(
                        "User %s has %d failed permission checks in 10 minutes",
                        userId, count
                    )
                );
            }
        });
        
        // 2. 비정상 시간대 접근
        int currentHour = LocalDateTime.now().getHour();
        if (currentHour < 6 || currentHour > 22) {
            List<AuditLog> offHoursAccess = auditLogRepository.findByTimestampHour(currentHour);
            if (!offHoursAccess.isEmpty()) {
                alertService.send(
                    "Security Alert",
                    String.format(
                        "%d access attempts during off-hours",
                        offHoursAccess.size()
                    )
                );
            }
        }
    }
}
```

---

### G. 테스트 전략

#### 1. 권한 시스템 테스트

```java
/**
 * 권한 시스템 통합 테스트
 */
@SpringBootTest
@Transactional
class PermissionSystemIntegrationTest {
    
    @Autowired
    private PermissionInheritanceService permissionService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Test
    @DisplayName("상위 조직 정책이 하위 조직으로 상속된다")
    void shouldInheritPermissionsFromParentOrg() {
        // Given: 조직 계층 생성
        Organization hq = createOrganization("본사", null, "/본사", 1);
        Organization branch = createOrganization("서울지점", hq.getOrgId(), "/본사/서울지점", 2);
        Organization team = createOrganization("준법감시팀", branch.getOrgId(), "/본사/서울지점/준법감시팀", 3);
        
        // 본사에 정책 할당
        DataPolicy hqPolicy = createPolicy(hq.getOrgId(), Set.of("case:read", "case:write"));
        
        // 사용자 생성 (준법감시팀 소속)
        User user = createUser("john", team.getOrgId());
        
        // When: 유효 권한 계산
        Set<Permission> effectivePermissions = permissionService.getEffectivePermissions(user.getUserId());
        
        // Then: 상위 조직 권한 상속 확인
        assertThat(effectivePermissions)
            .extracting(Permission::getCode)
            .contains("case:read", "case:write");
    }
    
    @Test
    @DisplayName("Deny 권한이 Allow 권한보다 우선한다")
    void shouldDenyWinOverAllow() {
        // Given
        User user = createUser("john", "org1");
        
        // 직접 권한: case:read (Allow)
        assignPermission(user, "case:read", Effect.ALLOW);
        
        // 그룹 권한: case:read (Deny)
        PermissionGroup group = createGroup("Restricted Group");
        assignGroupPermission(group, "case:read", Effect.DENY);
        addUserToGroup(user, group);
        
        // When
        Set<Permission> effectivePermissions = permissionService.getEffectivePermissions(user.getUserId());
        
        // Then: Deny가 우선
        assertThat(effectivePermissions)
            .extracting(Permission::getCode)
            .doesNotContain("case:read");
    }
    
    @Test
    @DisplayName("SoD 위반 시 예외가 발생한다")
    void shouldThrowExceptionOnSoDViolation() {
        // Given
        String caseId = createCase("john");
        
        // When & Then
        assertThatThrownBy(() -> 
            separationOfDutiesService.validateSoD(caseId, "john", "APPROVE"))
            .isInstanceOf(SeparationOfDutiesViolationException.class)
            .hasMessageContaining("cannot both create and APPROVE");
    }
}
```

---

### H. 마이그레이션 및 롤백 전략

```java
/**
 * 권한 시스템 마이그레이션
 * 
 * 시나리오: 레거시 Role 기반 → Feature-Action 기반 전환
 */
@Service
@RequiredArgsConstructor
public class PermissionMigrationService {
    
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    
    /**
     * 단계적 마이그레이션 (Strangler Fig Pattern)
     */
    public void migratePermissions() {
        // 1. 기존 Role을 PermissionGroup으로 변환
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            String legacyRole = user.getRole();
            
            // 2. Role에 해당하는 Permission 매핑
            Set<Permission> permissions = mapRoleToPermissions(legacyRole);
            
            // 3. PermissionGroup 생성 및 할당
            PermissionGroup group = PermissionGroup.builder()
                .groupId(UlidCreator.getUlid().toString())
                .groupName(legacyRole)
                .permissions(permissions)
                .build();
            
            permissionGroupRepository.insert(group);
            
            // 4. 사용자에게 그룹 할당
            assignGroupToUser(user.getUserId(), group.getGroupId());
            
            log.info("Migrated user {} from role {} to permission group", 
                user.getUserId(), legacyRole);
        }
    }
    
    /**
     * 롤백 전략
     */
    public void rollbackMigration() {
        // PermissionGroup을 다시 Role로 변환
        List<UserPermissionGroup> assignments = userPermissionGroupRepository.findAll();
        
        for (UserPermissionGroup assignment : assignments) {
            PermissionGroup group = permissionGroupRepository.findById(assignment.getGroupId());
            String roleName = group.getGroupName();
            
            User user = userRepository.findById(assignment.getUserId());
            user.setRole(roleName);
            userRepository.update(user);
            
            log.info("Rolled back user {} to role {}", user.getUserId(), roleName);
        }
    }
    
    private Set<Permission> mapRoleToPermissions(String role) {
        // 역할별 권한 매핑 로직
        Map<String, Set<String>> rolePermissionMap = Map.of(
            "INVESTIGATOR", Set.of("case:read", "case:investigate", "case:write"),
            "APPROVER_ORG", Set.of("case:read", "case:approve"),
            "COMPLIANCE_OFFICER", Set.of("case:*", "policy:*", "report:submit")
        );
        
        Set<String> permissionCodes = rolePermissionMap.getOrDefault(role, Set.of());
        return permissionRepository.findByCodes(permissionCodes);
    }
}
```

---

### I. 체크리스트: 권한 시스템 설계 검토

#### ✅ RBAC 설계

- [ ] 역할은 실제 업무 기능 기반으로 정의되었는가?
- [ ] 역할 계층은 3단계 이내인가?
- [ ] Feature-Action 기반으로 권한을 세분화했는가?
- [ ] 역할 폭발(Role Explosion)을 방지했는가?
- [ ] 최소 권한 원칙을 적용했는가?

#### ✅ 조직 계층 및 상속

- [ ] 조직 구조가 시스템 계층에 미러링되었는가?
- [ ] 권한 상속 규칙이 명확한가? (Explicit > Inherited, Deny > Allow)
- [ ] orgPath 사전 계산으로 쿼리 성능 최적화했는가?
- [ ] 권한 충돌 시나리오를 정의하고 해결 규칙을 명시했는가?

#### ✅ ABAC 구현

- [ ] PEP, PDP, PIP 아키텍처가 명확히 분리되었는가?
- [ ] SpEL 표현식으로 동적 정책 평가가 가능한가?
- [ ] Subject, Resource, Action, Environment 속성을 모두 고려했는가?

#### ✅ Row-Level Security

- [ ] RowScope 정책(OWN, ORG, ALL, CUSTOM)을 정의했는가?
- [ ] Dynamic SQL로 행 수준 필터링을 구현했는가?
- [ ] 조직 계층 기반 RLS를 지원하는가?

#### ✅ Field-Level Masking

- [ ] @Sensitive 어노테이션으로 민감 필드를 표시했는가?
- [ ] MaskType별 마스킹 로직을 구현했는가? (FULL, PARTIAL, EMAIL, PHONE)
- [ ] 역할별로 마스킹 정책을 다르게 적용할 수 있는가?

#### ✅ SoD (Separation of Duties)

- [ ] Maker-Checker 원칙을 적용했는가?
- [ ] 동일 사용자가 생성+승인을 모두 할 수 없도록 검증하는가?
- [ ] 승인 체인(계층적 승인)을 검증하는가?
- [ ] 이중 승인 정책을 지원하는가?

#### ✅ 성능 최적화

- [ ] 권한 계산 결과를 캐싱하는가? (Redis, TTL: 5분)
- [ ] 권한 변경 시 캐시를 무효화하는가?
- [ ] orgPath로 조직 계층 쿼리를 최적화했는가?

#### ✅ 보안 및 감사

- [ ] 모든 권한 변경을 감사 로그에 기록하는가?
- [ ] 비정상 패턴(반복 실패, 권한 상승 시도)을 탐지하는가?
- [ ] 오프-아워 접근을 모니터링하는가?

#### ✅ 테스트 전략

- [ ] 권한 상속 테스트를 작성했는가?
- [ ] Deny > Allow 우선순위 테스트를 작성했는가?
- [ ] SoD 위반 테스트를 작성했는가?
- [ ] Row-Level Security 테스트를 작성했는가?
- [ ] Field-Level Masking 테스트를 작성했는가?

#### ✅ 마이그레이션

- [ ] 레거시 시스템에서 마이그레이션 계획이 있는가?
- [ ] Strangler Fig Pattern으로 단계적 전환하는가?
- [ ] 롤백 전략이 준비되어 있는가?

---

## 🎯 Inspect-Hub 프로젝트: User/Organization/Permission 구현 지시문

> **AI 에이전트가 Inspect-Hub 프로젝트의 User/Organization/Permission/Policy 기능을 구현할 때 반드시 따라야 할 실행 규칙**
>
> **중요**: 이 섹션의 모든 규칙은 **MUST (필수)** 준수사항입니다.

---

### 📐 아키텍처 설계 원칙

#### 1. 엔티티 구조 (MUST)

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

### 🔧 구현 규칙

#### 규칙 1: Feature-Action 권한 체계 필수 사용

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

#### 규칙 2: @RequirePermission 어노테이션 사용

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

#### 규칙 3: 조직 계층 구현 시 orgPath 사용 필수

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

#### 규칙 4: RowScope 정책 적용 필수

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

#### 규칙 5: Field Masking 적용 필수

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

#### 규칙 6: Separation of Duties (SoD) 검증 필수

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

#### 규칙 7: 권한 캐싱 필수

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

#### 규칙 8: 감사 로그 100% 기록 필수

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

### 🧪 테스트 작성 규칙

#### 규칙 9: 권한 시스템 테스트 필수 작성

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

### 📋 구현 체크리스트

**AI 에이전트는 User/Organization/Permission 기능을 구현할 때 다음을 확인하십시오:**

#### ✅ 엔티티 설계
- [ ] User 엔티티에 userId (ULID), employeeId, orgId, status 필드 포함
- [ ] Organization 엔티티에 orgPath, level, parentOrgId 필드 포함
- [ ] Permission 엔티티를 Feature-Action 기반으로 설계
- [ ] PermissionGroup 엔티티로 역할(Role) 대체
- [ ] DataPolicy 엔티티에 rowScope, fieldMaskJson 필드 포함
- [ ] ❌ User 엔티티에 role 문자열 필드 사용 금지

#### ✅ 권한 검증
- [ ] @RequirePermission(feature, action) 어노테이션 정의
- [ ] AOP로 권한 검증 로직 구현
- [ ] 모든 비즈니스 메서드에 @RequirePermission 적용
- [ ] 권한 없을 시 AccessDeniedException 발생

#### ✅ 조직 계층
- [ ] orgPath 필드로 Materialized Path Pattern 구현
- [ ] 단일 쿼리로 하위/상위 조직 조회
- [ ] ❌ 재귀 쿼리 사용 금지

#### ✅ Row-Level Security
- [ ] RowScope 정책 정의 (OWN/ORG/ORG_HIERARCHY/ALL)
- [ ] 모든 목록 조회 API에 RowScope 필터링 적용
- [ ] MyBatis Dynamic SQL로 조건부 WHERE 절 구현

#### ✅ Field-Level Masking
- [ ] @Sensitive 어노테이션 정의
- [ ] MaskType별 마스킹 로직 구현 (FULL/PARTIAL/EMAIL/PHONE)
- [ ] 모든 응답 DTO에 마스킹 적용

#### ✅ Separation of Duties
- [ ] Maker-Checker 검증 로직 구현
- [ ] 승인 체인 검증 로직 구현
- [ ] 중복 승인 방지

#### ✅ 성능 최적화
- [ ] Redis로 권한 계산 결과 캐싱 (TTL: 5분)
- [ ] 권한 변경 시 캐시 무효화
- [ ] @Cacheable 어노테이션 사용

#### ✅ 감사 로깅
- [ ] 모든 권한 체크에 감사 로그 기록
- [ ] 권한 변경 시 감사 로그 기록
- [ ] 성공/실패 모두 로그에 기록

#### ✅ 테스트
- [ ] 권한 검증 테스트 작성
- [ ] RowScope 필터링 테스트 작성
- [ ] Field Masking 테스트 작성
- [ ] SoD 검증 테스트 작성
- [ ] 조직 계층 RLS 테스트 작성

---

### 🚨 금지 사항 (MUST NOT)

**절대로 다음과 같이 구현하지 마십시오:**

#### ❌ 금지 1: Role 문자열 직접 저장
```java
// ❌ FORBIDDEN
@Column(name = "role")
private String role;  // "ADMIN", "USER"
```

#### ❌ 금지 2: hasRole() 직접 사용
```java
// ❌ FORBIDDEN
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() { ... }
```

#### ❌ 금지 3: 재귀 쿼리로 조직 계층 탐색
```java
// ❌ FORBIDDEN: O(n) 성능 저하
public List<Organization> getChildren(String orgId) {
    // 재귀 로직...
}
```

#### ❌ 금지 4: RowScope 정책 미적용
```java
// ❌ FORBIDDEN: 모든 데이터 노출
public List<Case> listCases() {
    return caseRepository.findAll();  // 보안 위험!
}
```

#### ❌ 금지 5: Field Masking 미적용
```java
// ❌ FORBIDDEN: 민감 정보 노출
public CustomerResponse getCustomer(String customerId) {
    return customerRepository.findById(customerId);  // 마스킹 없음!
}
```

#### ❌ 금지 6: SoD 검증 생략
```java
// ❌ FORBIDDEN: 내부 부정 위험
public void approveCase(String caseId, String approverId) {
    // SoD 검증 없이 바로 승인 - 위험!
    caseRepository.approve(caseId, approverId);
}
```

#### ❌ 금지 7: 권한 캐싱 생략
```java
// ❌ FORBIDDEN: 성능 저하
public Set<Permission> getPermissions(String userId) {
    // 매번 복잡한 계산 - 비효율적!
    return calculatePermissionsWithHierarchy(userId);
}
```

#### ❌ 금지 8: 감사 로그 생략
```java
// ❌ FORBIDDEN: 추적 불가
public void changePermission(String userId, String permission) {
    // 로그 없이 권한 변경 - 감사 불가!
    userRepository.updatePermission(userId, permission);
}
```

---

### 📝 코드 생성 템플릿

**AI 에이전트는 다음 템플릿을 사용하여 코드를 생성하십시오:**

#### 템플릿 1: Service 메서드 구현

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

#### 템플릿 2: MyBatis Mapper (RowScope 적용)

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

### 🎓 요약: AI 에이전트 실행 지침

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

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-16 | Inspect-Hub 프로젝트 User/Organization/Permission 구현 지시문 추가 (엔티티 구조, 9가지 구현 규칙, 테스트 전략, 8가지 금지 사항, 코드 생성 템플릿) | PM |
| 2025-01-16 | 엔터프라이즈 권한/정책 시스템 베스트 프랙티스 섹션 추가 (RBAC, ABAC, 조직 계층, RLS, Field Masking, SoD, 성능 최적화, 보안/감사, 테스트, 마이그레이션) | PM |
| 2025-01-16 | 초안 작성 - Skills + MCP 최대 활용 지침 | PM |

---

**문서 작성**: 2025-01-16  
**최종 업데이트**: 2025-01-16  
**작성자**: Product Manager (John)
