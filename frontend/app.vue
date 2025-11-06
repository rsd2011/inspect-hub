<template>
  <div id="app">
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </div>
</template>

<script setup lang="ts">
// Global meta tags
useHead({
  title: 'Inspect Hub - AML Compliance System',
  meta: [
    { name: 'description', content: 'AML Integrated Compliance Monitoring System' },
    { name: 'viewport', content: 'width=device-width, initial-scale=1' }
  ],
  link: [
    { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
  ]
})
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
