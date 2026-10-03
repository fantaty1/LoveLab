# 服务器部署

部署目录包含三个服务：PostgreSQL、Spring Boot API和前端Nginx。只有前端的8088端口映射到服务器本机，数据库和API不公开端口。

## 启动

Linux服务器安装Docker和Compose插件，在deploy目录：

```sh
cp .env.example .env
# 编辑.env，设置独立数据库密码和首次管理员账号密码
docker compose -f compose.yml up -d --build
docker compose -f compose.yml logs -f api
```

`.env` 中 `POSTGRES_PASSWORD` 必填；`ADMIN_PASSWORD`至少12字符且UTF-8编码不超过72字节。种子内容导入后不会在重启时覆盖已有版本。初始管理员创建完成后移除运行配置里的管理员密码。

生产环境Secure Cookie需要HTTPS。在宿主机安装配置TLS的反向代理，参考 `nginx/https.example.conf` 替换域名与证书路径，再把HTTPS流量转发到127.0.0.1:8088。直接以HTTP访问8088只适合检查静态页面，不能作为生产登录入口。

## 更新与回滚

前后端独立Dockerfile，可分别构建与替换。发布前执行后端 `mvn verify` 与前端 `npm run check:content && npm run build`。数据库结构使用新增Flyway迁移文件更新，不修改已执行迁移；破坏性变更需安排数据迁移与恢复窗口。

保留上一版镜像标签和配置，应用回滚时恢复对应镜像。内容回滚使用管理接口发布旧版本；保留历史版本，避免已有存档引用丢失。

## 数据库备份与恢复

在deploy目录，生成独立归档：

```sh
docker compose -f compose.yml exec -T db pg_dump -U lovelab -d lovelab -Fc > lovelab-backup.dump
```

将备份保存在服务器以外并设置每日执行。恢复先在新的隔离数据库中演练；使用 `pg_restore` 恢复后检查账户、内容版本、存档和学习记录，再切换连接。不要直接覆盖正在服务的数据库。

当前会话和请求限流在API实例内存中，多实例部署需要共享会话和限流存储。容量按并发、请求速率、CPU、内存和数据库连接情况压测，累计注册人数不能直接换算成同时在线人数。后端默认10个数据库连接、100个Tomcat工作线程，容器内存上限1GB；这些是起始配置，尚未经过服务器压测。

本机没有Docker运行环境，Compose镜像构建与真实PostgreSQL验证需在提供Docker的环境执行；本地已提供H2开发与集成测试入口。
