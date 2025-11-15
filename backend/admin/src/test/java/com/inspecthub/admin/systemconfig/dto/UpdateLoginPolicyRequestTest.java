package com.inspecthub.admin.systemconfig.dto;

import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UpdateLoginPolicyRequest DTO 검증 테스트
 */
@DisplayName("UpdateLoginPolicyRequest DTO 검증 테스트")
class UpdateLoginPolicyRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("유효한 요청")
    class ValidRequests {

        @Test
        @DisplayName("모든 필드가 유효한 경우 검증 통과")
        void shouldPassValidation_WhenAllFieldsAreValid() {
            // Given
            Set<LoginMethod> enabledMethods = new LinkedHashSet<>(
                Arrays.asList(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL)
            );
            List<LoginMethod> priority = Arrays.asList(
                LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL
            );

            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name("시스템 로그인 정책")
                .enabledMethods(enabledMethods)
                .priority(priority)
                .build();

            // When
            Set<ConstraintViolation<UpdateLoginPolicyRequest>> violations =
                validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("name이 100자일 때 검증 통과")
        void shouldPassValidation_WhenNameIs100Characters() {
            // Given
            String name = "A".repeat(100);  // 정확히 100자
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name(name)
                .enabledMethods(Set.of(LoginMethod.LOCAL))
                .priority(List.of(LoginMethod.LOCAL))
                .build();

            // When
            Set<ConstraintViolation<UpdateLoginPolicyRequest>> violations =
                validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("name 필드 검증")
    class NameValidation {

        @Test
        @DisplayName("name이 null일 때 검증 실패")
        void shouldFailValidation_WhenNameIsNull() {
            // Given
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name(null)
                .enabledMethods(Set.of(LoginMethod.LOCAL))
                .priority(List.of(LoginMethod.LOCAL))
                .build();

            // When
            Set<ConstraintViolation<UpdateLoginPolicyRequest>> violations =
                validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("name") &&
                v.getMessage().contains("필수")
            );
        }

        @Test
        @DisplayName("name이 빈 문자열일 때 검증 실패")
        void shouldFailValidation_WhenNameIsBlank() {
            // Given
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name("   ")
                .enabledMethods(Set.of(LoginMethod.LOCAL))
                .priority(List.of(LoginMethod.LOCAL))
                .build();

            // When
            Set<ConstraintViolation<UpdateLoginPolicyRequest>> violations =
                validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("name")
            );
        }

        @Test
        @DisplayName("name이 100자를 초과할 때 검증 실패")
        void shouldFailValidation_WhenNameExceeds100Characters() {
            // Given
            String name = "A".repeat(101);  // 101자
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name(name)
                .enabledMethods(Set.of(LoginMethod.LOCAL))
                .priority(List.of(LoginMethod.LOCAL))
                .build();

            // When
            Set<ConstraintViolation<UpdateLoginPolicyRequest>> violations =
                validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("name") &&
                v.getMessage().contains("100")
            );
        }
    }

    @Nested
    @DisplayName("enabledMethods 필드 검증")
    class EnabledMethodsValidation {

        @Test
        @DisplayName("enabledMethods가 null일 때 검증 실패")
        void shouldFailValidation_WhenEnabledMethodsIsNull() {
            // Given
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name("정책")
                .enabledMethods(null)
                .priority(List.of(LoginMethod.LOCAL))
                .build();

            // When
            Set<ConstraintViolation<UpdateLoginPolicyRequest>> violations =
                validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("enabledMethods")
            );
        }

        @Test
        @DisplayName("enabledMethods가 비어있을 때 검증 실패")
        void shouldFailValidation_WhenEnabledMethodsIsEmpty() {
            // Given
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name("정책")
                .enabledMethods(Collections.emptySet())
                .priority(List.of(LoginMethod.LOCAL))
                .build();

            // When
            Set<ConstraintViolation<UpdateLoginPolicyRequest>> violations =
                validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("enabledMethods") &&
                v.getMessage().contains("1개")
            );
        }
    }

    @Nested
    @DisplayName("priority 필드 검증")
    class PriorityValidation {

        @Test
        @DisplayName("priority가 null일 때 검증 실패")
        void shouldFailValidation_WhenPriorityIsNull() {
            // Given
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name("정책")
                .enabledMethods(Set.of(LoginMethod.LOCAL))
                .priority(null)
                .build();

            // When
            Set<ConstraintViolation<UpdateLoginPolicyRequest>> violations =
                validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("priority")
            );
        }

        @Test
        @DisplayName("priority가 비어있을 때 검증 실패")
        void shouldFailValidation_WhenPriorityIsEmpty() {
            // Given
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name("정책")
                .enabledMethods(Set.of(LoginMethod.LOCAL))
                .priority(Collections.emptyList())
                .build();

            // When
            Set<ConstraintViolation<UpdateLoginPolicyRequest>> violations =
                validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("priority") &&
                v.getMessage().contains("비어있을 수 없습니다")
            );
        }
    }
}
