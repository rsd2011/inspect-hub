# ULID (Universally Unique Lexicographically Sortable Identifier)

Inspect-Hub 프로젝트는 모든 도메인의 Primary Key로 **ULID**를 사용합니다.

## ULID란?

ULID는 UUID와 유사하지만 다음과 같은 장점이 있습니다:

- ✅ **시간순 정렬 가능**: 타임스탬프 포함으로 자연스러운 정렬
- ✅ **26자 Base32 인코딩**: URL 안전, 대소문자 구분 없음
- ✅ **분산 환경 안전**: 충돌 없이 독립적으로 생성 가능
- ✅ **데이터베이스 인덱스 효율**: B-Tree 인덱스에 최적화
- ✅ **가독성**: UUID보다 짧고 읽기 쉬움

### ULID 구조 (26자)

```
01ARZ3NDEKTSV4RRFFQ69G5FAV
|----------| |------------|
 Timestamp      Randomness
  (10자)          (16자)
```

- **Timestamp (10자)**: 밀리초 단위 Unix epoch
- **Randomness (16자)**: 랜덤 데이터

## 사용법

### 1. BaseDomain 상속

모든 도메인는 `BaseDomain`를 상속받아 ULID ID를 사용합니다:

```java
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseDomain {
    private String username;
    private String email;
    // ... 필드들
}
```

`BaseDomain`는 다음을 포함합니다:
- `id` (String, ULID 26자)
- `createdAt`, `createdBy`, `updatedAt`, `updatedBy` (감사 필드)
- `deleted`, `deletedAt`, `deletedBy` (논리적 삭제)

### 2. ULID 생성

#### 도메인 생성 시

```java
User user = User.builder()
    .username("admin")
    .email("admin@example.com")
    .build();

// prePersist()를 호출하여 ID와 타임스탬프 자동 설정
user.prePersist("USER-001"); // 생성자 ID

// 이제 user.getId()는 ULID 값을 가짐
System.out.println(user.getId()); // "01ARZ3NDEKTSV4RRFFQ69G5FAV"
```

#### 직접 생성

```java
// 새 ULID 생성
String ulid = UlidGenerator.generate();
// "01ARZ3NDEKTSV4RRFFQ69G5FAV"

// Monotonic ULID (같은 밀리초 내 순차적)
String monotonicUlid = UlidGenerator.generateMonotonic();

// ULID 유효성 검사
boolean valid = UlidGenerator.isValid(ulid);

// ULID에서 타임스탬프 추출
long timestamp = UlidGenerator.getTimestamp(ulid);
```

### 3. MyBatis Mapper

#### XML Mapper

```xml
<mapper namespace="com.inspecthub.admin.user.mapper.UserMapper">
    <resultMap id="UserResultMap" type="User">
        <!-- ULID Primary Key -->
        <id property="id" column="id"/>
        <!-- ... 다른 필드들 -->
    </resultMap>

    <!-- INSERT -->
    <insert id="insert">
        INSERT INTO "USER" (id, username, email, created_at, created_by)
        VALUES (#{id}, #{username}, #{email}, #{createdAt}, #{createdBy})
    </insert>

    <!-- SELECT by ULID -->
    <select id="findById" resultMap="UserResultMap">
        SELECT * FROM "USER" WHERE id = #{id}
    </select>
</mapper>
```

#### Mapper Interface

```java
@Mapper
public interface UserMapper {
    int insert(User user);
    User findById(@Param("id") String id);
    List<User> findByOrganizationId(@Param("organizationId") String organizationId);
}
```

### 4. 데이터베이스 스키마

```sql
CREATE TABLE "USER" (
    -- Primary Key: ULID (26자)
    id VARCHAR(26) PRIMARY KEY,

    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,

    -- Foreign Key: ULID (다른 테이블 참조)
    organization_id VARCHAR(26),

    -- 감사 필드 (ULID)
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(26) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(26) NOT NULL,

    -- 논리적 삭제
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(26),

    CONSTRAINT fk_user_organization
        FOREIGN KEY (organization_id) REFERENCES "ORGANIZATION"(id)
);

-- 인덱스 생성 (ULID는 시간순 정렬되므로 인덱스 효율적)
CREATE INDEX idx_user_organization ON "USER"(organization_id) WHERE deleted = FALSE;
CREATE INDEX idx_user_created_at ON "USER"(created_at DESC);
```

### 5. Service 레이어 사용 예제

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public User createUser(String username, String email, String currentUserId) {
        User user = User.builder()
            .username(username)
            .email(email)
            .status("ACTIVE")
            .useYn("Y")
            .build();

        // ULID와 타임스탬프 자동 설정
        user.prePersist(currentUserId);

        userMapper.insert(user);
        return user;
    }

    public User getUserById(String userId) {
        return userMapper.findById(userId);
    }

    public void updateUser(String userId, String email, String currentUserId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found: " + userId);
        }

        user.setEmail(email);
        user.preUpdate(currentUserId); // 수정 타임스탬프 설정

        userMapper.update(user);
    }

    public void deleteUser(String userId, String currentUserId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found: " + userId);
        }

        user.softDelete(currentUserId); // 논리적 삭제
        userMapper.softDelete(user);
    }
}
```

## 주의사항

### ✅ DO

1. **항상 BaseDomain 상속**
   ```java
   public class MyEntity extends BaseDomain { }
   ```

2. **INSERT 전에 prePersist() 호출**
   ```java
   entity.prePersist(currentUserId);
   mapper.insert(entity);
   ```

3. **UPDATE 전에 preUpdate() 호출**
   ```java
   entity.preUpdate(currentUserId);
   mapper.update(entity);
   ```

4. **논리적 삭제 사용**
   ```java
   entity.softDelete(currentUserId);
   mapper.softDelete(entity);
   ```

5. **데이터베이스 컬럼은 VARCHAR(26)**
   ```sql
   id VARCHAR(26) PRIMARY KEY
   ```

### ❌ DON'T

1. **Sequential Integer PK 사용 금지**
   ```java
   // ❌ Bad
   private Long id;

   // ✅ Good
   private String id; // ULID
   ```

2. **UUID 사용 금지** (ULID 사용)
   ```java
   // ❌ Bad
   private UUID id;

   // ✅ Good
   private String id; // ULID
   ```

3. **물리적 삭제 금지** (운영 환경)
   ```java
   // ❌ Bad (운영 환경)
   mapper.deletePhysically(id);

   // ✅ Good
   entity.softDelete(userId);
   mapper.softDelete(entity);
   ```

4. **ID 수동 할당 금지**
   ```java
   // ❌ Bad
   User user = new User();
   user.setId("some-random-string"); // 잘못된 ULID 형식

   // ✅ Good
   user.prePersist(userId); // 자동 ULID 생성
   ```

## 장점 요약

### 1. 데이터베이스 성능
- B-Tree 인덱스 효율: 시간순 삽입으로 페이지 분할 최소화
- 빠른 조회: 26자 문자열로 효율적인 검색

### 2. 분산 시스템
- 중앙 ID 생성기 불필요
- 애플리케이션 서버에서 독립적으로 생성
- 충돌 위험 없음

### 3. 보안
- Sequential ID 노출 방지 (데이터 유추 불가)
- 예측 불가능한 ID

### 4. 개발 편의성
- 시간순 정렬 자동 지원
- UUID보다 짧고 읽기 쉬움
- Base32 인코딩으로 URL 안전

## 참고 자료

- [ULID Specification](https://github.com/ulid/spec)
- [f4b6a3 ulid-creator](https://github.com/f4b6a3/ulid-creator) - 사용 중인 Java 라이브러리
- [BaseDomain.java](backend/common/src/main/java/com/inspecthub/common/entity/BaseDomain.java)
- [UlidGenerator.java](backend/common/src/main/java/com/inspecthub/common/util/UlidGenerator.java)
- [UlidTypeHandler.java](backend/common/src/main/java/com/inspecthub/common/mybatis/typehandler/UlidTypeHandler.java)
- [ulid_example.sql](backend/common/src/main/resources/schema/ulid_example.sql)

## 예제 코드

완전한 예제 코드는 다음 파일들을 참고하세요:

- **Entity**: [User.java](backend/admin/src/main/java/com/inspecthub/admin/user/entity/User.java)
- **Mapper Interface**: [UserMapper.java](backend/admin/src/main/java/com/inspecthub/admin/user/mapper/UserMapper.java)
- **Mapper XML**: [UserMapper.xml](backend/admin/src/main/resources/mybatis/mapper/user/UserMapper.xml)
- **Schema**: [ulid_example.sql](backend/common/src/main/resources/schema/ulid_example.sql)
