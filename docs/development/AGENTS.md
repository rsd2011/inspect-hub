# Repository Guidelines

## Project Structure & Module Organization
- `backend/common` holds shared DTOs, exception types, and utilities; wire new shared contracts here so they can be reused across modules.
- `backend/server` is the Spring Boot service. Place controllers, services, and mappers under `src/main/java/com/inspecthub/aml/server/**`; keep SQL mappers under `src/main/resources/mybatis`.
- Environment defaults live in `backend/server/src/main/resources/application*.yml`; use profile-specific files (`application-dev.yml`, `application-prod.yml`) for overrides instead of in-code switches.

## Build, Test, and Development Commands
```bash
./gradlew clean build                 # compile all modules and run unit tests
./gradlew :backend:server:bootRun     # launch the API locally (H2 in-memory DB)
./gradlew :backend:server:bootRun --args='--spring.profiles.active=dev'  # run against the dev profile
./gradlew test                        # execute the full JUnit test suite
```

## Coding Style & Naming Conventions
- Target Java 21 with the default 4-space indentation; follow package paths like `com.inspecthub.aml.server.<layer>`.
- Prefer immutable DTOs with Lombok builders, and centralize mapping logic through MapStruct mappers in `mapper` packages.
- API endpoints should remain under `/api/v1/**`; include the `X-Tenant-ID` header in controller method signatures or interceptors handling tenant resolution.
- Database artifacts (SQL, MyBatis XML) should use `snake_case` for identifiers and live alongside the mapper that consumes them.

## Testing Guidelines
- Use JUnit 5 (provided by `spring-boot-starter-test`) and place specs under `src/test/java` mirroring the production package layout.
- Write integration tests with Spring's `@SpringBootTest` when touching security, tenant interceptors, or MyBatis mappings; keep unit tests fast by mocking external infrastructure.
- Add regression coverage for tenant-aware logic and audit requirements; new features should include at least one positive and one negative path test.

## Commit & Pull Request Guidelines
- The workspace has no accessible Git history; follow Conventional Commits (`feat:`, `fix:`, `chore:`) so future history stays searchable and release-ready.
- Keep commits scoped to a single concern and include relevant Gradle or configuration updates.
- Pull requests should describe intent, list affected modules (`backend/common`, `backend/server`), link any tracking issue, and include screenshots or sample API calls when UI or API behavior changes.

## Security & Configuration Tips
- Treat secrets as environment variables; never commit tenant credentials or JWT keys. Supply profile-specific overrides via `.env` or CI secrets.
- When running locally, ensure Redis, PostgreSQL, and Kafka-dependent components are toggled off or mocked unless the profile explicitly enables them.
- Multi-tenant behavior depends on the `X-Tenant-ID` header; verify new endpoints enforce it before requesting reviews.
