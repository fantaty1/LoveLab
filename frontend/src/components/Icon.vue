<script setup lang="ts">
import { computed } from 'vue'
import { PhArrowRight, PhArrowUpRight, PhArrowLeft, PhHeart, PhPlay, PhBookOpen, PhBookmarkSimple, PhCheck, PhX, PhClock, PhSparkle, PhSun, PhMoon, PhPath, PhArrowClockwise, PhChatCircle, PhQuestion, PhMedal, PhCoffee, PhConfetti, PhDog, PhCaretDown, PhList, PhLightbulb, PhCheckCircle } from '@phosphor-icons/vue'
const props = withDefaults(defineProps<{ name: string; size?: number; weight?: 'regular' | 'bold' | 'fill' | 'duotone' }>(), { size: 20, weight: 'regular' })
const icons: Record<string, typeof PhHeart> = { right: PhArrowRight, arrow: PhArrowUpRight, back: PhArrowLeft, heart: PhHeart, play: PhPlay, book: PhBookOpen, bookmark: PhBookmarkSimple, check: PhCheck, close: PhX, clock: PhClock, sparkle: PhSparkle, sun: PhSun, moon: PhMoon, path: PhPath, reset: PhArrowClockwise, chat: PhChatCircle, question: PhQuestion, medal: PhMedal, coffee: PhCoffee, confetti: PhConfetti, dog: PhDog, down: PhCaretDown, menu: PhList, bulb: PhLightbulb, done: PhCheckCircle }
const icon = computed(() => icons[props.name] ?? PhHeart)
</script>

<template><component :is="icon" :size="size" :weight="weight" aria-hidden="true" /></template>
