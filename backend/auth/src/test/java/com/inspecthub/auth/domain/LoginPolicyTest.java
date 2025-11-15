package com.inspecthub.auth.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * LoginPolicy 도메인 모델 테스트
 *
 * Jenkins 스타일 전역 정책 (조직별 정책 없음)
 * BDD 스타일(Given-When-Then)로 작성
 */
@DisplayName("LoginPolicy 도메인 모델 - 전역 정책")
class LoginPolicyTest {

    @Test
    @DisplayName("필수 필드로 LoginPolicy를 생성할 수 있다")
    void shouldCreateLoginPolicyWithRequiredFields() {
        // Given (준비)
        String id = "01JCXYZ1234567890ABCDEF123";  // ULID
        String name = "기본 로그인 정책";
        Set<LoginMethod> enabledMethods = Set.of(
            LoginMethod.SSO,
            LoginMethod.AD,
            LoginMethod.LOCAL
        );

        // When (실행)
        LoginPolicy policy = LoginPolicy.builder()
            .id(id)
            .name(name)
            .enabledMethods(enabledMethods)
            .build();

        // Then (검증)
        assertThat(policy).isNotNull();
        assertThat(policy.getId()).isEqualTo(id);
        assertThat(policy.getName()).isEqualTo(name);
        assertThat(policy.getEnabledMethods())
            .containsExactlyInAnyOrder(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL);
    }

    @Test
    @DisplayName("name이 null이면 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenNameIsNull() {
        // Given (준비)
        String id = "01JCXYZ1234567890ABCDEF125";
        String name = null;
        Set<LoginMethod> enabledMethods = Set.of(LoginMethod.SSO);

        // When & Then (실행 및 검증)
        assertThatThrownBy(() ->
            LoginPolicy.builder()
                .id(id)
                .name(name)
                    .enabledMethods(enabledMethods)
                .build()
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
    }

    @Test
    @DisplayName("enabledMethods가 비어있으면 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenEnabledMethodsIsEmpty() {
        // Given (준비)
        String id = "01JCXYZ1234567890ABCDEF126";
        String name = "테스트 정책";
        Set<LoginMethod> enabledMethods = Set.of();  // 빈 Set

        // When & Then (실행 및 검증)
        assertThatThrownBy(() ->
            LoginPolicy.builder()
                .id(id)
                .name(name)
                    .enabledMethods(enabledMethods)
                .build()
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("최소 1개");
    }

    @Test
    @DisplayName("enabledMethods가 null이면 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenEnabledMethodsIsNull() {
        // Given (준비)
        String id = "01JCXYZ1234567890ABCDEF127";
        String name = "테스트 정책";
        Set<LoginMethod> enabledMethods = null;

        // When & Then (실행 및 검증)
        assertThatThrownBy(() ->
            LoginPolicy.builder()
                .id(id)
                .name(name)
                    .enabledMethods(enabledMethods)
                .build()
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("enabledMethods");
    }

    @Test
    @DisplayName("기본 우선순위는 SSO > AD > LOCAL이다")
    void shouldHaveDefaultPriority() {
        // Given (준비)
        String id = "01JCXYZ1234567890ABCDEF128";
        String name = "기본 우선순위 정책";
        Set<LoginMethod> enabledMethods = Set.of(
            LoginMethod.SSO,
            LoginMethod.AD,
            LoginMethod.LOCAL
        );

        // When (실행)
        LoginPolicy policy = LoginPolicy.builder()
            .id(id)
            .name(name)
            .enabledMethods(enabledMethods)
            .build();

        // Then (검증)
        assertThat(policy.getPriority())
            .containsExactly(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL);
    }

    @Test
    @DisplayName("특정 로그인 방식이 활성화되어 있는지 확인할 수 있다")
    void shouldCheckIfMethodIsEnabled() {
        // Given (준비)
        LoginPolicy policy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF129")
            .name("SSO만 활성화")
            .enabledMethods(Set.of(LoginMethod.SSO))
            .build();

        // When & Then (실행 및 검증)
        assertThat(policy.isMethodEnabled(LoginMethod.SSO)).isTrue();
        assertThat(policy.isMethodEnabled(LoginMethod.AD)).isFalse();
        assertThat(policy.isMethodEnabled(LoginMethod.LOCAL)).isFalse();
    }

    @Test
    @DisplayName("최우선 로그인 방식을 조회할 수 있다")
    void shouldGetPrimaryMethod() {
        // Given (준비)
        LoginPolicy policy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF130")
            .name("SSO 우선 정책")
            .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.LOCAL))
            .build();

        // When (실행)
        LoginMethod primaryMethod = policy.getPrimaryMethod();

        // Then (검증)
        assertThat(primaryMethod).isEqualTo(LoginMethod.SSO);
    }

    @Test
    @DisplayName("특정 로그인 방식을 비활성화할 수 있다")
    void shouldDisableMethod() {
        // Given (준비)
        LoginPolicy policy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF131")
            .name("테스트 정책")
            .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL))
            .build();

        // When (실행)
        policy.disableMethod(LoginMethod.AD);

        // Then (검증)
        assertThat(policy.isMethodEnabled(LoginMethod.SSO)).isTrue();
        assertThat(policy.isMethodEnabled(LoginMethod.AD)).isFalse();
        assertThat(policy.isMethodEnabled(LoginMethod.LOCAL)).isTrue();
    }

    @Test
    @DisplayName("마지막 남은 로그인 방식은 비활성화할 수 없다")
    void shouldNotDisableLastMethod() {
        // Given (준비)
        LoginPolicy policy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF132")
            .name("SSO만 활성화")
            .enabledMethods(Set.of(LoginMethod.SSO))
            .build();

        // When & Then (실행 및 검증)
        assertThatThrownBy(() -> policy.disableMethod(LoginMethod.SSO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("최소 1개");
    }

    @Test
    @DisplayName("특정 로그인 방식을 활성화할 수 있다")
    void shouldEnableMethod() {
        // Given (준비)
        LoginPolicy policy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF133")
            .name("SSO만 활성화")
            .enabledMethods(Set.of(LoginMethod.SSO))
            .build();

        // When (실행)
        policy.enableMethod(LoginMethod.AD);

        // Then (검증)
        assertThat(policy.isMethodEnabled(LoginMethod.SSO)).isTrue();
        assertThat(policy.isMethodEnabled(LoginMethod.AD)).isTrue();
        assertThat(policy.isMethodEnabled(LoginMethod.LOCAL)).isFalse();
    }

    @Test
    @DisplayName("우선순위를 변경할 수 있다")
    void shouldUpdatePriority() {
        // Given (준비)
        LoginPolicy policy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF134")
            .name("우선순위 테스트")
            .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL))
            .build();

        List<LoginMethod> newPriority = List.of(
            LoginMethod.LOCAL,
            LoginMethod.AD,
            LoginMethod.SSO
        );

        // When (실행)
        policy.updatePriority(newPriority);

        // Then (검증)
        assertThat(policy.getPriority())
            .containsExactly(LoginMethod.LOCAL, LoginMethod.AD, LoginMethod.SSO);
        assertThat(policy.getPrimaryMethod()).isEqualTo(LoginMethod.LOCAL);
    }

    @Test
    @DisplayName("우선순위에 비활성화된 방식이 포함되면 예외가 발생한다")
    void shouldThrowExceptionWhenPriorityContainsDisabledMethod() {
        // Given (준비)
        LoginPolicy policy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF135")
            .name("우선순위 검증 테스트")
            .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.LOCAL))
            .build();

        List<LoginMethod> invalidPriority = List.of(
            LoginMethod.SSO,
            LoginMethod.AD,  // AD는 비활성화되어 있음
            LoginMethod.LOCAL
        );

        // When & Then (실행 및 검증)
        assertThatThrownBy(() -> policy.updatePriority(invalidPriority))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("비활성화된");
    }

    @Test
    @DisplayName("빈 우선순위로 변경할 수 없다")
    void shouldNotUpdateWithEmptyPriority() {
        // Given (준비)
        LoginPolicy policy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF136")
            .name("우선순위 빈 값 테스트")
            .enabledMethods(Set.of(LoginMethod.SSO))
            .build();

        List<LoginMethod> emptyPriority = List.of();

        // When & Then (실행 및 검증)
        assertThatThrownBy(() -> policy.updatePriority(emptyPriority))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("우선순위");
    }
}
