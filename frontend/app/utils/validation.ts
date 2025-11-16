/**
 * Validation Utilities
 */

export const isEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

export const isPhoneNumber = (phone: string): boolean => {
  const phoneRegex = /^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$/
  return phoneRegex.test(phone)
}

export const isEmployeeId = (employeeId: string): boolean => {
  return employeeId.length >= 3 && /^[a-zA-Z0-9]+$/.test(employeeId)
}

export const isStrongPassword = (password: string): boolean => {
  // At least 8 characters, 1 uppercase, 1 lowercase, 1 number
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/
  return passwordRegex.test(password)
}

export const validateRequired = (value: any): boolean => {
  if (typeof value === 'string') return value.trim().length > 0
  if (Array.isArray(value)) return value.length > 0
  return value !== null && value !== undefined
}

export const validateMinLength = (value: string, minLength: number): boolean => {
  return value.length >= minLength
}

export const validateMaxLength = (value: string, maxLength: number): boolean => {
  return value.length <= maxLength
}
