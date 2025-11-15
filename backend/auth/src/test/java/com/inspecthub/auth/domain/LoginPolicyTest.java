package com.inspecthub.auth.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * LoginPolicy 도메인 모델 테스트
 *
 * BDD 스타일(Given-When-Then)로 작성
 */
@DisplayName("LoginPolicy 도메인 모델")
class LoginPolicyTest {

    @Test
    @DisplayName("필수 필드로 LoginPolicy를 생성할 수 있다")
    void shouldCreateLoginPolicyWithRequiredFields() {
        // Given (준비)
        String id = "01JCXYZ1234567890ABCDEF123";  // ULID
        String name = "기본 로그인 정책";
        String orgId = "01JCORG1234567890ABCDEF123";
        Set<LoginMethod> enabledMethods = Set.of(
            LoginMethod.SSO,
            LoginMethod.AD,
            LoginMethod.LOCAL
        );

        // When (실행)
        LoginPolicy policy = LoginPolicy.builder()
            .id(id)
            .name(name)
            .orgId(orgId)
            .enabledMethods(enabledMethods)
            .build();

        // Then (검증)
        assertThat(policy).isNotNull();
        assertThat(policy.getId()).isEqualTo(id);
        assertThat(policy.getName()).isEqualTo(name);
        assertThat(policy.getOrgId()).isEqualTo(orgId);
        assertThat(policy.getEnabledMethods())
            .containsExactlyInAnyOrder(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL);
    }

    @Test
    @DisplayName("글로벌 정책은 orgId가 null이다")
    void shouldCreateGlobalPolicyWithNullOrgId() {
        // Given (준비)
        String id = "01JCXYZ1234567890ABCDEF124";
        String name = "글로벌 로그인 정책";
        String orgId = null;  // 글로벌 정책
        Set<LoginMethod> enabledMethods = Set.of(
            LoginMethod.SSO,
            LoginMethod.AD,
            LoginMethod.LOCAL
        );

        // When (실행)
        LoginPolicy policy = LoginPolicy.builder()
            .id(id)
            .name(name)
            .orgId(orgId)
            .enabledMethods(enabledMethods)
            .build();

        // Then (검증)
        assertThat(policy.getOrgId()).isNull();
        assertThat(policy.isGlobalPolicy()).isTrue();
    }

    @Test
    @DisplayName("name이 null이면 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenNameIsNull() {
        // Given (준비)
        String id = "01JCXYZ1234567890ABCDEF125";
        String name = null;
        String orgId = "01JCORG1234567890ABCDEF123";
        Set<LoginMethod> enabledMethods = Set.of(LoginMethod.SSO);

        // When & Then (실행 및 검증)
        assertThatThrownBy(() ->
            LoginPolicy.builder()
                .id(id)
                .name(name)
                .orgId(orgId)
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
        String orgId = "01JCORG1234567890ABCDEF123";
        Set<LoginMethod> enabledMethods = Set.of();  // 빈 Set

        // When & Then (실행 및 검증)
        assertThatThrownBy(() ->
            LoginPolicy.builder()
                .id(id)
                .name(name)
                .orgId(orgId)
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
        String orgId = "01JCORG1234567890ABCDEF123";
        Set<LoginMethod> enabledMethods = null;

        // When & Then (실행 및 검증)
        assertThatThrownBy(() ->
            LoginPolicy.builder()
                .id(id)
                .name(name)
                .orgId(orgId)
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
        String orgId = "01JCORG1234567890ABCDEF123";
        Set<LoginMethod> enabledMethods = Set.of(
            LoginMethod.SSO,
            LoginMethod.AD,
            LoginMethod.LOCAL
        );

        // When (실행)
        LoginPolicy policy = LoginPolicy.builder()
            .id(id)
            .name(name)
            .orgId(orgId)
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
            .orgId("01JCORG1234567890ABCDEF123")
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
            .orgId("01JCORG1234567890ABCDEF123")
            .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.LOCAL))
            .build();

        // When (실행)
        LoginMethod primaryMethod = policy.getPrimaryMethod();

        // Then (검증)
        assertThat(primaryMethod).isEqualTo(LoginMethod.SSO);
    }
}
