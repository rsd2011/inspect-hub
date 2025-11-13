# 🧑‍💻 사용자 및 권한 (Users & Roles)

## 역할별 접근 기능

| 역할 | 접근 기능(요약) |
| --- | --- |
| 조직 요청자(Reporter-Org) | 탐지 이벤트 확인, 케이스 생성/기초 입력 |
| 조직 승인자(Approver-Org) | STR/CTR 품질/위험 검토, 승인/반려 |
| 조사자/애널리스트(Analyst) | 조사·증빙 첨부, STR 초안 작성 |
| 준법 담당자(Manager-Compliance) | 조사/승인/반려, 예외 승인, 기준 리뷰 |
| 준법 승인자(Approver-Compliance) | 최종 내부 승인 |
| 준법 책임자(Compliance Officer) | 보고 기준 확정, 대외 보고 최종 승인/송신 |
| 시스템 관리자(System Admin) | 사용자/역할, 룰 배포, 스케줄, 연계/배치 설정 |
| 감사/내부통제(Auditor) | 조회 전용, 로그/이력 점검(쓰기 금지) |
| 읽기전용(Reader) | 대시보드·조회(다운로드 제한 가능) |

## SoD(분리의 원칙)

동일 케이스에서 Analyst ⇄ Approver 겸직 금지. 결재선/위임 규칙 별도 설정.
