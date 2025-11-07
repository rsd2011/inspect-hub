# Backend Development Agents & Tools

This document describes the automated agents and tools available for the Spring Boot backend.

## 🤖 Available Agents

### 1. API Generator

**Purpose:** Automatically generates complete API components following Spring Boot + MyBatis patterns.

**Command:**
```bash
python scripts/generate-api.py --module=<module> --entity=<Entity> --api=<api-name>
```

**Parameters:**
- `--module` (required): Module name (e.g., `auth`, `policy`, `detection`)
- `--entity` (required): Entity name in PascalCase (e.g., `User`, `Policy`, `Case`)
- `--api` (required): API operation in kebab-case (e.g., `create-user`, `list-policies`)

**Examples:**

```bash
# Generate user creation API
python scripts/generate-api.py --module=auth --entity=User --api=create-user

# Generate policy listing API
python scripts/generate-api.py --module=policy --entity=Policy --api=list-policies

# Generate case update API
python scripts/generate-api.py --module=investigation --entity=Case --api=update-case-status
```

**Generated Files:**

```
auth/src/main/java/com/inspecthub/auth/
├── controller/
│   └── UserController.java       # REST Controller with @RestController
├── service/
│   └── UserService.java          # Business logic with @Service
├── dto/
│   ├── CreateUserRequest.java   # Request DTO with validation
│   └── CreateUserResponse.java  # Response DTO
└── mapper/
    └── UserMapper.java           # MyBatis Mapper interface

auth/src/main/resources/mybatis/mapper/
└── UserMapper.xml                # MyBatis XML mapper

auth/src/test/java/com/inspecthub/auth/service/
└── UserServiceTest.java          # JUnit 5 + Mockito test
```

**Generated Component Features:**

**Controller:**
- ✅ REST endpoint with proper HTTP method
- ✅ Request/Response DTOs
- ✅ @Valid validation
- ✅ ApiResponse wrapper
- ✅ Swagger/OpenAPI annotations
- ✅ Logging

**Service:**
- ✅ @Service annotation
- ✅ @Transactional support
- ✅ Mapper injection
- ✅ Business logic placeholder
- ✅ Logging

**DTOs:**
- ✅ Lombok annotations (@Data, @Builder)
- ✅ Jakarta Validation constraints
- ✅ Swagger schema documentation
- ✅ Example values

**MyBatis Mapper:**
- ✅ @Mapper interface
- ✅ XML mapper template with SQL examples
- ✅ Common CRUD operations template
- ✅ ResultMap examples

**Test:**
- ✅ JUnit 5 + Mockito setup
- ✅ @ExtendWith(MockitoExtension.class)
- ✅ Mock dependencies
- ✅ Test method template

**Swagger/OpenAPI Documentation:**

⚠️  **IMPORTANT: All generated APIs are automatically documented with Swagger**

Generated components include:
- `@Tag` annotation on Controller for API grouping
- `@Operation` annotation on each endpoint with summary and description
- `@Schema` annotation on DTOs with field descriptions and examples
- Request/Response examples for testing

**Access Swagger UI:**
```
http://localhost:8090/swagger-ui.html
```

**Example Generated Swagger Annotations:**

```java
// Controller
@Tag(name = "User", description = "User API")
public class UserController {

    @Operation(summary = "Create User", description = "Create user operation")
    public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) { ... }
}

// DTO
@Schema(description = "Create User Request")
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Name", example = "John Doe")
    private String name;
}
```

**Swagger UI Features:**
- 🔍 Browse all API endpoints
- 📝 View request/response schemas
- ⚡ Test APIs directly (Try it out)
- 📋 View validation constraints
- 📦 Download OpenAPI spec (JSON/YAML)

**Configuration:**

Ensure `springdoc-openapi` is in dependencies (already included):
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.x.x'
```

### 2. Module Dependency Validator

**Purpose:** Validates module dependency architecture and detects violations.

**Command:**
```bash
python scripts/validate-modules.py
```

**Validation Rules:**

1. **Architecture Layers**
   - 🔵 Infrastructure: `common` (no dependencies on domain modules)
   - 🟢 Domain: `auth`, `policy`, `detection`, `investigation`, `reporting`, `batch`, `simulation`, `admin`
   - 🟡 Application: `server` (aggregates all domain modules)

2. **Dependency Rules**
   - ❌ Domain modules CANNOT depend on `server`
   - ❌ `common` CANNOT depend on domain modules
   - ✅ Domain modules CAN depend on `common`
   - ✅ Domain modules CAN depend on other domain modules
   - ✅ `server` SHOULD depend on all domain modules

3. **Circular Dependencies**
   - ❌ No circular dependencies allowed
   - Detects cycles across the entire dependency graph

4. **Package Structure**
   - ⚠️  Domain modules should have: `controller`, `service`, `dto`, `mapper`
   - ⚠️  MyBatis XML mappers should exist in `resources/mybatis/mapper`

**Example Output:**

```bash
$ python scripts/validate-modules.py

🔍 Module Dependency Validator

============================================================
ℹ️  Discovering modules...
✅ Found module: auth
✅ Found module: policy
✅ Found module: detection
... (10 modules total)

ℹ️  Parsing module dependencies...
  auth → common
  policy → common
  detection → common, policy
  server → admin, auth, batch, common, detection, investigation, policy, reporting, simulation

ℹ️  Validating dependency rules...
✅ All dependency rules passed

ℹ️  Checking for circular dependencies...
✅ No circular dependencies detected

ℹ️  Validating package structure...
✅ Package structure looks good

ℹ️  Validating resources...
✅ auth has 2 MyBatis mapper(s)

ℹ️  Generating dependency graph...
✅ Dependency graph saved to: module-dependencies.dot
ℹ️  Generate PNG: dot -Tpng module-dependencies.dot -o module-dependencies.png

============================================================
✅ Validation PASSED - all modules are properly configured!
```

**Dependency Graph:**

The validator generates a Graphviz DOT file showing module dependencies:

```bash
# Generate PNG visualization
dot -Tpng module-dependencies.dot -o module-dependencies.png

# View with xdot (if installed)
xdot module-dependencies.dot
```

**Graph Legend:**
- 🔵 Blue nodes: Infrastructure modules (`common`)
- 🟢 Green nodes: Domain modules
- 🟡 Yellow nodes: Application modules (`server`)

## 📋 Workflow Integration

### Development Workflow

1. **Create new API endpoint:**
   ```bash
   python scripts/generate-api.py --module=auth --entity=User --api=login
   ```

2. **Implement business logic:**
   - Edit `UserService.java`
   - Add SQL queries in `UserMapper.xml`
   - Add validation to `LoginRequest.java`

3. **Write tests:**
   - Implement test cases in `UserServiceTest.java`
   - Run tests: `./gradlew :auth:test`

4. **Validate module dependencies:**
   ```bash
   python scripts/validate-modules.py
   ```

5. **Run all tests:**
   ```bash
   ./gradlew test
   ```

### Pre-commit Validation

Add to `.git/hooks/pre-commit`:

```bash
#!/bin/bash
cd backend
python scripts/validate-modules.py
if [ $? -ne 0 ]; then
  echo "Module validation failed. Commit aborted."
  exit 1
fi
```

### CI/CD Pipeline

Add to `.github/workflows/backend-validate.yml`:

```yaml
name: Backend Validation

on: [push, pull_request]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v4
        with:
          python-version: '3.x'
      - name: Validate modules
        run: |
          cd backend
          python scripts/validate-modules.py
```

## 🔧 Extending Agents

### Customizing API Templates

Edit `scripts/generate-api.py`:

```python
def generate_controller(module, entity, api_name):
    # Customize controller template
    return f"""package com.inspecthub.{module}.controller;

// Your custom template
"""
```

### Adding Validation Rules

Edit `scripts/validate-modules.py`:

```python
def validate_custom_rule(self):
    """Add custom validation logic"""
    for module in self.modules:
        # Your validation logic
        if violation:
            error(f"Custom rule violated in {module}")
```

### Generating Other Components

Create new generator scripts:

```python
# scripts/generate-entity.py
# Generate JPA entities or domain models

# scripts/generate-config.py
# Generate Spring configuration classes
```

## 📚 Best Practices

### 1. API Generation

**DO:**
- Use descriptive entity names (`User`, `PolicySnapshot`, `CaseActivity`)
- Use verb-noun format for APIs (`create-user`, `update-status`, `list-policies`)
- Generate APIs before writing custom code
- Review generated code and customize as needed
- **Add meaningful descriptions to @Schema annotations**
- **Provide example values in Swagger annotations**
- **Test APIs using Swagger UI before writing E2E tests**

**DON'T:**
- Manually create controller/service/mapper files
- Skip validation annotations in DTOs
- Forget to implement SQL queries in XML mappers
- **Remove or skip Swagger annotations (required for API documentation)**
- **Leave @Schema descriptions empty or generic**

### 2. Module Dependencies

**DO:**
- Keep domain modules independent
- Use `common` for shared utilities
- Let `server` module aggregate everything
- Run validator before committing

**DON'T:**
- Create circular dependencies
- Make domain modules depend on `server`
- Put domain logic in `common`
- Skip dependency validation

### 3. Testing

**DO:**
- Write tests immediately after generation
- Use mock dependencies in unit tests
- Test business logic, not frameworks
- Aim for 80%+ code coverage

**DON'T:**
- Skip tests
- Test private methods
- Use real databases in unit tests
- Ignore test failures

### 4. MyBatis Mappers

**DO:**
- Use parameterized queries (#\{param\})
- Define resultMaps for complex objects
- Use `<if>` tags for dynamic SQL
- Keep SQL readable and maintainable

**DON'T:**
- Use string concatenation for SQL
- Put business logic in SQL
- Create N+1 query problems
- Forget to add XML mappers after generating interfaces

## 🐛 Troubleshooting

### API Generator Issues

**Problem:** "Module 'xyz' does not exist"

**Solution:**
```bash
# Verify module exists
ls -la backend/ | grep xyz

# Ensure build.gradle exists
ls -la backend/xyz/build.gradle
```

**Problem:** Generated files have wrong package

**Solution:** Check that module directory structure matches `com/inspecthub/{module}`

### Module Validator Issues

**Problem:** "Circular dependency detected"

**Solution:**
1. Check dependency graph: `module-dependencies.dot`
2. Identify cycle in the graph
3. Remove unnecessary dependencies from `build.gradle`
4. Re-run validator

**Problem:** "Domain module depends on server"

**Solution:**
1. Find the violating dependency in `build.gradle`
2. Refactor code to remove the dependency
3. Consider moving shared code to `common`

**Problem:** False positive warnings

**Solution:**
- Review warning messages carefully
- Some warnings are informational (e.g., missing MyBatis mappers in modules that don't use DB)
- Customize validator rules in `validate-modules.py` if needed

### Swagger/OpenAPI Issues

**Problem:** Swagger UI not accessible at http://localhost:8090/swagger-ui.html

**Solution:**
1. Verify server is running: `./gradlew :server:bootRun`
2. Check springdoc-openapi dependency in `server/build.gradle`:
   ```gradle
   implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.x.x'
   ```
3. Try alternative URLs:
   - http://localhost:8090/swagger-ui/index.html
   - http://localhost:8090/v3/api-docs (raw JSON spec)
4. Check application logs for errors

**Problem:** Generated API not appearing in Swagger UI

**Solution:**
1. Verify Controller has `@RestController` and `@RequestMapping` annotations
2. Check that Controller is in the correct package: `com.inspecthub.{module}.controller`
3. Ensure server module includes the domain module in dependencies
4. Restart the application: `./gradlew :server:bootRun`

**Problem:** Swagger annotations showing default/empty descriptions

**Solution:**
1. Edit generated DTO files and add meaningful descriptions:
   ```java
   @Schema(description = "User's email address", example = "user@example.com")
   private String email;
   ```
2. Update `@Operation` annotations in Controller:
   ```java
   @Operation(
       summary = "Create new user",
       description = "Creates a new user account with the provided information"
   )
   ```

**Problem:** Request/Response examples not showing in Swagger

**Solution:**
- Add `example` attribute to `@Schema` annotations in DTOs
- Use `@ExampleObject` for complex examples
- Ensure DTOs have proper Lombok annotations (@Data, @AllArgsConstructor)

## 📖 Related Documentation

- [Root CLAUDE.md](../CLAUDE.md) - Project architecture guidelines
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/)

## 🎯 Quick Reference

```bash
# Generate new API (with Swagger annotations)
python scripts/generate-api.py \
  --module=auth \
  --entity=User \
  --api=create-user

# Start backend server
./gradlew :server:bootRun

# Access Swagger UI (API documentation & testing)
# Open in browser: http://localhost:8090/swagger-ui.html
# OpenAPI spec: http://localhost:8090/v3/api-docs

# Validate module dependencies
python scripts/validate-modules.py

# Run module tests
./gradlew :auth:test

# Run all tests
./gradlew test

# Build all modules
./gradlew clean build

# Generate dependency graph visualization
dot -Tpng module-dependencies.dot -o module-dependencies.png
```

## 🚀 Future Enhancements

Potential agents to add:

1. **Entity Generator** - Generate domain entities
2. **Migration Generator** - Generate database migration scripts
3. **Integration Test Generator** - Generate API integration tests
4. **Performance Profiler** - Analyze API performance
5. **Security Auditor** - Check for security vulnerabilities
6. **API Documentation Generator** - Generate OpenAPI/Swagger docs

## 📖 Related Documentation

- [API Contract](../API-CONTRACT.md) ⭐ **Frontend ↔ Backend API 계약 및 동기화 계획 (필독!)**
- [Root CLAUDE.md](../CLAUDE.md) - Project architecture guidelines
- [Frontend Agents](../frontend/AGENTS.md) - Frontend automation tools
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
