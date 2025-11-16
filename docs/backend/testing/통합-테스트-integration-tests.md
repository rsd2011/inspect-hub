# 통합 테스트 (Integration Tests)

## Repository Integration Test

```java
package com.inspecthub.admin.repository;

import com.inspecthub.admin.domain.User;
import com.inspecthub.test.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
            .username("testuser")
            .passwordHash("$2a$12$encodedPassword")
            .email("test@example.com")
            .fullName("테스트 사용자")
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("사용자 저장 및 조회")
    void saveAndFind() {
        // When
        User saved = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();
        
        Optional<User> found = userRepository.findById(saved.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("username으로 사용자 조회")
    void findByUsername() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();
        
        // When
        Optional<User> found = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testUser.getId());
    }
    
    @Test
    @DisplayName("email 중복 체크")
    void existsByEmail() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();
        
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("other@example.com");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    @DisplayName("조직별 사용자 목록 조회")
    void findByOrgId() {
        // Given
        String orgId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZS";
        testUser.setOrgId(orgId);
        userRepository.save(testUser);
        
        User anotherUser = User.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZU")
            .username("user2")
            .passwordHash("$2a$12$hash2")
            .email("user2@example.com")
            .fullName("사용자2")
            .orgId(orgId)
            .status("ACTIVE")
            .build();
        userRepository.save(anotherUser);
        entityManager.flush();
        
        // When
        List<User> users = userRepository.findByOrgId(orgId);
        
        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
            .containsExactlyInAnyOrder("testuser", "user2");
    }
    
    @Test
    @DisplayName("Soft Delete")
    void softDelete() {
        // Given
        User saved = userRepository.save(testUser);
        String userId = saved.getId();
        entityManager.flush();
        
        // When
        saved.setDeleted(true);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("admin");
        userRepository.save(saved);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        User deletedUser = userRepository.findById(userId).orElseThrow();
        assertThat(deletedUser.isDeleted()).isTrue();
        assertThat(deletedUser.getDeletedAt()).isNotNull();
        assertThat(deletedUser.getDeletedBy()).isEqualTo("admin");
    }
}
```

---
