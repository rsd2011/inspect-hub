/**
 * Loading Manager Composable
 *
 * Manages global and scoped loading states
 */

export interface LoadingState {
  /**
   * Loading key (for scoped loading)
   */
  key: string

  /**
   * Loading message
   */
  message?: string

  /**
   * Start time
   */
  startTime: number
}

export function useLoadingManager() {
  const loadingStates = ref<Map<string, LoadingState>>(new Map())

  /**
   * Check if any loading is active
   */
  const isLoading = computed(() => loadingStates.value.size > 0)

  /**
   * Check if specific key is loading
   */
  function isLoadingKey(key: string): boolean {
    return loadingStates.value.has(key)
  }

  /**
   * Start loading
   */
  function startLoading(key: string = 'global', message?: string) {
    loadingStates.value.set(key, {
      key,
      message,
      startTime: Date.now(),
    })
  }

  /**
   * Stop loading
   */
  function stopLoading(key: string = 'global') {
    loadingStates.value.delete(key)
  }

  /**
   * Stop all loading
   */
  function stopAll() {
    loadingStates.value.clear()
  }

  /**
   * Get loading state for key
   */
  function getLoadingState(key: string): LoadingState | undefined {
    return loadingStates.value.get(key)
  }

  /**
   * Get all loading states
   */
  function getAllStates(): LoadingState[] {
    return Array.from(loadingStates.value.values())
  }

  /**
   * Wrap async function with loading state
   */
  async function withLoading<T>(
    fn: () => Promise<T>,
    key: string = 'global',
    message?: string,
  ): Promise<T> {
    startLoading(key, message)
    try {
      return await fn()
    }
    finally {
      stopLoading(key)
    }
  }

  return {
    // State
    isLoading,

    // Methods
    startLoading,
    stopLoading,
    stopAll,
    isLoadingKey,
    getLoadingState,
    getAllStates,
    withLoading,

    // Computed
    loadingCount: computed(() => loadingStates.value.size),
    loadingKeys: computed(() => Array.from(loadingStates.value.keys())),
  }
}

export type LoadingManager = ReturnType<typeof useLoadingManager>
