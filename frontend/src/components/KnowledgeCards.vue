<script setup lang="ts">
import { computed, nextTick, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { cards } from '../data/content'
import { useProgress } from '../stores/progress'
import Icon from '../components/Icon.vue'
const progress=useProgress(); const route=useRoute(); const router=useRouter(); const filter=ref('全部'); const search=ref('')
const categories=['全部','自然认识','聊天沟通','邀约表达','约会相处','关心与边界','判断与修复']
const visible=computed(()=>cards.filter(card=>(filter.value==='全部'||card.category===filter.value)&&(card.title+card.intro+card.category).includes(search.value.trim())))
const active=computed(()=>cards.find(c=>c.id===route.query.card))
let previousFocus: HTMLElement | null = null
watch(active, async value => {
  if (value) {
    filter.value = '全部'
    previousFocus = document.activeElement instanceof HTMLElement ? document.activeElement : null
    document.body.style.overflow = 'hidden'
    await nextTick()
    document.querySelector<HTMLElement>('.card-modal')?.focus()
  } else {
    document.body.style.overflow = ''
    previousFocus?.focus()
  }
}, { immediate: true })
function trapFocus(event: KeyboardEvent) {
  if (event.key !== 'Tab') return
  const dialog = document.querySelector<HTMLElement>('.card-modal')
  const elements = dialog?.querySelectorAll<HTMLElement>('button, a[href], input, [tabindex="0"]')
  if (!elements?.length) return
  const first = elements[0]!, last = elements[elements.length - 1]!
  if (event.shiftKey && (document.activeElement === first || document.activeElement === dialog)) {
    event.preventDefault(); last.focus()
  } else if (!event.shiftKey && (document.activeElement === last || document.activeElement === dialog)) {
    event.preventDefault(); first.focus()
  }
}
window.addEventListener('keydown', trapFocus)
onUnmounted(() => { document.body.style.overflow = ''; window.removeEventListener('keydown', trapFocus) })
function open(id: string) {router.replace({path:'/handbook',query:{view:'cards',card:id}})}
function close() {router.replace({path:'/handbook',query:{view:'cards'}})}
</script>
<template><div class="page-width subpage"><div class="page-heading"><span class="page-overline">不背套路，好好表达</span><h1>随手翻翻的知识卡</h1><p>每张卡，只讲一件能用上的小事。</p></div><div class="handbook-controls"><div class="filter-tabs" aria-label="知识卡分类"><button v-for="category in categories" :key="category" :class="{selected:filter===category}" :aria-pressed="filter===category" @click="filter=category">{{ category }}</button></div><input v-model="search" class="search-input" type="search" placeholder="找找你想练的内容" aria-label="搜索知识卡" /></div><div class="knowledge-grid"><article v-for="card in visible" :key="card.id" class="knowledge-card"><div class="knowledge-top"><span class="knowledge-icon"><Icon :name="card.icon" :size="25" /></span><button class="icon-button" :aria-label="progress.saved.favorites.includes(card.id)?`取消收藏${card.title}`:`收藏${card.title}`" :aria-pressed="progress.saved.favorites.includes(card.id)" @click="progress.toggleFavorite(card.id)"><Icon name="bookmark" :weight="progress.saved.favorites.includes(card.id)?'fill':'regular'" /></button></div><small>{{ card.category }}</small><h2><button @click="open(card.id)">{{ card.title }}</button></h2><p>{{ card.intro }}</p><button class="text-link" @click="open(card.id)">读这张卡<Icon name="right" :size="17" /></button></article></div><div v-if="!visible.length" class="empty-state"><Icon name="book" :size="40" /><h2>暂时没找到这张卡</h2><p>换个关键词，或看看其他分类。</p><button class="button secondary" @click="search='';filter='全部'">查看全部知识卡</button></div></div><Teleport to="body"><div v-if="active" class="modal-backdrop" @click.self="close" @keydown.esc="close"><section class="card-modal" role="dialog" aria-modal="true" :aria-label="active.title" tabindex="-1"><div class="modal-top"><span><Icon :name="active.icon" :size="25" />{{ active.category }}</span><button class="icon-button" aria-label="关闭知识卡" @click="close"><Icon name="close" :size="23" /></button></div><h2>{{ active.title }}</h2><p class="modal-intro">{{ active.intro }}</p><h3>试着这样做</h3><p>{{ active.action }}</p><blockquote>“{{ active.example }}”</blockquote><h3>也记得留意</h3><p>{{ active.caution }}</p><a class="source-link" :href="active.url" target="_blank" rel="noopener noreferrer">{{ active.source }}<Icon name="arrow" :size="16" /></a><button class="button primary" @click="progress.toggleFavorite(active.id)"><Icon name="bookmark" :size="19" :weight="progress.saved.favorites.includes(active.id)?'fill':'regular'" />{{ progress.saved.favorites.includes(active.id)?'已收藏，点击取消':'收进我的手册' }}</button></section></div></Teleport></template>
