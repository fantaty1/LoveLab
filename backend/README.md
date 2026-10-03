# LoveLab 后端

Java 17、Spring Boot 3.5.16、Spring Security、JdbcTemplate、Flyway。使用明确的SQL管理内容版本与进度事务；当前为一个可独立部署的服务。

## 开发环境

需要JDK 17+、Maven 3.6.3+。本机入口 `scripts/maven.ps1` 优先读取 `JAVA_HOME` 和PATH，也兼容现有的 `D:\develop2\JDK 17` 与 `D:\develop2\apache-maven-3.8.8`。依赖缓存仅写到本项目 `.m2/`，不修改电脑的Maven设置。

```powershell
.\scripts\maven.ps1 -B -ntp test
.\scripts\maven.ps1 -B -ntp package
.\scripts\maven.ps1 spring-boot:run
```

服务启动后可以执行 `.\scripts\smoke.ps1` 验证真实HTTP链路。该命令会创建一个独立测试账号及示例学习记录，不打印或保存密码。当前检查结果见 `../docs/Backend-Validation.md`。

macOS/Linux或已配置Maven的环境直接用 `mvn verify`、`mvn spring-boot:run`。可运行产物为 `target/lovelab-backend-0.1.0.jar`。

默认local环境绑定127.0.0.1:8080，数据库是 `data/lovelab.mv.db`。关闭后端后可以复制该文件备份本地数据。test环境使用独立的内存数据库，不触碰本地文件；prod环境要求显式提供PostgreSQL连接和密码。

## 已有接口模块

- 用户注册、登录、登出、当前账号、安全令牌。用户名大小写归一，密码BCrypt编码，Cookie保存会话。
- 公开剧情、知识卡、教程、题目与手册元数据。列表分页和搜索，详情只读取公开版本。
- 开始一局、逐次提交选择、继续存档、完成记录。服务端决定反馈、下一幕和完成状态。
- 今日题按上海日期锁定内容版本，支持后台排期与未排期时的备用题。
- 收藏、主动已读、学习汇总与明确确认后的游客合并。合并中的任一无效路径导致整次事务回滚。
- 内容草稿、独立审核、发布、下架、历史版本回滚、角色管理、操作记录。
- 游客或登录用户的内容问题反馈、编辑处理记录。

## 首个管理员

不预置账号或通用密码。首次启动时自行设置环境变量：

```powershell
$env:ADMIN_USERNAME = 'your_admin_name'
$env:ADMIN_PASSWORD = Read-Host '输入至少12字符且UTF-8编码不超过72字节的初始化密码' -MaskInput
.\scripts\maven.ps1 spring-boot:run
```

仅在该用户名不存在时创建ADMIN，不会在重启时覆盖密码。创建完成后从后续运行环境中移除 `ADMIN_PASSWORD`。使用另一个注册账号，通过管理员角色接口授予EDITOR或ADMIN；作者不能审核自己的草稿。后台页面尚未创建，先通过HTTP接口进行操作。

会话在单个服务实例内存中保存，重启后需要重新登录；账号、内容和学习记录均保存在数据库。角色权限每次请求重新读取，撤销角色无需等待会话过期。后续多实例部署时替换为Spring Session JDBC或Redis共享会话。

## 内容和版本

`content/seed.json` 来源于现有前端内容快照。运行 `node --experimental-strip-types scripts/export-content.mjs`（frontend目录）可重新导出，再重新构建后端。初始导入只添加不存在的条目；已存在内容通过版本接口更新，重新导出文件不会覆盖线上编辑。

公开接口只读取 `published_version` 指针。每次编辑创建新版本，不覆盖旧JSON。新局使用当前公开版本；已有局保存自己的版本。下架保留历史数据、停止新局、暂停后续选择；回滚通过发布已审核的旧版本完成。

内容结构校验检查ID、必填解说、目录锚点、分支引用、循环与可达性。审核/发布还检查知识卡、主题和来源引用；内容是否符合事实仍需要编辑复核，结构校验不能代替人工审稿。

种子内容是已有试玩素材，正式发布前需完成人工审核；包括其中的来源主张和尚未补齐的题库预算。

## 配置

| 环境变量 | 用途 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | local / prod，默认local |
| `PORT` | 端口，默认8080 |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | prod PostgreSQL连接 |
| `SEED_ENABLED` | 是否导入示例；local=true，prod默认false |
| `ALLOWED_ORIGINS` | 允许的前端源，逗号分隔；prod默认同域 |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | 首次初始化管理员 |

数据库迁移由Flyway启动时执行，禁止自动清库。发布后添加新迁移文件，不能修改已执行迁移。

限制：当前不提供密码重置UI、管理UI、邮件、支付或AI服务。旧前端游客记录没有内容版本和局UUID；迁移接入时需要明确标记旧数据版本，并为每一局生成稳定的UUID，不能由客户端提交评级或完成状态。
