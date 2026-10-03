<script setup lang="ts">
import { computed, ref } from 'vue'
import { topics, tutorials, topicById, tutorialBookmark, tutorialMinutes } from '../data/tutorials'
import { useProgress } from '../stores/progress'
import Icon from './Icon.vue'
const progress = useProgress()
const filter = ref('all')
const search = ref('')
const onlySaved = ref(false)
const featured = tutorials[0]!
const visible = computed(() => tutorials.filter(article => {
  const matchesTopic = filter.value === 'all' || article.topic === filter.value
  const matchesSaved = !onlySaved.value || progress.saved.favorites.includes(tutorialBookmark(article.slug))
  const words = `${article.title} ${article.description} ${article.tags.join(' ')} ${article.sections.flatMap(s => s.paragraphs).join(' ')}`
  return matchesTopic && matchesSaved && words.includes(search.value.trim())
}))
function reset() { filter.value = 'all'; search.value = ''; onlySaved.value = false }
</script>

<template>
  <section class="library-hero">
    <div class="library-hero-copy"><span class="page-overline">恋爱手册 · 读一点，懂一点</span><h1>把喜欢说清楚，<br /><span>把相处练自然。</span></h1><p>聊天怎么接，约会怎么约，靠近怎么有分寸。<br />从情绪认同开始，学会认真地喜欢一个人。</p><div class="library-facts"><span>{{ tutorials.length }} 篇教程</span><span>6 个主题</span><span>每篇都有小练习</span></div></div>
    <RouterLink :to="`/handbook/${featured.slug}`" class="featured-reading"><div class="reading-cover-heading"><span><Icon name="heart" :size="18" />从这篇开始</span><span>约 {{ tutorialMinutes(featured) }} 分钟</span></div><h2>她说「今天好烦」，<br />先别急着当人生导师。</h2><p>先接住情绪，再确认需要。<br />一篇读懂：认同、附和和给建议的区别。</p><span class="reading-cover-link">读读怎么做<Icon name="right" :size="20" /></span></RouterLink>
  </section>
  <section class="reading-path" aria-label="推荐入门顺序"><div><Icon name="path" :size="23" /><span><strong>第一次来？按这个顺序读</strong><small>先会倾听，再会聊天，最后把邀约说清楚。</small></span></div><div class="reading-path-links"><RouterLink to="/handbook/emotional-validation">1 情绪认同</RouterLink><Icon name="right" :size="15" /><RouterLink to="/handbook/two-way-conversation">2 双向聊天</RouterLink><Icon name="right" :size="15" /><RouterLink to="/handbook/clear-invitation">3 具体邀约</RouterLink></div></section>
  <div class="library-toolbar"><div class="filter-tabs" aria-label="教程主题"><button :class="{ selected: filter === 'all' }" :aria-pressed="filter === 'all'" @click="filter = 'all'">全部主题</button><button v-for="topic in topics" :key="topic.id" :class="{ selected: filter === topic.id }" :aria-pressed="filter === topic.id" @click="filter = topic.id">{{ topic.title }}</button></div><label class="tutorial-search"><span class="sr-only">搜索教程</span><input v-model="search" class="search-input" type="search" placeholder="搜索情绪、牵手、约会…" /></label></div>
  <div class="library-results-heading"><div><h2>{{ filter === 'all' ? '随时可以翻一翻' : topics.find(topic => topic.id === filter)?.title }}</h2><span role="status">{{ visible.length }} 篇{{ onlySaved ? '收藏' : '' }}教程</span></div><button class="text-link" :class="{ 'saved-filter': onlySaved }" :aria-pressed="onlySaved" @click="onlySaved = !onlySaved"><Icon name="bookmark" :size="18" :weight="onlySaved ? 'fill' : 'regular'" />只看收藏</button></div>
  <div v-if="visible.length" class="tutorial-list"><article v-for="article in visible" :key="article.slug" class="tutorial-row"><span class="tutorial-topic-icon" :class="article.topic"><Icon :name="topicById(article.topic).icon" :size="25" /></span><RouterLink :to="`/handbook/${article.slug}`" class="tutorial-row-body"><div class="tutorial-row-meta"><span>{{ topicById(article.topic).title }}</span><span>{{ tutorialMinutes(article) }} 分钟</span><span v-if="progress.saved.readArticles.includes(article.slug)" class="read-label"><Icon name="check" :size="13" />已读</span></div><h3>{{ article.title }}</h3><p>{{ article.description }}</p><div class="tutorial-tags"><span v-for="tag in article.tags" :key="tag">{{ tag }}</span></div></RouterLink><div class="tutorial-row-actions"><button class="icon-button" :aria-label="`${progress.saved.favorites.includes(tutorialBookmark(article.slug)) ? '取消收藏教程' : '收藏教程'}：${article.title}`" :aria-pressed="progress.saved.favorites.includes(tutorialBookmark(article.slug))" @click="progress.toggleFavorite(tutorialBookmark(article.slug))"><Icon name="bookmark" :size="20" :weight="progress.saved.favorites.includes(tutorialBookmark(article.slug)) ? 'fill' : 'regular'" /></button><RouterLink :to="`/handbook/${article.slug}`" class="icon-button" :aria-label="`阅读：${article.title}`"><Icon name="arrow" :size="21" /></RouterLink></div></article></div>
  <div v-else class="empty-state"><Icon name="book" :size="35" /><h3>{{ onlySaved ? '这个范围里还没有收藏' : '暂时没找到这篇教程' }}</h3><p>试试其他关键词或主题，也可以查看全部教程。</p><button class="button secondary" @click="reset">查看全部教程</button></div>
  <p class="library-editor-note"><Icon name="book" :size="17" />教程结合狗头军师知识库 与公开关系教育资料整理。具体场景和对话示例由 LoveLab 编写，每篇末尾可查看参考来源。</p>
</template>
