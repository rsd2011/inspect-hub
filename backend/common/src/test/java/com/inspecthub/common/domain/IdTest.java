package com.inspecthub.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Id 기본 클래스 테스트
 *
 * Value Object 패턴으로 구현된 ID의 기본 동작 검증
 */
@DisplayName("Id 기본 클래스 테스트")
class IdTest {

    // 테스트용 구체 클래스 (ULID 기반)
    static class TestId extends Id<String> {
        protected TestId(String value) {
            super(value);
        }

        public static TestId of(String value) {
            return new TestId(value);
        }
    }

    @Nested
    @DisplayName("생성")
    class Creation {

        @Test
        @DisplayName("유효한 값으로 ID 생성 시 성공한다")
        void shouldCreateId_WhenValidValue() {
            // Given
            String value = "01ARZ3NDEKTSV4RRFFQ69G5FAV"; // 유효한 ULID

            // When
            TestId id = TestId.of(value);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.getValue()).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값으로 ID 생성 시 예외를 발생시킨다")
        void shouldThrowException_WhenNullValue() {
            // Given
            String value = null;

            // When & Then
            assertThatThrownBy(() -> TestId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID value cannot be null");
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 값을 가진 두 ID는 동등하다")
        void shouldBeEqual_WhenSameValue() {
            // Given
            String ulid = "01ARZ3NDEKTSV4RRFFQ69G5FAV";
            TestId id1 = TestId.of(ulid);
            TestId id2 = TestId.of(ulid);

            // When & Then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 두 ID는 동등하지 않다")
        void shouldNotBeEqual_WhenDifferentValue() {
            // Given
            TestId id1 = TestId.of("01ARZ3NDEKTSV4RRFFQ69G5FAV");
            TestId id2 = TestId.of("01ARZ3NDEKTSV4RRFFQ69G5FAW"); // 마지막 글자 다름

            // When & Then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("null과 비교 시 동등하지 않다")
        void shouldNotBeEqual_WhenComparedWithNull() {
            // Given
            TestId id = TestId.of("01ARZ3NDEKTSV4RRFFQ69G5FAV");

            // When & Then
            assertThat(id).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과 비교 시 동등하지 않다")
        void shouldNotBeEqual_WhenComparedWithDifferentType() {
            // Given
            TestId id = TestId.of("01ARZ3NDEKTSV4RRFFQ69G5FAV");
            String value = "01ARZ3NDEKTSV4RRFFQ69G5FAV";

            // When & Then
            assertThat(id).isNotEqualTo(value);
        }
    }

    @Nested
    @DisplayName("문자열 변환")
    class StringConversion {

        @Test
        @DisplayName("toString()은 ID 값의 문자열 표현을 반환한다")
        void shouldReturnStringRepresentation() {
            // Given
            String ulid = "01ARZ3NDEKTSV4RRFFQ69G5FAV";
            TestId id = TestId.of(ulid);

            // When
            String result = id.toString();

            // Then
            assertThat(result).contains(ulid);
        }
    }

    @Nested
    @DisplayName("불변성")
    class Immutability {

        @Test
        @DisplayName("ID는 불변 객체이다")
        void shouldBeImmutable() {
            // Given
            String originalValue = "01ARZ3NDEKTSV4RRFFQ69G5FAV";
            TestId id = TestId.of(originalValue);

            // When
            String retrievedValue = id.getValue();

            // Then
            assertThat(retrievedValue).isEqualTo(originalValue);
            // getValue()로 얻은 값을 변경해도 원본 ID는 영향받지 않아야 함
            assertThat(id.getValue()).isEqualTo(originalValue);
        }
    }
}
