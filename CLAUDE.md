# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Inspect-Hub** is a comprehensive AML (Anti-Money Laundering) integrated compliance monitoring system for financial institutions in Korea. The system detects suspicious transactions (STR), currency transaction reports (CTR), watch-list filtering (WLF), and manages compliance workflows with regulatory compliance for Korean financial law and FATF guidelines.

**Current Status:** Planning/POC Phase - comprehensive PRD exists in `PRD.md` (Korean language, 486 lines), but no code implementation yet.

## Tech Stack

**Backend:**
- Java 21 + Spring Boot 3.3.2
- Spring Security for authentication/authorization
- MyBatis for database access
- Gradle multi-module build system
- PostgreSQL (primary DB) + Redis (caching) + Kafka (messaging)
- Spring Batch for batch processing

**Frontend:**
- Nuxt 3 (Vue 3 Composition API) - **SPA mode only** (`ssr: false`)
- Vite build system
- UI: PrimeVue + RealGrid (commercial grid) + Tailwind CSS (with prefix)
- State: Pinia
- Validation: VeeValidate + Zod
- Charts: Apache ECharts
- i18n: @nuxtjs/i18n

**Project Structure:**
```
inspect-hub/
├── backend/          # Spring Boot multi-module
│   ├── common/       # Shared entities, DTOs, utilities
│   └── server/       # Main application, controllers, services
└── frontend/         # Nuxt 3 application
```

## Core Architecture Principles

### ~~1. Multi-Tenancy (Highest Priority)~~

~~**Complete isolation per financial institution (tenant):**~~
- ~~Tenant-specific policies, rules, detection criteria, thresholds, and snapshots~~
- ~~Per-tenant admin controls, configurations, and reporting~~
- ~~Network/data complete separation (VPC/Subnet or Schema-based)~~

~~**All tables must include tenant isolation:**~~
- ~~Add `tenant_id` column to every table~~
- ~~Enforce tenant filtering in all queries via MyBatis interceptor or row-level security~~
- ~~Cache keys must include tenant prefix~~
- ~~File storage must use tenant-specific paths~~

### 2. Snapshot-Based Versioning

**All policy/standard data uses snapshot structure:**
- Similar to Spring Batch's `BATCH_JOB_INSTANCE` / `BATCH_JOB_EXECUTION` pattern
- Core table: `STANDARD_SNAPSHOT` with fields:
  - `id`, `type` (KYC/STR/CTR/RBA/WLF/FIU)
  - `version`, `effective_from`, `effective_to`
  - `status`, `created_by`, `approved_by`
  - `prev_id`, `next_id` (version chain)

**Version control requirements:**
- Immutable once approved/deployed
- Support rollback to previous snapshot versions
- What-if analysis using draft snapshots
- Audit trail of all version changes

### 3. Audit Requirements (100% Coverage)

**Every action must be logged:**
- User access logs (login, page views, data access)
- Data modification logs (before/after state)
- Configuration changes (policy updates, rule deployments)
- Approval workflows (who approved/rejected what, when)

**Implementation:**
- `AUDIT_LOG` table with: `who`, `when`, `what`, `before_json`, `after_json`
- Use AOP interceptors for automatic audit logging
- Never delete audit logs (retention policy: regulatory requirement, typically 5-10 years)

### 4. Separation of Duties (SoD)

**Maker-Checker principle:**
- Same user cannot both create and approve (STR/CTR cases, policy changes, etc.)
- Enforce at application level via Role-Based Access Control (RBAC)
- Approval lines configured by organization + permission group

### 5. Detection Engine Architecture

**Core data flow:**
```
Data Ingestion → Detection (STR/CTR/WLF) → Event Generation → Case Creation →
Investigation → Approval Workflow → Reporting → FIU Submission
```

**Detection tables:**
- `TX_STAGING` - Incoming transaction data
- `DETECTION_EVENT` - Rule matches (linked to `snapshot_ver`)
- `ALERT_CASE` - Investigation cases
- `CASE_ACTIVITY` - Case actions/comments
- `REPORT_STR` / `REPORT_CTR` - Final reports

**Batch processing structure (Spring Batch pattern):**
- `INSPECTION_INSTANCE` - Job instance (type, snapshot_ver, created_ts)
- `INSPECTION_EXECUTION` - Job execution (status, start/end time, read/write/skip counts)

## Key Technical Requirements

### Performance Targets

- **Batch processing:** ≥10 million transactions/day (overnight window)
- **Real-time API:** 300-500 TPS peak load
- **UI response:** <1 second (queries), <3 seconds (complex searches)
- **Availability:** 99.9% SLA

### Security Requirements

- **Encryption:** TLS in transit, field-level encryption for PII at rest
- **Authentication:** Spring Security + JWT/OAuth2
- **Authorization:** RBAC + SoD enforcement
- **Data masking:** Configurable by role (via `DATA_POLICY` table)
- **Access control:** Menu permissions managed in `MENU` table with `PERM_GROUP_FEATURE` mapping

### Database Design Patterns

**User & Permission Structure:**
```
USER → belongs to → ORGANIZATION → references → ORGANIZATION_POLICY
USER → belongs to → PERMISSION_GROUP → has → MENU + FEATURE_ACTION permissions
```

**Approval Line:**
- Template-based by business type
- Each step references `PERMISSION_GROUP` (not individual users)
- Example: `STEP1=PG.REVIEWER`, `STEP2=PG.APPROVER`, `STEP3=PG.HEAD_COMPLIANCE`
- Self-approval prevention (same user cannot be requester and approver)

**Data Policy (Row-Level + Field-Level):**
```sql
DATA_POLICY(
  policy_id, feature_code, action_code, perm_group_code, org_policy_id,
  business_type, row_scope, default_mask_rule, priority, active
)
```
- `row_scope`: OWN | ORG | ALL | CUSTOM
- `default_mask_rule`: NONE | PARTIAL | FULL | HASH

## Korean Terminology Reference

Key terms from PRD (Korean ↔ English):
- **자금세탁방지** = Anti-Money Laundering (AML)
- **의심거래보고** = Suspicious Transaction Report (STR)
- **고액현금거래보고** = Currency Transaction Report (CTR)
- **감시대상명단** = Watch List Filtering (WLF)
- **준법감시** = Compliance Monitoring
- **금융정보분석원** = Financial Intelligence Unit (FIU)
- **스냅샷** = Snapshot (versioning system)
- **결재선** = Approval Line
- **권한그룹** = Permission Group

## Development Commands

**Once the project is initialized, standard commands will be:**

### Backend (Gradle)
```bash
# Build all modules
./gradlew clean build

# Run tests
./gradlew test

# Run specific test
./gradlew test --tests com.example.ClassName.methodName

# Start local server
./gradlew bootRun

# Check dependencies
./gradlew dependencies
```

### Frontend (Nuxt)
```bash
# Install dependencies
npm install

# Development server
npm run dev

# Build for production (SPA mode)
npm run build

# Preview production build
npm run preview

# Run tests
npm run test

# Lint
npm run lint
```

## Important Implementation Notes

1. **No SSR:** Frontend must use `ssr: false` in Nuxt config - static resource deployment only
2. **Tailwind Prefix:** Use prefix to avoid conflicts with PrimeVue/RealGrid CSS
3. **Field Naming:** Use snake_case for database columns, camelCase for Java/TypeScript
4. **ID Strategy:** Use ULID or UUID for global identifiers, avoid sequential integers for security
5. **MyBatis TypeHandlers:** Create custom handlers for LocalDateTime, JSON columns, encrypted fields
6. **Cache Strategy:** ~~Redis with tenant-prefixed keys,~~ Redis cache with Caffeine for local L1 cache
7. **API Versioning:** Use `/api/v1/` prefix, prepare for future versions
8. **Error Handling:** Unified exception handler with i18n error codes
9. **Logging:** Structured JSON logs with trace IDs for distributed tracing
10. **Testing:** Minimum 80% code coverage for business logic

## MVP Scope (Priority)

**Must-Have Features:**
1. ~~Multi-tenancy management (tenant isolation, admin controls)~~
2. Policy & criteria management (snapshot-based, KYC/STR/CTR/WLF rules)
3. Detection engine (STR/CTR/WLF with configurable rules)
4. Investigation & workflow (case management, approval workflows)
5. Reporting module (FIU-compliant report generation)
6. User & access control (RBAC, SoD enforcement, approval lines)
7. Audit logging (100% coverage)
8. Basic dashboard (detection metrics, case status)

**Future Enhancements:**
- Risk simulation module (what-if analysis, backtesting)
- Advanced WLF matching algorithms (fuzzy matching, ensemble scoring)
- SMTP email system for product risk surveys
- ML-based rule optimization
- Regulatory change notifications

## Reference

- **PRD:** `/home/rsd/workspace/inspect-hub/PRD.md` (Korean, 486 lines)
- **Timeline:** MVP Go-Live target: January 20, 2026
