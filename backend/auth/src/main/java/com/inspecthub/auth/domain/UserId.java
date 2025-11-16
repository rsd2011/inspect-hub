package com.inspecthub.auth.domain;

import com.github.f4b6a3.ulid.UlidCreator;
import com.inspecthub.common.domain.Id;
import lombok.EqualsAndHashCode;

/**
 * User ID Value Object (ULID)
 */
@EqualsAndHashCode(callSuper = true)
public class UserId extends Id<String> {

    private UserId(String value) {
        super(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("UserId는 비어있을 수 없습니다");
        }
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(UlidCreator.getUlid().toString());
    }

    @Override
    public String toString() {
        return getValue();
    }
}
