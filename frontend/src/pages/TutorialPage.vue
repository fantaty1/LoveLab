<script setup lang="ts">
import { computed, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { tutorials, tutorialSources, topicById, tutorialBookmark, tutorialMinutes } from '../data/tutorials'
import { useProgress } from '../stores/progress'
import Icon from '../components/Icon.vue'
const route = useRoute()
const progress = useProgress()
const article = computed(() => tutorials.find(item => item.slug === route.params.slug))
const related = computed(() => article.value?.related.map(slug => tutorials.find(item => item.slug === slug)).filter(item => item !== undefined) ?? [])
const isSaved = computed(() => article.value ? progress.saved.favorites.includes(tutorialBookmark(article.value.slug)) : false)
const isRead = computed(() => article.value ? progress.saved.readArticles.includes(article.value.slug) : false)
watch(article, value => { document.title = value ? `${value.title} · LoveLab 恋爱手册` : '教程未找到 · LoveLab' }, { immediate: true, flush: 'post' })
onUnmounted(() => document.title = 'LoveLab · 恋爱练习场')
</script>

<template>
  <div v-if="article" class="page-width tutorial-page">
    <div class="article-topline"><RouterLink to="/handbook" class="text-link"><Icon name="back" :size="18" />回到恋爱手册</RouterLink><span>{{ topicById(article.topic).title }}<span class="divider">/</span>完整教程</span></div>
    <div class="article-layout">
      <aside class="article-sidebar"><nav aria-label="本篇目录"><span class="article-directory-label">这篇读什么</span><a href="#article-start">先记住这一点</a><a v-for="section in article.sections" :key="section.id" :href="`#${section.id}`">{{ section.title }}</a><a href="#example">看看怎么说</a><a href="#pitfalls">常见误区</a><a href="#practice">试着练一下</a><a href="#sources">参考与延伸阅读</a></nav><div class="article-sidebar-note"><Icon :name="topicById(article.topic).icon" :size="24" /><span>{{ topicById(article.topic).description }}</span><small>一篇只练一个小动作。<br />下次交流时，试着用一次。</small></div></aside>
      <article class="article-document">
        <header class="article-heading"><div class="article-meta"><span>{{ topicById(article.topic).title }}</span><span><Icon name="clock" :size="15" />约 {{ tutorialMinutes(article) }} 分钟</span><span v-if="isRead" class="read-label"><Icon name="check" :size="14" />已读</span></div><h1>{{ article.title }}</h1><p>{{ article.description }}</p><div class="article-actions"><button class="button secondary" :aria-pressed="isSaved" @click="progress.toggleFavorite(tutorialBookmark(article.slug))"><Icon name="bookmark" :size="18" :weight="isSaved ? 'fill' : 'regular'" />{{ isSaved ? '已收藏教程' : '收藏这篇教程' }}</button><span>适合：{{ article.stage }}</span></div></header>
        <details class="mobile-article-directory"><summary><Icon name="menu" :size="19" />本篇目录</summary><a href="#article-start">先记住这一点</a><a v-for="section in article.sections" :key="section.id" :href="`#${section.id}`">{{ section.title }}</a><a href="#example">看看怎么说</a><a href="#pitfalls">常见误区</a><a href="#practice">试着练一下</a><a href="#sources">参考与延伸阅读</a></details>
        <section id="article-start" class="article-takeaway"><Icon name="bulb" :size="24" /><div><h2>先记住这一点</h2><p>{{ article.takeaway }}</p></div></section>
        <section v-for="section in article.sections" :id="section.id" :key="section.id" class="article-section"><h2>{{ section.title }}</h2><p v-for="paragraph in section.paragraphs" :key="paragraph">{{ paragraph }}</p><ol v-if="section.steps" class="article-steps"><li v-for="step in section.steps" :key="step">{{ step }}</li></ol></section>
        <section id="example" class="article-section"><h2>看看怎么说</h2><p class="example-context">{{ article.example.situation }}</p><div class="article-example"><div class="example-before"><span><Icon name="close" :size="18" />容易踩雷</span><p>{{ article.example.before }}</p></div><div class="example-after"><span><Icon name="check" :size="18" />可以试试</span><p>{{ article.example.after }}</p></div></div><p class="example-reason">{{ article.example.explanation }}</p><small class="example-note">对话是原创示例。改成你的口吻，只说你真心认同、也做得到的话。</small></section>
        <section id="pitfalls" class="article-section"><h2>这些误区，留意一下</h2><ul class="article-pitfalls"><li v-for="pitfall in article.pitfalls" :key="pitfall"><Icon name="question" :size="18" /><span>{{ pitfall }}</span></li></ul></section>
        <section id="practice" class="article-practice article-section"><div class="practice-heading"><Icon name="path" :size="22" /><span>读完，试着练一下</span></div><h2>{{ article.practice.prompt }}</h2><details :key="article.slug"><summary>先自己想一句，再看参考表达<Icon name="down" :size="17" /></summary><div class="practice-answer"><p>{{ article.practice.answer }}</p><small>{{ article.practice.reason }}</small></div></details></section>
        <section id="sources" class="article-sources article-section"><h2>参考与延伸阅读</h2><p>教学框架参考以下资料，场景、对话和练习由 LoveLab 重新编写。标注为作者经验的内容用于提供思路，不代表适合所有人的定律。</p><div v-for="ref in article.refs" :key="ref.source" class="article-source"><div><span>{{ tutorialSources[ref.source]?.kind }}</span><a :href="tutorialSources[ref.source]?.url" target="_blank" rel="noopener noreferrer">{{ tutorialSources[ref.source]?.title }}<Icon name="arrow" :size="15" /></a><p>{{ ref.note }}</p></div></div><p class="article-review-note">资料查阅：2026-10-03 · 内容整理：LoveLab<br />部分网络来源面向已有伴侣；这里针对初识与约会的情景，是本站的教学改编，不保证特定对象的回应。</p></section>
        <div class="article-completion"><div><Icon :name="isRead ? 'done' : 'book'" :size="26" /><span><strong>{{ isRead ? '这篇，你已经读过了' : '读完这篇，带走一个小动作' }}</strong><small>阅读标记和收藏会保存在当前浏览器。</small></span></div><button class="button primary" :aria-pressed="isRead" @click="progress.toggleRead(article.slug)">{{ isRead ? '取消已读标记' : '标记为已读' }}<Icon name="check" :size="18" /></button></div>
        <section class="article-related"><h2>接着读一点</h2><RouterLink v-for="item in related" :key="item.slug" :to="`/handbook/${item.slug}`"><div><small>{{ topicById(item.topic).title }} · {{ tutorialMinutes(item) }} 分钟</small><h3>{{ item.title }}</h3></div><Icon name="arrow" :size="20" /></RouterLink></section>
        <RouterLink v-if="article.story" :to="`/play/${article.story}`" class="article-game-link"><Icon name="play" :size="18" weight="fill" />读懂了？去小剧场里选一次<Icon name="right" :size="19" /></RouterLink>
      </article>
    </div>
  </div>
  <div v-else class="page-width subpage"><div class="empty-state"><Icon name="book" :size="40" /><h1>这篇教程还没找到</h1><p>可以回到手册，换一篇读读。</p><RouterLink to="/handbook" class="button primary">回到恋爱手册</RouterLink></div></div>
</template>
