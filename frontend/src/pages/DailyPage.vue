<script setup lang="ts">
import { ref } from 'vue'
import Icon from '../components/Icon.vue'
import ChoicePanel from '../components/ChoicePanel.vue'
import { dailyQuestion } from '../data/content'
import { useProgress } from '../stores/progress'
const selected = ref<number>(); const finished = ref(false); const progress = useProgress()
const day = new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Shanghai'}).format(new Date())
function complete() { finished.value=true; if(!progress.saved.daily.includes(day)) progress.saved.daily.push(day) }
function retry() { selected.value=undefined; finished.value=false }
</script>
<template><div class="page-width daily-page"><RouterLink class="text-link" to="/"><Icon name="back" :size="18" />回到开玩</RouterLink><div class="page-heading"><span class="page-overline"><Icon name="sun" :size="18" />每天一点，刚刚好</span><h1>今日心动题</h1><p>不用交卷，不怕选错。先试试你会怎么做。</p></div><div v-if="!finished" class="daily-game"><div class="daily-scene"><span class="soft-label">{{ dailyQuestion.context }}</span><h2>“{{ dailyQuestion.line }}”</h2><p>{{ dailyQuestion.aside }}</p><div class="narrator"><Icon name="dog" :size="26" /><span>军师提醒：体贴和边界，都可以好好表达。</span></div></div><ChoicePanel :scene="dailyQuestion" :selected="selected" final @choose="value=>selected=value" @next="complete" /></div><div v-else class="daily-finished"><Icon name="done" :size="48" /><h2>今天这道题，练过了。</h2><p>顺手帮忙可以很自然，清楚拒绝也可以被尊重。<br />重点是双方的意愿与表达。</p><div class="result-actions"><button class="button primary" @click="retry">换个选择试试<Icon name="reset" :size="18" /></button><RouterLink class="button secondary" to="/stories">去玩小剧场<Icon name="right" :size="18" /></RouterLink></div></div></div></template>
