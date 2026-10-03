import sharp from 'sharp'
import { mkdir, copyFile } from 'node:fs/promises'
import { resolve } from 'node:path'

const [name, source] = process.argv.slice(2)
if (!name || !source || !['cafe', 'park', 'listening'].includes(name)) throw new Error('Usage: node scripts/prepare-assets.mjs cafe|park|listening source.png')
await mkdir(resolve('public/images'), { recursive: true })
await mkdir(resolve('assets/source'), { recursive: true })
if (resolve(source) !== resolve(`assets/source/${name}.png`)) await copyFile(source, resolve(`assets/source/${name}.png`))
const result = await sharp(source).resize({ width: 1200, withoutEnlargement: true }).webp({ quality: 83 }).toFile(resolve(`public/images/${name}.webp`))
await sharp(source).resize({ width: 600 }).webp({ quality: 81 }).toFile(resolve(`public/images/${name}-600.webp`))
console.log(`${name}: ${result.width} × ${result.height}, ${Math.round(result.size / 1024)} KB`)
