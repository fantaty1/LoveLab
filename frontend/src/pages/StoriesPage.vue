<script setup lang="ts">
import { ref, computed } from 'vue'
import { stories } from '../data/content'
import StoryCard from '../components/StoryCard.vue'
const filter = ref('全部剧场')
const categories = ['全部剧场', '初次认识', '约会相处', '聊天沟通']
const visible = computed(() => stories.filter(s => filter.value === '全部剧场' || s.category === filter.value))
</script>
<template><div class="page-width subpage"><div class="page-heading"><span class="page-overline">换个情景，试试另一个自己</span><h1>心动小剧场</h1><p>有点好笑，有点尴尬，也有点值得认真。</p></div><div class="filter-tabs" aria-label="剧场分类"><button v-for="category in categories" :key="category" :class="{selected:filter===category}" :aria-pressed="filter===category" @click="filter=category">{{ category }}</button></div><div class="story-grid"><StoryCard v-for="story in visible" :key="story.id" :story="story" /></div><div class="quiet-note">每一次选择，都可以再试一次。剧情是虚构的，表达可以是真诚的。</div></div></template>
