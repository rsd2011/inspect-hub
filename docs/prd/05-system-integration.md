# 🔗 시스템 연계 (System Integration)

## 연계 대상

- 코어뱅킹/카드/채널 거래
- 고객/계좌/상품 마스터
- HR(직무/조직)
- 인증/SSO(IdP)
- 대외 제재/WLF 소스

## 교환 방식(예시)

### 실시간

- **REST/GraphQL**
- **Kafka(MQ)** 이벤트

### 배치

- SFTP/파일 교환(CSV/Parquet)
- DB 링크/CDC

### 대외 보고

- 포맷(XML/CSV) + SFTP/전용 포털 연계(기관 규격 준수)

## 인증/보안 토큰

- OIDC/OAuth2
- mTLS
- 사내망 접근통제
