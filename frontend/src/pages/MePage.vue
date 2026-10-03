<script setup lang="ts">
import { computed } from 'vue'
import { useProgress } from '../stores/progress'
import { stories, cards } from '../data/content'
import { tutorials, tutorialBookmark, topicById } from '../data/tutorials'
import Icon from '../components/Icon.vue'
const progress = useProgress()
const practiced = computed(() => stories.filter(story => progress.saved.runs[story.id]))
const favoriteCards = computed(() => cards.filter(card => progress.saved.favorites.includes(card.id)))
const favoriteTutorials = computed(() => tutorials.filter(article => progress.saved.favorites.includes(tutorialBookmark(article.slug))))
const readTutorials = computed(() => progress.saved.readArticles.map(slug => tutorials.find(article => article.slug === slug)).filter(article => article !== undefined).reverse())
</script>

<template>
  <div class="page-width subpage">
    <div class="my-intro"><span class="my-avatar"><Icon name="dog" :size="43" /></span><div><span class="page-overline">今天也在慢慢变自然</span><h1>我的练习</h1><p>进度保存在当前浏览器，清理浏览器数据后可能丢失。</p></div></div>
    <div class="my-stats"><div><strong>{{ progress.completed }}</strong><span>完成的小剧场</span></div><div><strong>{{ readTutorials.length }}</strong><span>读过的完整教程</span></div><div><strong>{{ favoriteCards.length + favoriteTutorials.length }}</strong><span>收藏的内容</span></div></div>
    <section class="my-section"><div class="my-reading-heading"><h2>收进手册的教程</h2><RouterLink to="/handbook" class="text-link">继续阅读<Icon name="right" :size="17" /></RouterLink></div><div v-if="favoriteTutorials.length" class="favorite-list"><RouterLink v-for="article in favoriteTutorials" :key="article.slug" :to="`/handbook/${article.slug}`"><Icon :name="topicById(article.topic).icon" :size="23" /><span>{{ article.title }}</span><Icon name="arrow" :size="19" /></RouterLink></div><div v-else class="empty-state compact"><Icon name="book" :size="31" /><p>读到想多看几次的方法，把完整教程收藏在这里。</p><RouterLink to="/handbook" class="text-link">看看系统教程<Icon name="right" :size="17" /></RouterLink></div></section>
    <section v-if="readTutorials.length" class="my-section"><div class="my-reading-heading"><h2>我的阅读足迹</h2><span>已读 {{ readTutorials.length }} / {{ tutorials.length }} 篇</span></div><div class="reading-history"><RouterLink v-for="article in readTutorials" :key="article.slug" :to="`/handbook/${article.slug}`"><Icon name="done" :size="20" /><span>{{ article.title }}</span><small>{{ topicById(article.topic).title }}</small><Icon name="arrow" :size="18" /></RouterLink></div></section>
    <section class="my-section"><h2>我的小剧场</h2><div v-if="practiced.length" class="progress-list"><RouterLink v-for="story in practiced" :key="story.id" :to="`/play/${story.id}`" class="progress-row"><img :src="story.image" alt="" width="96" height="72" /><div><small>{{ story.category }}</small><h3>{{ story.title }}{{ story.subtitle }}</h3><p>{{ progress.saved.runs[story.id]?.complete ? '已完成 · 可以重玩' : '尚未完成 · 接着上一幕' }}</p></div><Icon name="arrow" :size="22" /></RouterLink></div><div v-else class="empty-state"><Icon name="path" :size="35" /><h3>第一幕，还在等你</h3><p>不用准备得很完美，从一个小选择开始。</p><RouterLink to="/stories" class="button primary">选一个小剧场<Icon name="right" :size="18" /></RouterLink></div></section>
    <section class="my-section"><h2>收藏的随手知识卡</h2><div v-if="favoriteCards.length" class="favorite-list"><RouterLink v-for="card in favoriteCards" :key="card.id" :to="{ path: '/handbook', query: { card: card.id } }"><Icon :name="card.icon" :size="23" /><span>{{ card.title }}</span><Icon name="arrow" :size="19" /></RouterLink></div><div v-else class="empty-state compact"><Icon name="bookmark" :size="30" /><p>在小剧场或手册里，看到有用的知识卡就收起来。</p><RouterLink class="text-link" to="/handbook?view=cards">翻翻知识卡<Icon name="right" :size="17" /></RouterLink></div></section>
    <p class="save-note">你已练过今日题 {{ progress.saved.daily.length }} 天。阅读、收藏和练习都记录在当前浏览器。</p>
  </div>
</template>
