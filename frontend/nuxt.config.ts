// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },

  // SPA mode only - no SSR
  ssr: false,

  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt',
    '@nuxtjs/i18n',
    '@nuxt/eslint'
  ],

  // Tailwind CSS configuration
  tailwindcss: {
    cssPath: '~/assets/css/tailwind.css',
    configPath: 'tailwind.config.js',
    exposeConfig: false,
    viewer: true,
    // Prefix to avoid conflicts with PrimeVue/RealGrid
    config: {
      prefix: 'tw-'
    }
  },

  // i18n configuration
  i18n: {
    locales: [
      { code: 'ko', iso: 'ko-KR', file: 'ko.json', name: '한국어' },
      { code: 'en', iso: 'en-US', file: 'en.json', name: 'English' }
    ],
    defaultLocale: 'ko',
    lazy: true,
    langDir: 'locales/',
    strategy: 'no_prefix',
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'i18n_locale',
      redirectOn: 'root'
    }
  },

  // CSS
  css: [
    '~/assets/css/main.css'
  ],

  // Auto-import components
  components: [
    {
      path: '~/components',
      pathPrefix: false
    }
  ],

  // Vite configuration
  vite: {
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: '@use "~/assets/css/_variables.scss" as *;'
        }
      }
    }
  },

  // Runtime config
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8090/api/v1'
    }
  },

  // TypeScript
  typescript: {
    strict: true,
    typeCheck: true
  },

  // Build configuration
  build: {
    transpile: ['primevue', 'echarts', 'vue-echarts']
  }
})
