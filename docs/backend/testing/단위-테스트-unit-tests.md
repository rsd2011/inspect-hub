# 단위 테스트 (Unit Tests)

## Service Layer Unit Test

```java
package com.inspecthub.admin.service;

import com.inspecthub.admin.domain.User;
import com.inspecthub.admin.dto.UserCreateRequest;
import com.inspecthub.admin.dto.UserResponse;
import com.inspecthub.admin.repository.UserRepository;
import com.inspecthub.common.exception.DuplicateResourceException;
import com.inspecthub.common.exception.ResourceNotFoundException;
import com.inspecthub.common.util.UlidGenerator;
import com.inspecthub.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@DisplayName("UserService 단위 테스트")
class UserServiceTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UlidGenerator ulidGenerator;
    
    @InjectMocks
    private UserService userService;
    
    private UserCreateRequest request;
    private User user;
    
    @BeforeEach
    void setUp() {
        // Given: 테스트 데이터 준비
        request = UserCreateRequest.builder()
            .username("testuser")
            .password("Password123!")
            .employeeId("EMP001")
            .fullName("테스트 사용자")
            .orgId("01ARZ3NDEKTSV4RRFFQ69G5FAV")
            .permGroupCodes(List.of("PG_ADMIN"))
            .build();

        user = User.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
            .username("testuser")
            .passwordHash("$2a$12$encodedPassword")
            .employeeId("EMP001")
            .fullName("테스트 사용자")
            .status("ACTIVE")
            .build();
    }

    @Test
    @DisplayName("사용자 생성 - 성공")
    void createUser_Success() {
        // Given
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByEmployeeId(anyString())).willReturn(false);
        given(ulidGenerator.generate()).willReturn("01HGW2N7XKQJBZ9VFQR8X7Y3ZT");
        given(passwordEncoder.encode(anyString())).willReturn("$2a$12$encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);

        // When
        UserResponse response = userService.createUser(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmployeeId()).isEqualTo("EMP001");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmployeeId("EMP001");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 생성 - 중복 username")
    void createUser_DuplicateUsername() {
        // Given
        given(userRepository.existsByUsername(anyString())).willReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("username");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("사용자 조회 - 성공")
    void getUser_Success() {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        
        // When
        UserResponse response = userService.getUser(userId);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }
    
    @Test
    @DisplayName("사용자 조회 - 존재하지 않음")
    void getUser_NotFound() {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.getUser(userId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
        
        verify(userRepository).findById(userId);
    }
}
```

## Domain Model Unit Test

```java
package com.inspecthub.policy.domain;

import com.inspecthub.common.exception.InvalidStateException;
import com.inspecthub.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StandardSnapshot 도메인 테스트")
class StandardSnapshotTest extends BaseUnitTest {

    private StandardSnapshot snapshot;
    
    @BeforeEach
    void setUp() {
        snapshot = StandardSnapshot.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
            .type("STR")
            .version(1)
            .status("DRAFT")
            .criteriaJson("{\"threshold\": 10000000}")
            .effectiveFrom(LocalDateTime.now().plusDays(1))
            .build();
    }
    
    @Test
    @DisplayName("스냅샷 활성화 - 성공")
    void activate_Success() {
        // Given
        assertThat(snapshot.getStatus()).isEqualTo("DRAFT");
        
        // When
        snapshot.activate();
        
        // Then
        assertThat(snapshot.getStatus()).isEqualTo("ACTIVE");
        assertThat(snapshot.getActivatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("스냅샷 활성화 - 이미 활성화됨")
    void activate_AlreadyActive() {
        // Given
        snapshot.activate();
        
        // When & Then
        assertThatThrownBy(() -> snapshot.activate())
            .isInstanceOf(InvalidStateException.class)
            .hasMessageContaining("already active");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "DEPRECATED", "ROLLED_BACK"})
    @DisplayName("스냅샷 수정 - 잘못된 상태")
    void update_InvalidStatus(String status) {
        // Given
        snapshot.setStatus(status);
        
        // When & Then
        assertThatThrownBy(() -> snapshot.updateCriteria("{\"new\": \"data\"}"))
            .isInstanceOf(InvalidStateException.class)
            .hasMessageContaining("DRAFT");
    }
    
    @Test
    @DisplayName("스냅샷 버전 체인 연결")
    void linkVersionChain() {
        // Given
        StandardSnapshot prevSnapshot = StandardSnapshot.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZS")
            .type("STR")
            .version(1)
            .status("ACTIVE")
            .build();
        
        // When
        snapshot.linkToPrevious(prevSnapshot);
        
        // Then
        assertThat(snapshot.getPrevId()).isEqualTo(prevSnapshot.getId());
        assertThat(prevSnapshot.getNextId()).isEqualTo(snapshot.getId());
    }
}
```

---
