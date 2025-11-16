// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },

  // ⚠️ CRITICAL: Set srcDir to 'app/' for Nuxt 4 official structure
  srcDir: 'app/',

  // ⚠️ CRITICAL: SPA mode only - SSR STRICTLY FORBIDDEN
  ssr: false,

  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt',
    '@nuxtjs/i18n',
    '@nuxt/eslint'
  ],

  // Tailwind CSS configuration
  tailwindcss: {
    cssPath: '~/assets/styles/tailwind.css',
    configPath: 'tailwind.config.js',
    exposeConfig: false,
    viewer: true,
    // ⚠️ CRITICAL: Prefix to avoid conflicts with PrimeVue/RealGrid
    config: {
      prefix: 'tw-'
    }
  },

  // i18n configuration
  i18n: {
    locales: [
      { code: 'ko', language: 'ko-KR', file: 'ko.json', name: '한국어' },
      { code: 'en', language: 'en-US', file: 'en.json', name: 'English' }
    ],
    defaultLocale: 'ko',
    strategy: 'no_prefix',
    langDir: '../i18n/locales',
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'i18n_locale',
      redirectOn: 'root'
    }
  },

  // CSS
  css: [
    '~/assets/styles/fonts.css',
    '~/assets/styles/main.css'
  ],

  // Vite configuration
  vite: {
    server: {
      allowedHosts: ['inspecthub.rsd-toy.com'],
      hmr: {
        overlay: true
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: '@use "~/assets/styles/_variables.scss" as *;'
        }
      }
    },
    build: {
      // Chunk size warnings threshold
      chunkSizeWarningLimit: 1000,
      // Rollup options for code splitting
      rollupOptions: {
        output: {
          manualChunks: {
            'primevue': ['primevue'],
            'charts': ['echarts', 'vue-echarts'],
            'vendor': ['pinia', 'zod', 'vee-validate']
          }
        }
      }
    },
    optimizeDeps: {
      include: ['primevue', 'echarts', 'vue-echarts', 'pinia', 'zod']
    }
  },

  // Runtime config
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8090/api/v1'
    }
  },

  // App head configuration
  app: {
    head: {
      title: 'Inspect Hub - AML Compliance System',
      meta: [
        { name: 'description', content: 'AML Integrated Compliance Monitoring System' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' }
      ],
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
      ]
    }
  },

  // TypeScript
  typescript: {
    strict: true,
    typeCheck: false // Disable build-time type checking
  },

  // Build configuration
  build: {
    transpile: ['primevue', 'echarts', 'vue-echarts']
  },

  // Nitro configuration (Server engine)
  nitro: {
    compressPublicAssets: true,
    minify: true
  },

  // Router configuration
  router: {
    options: {
      strict: true,
      sensitive: false
    }
  },

  // Experimental features
  experimental: {
    payloadExtraction: false,
    renderJsonPayloads: true,
    typedPages: true
  },

  // Features
  features: {
    inlineStyles: false
  }
})
