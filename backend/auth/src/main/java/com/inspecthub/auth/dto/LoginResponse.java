package com.inspecthub.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String userId;
    private String username;
    private String accessToken;
    private String refreshToken;
    private List<String> roles;
    private Long expiresIn;  // milliseconds
}
