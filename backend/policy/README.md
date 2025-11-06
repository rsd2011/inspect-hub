# Policy Management Module

정책 및 기준 관리 모듈 - 스냅샷 기반 버전 관리

## 주요 기능

### 1. KYC 위험평가 기준 관리 (`kyc`)
- 고객/국가/상품/행동 팩터(Factor) 관리
- 평가항목(Criterion) 및 세부 평가기준 관리
- 가중치 및 등급 구간 관리

### 2. WLF 매칭 정책 관리 (`wlf`)
- 매칭 알고리즘 및 임계치 관리
- 개인(Individual) / 법인(Organization) 정책 분리
- 앙상블 알고리즘 가중치 관리
- 카테고리 관리 (PEP, Sanction 등)

### 3. STR 기준 관리 (`str`)
- 룰 시나리오/팩터 관리
- 가중치/임계치/종합 위험 기준

### 4. CTR 기준 관리 (`ctr`)
- 금액 및 빈도 기준 관리

### 5. 상품위험 설문 (`product`)
- SMTP 이메일 설문 발송
- 설문 응답 수집 및 위험 팩터 자동 산출

### 6. 스냅샷 버전 관리 (`snapshot`)
- 불변 스냅샷 생성/배포
- 버전 체인 관리 (prev_id, next_id)
- 롤백 지원

## 패키지 구조

```
policy/
├── kyc/              # KYC 위험평가 기준
├── wlf/              # WLF 매칭 정책
├── str/              # STR 기준
├── ctr/              # CTR 기준
├── product/          # 상품위험 설문
├── snapshot/         # 스냅샷 버전 관리
└── config/           # 모듈 설정
```

## 의존성

- `backend:common` - 공통 도메인 엔티티 및 유틸리티
