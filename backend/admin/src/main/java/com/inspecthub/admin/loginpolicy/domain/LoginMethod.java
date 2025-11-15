package com.inspecthub.admin.loginpolicy.domain;

/**
 * 로그인 방식 Enum
 *
 * 우선순위: SSO > AD > LOCAL
 */
public enum LoginMethod {
    /**
     * Single Sign-On (SSO)
     * 최우선 로그인 방식
     */
    SSO,

    /**
     * Active Directory (AD)
     * LDAP/Kerberos 인증
     */
    AD,

    /**
     * 일반 로그인 (Fallback)
     * 자체 DB 인증 (BCrypt)
     */
    LOCAL
}
