# API 테스트

## MockMvc Controller Test

```java
package com.inspecthub.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspecthub.admin.dto.UserCreateRequest;
import com.inspecthub.admin.dto.UserResponse;
import com.inspecthub.admin.service.UserService;
import com.inspecthub.common.exception.DuplicateResourceException;
import com.inspecthub.common.exception.ResourceNotFoundException;
import com.inspecthub.test.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController API 테스트")
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/users - 사용자 생성 성공")
    void createUser_Success() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .password("Password123!")
            .email("newuser@example.com")
            .fullName("신규 사용자")
            .orgId("01HGW2N7XKQJBZ9VFQR8X7Y3ZS")
            .permGroupCodes(List.of("PG_USER"))
            .build();
        
        UserResponse response = UserResponse.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
            .username("newuser")
            .email("newuser@example.com")
            .fullName("신규 사용자")
            .status("ACTIVE")
            .build();
        
        given(userService.createUser(any(UserCreateRequest.class))).willReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value("01HGW2N7XKQJBZ9VFQR8X7Y3ZT"))
            .andExpect(jsonPath("$.data.username").value("newuser"))
            .andExpect(jsonPath("$.data.email").value("newuser@example.com"));
        
        verify(userService).createUser(any(UserCreateRequest.class));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/users - 중복 username (409 Conflict)")
    void createUser_DuplicateUsername() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("existinguser")
            .password("Password123!")
            .email("user@example.com")
            .fullName("사용자")
            .orgId("01HGW2N7XKQJBZ9VFQR8X7Y3ZS")
            .permGroupCodes(List.of("PG_USER"))
            .build();
        
        given(userService.createUser(any(UserCreateRequest.class)))
            .willThrow(new DuplicateResourceException("Username already exists: existinguser"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"))
            .andExpect(jsonPath("$.error.message").containsString("existinguser"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/users/{id} - 사용자 조회 성공")
    void getUser_Success() throws Exception {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        UserResponse response = UserResponse.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .fullName("테스트 사용자")
            .status("ACTIVE")
            .build();
        
        given(userService.getUser(userId)).willReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(userId))
            .andExpect(jsonPath("$.data.username").value("testuser"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/users/{id} - 존재하지 않는 사용자 (404)")
    void getUser_NotFound() throws Exception {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        given(userService.getUser(userId))
            .willThrow(new ResourceNotFoundException("User not found: " + userId));
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/users/{id} - Soft Delete 성공")
    void deleteUser_Success() throws Exception {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        willDoNothing().given(userService).deleteUser(userId);
        
        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isNoContent());
        
        verify(userService).deleteUser(userId);
    }
    
    @Test
    @DisplayName("POST /api/v1/users - 인증 없이 요청 (401)")
    void createUser_Unauthorized() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .password("Password123!")
            .email("newuser@example.com")
            .fullName("신규 사용자")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/users - 권한 없음 (403)")
    void createUser_Forbidden() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .password("Password123!")
            .email("newuser@example.com")
            .fullName("신규 사용자")
            .build();
        
        // When & Then (USER 권한으로는 사용자 생성 불가)
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }
}
```

## RestAssured E2E Test

```java
package com.inspecthub.e2e;

import com.inspecthub.admin.dto.LoginRequest;
import com.inspecthub.admin.dto.UserCreateRequest;
import com.inspecthub.test.BaseIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User API E2E 테스트")
class UserApiE2ETest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;
    
    private static String accessToken;
    private static String createdUserId;
    
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api/v1";
    }
    
    @Test
    @Order(1)
    @DisplayName("E2E: 로그인")
    void login() {
        LoginRequest request = LoginRequest.builder()
            .username("admin")
            .password("admin123")
            .build();
        
        accessToken = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.accessToken", notNullValue())
            .extract()
            .path("data.accessToken");
    }
    
    @Test
    @Order(2)
    @DisplayName("E2E: 사용자 생성")
    void createUser() {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("e2euser")
            .password("Password123!")
            .email("e2e@example.com")
            .fullName("E2E 테스트 사용자")
            .orgId("01HGW2N7XKQJBZ9VFQR8X7Y3ZS")
            .permGroupCodes(List.of("PG_USER"))
            .build();
        
        createdUserId = given()
            .header("Authorization", "Bearer " + accessToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("success", is(true))
            .body("data.username", equalTo("e2euser"))
            .body("data.email", equalTo("e2e@example.com"))
            .extract()
            .path("data.id");
    }
    
    @Test
    @Order(3)
    @DisplayName("E2E: 사용자 조회")
    void getUser() {
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .get("/users/{id}", createdUserId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.id", equalTo(createdUserId))
            .body("data.username", equalTo("e2euser"));
    }
    
    @Test
    @Order(4)
    @DisplayName("E2E: 사용자 목록 조회")
    void listUsers() {
        given()
            .header("Authorization", "Bearer " + accessToken)
            .queryParam("page", 1)
            .queryParam("size", 20)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.items", notNullValue())
            .body("data.items", hasSize(greaterThanOrEqualTo(1)))
            .body("data.pagination.page", equalTo(1))
            .body("data.pagination.size", equalTo(20));
    }
    
    @Test
    @Order(5)
    @DisplayName("E2E: 사용자 수정")
    void updateUser() {
        Map<String, Object> updateRequest = Map.of(
            "fullName", "수정된 이름",
            "email", "updated@example.com"
        );
        
        given()
            .header("Authorization", "Bearer " + accessToken)
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/users/{id}", createdUserId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.fullName", equalTo("수정된 이름"))
            .body("data.email", equalTo("updated@example.com"));
    }
    
    @Test
    @Order(6)
    @DisplayName("E2E: 사용자 삭제")
    void deleteUser() {
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .delete("/users/{id}", createdUserId)
        .then()
            .statusCode(204);
        
        // 삭제 후 조회 시 deleted=true 확인
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .get("/users/{id}", createdUserId)
        .then()
            .statusCode(200)
            .body("data.deleted", is(true))
            .body("data.deletedAt", notNullValue());
    }
}
```

---
