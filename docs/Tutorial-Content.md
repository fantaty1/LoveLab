# 恋爱手册：系统教程模块

版本：首批20篇。整理日期：2026-10-03。

## 模块结构

系统教程位于 `/handbook`，随手知识卡位于 `/handbook?view=cards`。每篇长文使用 `/handbook/:slug` 独立地址。长文与卡片互补：长文讲清楚方法的背景、适用条件和后续调整，卡片方便临时回看。

文章包含核心提示、适用阶段、三个正文段落组、行动步骤、原创正反例、三个常见误区、参考练习与来源。目录在桌面侧边固定，在手机上折叠。收藏、主动标记已读及阅读列表使用本地存储，旧版练习数据可继续使用。

## 首批目录

| 分类 | 教程 |
| --- | --- |
| 情绪认同 | 她说今天好烦；倾听还是给建议；回应开心分享 |
| 自然聊天 | 现场开场；双向接话；具体赞美；轻松幽默 |
| 邀约与约会 | 具体邀请；第一次约会准备；约会氛围；见面后联系 |
| 氛围与靠近 | 眼神坐姿与距离；牵手拥抱；表达好感 |
| 误区与边界 | 消息节奏；看实际回应；自身边界 |
| 关心与修复 | 具体关心；道歉补救；分歧沟通 |

推荐阅读顺序为情绪认同、双向聊天、具体邀约。部分文章关联现有剧场，让用户读完再练一次。

## 素材取用

本地 `E:\skills\goutoujunshi` 中取用：情绪价值指南、巧妙接话指南、场景感与社交校准、主动表达与第一次见面、真诚赞美、清晰拒绝、同意与边界、实用沟通导读。保留既有第三方版权与MIT许可声明。

外部资料只提炼原理并链接原页，正文案例、正反例与练习重新编写。没有复制付费课程、整篇文章或未核实的“大师语录”。来源不代表作者认可网站的场景改编。

| 来源 | 本次采用的要点 | 适用限制 |
| --- | --- | --- |
| [Gottman：情绪与共情](https://www.gottman.com/blog/stop-trying-fix-partners-feelings/) | 先倾听和认同感受，再考虑建议 | 原文主要面向伴侣；初识案例为本站改编 |
| [Gottman：开放问题](https://www.gottman.com/blog/better-conversations-partner-just-anyone-else/) | 开放问题给对方自由表达空间 | 不等于应追问所有私人内容 |
| [Gottman：约会交流](https://www.gottman.com/blog/getting-to-know-you-questions-to-ask-your-dating-partner/) | 交流是了解彼此偏好的机会 | 不完整复制题库，不宣称能使人爱上自己 |
| [Gottman：道歉](https://www.gottman.com/blog/the-art-of-the-mindful-apology-why-sorry-is-not-enough/) | 承认、修复与改进 | 不要求受影响的一方必须立即原谅 |
| [love is respect：身体边界](https://www.loveisrespect.org/resources/physical-boundaries/) | 开放沟通、个人舒适与调整 | 不用单一动作推定恋爱或接触意愿 |
| [love is respect：同意](https://www.loveisrespect.org/everyone-deserves-a-healthy-relationship/understand-consent/) | 同意是持续的双方选择 | 不把恋爱身份当永久许可 |
| [love is respect：关系边界](https://www.loveisrespect.org/everyone-deserves-a-healthy-relationship/how-to-set-boundaries/) | 双方表达需要与限制 | 边界不能用于查手机或限制社交 |
| [Mark Manson：真实表达](https://markmanson.net/vulnerability-in-relationships) | 真实表达与表演脆弱不同 | 作者个人经验，不将吸引效果写成研究结论 |
| [Mark Manson：边界](https://markmanson.net/boundaries) | 支持彼此不等于控制情绪 | 原则性经验，不替具体人物作诊断 |

对应关系保存在 `frontend/src/data/tutorials.ts` 的 `tutorialSources` 和每篇 `refs` 中。网页展示来源名称和采用要点，内部文件路径仅作为编辑参考。

## 内容扩展规则

1. 从具体场景出发，一篇主要讲一个动作，避免堆砌万能话术。
2. 区分研究、专业教育、作者经验和本站情景改编；新增主张先查出处。
3. 情绪认同必须区分感受、事实判断和行为；不要机械要求附和。
4. 约会、好感与接触使用清楚的反馈分支，保留双方的选择和停止空间。
5. 用真实细节写示例，不把性别、MBTI或个别动作作为读心公式。
6. 新增教程需通过目录、来源、关联链接和练习完整性检查，再用手机查看长文。

## 验证

自动内容检查：`npm run check:content`。检查20个独立地址、主题非空、目录锚点唯一、正文完整、正反例和练习完整、引用有效、关联教程与剧场存在。

构建检查：`npm run build`，包含TypeScript检查。浏览器验证覆盖主题筛选、搜索、收藏筛选、练习展开、刷新后的已读状态、我的阅读，以及旧知识卡兼容。另验证了手机折叠目录与锚点位置、浅色和深色阅读、关联剧情入口，以及不存在的文章提示；手机页面无横向溢出。当前未接入正式账户或内容管理服务。

界面截图保存在 `frontend/screenshots/handbook-desktop.png`、`handbook-mobile.png`、`tutorial-desktop.png`、`tutorial-mobile.png`。
