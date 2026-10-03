<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TutorialLibrary from '../components/TutorialLibrary.vue'
import KnowledgeCards from '../components/KnowledgeCards.vue'
import Icon from '../components/Icon.vue'
const route = useRoute()
const showCards = computed(() => route.query.view === 'cards' || Boolean(route.query.card))
</script>
<template>
  <div class="page-width handbook-page" :class="{ 'cards-mode': showCards }">
    <nav class="handbook-mode-nav" aria-label="手册阅读方式">
      <RouterLink to="/handbook" :class="{ current: !showCards }" :aria-current="!showCards ? 'page' : undefined"><Icon name="book" :size="18" />系统教程</RouterLink>
      <RouterLink to="/handbook?view=cards" :class="{ current: showCards }" :aria-current="showCards ? 'page' : undefined"><Icon name="bulb" :size="18" />随手知识卡</RouterLink>
    </nav>
    <TutorialLibrary v-if="!showCards" />
  </div>
  <KnowledgeCards v-if="showCards" />
</template>
