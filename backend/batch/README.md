# Batch Module

Spring Batch 기반 배치 처리 모듈

## 주요 기능

### 1. 배치 Job (`job`)
- STR/CTR/WLF 점검 Job
- Spring Batch Job 정의
- Job Instance/Execution 패턴

### 2. 스케줄러 (`scheduler`)
- Cron 기반 스케줄링
- Job 트리거 관리

### 3. 실행 이력 (`execution`)
- Batch 실행 이력 관리
- 성능 지표 (read/write/skip counts)
- 실패 재처리

## 패키지 구조

```
batch/
├── job/              # Batch Job 정의
├── scheduler/        # 스케줄러
├── execution/        # 실행 이력
└── config/           # Batch 설정
```

## 의존성

- `backend:common` - 공통 기능
- `backend:detection` - 탐지 로직 사용
- `backend:investigation` - 케이스 생성
- `spring-boot-starter-batch` - Spring Batch
