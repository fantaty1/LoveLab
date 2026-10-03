import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import type { Choice } from '../data/content'

type Run = { scene: string; choices: { scene: string; option: number; rating: string; skill: string }[]; complete: boolean }
type Saved = { runs: Record<string, Run>; favorites: string[]; daily: string[]; theme: string; readArticles: string[] }
const key = 'lovelab-preview-v1'
function load(): Saved {
  try { const saved = JSON.parse(localStorage.getItem(key) ?? '{}'); return { runs: saved.runs ?? {}, favorites: saved.favorites ?? [], daily: saved.daily ?? [], theme: saved.theme ?? 'system', readArticles: Array.isArray(saved.readArticles) ? saved.readArticles : [] } }
  catch { return { runs: {}, favorites: [], daily: [], theme: 'system', readArticles: [] } }
}
export const useProgress = defineStore('progress', () => {
  const saved = ref(load())
  const notice = ref('')
  const storageError = ref(false)
  let noticeTimer: ReturnType<typeof setTimeout>
  watch(saved, value => { try { localStorage.setItem(key, JSON.stringify(value)); storageError.value = false } catch { storageError.value = true } }, {deep: true})
  const completed = computed(() => Object.values(saved.value.runs).filter(run => run.complete).length)
  const skills = computed(() => [...new Set(Object.values(saved.value.runs).flatMap(run => run.choices.map(c => c.skill)))])
  function toast(text: string) { notice.value = text; clearTimeout(noticeTimer); noticeTimer = setTimeout(() => notice.value = '', 2800) }
  function start(id: string, first: string, restart = false) { if (restart || !saved.value.runs[id]) saved.value.runs[id] = {scene: first, choices: [], complete: false} }
  function choose(id: string, scene: string, index: number, choice: Choice) {
    const run = saved.value.runs[id]; if (!run) return
    run.choices = [...run.choices.filter(c => c.scene !== scene), {scene, option: index, rating: choice.rating, skill: choice.skill}]
  }
  function advance(id: string, next?: string) { const run = saved.value.runs[id]; if (!run) return; if (next) run.scene = next; else run.complete = true }
  function toggleFavorite(id: string) { const idx = saved.value.favorites.indexOf(id); if (idx >= 0) saved.value.favorites.splice(idx, 1); else saved.value.favorites.push(id); toast(idx >= 0 ? '已取消收藏' : '已收进你的恋爱手册') }
  function toggleRead(slug: string) { const idx = saved.value.readArticles.indexOf(slug); if (idx >= 0) saved.value.readArticles.splice(idx, 1); else saved.value.readArticles.push(slug); toast(idx >= 0 ? '已取消阅读标记' : '已记下这篇教程') }
  return {saved, completed, skills, notice, storageError, toast, start, choose, advance, toggleFavorite, toggleRead}
})
