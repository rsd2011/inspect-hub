package com.inspecthub.common.exception;

import lombok.Getter;

/**
 * 도메인 불변식 위반 예외
 *
 * 도메인 엔티티의 규칙이 위반되었을 때 발생하는 예외
 * 예: 비밀번호 길이 제한, 이메일 형식, 상태 전이 규칙 등
 */
@Getter
public class DomainException extends RuntimeException {

    private final String domainName;
    private final String rule;

    public DomainException(String domainName, String rule, String message) {
        super(message);
        this.domainName = domainName;
        this.rule = rule;
    }

    public DomainException(String domainName, String rule, String message, Throwable cause) {
        super(message, cause);
        this.domainName = domainName;
        this.rule = rule;
    }
}
