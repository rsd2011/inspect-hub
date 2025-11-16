# 📖 API 문서화

## Swagger/OpenAPI 사용

**자동 문서화:**
- Springdoc OpenAPI 사용
- 모든 API 자동 문서화
- Swagger UI 제공

**접속:**
```
http://localhost:8090/swagger-ui.html
```

## Annotation 예시

```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "User Management API")
public class UserController {
    
    @PostMapping
    @Operation(
        summary = "Create User",
        description = "Create a new user account",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
        @Valid @RequestBody 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User creation request",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateUserRequest.class))
        )
        CreateUserRequest request
    ) {
        // ...
    }
}
```

## DTO Schema 문서화

```java
@Schema(description = "Create User Request")
public class CreateUserRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    @Schema(
        description = "Username (3-50 characters)",
        example = "admin",
        required = true
    )
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email
    @Schema(
        description = "Email address",
        example = "admin@example.com",
        required = true
    )
    private String email;
    
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$")
    @Schema(
        description = "Password (min 8 chars, must contain letters and numbers)",
        example = "SecurePassword123!",
        required = true,
        format = "password"
    )
    private String password;
}
```

---
