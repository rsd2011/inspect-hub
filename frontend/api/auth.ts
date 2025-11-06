export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  userId: string
  username: string
  accessToken: string
  refreshToken: string
  roles: string[]
  expiresIn: number
}

export interface TokenRefreshRequest {
  refreshToken: string
}

export interface TokenRefreshResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export const authApi = {
  /**
   * 로그인 API
   */
  async login(request: LoginRequest): Promise<LoginResponse> {
    const config = useRuntimeConfig()
    const response = await $fetch<LoginResponse>(`${config.public.apiBase}/auth/login`, {
      method: 'POST',
      body: request,
    })
    return response
  },

  /**
   * 토큰 갱신 API
   */
  async refreshToken(request: TokenRefreshRequest): Promise<TokenRefreshResponse> {
    const config = useRuntimeConfig()
    const response = await $fetch<TokenRefreshResponse>(`${config.public.apiBase}/auth/refresh`, {
      method: 'POST',
      body: request,
    })
    return response
  },

  /**
   * 로그아웃 API
   */
  async logout(accessToken: string): Promise<void> {
    const config = useRuntimeConfig()
    await $fetch(`${config.public.apiBase}/auth/logout`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    })
  },

  /**
   * 현재 사용자 정보 조회 (테스트용)
   */
  async getCurrentUser(accessToken: string): Promise<string> {
    const config = useRuntimeConfig()
    const response = await $fetch<string>(`${config.public.apiBase}/auth/me`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    })
    return response
  },
}
