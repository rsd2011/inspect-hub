# Simulation Module

리스크 분석 및 What-if 시뮬레이션 모듈

## 주요 기능

### 1. KYC 시뮬레이션 (`kyc`)
- 팩터/가중치 변경 시 위험점수 변동 분석

### 2. STR 시뮬레이션 (`str`)
- 룰 임계치 조정 영향 분석
- 탐지량/정탐률 예측

### 3. CTR 시뮬레이션 (`ctr`)
- 금액 기준 변경 시 탐지건수 분석

### 4. WLF 시뮬레이션 (`wlf`)
- 알고리즘/가중치 조정 시 탐지율/오탐율 비교

### 5. 버전 비교 (`comparison`)
- 스냅샷 버전 간 결과 비교 (Diff)
- 성과 지표 산출
- 시각화 데이터 생성

## 패키지 구조

```
simulation/
├── kyc/              # KYC 시뮬레이션
├── str/              # STR 시뮬레이션
├── ctr/              # CTR 시뮬레이션
├── wlf/              # WLF 시뮬레이션
├── comparison/       # 버전 비교
└── config/           # 모듈 설정
```

## 의존성

- `backend:common` - 공통 기능
- `backend:policy` - 정책 버전 참조
- `backend:detection` - 탐지 엔진 재사용
