package com.inspecthub.admin.systemconfig.dto;

import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LoginPolicyResponse DTO 테스트
 */
@DisplayName("LoginPolicyResponse DTO 테스트")
class LoginPolicyResponseTest {

    @Test
    @DisplayName("Builder로 생성 시 모든 필드가 정확히 설정된다")
    void shouldCreateWithAllFields() {
        // Given
        String id = "01JCXYZ1234567890ABCDEF002";
        String name = "시스템 로그인 정책";
        Set<LoginMethod> enabledMethods = new LinkedHashSet<>(
            Arrays.asList(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL)
        );
        List<LoginMethod> priority = Arrays.asList(
            LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL
        );
        boolean active = true;
        String createdBy = "SYSTEM";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        String updatedBy = "ADMIN";
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        LoginPolicyResponse response = LoginPolicyResponse.builder()
            .id(id)
            .name(name)
            .enabledMethods(enabledMethods)
            .priority(priority)
            .active(active)
            .createdBy(createdBy)
            .createdAt(createdAt)
            .updatedBy(updatedBy)
            .updatedAt(updatedAt)
            .build();

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.getEnabledMethods()).containsExactlyInAnyOrder(
            LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL
        );
        assertThat(response.getPriority()).containsExactly(
            LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL
        );
        assertThat(response.isActive()).isTrue();
        assertThat(response.getCreatedBy()).isEqualTo(createdBy);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("필수 필드만으로 생성 가능하다")
    void shouldCreateWithRequiredFieldsOnly() {
        // Given
        String id = "01JCXYZ1234567890ABCDEF002";
        String name = "최소 정책";
        Set<LoginMethod> enabledMethods = new LinkedHashSet<>(
            Collections.singletonList(LoginMethod.LOCAL)
        );
        List<LoginMethod> priority = Collections.singletonList(LoginMethod.LOCAL);

        // When
        LoginPolicyResponse response = LoginPolicyResponse.builder()
            .id(id)
            .name(name)
            .enabledMethods(enabledMethods)
            .priority(priority)
            .build();

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.getEnabledMethods()).containsExactly(LoginMethod.LOCAL);
        assertThat(response.getPriority()).containsExactly(LoginMethod.LOCAL);
    }

    @Test
    @DisplayName("enabledMethods가 불변 컬렉션이다")
    void shouldReturnImmutableEnabledMethods() {
        // Given
        Set<LoginMethod> enabledMethods = new LinkedHashSet<>(
            Arrays.asList(LoginMethod.SSO, LoginMethod.LOCAL)
        );
        LoginPolicyResponse response = LoginPolicyResponse.builder()
            .id("test-id")
            .name("test")
            .enabledMethods(enabledMethods)
            .priority(new ArrayList<>(enabledMethods))
            .build();

        // When
        Set<LoginMethod> returnedMethods = response.getEnabledMethods();

        // Then
        assertThat(returnedMethods).isNotSameAs(enabledMethods);
        assertThat(returnedMethods).containsExactlyInAnyOrderElementsOf(enabledMethods);
    }

    @Test
    @DisplayName("priority가 불변 컬렉션이다")
    void shouldReturnImmutablePriority() {
        // Given
        List<LoginMethod> priority = Arrays.asList(LoginMethod.SSO, LoginMethod.LOCAL);
        LoginPolicyResponse response = LoginPolicyResponse.builder()
            .id("test-id")
            .name("test")
            .enabledMethods(new LinkedHashSet<>(priority))
            .priority(priority)
            .build();

        // When
        List<LoginMethod> returnedPriority = response.getPriority();

        // Then
        assertThat(returnedPriority).isNotSameAs(priority);
        assertThat(returnedPriority).containsExactlyElementsOf(priority);
    }
}
