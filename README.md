# 二手数码交易平台与智能质检辅助系统

毕业设计项目。面向二手数码设备交易场景,提供商品发布、智能质检、交易订单、买卖家实时聊天等完整功能;智能质检模块基于 AgentScope Java(HarnessAgent)+ 多模态大模型,实现"机型识别 → 缺陷检测 → 成色分级 → 参考定价"的自动化质检流程。

## 仓库结构

| 目录 | 说明 |
| --- | --- |
| `backend/` | 后端服务(Spring Boot 3,基于 RuoYi-Vue-Plus 改造) |
| `frontend/` | 前端工程(Vue 3 + Element Plus + Vite) |

## 技术栈

- **后端**:JDK 17、Spring Boot 3.5、MyBatis Plus、PostgreSQL、Redis、RocketMQ、Sa-Token、WebSocket、Knife4j
- **前端**:Vue 3、TypeScript、Element Plus、Pinia、UnoCSS
- **智能质检**:AgentScope Java(HarnessAgent)、Qwen-VL / GLM-4V 多模态大模型
- **存储**:本地磁盘(可扩展 MinIO)

## 快速开始

### 1. 初始化数据库(PostgreSQL)

依次执行:

```sql
-- 若依系统表(用户/角色/权限/字典等)
backend/script/sql/postgres/postgres_sm_vue_5.X.sql
-- 定时任务表
backend/script/sql/postgres/postgres_sm_job.sql
-- 工作流表(可选)
backend/script/sql/postgres/postgres_sm_workflow.sql
-- 业务表(商品/质检/订单/聊天/通知等 23 张)
backend/script/sql/postgres/postgres_sm.sql
```

### 2. 启动后端

修改 `backend/sm-admin/src/main/resources/application-dev.yml` 中的数据库、Redis、RocketMQ 连接配置后,启动 `SmApplication`(默认端口 8080)。

接口文档:http://localhost:8080/swagger-ui/index.html

### 3. 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

## 模块划分(后端)

| 模块 | 职责 |
| --- | --- |
| `sm-admin` | 启动模块 |
| `sm-common` | 通用组件(core/redis/web/mybatis/oss/satoken 等) |
| `sm-modules/sm-system` | 系统管理(用户/角色/菜单,复用若依) |
| `sm-modules/sm-trade` | 交易域:商品、订单、评价、地址(待开发) |
| `sm-modules/sm-inspection` | 质检域:Harness 编排、感知工具、规则引擎(待开发) |
| `sm-modules/sm-message` | 消息域:聊天、站内通知(待开发) |
| `sm-extend` | 扩展服务(monitor-admin、snailjob-server) |

## 项目进度

- [x] 项目初始化与去若依化改造(sm- 前缀 / com.sm 包)
- [x] 数据库设计(23 张业务表,已建表)
- [x] 系统详细设计(含智能质检核心算法)
- [ ] 商品模块
- [ ] 智能质检模块(核心)
- [ ] 订单模块
- [ ] 聊天与通知模块
- [ ] 管理后台扩展

## 致谢

本项目基于 [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus)(MIT License)构建,感谢原作者与社区的贡献。
