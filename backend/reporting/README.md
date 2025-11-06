# Reporting Module

보고서 생성 및 FIU 제출 모듈

## 주요 기능

### 1. STR 보고서 (`str`)
- 의심거래보고서 생성
- FIU 템플릿 기반 생성

### 2. CTR 보고서 (`ctr`)
- 고액현금거래보고서 생성

### 3. FIU 제출 (`fiu`)
- 금융정보분석원 전송
- 수신확인 트래킹
- 재전송 관리

### 4. 템플릿 (`template`)
- 보고서 템플릿 관리
- PDF 생성

## 패키지 구조

```
reporting/
├── str/              # STR 보고서
├── ctr/              # CTR 보고서
├── fiu/              # FIU 제출
├── template/         # 템플릿 관리
└── config/           # 모듈 설정
```

## 의존성

- `backend:common` - 공통 기능
- `backend:investigation` - 케이스 정보 참조
- `org.apache.pdfbox:pdfbox` - PDF 생성
