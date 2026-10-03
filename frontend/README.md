# LoveLab 前端试玩

Vue 3 + TypeScript + Vite + Pinia + Vue Router，样式使用 Tailwind CSS 4 和项目设计变量，图标采用 Phosphor。当前版本用于验证视觉设计与互动体验。

## 本地运行

需要 Node.js 22 和 npm。在本目录执行：

```powershell
npm install
npm run dev
```

开发地址：`http://127.0.0.1:5173`。

```powershell
npm run build
npm run preview
```

生产构建预览地址：`http://127.0.0.1:4173`。端口已占用时需先结束之前的对应服务。

## 已实现

- 首页、剧场列表、剧情选择、即时拆解、章节推进、结局复盘与重玩。
- 三个示例故事，共九幕，围绕认识、约会安排与倾听。
- 一道今日题示例，以及按上海日期去重的练习天数记录。
- 十二张手册卡，支持分类、搜索、详情、来源链接与收藏。
- 二十篇完整教程、六个主题，支持搜索、收藏、阅读目录、展开练习答案、主动标记已读和关联剧场；资料取用见 `../docs/Tutorial-Content.md`。
- 本地练习进度、刷新续玩、收藏列表，浅色与深色模式。
- 手机适配、键盘焦点、弹窗焦点限制、减少动态效果的偏好支持。

当前数据位于 `src/data/content.ts`，用户记录存于当前浏览器的 localStorage。正式后端、账户同步、题目轮换、支付与内容管理尚未接入；三个故事主要沿章节推进，以不同反馈验证选择体验，后续按 PRD 拓展独立剧情分支。每日题暂时使用同一道示例。

## 代码位置

- `src/pages/`：页面。
- `src/components/`：选项面板、图标和故事卡。
- `src/stores/progress.ts`：本地记录，可在接入后端时替换持久化层。
- `src/style.css`：配色、排版、响应式与动态效果。
- `src/data/tutorials.ts`：系统教程正文、主题与来源；`src/tutorials.css`：长文阅读样式。
- `public/images/`：网页插画，含 600px 与 1200px WebP。
- `assets/source/`：生成插画原图。
- `screenshots/`：浏览器预览截图。

重新压缩原图：`node scripts/prepare-assets.mjs cafe assets/source/cafe.png`；另两张使用 `park`、`listening`。

新增或修改教程后运行 `npm run check:content`，检查路径、来源、目录锚点、练习和关联内容。检查脚本使用 Node.js 22 的实验类型擦除功能。

## 部署接入

将 `dist/` 部署为静态文件，Nginx 对前端路径使用 `try_files $uri $uri/ /index.html`。独立后端位于 `../backend/`，接口文档见 `../docs/API.md`。Vite开发和预览配置已将 `/api` 代理到127.0.0.1:8080；原有进程需要重启才能应用配置。`src/api/client.ts` 提供带Cookie和CSRF的传输层，登录页面与各页面云同步尚未接入。

设计依据和验证记录见 `../docs/UI-Design.md`。内容参考的许可见 `../docs/THIRD-PARTY-NOTICES.md`。
