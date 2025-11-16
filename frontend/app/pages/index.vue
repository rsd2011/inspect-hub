<template>
  <div class="home-page">
    <h1 class="tw-text-3xl tw-font-bold">Welcome to Inspect Hub</h1>
    <p class="tw-mt-4 tw-text-gray-600">AML Compliance Monitoring System</p>
    
    <div class="tw-mt-8">
      <Button @click="goToDashboard">
        Go to Dashboard
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
})

const router = useRouter()

const goToDashboard = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.home-page {
  padding: 2rem;
}
</style>
