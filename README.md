# LoveLab

Vue 3 前端与 Java 17 / Spring Boot 3.5.16 后端分别构建和运行。后端采用按业务模块组织的单体服务，服务器使用 PostgreSQL，本地开发使用 H2 文件数据库。

## 项目目录

```text
LoveLab/
├─ frontend/                用户网站、API客户端和Vite代理
├─ backend/
│  ├─ pom.xml               锁定Spring Boot版本与依赖
│  ├─ scripts/              Windows构建入口、独立Maven设置
│  ├─ src/main/java/com/lovelab/
│  │  ├─ auth/              账号、密码、登录、请求限流
│  │  ├─ content/           公开内容查询、版本模型、结构验证
│  │  ├─ game/              剧情选择、版本绑定、云端存档
│  │  ├─ daily/             上海日期的每日题和答题记录
│  │  ├─ learning/          收藏、已读、学习汇总、游客合并
│  │  ├─ admin/             审核、发布、下架、回滚、排期、角色
│  │  ├─ feedback/          内容反馈与处理
│  │  ├─ bootstrap/         初始内容和管理员创建
│  │  ├─ config/            安全、跨域、请求边界、时间配置
│  │  └─ common/            统一异常、分页、JSON编解码
│  ├─ src/main/resources/
│  │  ├─ application*.yml   通用 / 本地 / 生产配置
│  │  └─ db/migration/      Flyway数据库迁移
│  └─ src/test/             数据库、权限和业务集成测试
├─ content/                 随构建打包的初始内容快照
├─ deploy/                  Docker Compose、Nginx和部署说明
└─ docs/                    PRD、设计、API、内容规范与验证记录
```

Controller处理HTTP与字段校验，Service负责事务和业务规则，Repository集中SQL。业务DTO放在对应模块，通用目录不承载剧情或内容业务。后续增加后台页面时再创建独立的 `admin/` 前端项目。

## 本地启动

在 `backend/` 执行：

```powershell
.\scripts\maven.ps1 -B -ntp verify
.\scripts\maven.ps1 spring-boot:run
```

启动后：`http://127.0.0.1:8080/actuator/health`。首次启动自动迁移数据库并导入3个故事、12张知识卡、20篇教程、6个主题与1道每日题示例。数据库文件保存在 `backend/data/`，重启不会重置内容和学习记录。

在 `frontend/` 执行 `npm run dev`，通过 `/api` 代理到8080。网站目前仍使用已有本地试玩数据；API客户端已备好，登录界面和页面云同步属于下一步接入工作。

更多命令和管理员初始化见 [后端说明](backend/README.md)；接口约定见 [API文档](docs/API.md)，服务器配置见 [部署说明](deploy/README.md)。
