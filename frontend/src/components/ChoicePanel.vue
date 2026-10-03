<script setup lang="ts">
import type { Choice, Scene } from '../data/content'
import { computed, ref, watch } from 'vue'
import Icon from './Icon.vue'
import { useProgress } from '../stores/progress'
const props = defineProps<{ scene: Scene; selected?: number; final?: boolean }>()
const emit = defineEmits<{ choose: [index: number]; next: [] }>()
const expanded = ref(false)
const progress = useProgress()
const choice = computed<Choice | undefined>(() => props.selected === undefined ? undefined : props.scene.choices[props.selected])
watch(() => props.scene.id, () => expanded.value = false)
</script>
<template><div class="choice-panel"><div class="choice-heading"><span><Icon name="path" :size="18" />换你来接这一句</span><small>{{ choice ? '看看这个选择会怎样' : '选一个你会说的' }}</small></div><div class="choices"><button v-for="(option,index) in scene.choices" :key="option.text" class="choice" :class="{chosen:selected===index, muted:selected!==undefined&&selected!==index}" :disabled="selected!==undefined" @click="emit('choose',index)"><span class="option-letter">{{ String.fromCharCode(65+index) }}</span><span>{{ option.text }}</span><Icon v-if="selected===index" name="check" :size="20" /><Icon v-else name="arrow" :size="18" /></button></div><Transition name="feedback"><section v-if="choice" class="feedback-card" aria-live="polite"><div class="feedback-header"><span class="feedback-icon"><Icon name="bulb" :size="23" /></span><div><small>军师来拆解</small><h3>{{ choice.rating }}</h3></div><button class="icon-button" :aria-label="progress.saved.favorites.includes(choice.card)?'取消收藏知识卡':'收藏知识卡'" :aria-pressed="progress.saved.favorites.includes(choice.card)" @click="progress.toggleFavorite(choice.card)"><Icon name="bookmark" :weight="progress.saved.favorites.includes(choice.card)?'fill':'regular'" /></button></div><p class="response">{{ choice.response }}</p><p>{{ choice.explanation }}</p><button class="text-link explanation-toggle" @click="expanded=!expanded" :aria-expanded="expanded">{{ expanded ? '收起说明' : '为什么这样判断' }}<Icon name="down" :size="16" /></button><div v-if="expanded" class="evidence-note">这是虚构情景中的一种后续。我们评价的是表达和行为，无法据此预测某个人会不会喜欢你。<RouterLink :to="{path:'/handbook',query:{card:choice.card}}">查看相关知识卡</RouterLink></div><button class="button primary feedback-continue" @click="emit('next')">{{ final ? '看本局复盘' : '继续下一幕' }}<Icon name="right" :size="18" /></button></section></Transition></div></template>
