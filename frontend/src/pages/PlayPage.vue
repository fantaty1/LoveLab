<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { stories } from '../data/content'
import { useProgress } from '../stores/progress'
import Icon from '../components/Icon.vue'
import ChoicePanel from '../components/ChoicePanel.vue'
const route = useRoute(); const router = useRouter(); const progress = useProgress()
const story = computed(() => stories.find(s => s.id===route.params.id))
const run = computed(() => progress.saved.runs[String(route.params.id)])
watch(story, value => { if (value) progress.start(value.id, value.scenes[0]!.id); else router.replace('/stories') }, {immediate:true})
const scene = computed(() => story.value?.scenes.find(s=>s.id===run.value?.scene) ?? story.value?.scenes[0])
const selected = computed(() => run.value?.choices.find(c=>c.scene===scene.value?.id)?.option)
const index = computed(() => story.value?.scenes.findIndex(s=>s.id===scene.value?.id) ?? 0)
const last = computed(() => selected.value!==undefined && !scene.value?.choices[selected.value]?.next)
function choose(option: number) { if(story.value&&scene.value) progress.choose(story.value.id,scene.value.id,option,scene.value.choices[option]!) }
function next() { if(story.value&&scene.value&&selected.value!==undefined) progress.advance(story.value.id,scene.value.choices[selected.value]!.next) }
function restart() { if(story.value) progress.start(story.value.id,story.value.scenes[0]!.id,true) }
</script>
<template><div v-if="story&&scene&&run" class="page-width play-page"><div class="play-top"><RouterLink to="/stories" class="text-link"><Icon name="back" :size="19" />返回剧场</RouterLink><span>{{ story.category }}<span class="divider">/</span>{{ run.complete ? '本局完成' : `第 ${index+1} 幕 · 共 ${story.scenes.length} 幕` }}</span><button class="text-link" @click="restart"><Icon name="reset" :size="17" />重新开始</button></div><template v-if="!run.complete"><div class="play-title"><h1>{{ story.title }}{{ story.subtitle }}</h1><p>{{ scene.context }}</p></div><div class="game-grid"><section class="scene-stage" :class="story.tint"><img :src="story.image" :srcset="`${story.image.replace('.webp', '-600.webp')} 600w, ${story.image} 1200w`" sizes="(max-width: 720px) calc(100vw - 36px), 600px" :alt="`${story.character}和你交流的虚构情景`" width="1536" height="1024" /><div class="dialogue"><span class="character-name">{{ scene.speaker }}</span><h2>“{{ scene.line }}”</h2></div><div class="narrator"><Icon name="dog" :size="25" /><p>{{ scene.aside }}</p></div></section><ChoicePanel :key="scene.id" :scene="scene" :selected="selected" :final="last" @choose="choose" @next="next" /></div><p class="save-note"><Icon name="done" :size="15" />进度已保存在本机浏览器，你随时可以回来继续。</p></template><section v-else class="result-page"><span class="result-medal"><Icon name="confetti" :size="44" weight="duotone" /></span><span class="page-overline">这一幕，你认真试过了</span><h1>不用完美，<br /><span>自然一点就很好。</span></h1><p>你完成了「{{ story.title }}{{ story.subtitle }}」</p><div class="result-grid"><article><Icon name="medal" :size="27" /><h3>这次练过的能力</h3><div class="skill-tags"><span v-for="skill in [...new Set(run.choices.map(c=>c.skill))]" :key="skill">{{ skill }}</span></div></article><article><Icon name="bulb" :size="27" /><h3>把一个小动作带回现实</h3><p>下一次交流，先接住对方说的内容，再分享一点自己的真实感受。</p></article></div><div class="result-actions"><button class="button primary" @click="restart"><Icon name="reset" :size="18" />换个选择再试</button><RouterLink class="button secondary" to="/stories">去其他小剧场<Icon name="right" :size="18" /></RouterLink></div><p class="quiet-note">结局属于这个故事，现实中的选择属于双方。</p></section></div></template>
