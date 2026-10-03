<script setup lang="ts">
import type { Story } from '../data/content'
import Icon from './Icon.vue'
defineProps<{ story: Story; featured?: boolean }>()
</script>
<template>
  <RouterLink :to="`/play/${story.id}`" class="story-card" :class="[story.tint, {featured}]">
    <div class="story-picture"><img :src="story.image" :srcset="`${story.image.replace('.webp', '-600.webp')} 600w, ${story.image} 1200w`" sizes="(max-width: 720px) calc(100vw - 36px), 600px" :alt="`${story.character}的约会情景插画`" loading="lazy" width="800" height="530" /></div>
    <div class="story-info"><div class="story-meta"><span>{{ story.category }}</span><span><Icon name="clock" :size="15" />{{ story.duration }}</span></div><div class="story-title-row"><h3>{{ story.title }}<br />{{ story.subtitle }}</h3><span class="circle-arrow"><Icon name="arrow" :size="23" /></span></div><p>{{ story.description }}</p></div>
  </RouterLink>
</template>
