import { readFile } from 'node:fs/promises'
const report = JSON.parse(await readFile('reports/lighthouse-mobile.json', 'utf8'))
const scores = Object.fromEntries(Object.entries(report.categories).map(([key, value]) => [key, Math.round(value.score * 100)]))
const metrics = Object.fromEntries(['largest-contentful-paint', 'cumulative-layout-shift', 'total-blocking-time'].map(key => [key, report.audits[key].displayValue]))
const failures = Object.values(report.audits).filter(audit => audit.score !== null && audit.score < 1 && audit.details?.items?.length).map(audit => ({ id: audit.id, title: audit.title, items: audit.details.items.slice(0, 3) }))
console.log(JSON.stringify({ scores, metrics, failures }, null, 2))
