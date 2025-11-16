# 🌐 API 개발

## API 구조

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

## Controller 패턴

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

## DTO 패턴

### Request DTO

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

### Response DTO

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

## ApiResponse 래퍼

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

## Swagger/OpenAPI 문서화

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
