package com.inspecthub.common.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * 엔티티 ID의 기본 클래스
 *
 * DDD Value Object 패턴으로 구현된 타입 안전 ID
 * - 불변 객체
 * - 동등성 기반 비교
 * - null 불가
 *
 * @param <T> ID 값의 타입 (Long, String 등)
 */
public abstract class Id<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final T value;

    /**
     * ID 생성자
     *
     * @param value ID 값 (null 불가)
     * @throws IllegalArgumentException value가 null인 경우
     */
    protected Id(T value) {
        if (value == null) {
            throw new IllegalArgumentException("ID value cannot be null");
        }
        this.value = value;
    }

    /**
     * ID 값을 반환한다
     *
     * @return ID 값
     */
    public T getValue() {
        return value;
    }

    /**
     * 동등성 비교
     *
     * 같은 타입이고 같은 값을 가진 경우 동등하다고 판단
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id<?> id = (Id<?>) o;
        return Objects.equals(value, id.value);
    }

    /**
     * 해시코드 생성
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{value=" + value + "}";
    }
}
