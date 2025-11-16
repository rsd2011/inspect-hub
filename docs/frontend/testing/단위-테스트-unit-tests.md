# 단위 테스트 (Unit Tests)

## Utility Functions 테스트

```typescript
// shared/lib/utils/formatters.test.ts
import { describe, it, expect } from 'vitest'
import { formatCurrency, formatDate, formatPhoneNumber } from './formatters'

describe('formatters', () => {
  describe('formatCurrency', () => {
    it('숫자를 한국 원화 형식으로 변환', () => {
      expect(formatCurrency(1000000)).toBe('1,000,000')
      expect(formatCurrency(0)).toBe('0')
      expect(formatCurrency(-5000)).toBe('-5,000')
    })
    
    it('소수점 처리', () => {
      expect(formatCurrency(1234.56)).toBe('1,235')  // 반올림
      expect(formatCurrency(1234.56, true)).toBe('1,234.56')  // 소수점 유지
    })
    
    it('null/undefined 처리', () => {
      expect(formatCurrency(null)).toBe('0')
      expect(formatCurrency(undefined)).toBe('0')
    })
  })
  
  describe('formatDate', () => {
    it('Date 객체를 YYYY-MM-DD 형식으로 변환', () => {
      const date = new Date('2025-01-13T10:30:00')
      expect(formatDate(date)).toBe('2025-01-13')
    })
    
    it('커스텀 형식 지원', () => {
      const date = new Date('2025-01-13T10:30:00')
      expect(formatDate(date, 'YYYY년 MM월 DD일')).toBe('2025년 01월 13일')
    })
    
    it('ISO 문자열 처리', () => {
      expect(formatDate('2025-01-13T10:30:00Z')).toBe('2025-01-13')
    })
  })
  
  describe('formatPhoneNumber', () => {
    it('한국 전화번호 형식 변환', () => {
      expect(formatPhoneNumber('01012345678')).toBe('010-1234-5678')
      expect(formatPhoneNumber('0212345678')).toBe('02-1234-5678')
    })
    
    it('이미 형식화된 번호 유지', () => {
      expect(formatPhoneNumber('010-1234-5678')).toBe('010-1234-5678')
    })
    
    it('잘못된 입력 처리', () => {
      expect(formatPhoneNumber('123')).toBe('123')
      expect(formatPhoneNumber('')).toBe('')
    })
  })
})
```

## Validation Functions 테스트

```typescript
// shared/lib/utils/validators.test.ts
import { describe, it, expect } from 'vitest'
import { validateEmail, validatePassword, validateSSN } from './validators'

describe('validators', () => {
  describe('validateEmail', () => {
    it('유효한 이메일 검증', () => {
      expect(validateEmail('test@example.com')).toBe(true)
      expect(validateEmail('user.name+tag@example.co.kr')).toBe(true)
    })
    
    it('잘못된 이메일 거부', () => {
      expect(validateEmail('invalid')).toBe(false)
      expect(validateEmail('test@')).toBe(false)
      expect(validateEmail('@example.com')).toBe(false)
      expect(validateEmail('')).toBe(false)
    })
  })
  
  describe('validatePassword', () => {
    it('비밀번호 강도 검증 (8자 이상, 대소문자, 숫자, 특수문자)', () => {
      expect(validatePassword('Password123!')).toBe(true)
      expect(validatePassword('Abcd1234!@#$')).toBe(true)
    })
    
    it('약한 비밀번호 거부', () => {
      expect(validatePassword('short')).toBe(false)  // 너무 짧음
      expect(validatePassword('password123')).toBe(false)  // 대문자 없음
      expect(validatePassword('PASSWORD123')).toBe(false)  // 소문자 없음
      expect(validatePassword('Password')).toBe(false)  // 숫자 없음
      expect(validatePassword('Password123')).toBe(false)  // 특수문자 없음
    })
  })
  
  describe('validateSSN', () => {
    it('주민등록번호 형식 검증 (마스킹된 형식)', () => {
      expect(validateSSN('123456-1******')).toBe(true)
      expect(validateSSN('000101-3******')).toBe(true)
    })
    
    it('잘못된 형식 거부', () => {
      expect(validateSSN('123456-1234567')).toBe(false)  // 전체 노출
      expect(validateSSN('12345-1******')).toBe(false)  // 앞자리 부족
      expect(validateSSN('1234567-1******')).toBe(false)  // 앞자리 초과
    })
  })
})
```

---
