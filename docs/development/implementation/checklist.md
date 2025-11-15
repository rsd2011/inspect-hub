## 📝 구현 체크리스트

### Backend

- [ ] ConfigurationService 인터페이스 및 구현 클래스 작성
- [ ] SystemConfig Entity 및 Repository 작성
- [ ] ConfigChangeHistory Entity 및 Repository 작성
- [ ] SystemConfigController REST API 작성
- [ ] ConfigurationService 단위 테스트 작성 (우선순위 로직 검증)
- [ ] SystemConfigController 통합 테스트 작성
- [ ] Flyway 마이그레이션 스크립트 작성 (V1, V2, V3)
- [ ] application.properties 기본 설정값 추가
- [ ] Redis 캐시 설정 (TTL 5분)
- [ ] 설정 변경 감사 로그 자동 기록 AOP
- [ ] 설정 검증 로직 작성 (범위, 타입, 형식)
- [ ] AD Connection Test API 작성
- [ ] SSO Authorization Test API 작성

### Frontend

- [ ] System Settings 페이지 라우팅 설정 (/admin/system-settings)
- [ ] SystemConfigStore Pinia 스토어 작성
- [ ] SystemSettingsPage 컴포넌트 작성 (TabView)
- [ ] LoginSettingsPanel 컴포넌트 작성
- [ ] PasswordSettingsPanel 컴포넌트 작성
- [ ] SessionSettingsPanel 컴포넌트 작성
- [ ] IpSettingsPanel 컴포넌트 작성
- [ ] AccountSettingsPanel 컴포넌트 작성
- [ ] AdvancedSettingsPanel 컴포넌트 작성
- [ ] SettingToggle Molecule 컴포넌트 작성
- [ ] SettingInput Molecule 컴포넌트 작성 (타입별)
- [ ] SettingSection Organism 컴포넌트 작성
- [ ] 변경 이력 모달 컴포넌트 작성
- [ ] AD/SSO 설정 모달 컴포넌트 작성
- [ ] 테스트 결과 표시 Toast/Modal
- [ ] isDirty 상태 기반 페이지 이탈 경고
- [ ] 일괄 저장/리셋 기능 구현
- [ ] Export/Import JSON 기능 구현 (Optional)

### Documentation

- [ ] System Configuration API 문서 작성 (Swagger)
- [ ] UI 사용 가이드 작성
- [ ] 설정 키 레퍼런스 문서 작성
- [ ] 마이그레이션 가이드 작성 (SecurityPolicy → SYSTEM_CONFIG)
- [ ] 운영 가이드 작성 (Properties vs DB 선택 기준)

---

