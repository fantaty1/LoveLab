# LoveLab API v1

服务端：`http://127.0.0.1:8080`，业务接口前缀 `/api/v1`。前端Vite开发和生产预览均提供 `/api` 代理；重启原有Vite进程后新代理配置生效。服务器经Nginx统一域名。

## 通用约定

- JSON请求使用 `Content-Type: application/json`，日期为 `YYYY-MM-DD`，时间含时区。
- `kind`：STORY、CARD、TUTORIAL、QUESTION；路径也接受小写。slug使用小写字母、数字与连字符。
- 列表返回 `{items, page, size, total}`，page从0开始，size为1至50。
- 错误返回 `{code, message, requestId}`，响应头同时提供 `X-Request-Id`。400字段/内容结构无效；401未登录/凭据错误；403权限或CSRF失败；404不存在/不属于当前账号；409版本、进度或操作冲突；410已下架；429限流。
- 不接受未定义JSON字段，因此不能在选择请求里提交 `rating`、`skill` 或 `complete`。
- 请求体上限1MB，单条内容文档上限512KB。文章正文为纯文本结构，展示时使用Vue文本绑定。

## 登录与CSRF

认证使用HttpOnly的 `LOVELAB_SESSION` Cookie，无Bearer Token。上线prod使用Secure、SameSite=Lax Cookie，因此登录须通过HTTPS访问。所有写请求（包括注册、登录、反馈）需要CSRF令牌：

1. GET `/auth/csrf`，保留Cookie，取得 `{token, headerName}`。
2. 写请求添加返回的安全令牌请求头（当前为 `X-CSRF-TOKEN`），浏览器使用 `credentials: 'include'`。
3. 登录成功或登出后重新取得令牌。令牌失效时重新读取，重新提交前保留同一个业务UUID与选择序号。

前端传输层位于 `frontend/src/api/client.ts`。密码至少12个字符，UTF-8编码不能超过72字节；用户名3至32位ASCII字母、数字或下划线，存储前转换为小写。注册不会自动登录。

| 方法 | 路径 | 权限 / 请求 |
| --- | --- | --- |
| GET | `/auth/csrf` | 公开 |
| POST | `/auth/register` | `{username,password}`，201返回 `{id,username,role}` |
| POST | `/auth/login` | 同上，200返回账号并轮换会话标识 |
| POST | `/auth/logout` | 204 |
| GET | `/auth/me` | 已登录，返回账号 |

单个来源IP每15分钟最多20次登录、5次注册、30次反馈提交，返回429和Retry-After。限流为单实例内存实现。

## 公开内容与今日题

| 方法 | 路径 | 返回 |
| --- | --- | --- |
| GET | `/content/metadata` | 六个主题和来源字典 `{topics,sources}` |
| GET | `/content/{kind}?q=&topic=&page=0&size=20` | 分页摘要，搜索标题/标签/正文，topic匹配主题ID或分类 |
| GET | `/content/{kind}/{slug}` | `{kind,slug,version,document}`，完整正文或剧情 |
| GET | `/daily` | `{date,question:{kind,slug,version,document},fallback}` |

今日题第一次取用时锁定上海日期对应的版本；未来日期可以提前排期。未排期时使用按slug排序的第一道公开题并记为fallback。已有答题记录的日期不能换题；紧急下架后可替换备用题。

独立题和剧情选项沿用前端格式 `{text,rating,response,explanation,next?,card,skill}`。此版本用于学习体验，公开文档中包含解说；后续付费内容需要独立的权益和答案展示策略。

## 剧场与答题

| 方法 | 路径 | 请求 / 说明 |
| --- | --- | --- |
| POST | `/runs` | `{id:UUID,storySlug}`，201创建或返回同局 |
| GET | `/runs?page=0&size=20` | 当前账号的局列表（story为null，详情另取） |
| GET | `/runs/{id}` | 存档、选项反馈和绑定的完整剧情版本 |
| POST | `/runs/{id}/choices` | `{sequence,nodeId,optionIndex}` |
| POST | `/daily/attempts` | `{id:UUID,date,questionSlug,version,optionIndex}` |

`optionIndex`从0开始。`sequence`从1开始并严格递增。下一幕和评级由服务端发布配置决定；最终选项没有next即完成。返回Run：

```json
{
  "id": "客户端生成的UUID",
  "storySlug": "coffee",
  "version": 1,
  "currentNode": "share",
  "status": "ACTIVE",
  "revision": 1,
  "available": true,
  "updatedAt": "含时区的时间",
  "choices": [{"sequence":1,"nodeId":"hello","optionIndex":0,"feedback":{"text":"...","rating":"...","response":"...","explanation":"...","next":"share","card":"opening","skill":"自然认识"}}],
  "story": {"id":"coffee","scenes":[]}
}
```

相同局ID重复开始不新增记录；相同序号、节点和选项重试不新增选择。相同序号改变选项、跳幕、跳序号返回409。完成后重玩生成新UUID，旧局保留。其他账号读取该局返回404。下架存档仍可查看，`available=false`，继续提交返回410。

每日题尝试同一个UUID重试须带相同日期、题目版本和选项。更换选项属于新的尝试，必须使用新UUID；服务器按上海日期统计练习天数。

## 学习记录与游客合并

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/me` | `{completedRuns,readTutorials,favorites,practiceDays,records}` |
| PUT / DELETE | `/me/favorites/{kind}/{slug}` | 收藏或取消，幂等，204；支持STORY/CARD/TUTORIAL |
| PUT / DELETE | `/me/read/{slug}` | 教程已读或取消，幂等，204 |
| POST | `/me/import` | 显式确认后合并本地记录，200返回学习汇总 |

合并请求：

```json
{
  "confirmed": true,
  "favorites": [{"kind":"CARD","slug":"listen"}],
  "readArticles": ["emotional-validation"],
  "runs": [{"id":"稳定的UUID","storySlug":"listening","version":1,"choices":[{"nodeId":"tired","optionIndex":0}]}]
}
```

不删除云端已有收藏或已读；重复UUID和路径去重。冲突返回409并回滚整个合并，客户端保留本地记录供用户处理。单次最多100项收藏、100篇已读、30局，每局最多100个选项。旧本地每日题只记录练习日期、没有对应答案，因此不伪造迁移为服务器答题记录。

## 内容运营接口

用户角色USER，编辑EDITOR，管理员ADMIN。ADMIN可创建内容、审核其他作者的草稿。EDITOR可以预览和创建版本、处理反馈；审核、发布、下架、角色修改与排期须ADMIN。

| 方法 | 路径 | 请求 / 说明 |
| --- | --- | --- |
| GET | `/admin/content/{kind}?page=0&size=20` | 条目、最新版本和公开版本 |
| GET | `/admin/content/{kind}/{slug}/versions/{version}` | 草稿/旧版本详情 |
| POST | `/admin/content/{kind}/{slug}/versions` | `{document:完整文档}`，新建不可变DRAFT版本，201 |
| POST | `/admin/content/{kind}/{slug}/versions/{version}/review` | `{note:审核意见}`，作者与审核者须不同 |
| POST | `/admin/content/{kind}/{slug}/versions/{version}/publish` | 发布REVIEWED版本，或回滚到已PUBLISHED的旧版本 |
| DELETE | `/admin/content/{kind}/{slug}/publication` | 下架，204 |
| PUT | `/admin/daily/{day}` | `{questionSlug,version}`，排期公开题，204 |
| PUT | `/admin/users/{id}/role` | `{role:"EDITOR"}`，角色更新立即影响已有会话，不能修改自己 |
| GET | `/admin/audit?page=0&size=20` | 管理操作记录 |

目前主题与来源字典来自种子快照；新文档引用已有ID，新增主题/来源需扩展元数据配置。后台网页编辑器未包含在本次后端范围内。

## 内容反馈

POST `/feedback`（游客可用，但须CSRF）：`{kind,slug,category,message}`，category为UNCLEAR / MISSING_CONTEXT / CONTENT_ISSUE，message最多1000字符。201返回 `{id}`。

GET `/admin/feedback?status=OPEN&page=0&size=20`；PUT `/admin/feedback/{id}` 请求 `{note}` 标记处理完成并写入操作记录，204。不要在反馈里收集真实聊天记录、联系方式或密码。
