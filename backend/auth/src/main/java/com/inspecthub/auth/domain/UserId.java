package com.inspecthub.auth.domain;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User ID Value Object (ULID)
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserId {

    private String value;

    private UserId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserId는 null이거나 비어있을 수 없습니다");
        }
        this.value = value;
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(UlidCreator.getUlid().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
