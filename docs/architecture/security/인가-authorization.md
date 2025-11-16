# 🛡️ 인가 (Authorization)

## RBAC (Role-Based Access Control)

**Role 계층 구조:**

```
ROLE_SUPER_ADMIN (최고 관리자)
    ↓
ROLE_ADMIN (관리자)
    ↓
ROLE_COMPLIANCE_OFFICER (준법감시 담당자)
    ↓
ROLE_INVESTIGATOR (조사자)
    ↓
ROLE_REVIEWER (검토자)
    ↓
ROLE_USER (일반 사용자)
```

**Permission 구조:**

```
{resource}:{action}

Examples:
- user:read      # 사용자 조회
- user:write     # 사용자 생성/수정
- user:delete    # 사용자 삭제
- policy:read
- policy:write
- policy:approve # 정책 승인
- case:read
- case:write
- case:approve   # 사례 승인
- report:read
- report:submit  # FIU 보고서 제출
```

## 권한 체크 방법

### 1. Method Security (권장)

```java
@Service
public class PolicyService {
    
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('policy:write')")
    public Policy createPolicy(CreatePolicyRequest request) {
        // ...
    }
    
    @PreAuthorize("hasAuthority('policy:approve')")
    public void approvePolicy(String policyId) {
        // ...
    }
    
    @PreAuthorize("@permissionEvaluator.canAccessPolicy(#policyId)")
    public Policy getPolicy(String policyId) {
        // Custom permission evaluator
    }
}
```

### 2. Controller Security

```java
@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {
    
    @PostMapping
    @PreAuthorize("hasAuthority('policy:write')")
    public ResponseEntity<?> createPolicy(@RequestBody CreatePolicyRequest request) {
        // ...
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('policy:approve')")
    public ResponseEntity<?> approvePolicy(@PathVariable String id) {
        // ...
    }
}
```

### 3. Programmatic Check

```java
@Service
@RequiredArgsConstructor
public class CaseService {
    
    private final PermissionManager permissionManager;
    
    public void assignCase(String caseId, String userId) {
        // Check permission programmatically
        if (!permissionManager.hasPermission("case:assign")) {
            throw new AccessDeniedException("Insufficient permissions");
        }
        
        // Business logic
    }
}
```

## Separation of Duties (SoD)

**Maker-Checker 원칙:**

```java
@Service
@RequiredArgsConstructor
public class ApprovalService {
    
    public void approveCase(String caseId, String approverId) {
        Case case = caseRepository.findById(caseId)
            .orElseThrow(() -> new CaseNotFoundException(caseId));
        
        // SoD: 생성자와 승인자가 같으면 안됨
        if (case.getCreatedBy().equals(approverId)) {
            throw new SeparationOfDutiesViolationException(
                "Maker cannot be the same as Checker"
            );
        }
        
        // Approval logic
        case.setStatus("APPROVED");
        case.setApprovedBy(approverId);
        case.setApprovedAt(LocalDateTime.now());
        
        caseRepository.update(case);
        auditLogger.log("CASE_APPROVED", caseId, approverId);
    }
}
```

## Data-Level Permission (행/필드 수준 권한)

**Row-Level Security:**

```java
@Service
public class CaseQueryService {
    
    public List<Case> listCases(String userId) {
        User user = userRepository.findById(userId);
        DataPolicy policy = dataPolicyRepository.findByUserRole(user.getRole());
        
        // Scope 기반 필터링
        switch (policy.getRowScope()) {
            case "OWN":
                // 본인이 생성한 사례만
                return caseRepository.findByCreatedBy(userId);
            
            case "ORG":
                // 동일 조직의 사례만
                return caseRepository.findByOrganization(user.getOrgId());
            
            case "ALL":
                // 전체 사례
                return caseRepository.findAll();
            
            default:
                return Collections.emptyList();
        }
    }
}
```

**Field-Level Masking:**

```java
@Service
public class DataMaskingService {
    
    public User maskSensitiveFields(User user, String viewerId) {
        DataPolicy policy = dataPolicyRepository.findByUser(viewerId);
        
        // PII 마스킹
        if (policy.shouldMask("ssn")) {
            user.setSsn(mask(user.getSsn(), MaskType.PARTIAL)); // "123-45-6789" → "***-**-6789"
        }
        
        if (policy.shouldMask("email")) {
            user.setEmail(mask(user.getEmail(), MaskType.EMAIL)); // "admin@example.com" → "a***n@example.com"
        }
        
        return user;
    }
    
    private String mask(String value, MaskType type) {
        // Masking logic
    }
}
```

---
