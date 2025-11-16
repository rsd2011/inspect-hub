/**
 * Application Constants
 */

export const APP_NAME = 'Inspect Hub'
export const APP_VERSION = '1.0.0'

export const DATE_FORMAT = {
  DEFAULT: 'YYYY-MM-DD',
  DATETIME: 'YYYY-MM-DD HH:mm:ss',
  TIME: 'HH:mm:ss',
  KOREAN: 'YYYY년 MM월 DD일',
}

export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 20,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100],
}

export const LOGIN_METHOD = {
  LOCAL: 'LOCAL',
  AD: 'AD',
  SSO: 'SSO',
} as const

export const USER_ROLES = {
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  ANALYST: 'ANALYST',
  VIEWER: 'VIEWER',
} as const

export const DETECTION_TYPE = {
  STR: 'STR',
  CTR: 'CTR',
  WLF: 'WLF',
} as const
