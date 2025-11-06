# Detection Module

탐지 엔진 모듈 - STR/CTR/WLF 실시간 탐지

## 주요 기능

### 1. STR 탐지 엔진 (`str`)
- 의심거래 패턴 탐지
- 룰 기반 엔진
- 실시간/배치 탐지 지원

### 2. CTR 탐지 엔진 (`ctr`)
- 고액현금거래 탐지
- 금액/빈도 기준 검증

### 3. WLF 탐지 엔진 (`wlf`)
- 요주의인물 매칭
- 복수 알고리즘 앙상블
- 유사도 계산 및 임계치 비교

### 4. 공통 엔진 (`engine`)
- 룰 엔진 인터페이스
- 탐지 이벤트 생성
- 스냅샷 기반 실행

## 패키지 구조

```
detection/
├── str/              # STR 탐지
├── ctr/              # CTR 탐지
├── wlf/              # WLF 탐지
├── engine/           # 공통 탐지 엔진
└── config/           # 모듈 설정 (Redis 캐시 등)
```

## 의존성

- `backend:common` - 공통 기능
- `backend:policy` - 정책 및 기준 참조
