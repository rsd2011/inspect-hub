package com.inspecthub.auth.repository;

import com.inspecthub.auth.domain.LoginMethod;
import com.inspecthub.auth.domain.LoginPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LoginPolicyRepository 통합 테스트
 * 
 * Testcontainers로 실제 PostgreSQL 환경에서 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("LoginPolicyRepository 통합 테스트")
class LoginPolicyRepositoryIntegrationTest {

    @Autowired
    private LoginPolicyRepository loginPolicyRepository;

    @Nested
    @DisplayName("findGlobalPolicy() - 시스템 전역 정책 조회")
    class FindGlobalPolicy {

        @Test
        @DisplayName("시스템 전역 정책이 존재하면 반환한다")
        void shouldReturnGlobalPolicy_WhenExists() {
            // When
            Optional<LoginPolicy> result = loginPolicyRepository.findGlobalPolicy();

            // Then
            assertThat(result).isPresent();
            LoginPolicy policy = result.get();
            assertThat(policy.getName()).isEqualTo("시스템 로그인 정책");
            assertThat(policy.getEnabledMethods())
                .containsExactlyInAnyOrder(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL);
            assertThat(policy.getPriority())
                .containsExactly(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL);
            assertThat(policy.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("save() - 로그인 정책 저장 (UPSERT)")
    class Save {

        @Test
        @DisplayName("새로운 정책을 INSERT 한다")
        void shouldInsertNewPolicy() {
            // Given
            String newId = "01JCXYZ9999999999ABCDEF999";
            Set<LoginMethod> enabledMethods = new LinkedHashSet<>();
            enabledMethods.add(LoginMethod.SSO);
            
            List<LoginMethod> priority = new ArrayList<>();
            priority.add(LoginMethod.SSO);
            
            LoginPolicy newPolicy = LoginPolicy.builder()
                .id(newId)
                .name("테스트 정책")
                .enabledMethods(enabledMethods)
                .priority(priority)
                .createdBy("TEST_USER")
                .build();

            // When
            loginPolicyRepository.save(newPolicy);

            // Then
            Optional<LoginPolicy> saved = loginPolicyRepository.findById(newId);
            assertThat(saved).isPresent();
            assertThat(saved.get().getName()).isEqualTo("테스트 정책");
            assertThat(saved.get().getEnabledMethods()).containsExactly(LoginMethod.SSO);
        }

        @Test
        @DisplayName("기존 정책을 UPDATE 한다")
        void shouldUpdateExistingPolicy() {
            // Given: 기존 정책 조회
            LoginPolicy existing = loginPolicyRepository.findGlobalPolicy().orElseThrow();
            
            // 정책 수정
            Set<LoginMethod> newMethods = new LinkedHashSet<>();
            newMethods.add(LoginMethod.LOCAL);
            
            List<LoginMethod> newPriority = new ArrayList<>();
            newPriority.add(LoginMethod.LOCAL);
            
            LoginPolicy updated = LoginPolicy.builder()
                .id(existing.getId())
                .name(existing.getName())
                .enabledMethods(newMethods)
                .priority(newPriority)
                .createdBy(existing.getCreatedBy())
                .createdAt(existing.getCreatedAt())
                .updatedBy("TEST_UPDATER")
                .build();

            // When
            loginPolicyRepository.save(updated);

            // Then
            LoginPolicy result = loginPolicyRepository.findById(existing.getId()).orElseThrow();
            assertThat(result.getEnabledMethods()).containsExactly(LoginMethod.LOCAL);
        }
    }

    @Nested
    @DisplayName("findById() - ID로 정책 조회")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회 시 정책을 반환한다")
        void shouldReturnPolicy_WhenIdExists() {
            // Given
            String existingId = "01JCXYZ1234567890ABCDEF002";

            // When
            Optional<LoginPolicy> result = loginPolicyRepository.findById(existingId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(existingId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmpty_WhenIdNotExists() {
            // Given
            String nonExistentId = "01JCXYZ0000000000000000000";

            // When
            Optional<LoginPolicy> result = loginPolicyRepository.findById(nonExistentId);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
