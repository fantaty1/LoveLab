import { mkdir, writeFile } from 'node:fs/promises'
import { stories, cards, dailyQuestion } from '../src/data/content.ts'
import { tutorials, topics, tutorialSources, tutorialMinutes } from '../src/data/tutorials.ts'

const out = new URL('../../content/', import.meta.url)
await mkdir(out, { recursive: true })
await writeFile(new URL('seed.json', out), JSON.stringify({
  formatVersion: 1, stories, cards,
  tutorials: tutorials.map(t => ({ ...t, minutes: tutorialMinutes(t) })),
  questions: [dailyQuestion], metadata: { topics, sources: tutorialSources },
}, null, 2) + '\n', 'utf8')
console.log(`导出 ${stories.length} 个故事、${cards.length} 张卡片、${tutorials.length} 篇教程与今日题到 content/seed.json。`)
