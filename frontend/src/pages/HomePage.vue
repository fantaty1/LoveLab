<script setup lang="ts">
import { computed } from 'vue'
import Icon from '../components/Icon.vue'
import StoryCard from '../components/StoryCard.vue'
import { stories, cards } from '../data/content'
import { useProgress } from '../stores/progress'
const progress = useProgress()
const tutorialForCard: Record<string, string> = { invite: 'clear-invitation', listen: 'emotional-validation', boundaries: 'say-no' }
const continued = computed(() => stories.find(story => progress.saved.runs[story.id] && !progress.saved.runs[story.id]?.complete))
</script>
<template>
  <div class="home page-width">
    <section class="hero">
      <div class="hero-copy"><div class="hero-label"><Icon name="sparkle" :size="17" />给第一次心动，留一点练习的余地</div><h1>恋爱这件事，<br /><span>先练练再上场。</span></h1><p class="hero-description">在游戏里先尴尬一次，<br />现实里，就能自然一点。</p><RouterLink class="button primary" :to="`/play/${continued?.id ?? 'coffee'}`"><Icon name="play" weight="fill" :size="17" />{{ continued ? '继续上次练习' : '开始试玩' }}<Icon name="right" :size="19" /></RouterLink></div>
      <div class="hero-art"><div class="illustration-mat"><img src="/images/cafe.webp" srcset="/images/cafe-600.webp 600w, /images/cafe.webp 1200w" sizes="(max-width: 720px) calc(100vw - 36px), 580px" alt="咖啡店里，紧张的男生与女生自然交流，一只小狗在桌下陪伴" width="1536" height="1024" fetchpriority="high" /><div class="art-bottom"><span><Icon name="coffee" :size="17" />第一次见面，脑子突然空白</span><span>这里可以重来 <Icon name="reset" :size="16" /></span></div></div></div>
    </section>
    <section class="how-it-works" aria-label="玩法说明"><div><span class="step-icon"><Icon name="path" :size="22" /></span><span><strong>选一选</strong><small>换你来决定怎么做</small></span></div><Icon class="step-arrow" name="right" :size="18" /><div><span class="step-icon"><Icon name="chat" :size="22" /></span><span><strong>看剧情</strong><small>每个选择，都有下一幕</small></span></div><Icon class="step-arrow" name="right" :size="18" /><div><span class="step-icon"><Icon name="bulb" :size="22" /></span><span><strong>懂一点</strong><small>把有用的带回现实</small></span></div></section>
    <section class="featured-section"><div class="section-heading"><h2>你的下一幕，<span>会怎样？</span></h2><p>轻松的小故事，认真一点的小选择。</p></div><div class="featured-grid"><StoryCard :story="stories[0]!" featured /><aside class="daily-card"><div class="daily-heading"><span><Icon name="sun" :size="19" />今日一题</span><span>约 1 分钟</span></div><div class="daily-quote"><span class="quote-mark">“</span><h3>帮我拿一下包嘛。</h3><p>顺手帮忙，还是保持边界？<br />这题真的只有一个答案吗？</p></div><div class="daily-note"><Icon name="dog" :size="29" /><span>军师提醒：<br />一个包，不是一份恋爱合同。</span></div><RouterLink to="/daily" class="button daily-button">我来选选<Icon name="right" :size="19" /></RouterLink></aside></div><RouterLink to="/stories" class="text-link more-stories">看看其他小剧场<Icon name="right" :size="18" /></RouterLink></section>
    <section class="handbook-preview"><div class="handbook-intro"><span class="outline-icon"><Icon name="book" :size="30" /></span><h2>心动之外，<br />也需要一点方法。</h2><p>不背套路，学着把话说清楚，<br />把人放在心上。</p><RouterLink to="/handbook" class="text-link">开始系统学习<Icon name="right" :size="18" /></RouterLink></div><div class="handbook-rows"><RouterLink v-for="card in [cards[4]!, cards[2]!, cards[9]!]" :key="card.id" :to="`/handbook/${tutorialForCard[card.id]}`" class="handbook-row"><div><small>{{ card.category }}</small><h3>{{ card.title }}</h3><p>{{ card.intro }}</p></div><Icon name="arrow" :size="22" /></RouterLink></div></section>
  </div>
</template>
