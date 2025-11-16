# 🔀 Service 레이어 예외 처리 패턴

## 패턴 1: BusinessException 던지기 (유스케이스 중단)

**사용 시기:**
- 트랜잭션 롤백이 필요한 경우
- 유스케이스를 중단시켜야 하는 실패

```java
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public TokenResponse authenticate(LoginRequest request) {
        // 1. 사용자 조회 - 없으면 예외
        User user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001));
        
        // 2. 비밀번호 검증 - 실패 시 예외
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_002);
        }
        
        // 3. 계정 상태 검증
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.AUTH_003);
        }
        
        // 4. JWT 생성
        return jwtTokenProvider.generateToken(user);
    }
}
```

## 패턴 2: Result<T> 반환 (정상 분기)

**사용 시기:**
- 실패도 정상 흐름인 경우
- 트랜잭션 롤백이 불필요한 경우

**Result 타입 정의:**
```java
@Getter
public class Result<T> {
    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String errorMessage;
    
    private Result(boolean success, T data, String errorCode, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, null);
    }
    
    public static <T> Result<T> failure(String errorCode, String errorMessage) {
        return new Result<>(false, null, errorCode, errorMessage);
    }
    
    public boolean isFailure() {
        return !success;
    }
}
```

**Service 구현:**
```java
@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    public Result<List<RecommendationDto>> getRecommendations(String userId) {
        try {
            List<Recommendation> recommendations = recommendationRepository.findByUserId(userId);
            
            if (recommendations.isEmpty()) {
                // ✅ 빈 결과도 성공으로 간주
                return Result.success(Collections.emptyList());
            }
            
            List<RecommendationDto> dtos = recommendations.stream()
                    .map(RecommendationDto::from)
                    .toList();
            
            return Result.success(dtos);
            
        } catch (Exception e) {
            log.error("Failed to get recommendations", e);
            return Result.failure("RECOMMENDATION_ERROR", "추천 조회 실패");
        }
    }
}
```

**Controller에서 Result 처리:**
```java
@RestController
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationDto>>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Result<List<RecommendationDto>> result = 
                recommendationService.getRecommendations(userDetails.getUsername());
        
        if (result.isFailure()) {
            // Result 실패를 예외로 변환하여 전역 핸들러로 전파
            throw new BusinessException(result.getErrorCode(), result.getErrorMessage());
        }
        
        return ResponseEntity.ok(ApiResponse.success(result.getData()));
    }
}
```

---
