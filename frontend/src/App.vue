<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Icon from './components/Icon.vue'
import { useProgress } from './stores/progress'
const progress = useProgress()
const route = useRoute()
const dark = ref(false)
let media: MediaQueryList
function applyTheme() { dark.value = progress.saved.theme === 'dark' || (progress.saved.theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches); document.documentElement.dataset.theme = dark.value ? 'dark' : 'light' }
function toggleTheme() { progress.saved.theme = dark.value ? 'light' : 'dark'; applyTheme() }
onMounted(() => { applyTheme(); media = window.matchMedia('(prefers-color-scheme: dark)'); media.addEventListener('change', applyTheme) })
onUnmounted(() => media?.removeEventListener('change', applyTheme))
watch(() => route.path, () => document.title = 'LoveLab · 恋爱练习场')
</script>

<template>
  <a class="skip-link" href="#main">跳到主要内容</a>
  <header class="site-header">
    <div class="header-inner">
      <RouterLink class="brand" to="/" aria-label="LoveLab 首页"><span class="brand-symbol"><Icon name="heart" :size="24" weight="fill" /></span><span>Love<span class="brand-lab">Lab</span><small>恋爱练习场</small></span></RouterLink>
      <nav class="main-nav" aria-label="主导航">
        <RouterLink to="/" :class="{active: route.path === '/' || route.path === '/stories' || route.path.startsWith('/play') || route.path === '/daily'}">开玩</RouterLink>
        <RouterLink to="/handbook" :class="{active: route.path.startsWith('/handbook')}">恋爱手册</RouterLink>
        <RouterLink to="/me" :class="{active: route.path === '/me'}">我的练习</RouterLink>
      </nav>
      <div class="header-actions"><button class="icon-button theme-button" @click="toggleTheme" :aria-label="dark ? '切换浅色模式' : '切换深色模式'"><Icon :name="dark ? 'sun' : 'moon'" :size="19" /></button><RouterLink to="/me" class="session-avatar" aria-label="查看我的本地练习"><Icon name="dog" :size="23" /></RouterLink></div>
    </div>
  </header>
  <main id="main"><RouterView /></main>
  <footer class="site-footer"><div><RouterLink to="/" class="footer-brand">LoveLab<span>心动有时，练习有用。</span></RouterLink><p>把选择留给双方，把尴尬留在这里。</p></div><span>原创情景 · 真实表达 · 好好相处</span></footer>
  <Transition name="toast"><div v-if="progress.notice" class="toast" role="status"><Icon name="check" />{{ progress.notice }}</div></Transition>
  <div v-if="progress.storageError" class="storage-warning" role="alert">浏览器暂时无法保存记录，你仍可以继续玩。</div>
</template>
