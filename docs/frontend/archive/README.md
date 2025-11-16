# Archive - Deprecated Documentation

> **아카이브 문서 - 더 이상 사용되지 않음**
>
> **Archived Date**: 2025-01-16
> **Reason**: Nuxt 4 공식 베스트 프랙티스 구조 채택으로 FSD (Feature-Sliced Design) 아키텍처 폐기

---

## 📁 아카이브된 문서

### 1. ARCHITECTURE.md
- **내용**: FSD + Atomic Design + Nuxt 4 아키텍처 가이드
- **폐기 이유**: Nuxt 4 공식 구조로 전환하면서 FSD 복잡도 제거
- **작성일**: 2025-01-15

### 2. NUXT4_FSD_COMPATIBILITY.md
- **내용**: Nuxt 4와 FSD 호환성 가이드 (nuxt.config.ts 설정)
- **폐기 이유**: FSD 사용 중단으로 호환성 가이드 불필요
- **작성일**: 2025-01-16

---

## 🔄 아키텍처 변경 이력

### Before (FSD 구조)
```
frontend/
├── app/         # 애플리케이션 초기화
├── pages/       # 페이지 라우트
├── widgets/     # 독립적인 UI 블록
├── features/    # 사용자 시나리오 (비즈니스 기능)
├── entities/    # 비즈니스 엔티티
└── shared/      # 공통 리소스
    ├── ui/      # Atomic Design (atoms, molecules, organisms)
    └── lib/     # 공통 라이브러리
```

**장점:**
- Feature별 코드 격리
- 명확한 의존성 방향
- 대규모 프로젝트 확장성

**단점:**
- 러닝 커브 높음
- Nuxt 4 공식 구조와 충돌
- 초기 설정 복잡 (nuxt.config.ts 커스터마이징 필요)

---

### After (Nuxt 4 공식 구조)
```
frontend/
└── app/
    ├── components/    # 모든 컴포넌트
    ├── composables/   # 모든 Composables
    ├── pages/         # 페이지 라우트
    ├── layouts/       # 레이아웃
    ├── middleware/    # 미들웨어
    ├── plugins/       # 플러그인
    ├── utils/         # 유틸리티
    └── assets/        # 정적 리소스
```

**장점:**
- Nuxt 4 공식 베스트 프랙티스
- Auto-import 기본 지원
- 러닝 커브 낮음
- 설정 간단

**단점:**
- Feature별 격리 부족 (폴더 정리로 보완)
- 대규모 프로젝트 시 관리 어려움

---

## 📚 참고 문서

**최신 구조 가이드:**
- [NUXT4_STRUCTURE.md](../NUXT4_STRUCTURE.md) - Nuxt 4 공식 구조 가이드
- [README.md](../README.md) - Frontend 전체 가이드

**마이그레이션:**
- [REFACTORING_PLAN.md](../REFACTORING_PLAN.md) - FSD → Nuxt 4 리팩터링 계획

---

## 🗂️ 아카이브 정책

**보존 기간:** 6개월 (2025-07-16까지)

**보존 이유:**
- 아키텍처 결정 이력 추적
- 필요 시 참고 자료로 활용
- 향후 FSD 재검토 시 참고

**삭제 예정일:** 2025-07-16 (필요 시 연장 가능)

---

## 📞 문의

**아키텍처 변경 관련 문의:**
- GitHub Issues: #architecture-change
- Slack: #inspect-hub-frontend
