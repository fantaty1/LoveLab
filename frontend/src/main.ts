import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import HomePage from './pages/HomePage.vue'
import './style.css'
import './tutorials.css'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomePage },
    { path: '/stories', component: () => import('./pages/StoriesPage.vue') },
    { path: '/play/:id', component: () => import('./pages/PlayPage.vue') },
    { path: '/daily', component: () => import('./pages/DailyPage.vue') },
    { path: '/handbook', component: () => import('./pages/HandbookPage.vue') },
    { path: '/handbook/:slug', component: () => import('./pages/TutorialPage.vue') },
    { path: '/me', component: () => import('./pages/MePage.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior(to, from, saved) { if (saved) return saved; if (to.hash) return { el: to.hash, top: 112 }; return to.path === from.path ? undefined : { top: 0 } },
})

createApp(App).use(createPinia()).use(router).mount('#app')
