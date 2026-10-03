import assert from 'node:assert/strict'
import { tutorials, topics, tutorialSources } from '../src/data/tutorials.ts'
import { stories } from '../src/data/content.ts'

assert.equal(new Set(tutorials.map(a => a.slug)).size, tutorials.length, '教程路径不可重复')
assert.equal(tutorials.length, 20, '首批教程数量')
for (const topic of topics) assert.ok(tutorials.some(a => a.topic === topic.id), `空主题：${topic.id}`)
for (const article of tutorials) {
  assert.ok(topics.some(topic => topic.id === article.topic), `${article.slug} 主题不存在`)
  const ids = [...article.sections.map(section => section.id), 'article-start', 'example', 'pitfalls', 'practice', 'sources']
  assert.equal(new Set(ids).size, ids.length, `${article.slug} 目录锚点重复`)
  assert.ok(article.sections.length >= 3 && article.pitfalls.length >= 3, `${article.slug} 正文或误区不足`)
  const text = article.sections.flatMap(section => [...section.paragraphs, ...(section.steps ?? [])]).join('')
  assert.ok(text.length >= 350, `${article.slug} 正文过短`)
  assert.ok(Object.values(article.example).every(Boolean), `${article.slug} 示例不完整`)
  assert.ok(Object.values(article.practice).every(Boolean), `${article.slug} 练习不完整`)
  for (const ref of article.refs) {
    assert.ok(tutorialSources[ref.source], `${article.slug} 引用不存在：${ref.source}`)
    assert.ok(ref.note.length > 5, `${article.slug} 引用缺少适用说明`)
    assert.equal(new URL(tutorialSources[ref.source].url).protocol, 'https:')
  }
  for (const related of article.related) assert.ok(tutorials.some(a => a.slug === related && a.slug !== article.slug), `${article.slug} 关联教程失效：${related}`)
  if (article.story) assert.ok(stories.some(story => story.id === article.story), `${article.slug} 关联剧场失效`)
}
console.log(`${tutorials.length} 篇教程、${topics.length} 个主题：路径、目录、正文、引用、练习与关联入口检查通过。`)
