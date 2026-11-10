# 二手数码交易平台与智能质检辅助系统(后端)

毕业设计项目「二手数码交易平台与智能质检辅助系统」的后端服务。

## 技术栈

- Spring Boot 3.5 / JDK 17 / Maven
- MyBatis Plus / PostgreSQL
- Redis / RocketMQ(异步消息:质检任务、订单延迟取消)
- Sa-Token(JWT 认证 + RBAC 权限)
- WebSocket(买卖家实时聊天)
- Knife4j(接口文档)
- AgentScope Java(智能质检,ReAct 模式 + 多模态大模型)

## 模块说明

| 模块 | 说明 |
| --- | --- |
| sm-admin | 启动模块,统一入口 |
| sm-common | 通用组件(core/redis/web/mybatis/oss/satoken 等) |
| sm-modules | 业务模块(system/job/generator/workflow) |
| sm-extend | 扩展服务(monitor-admin、snailjob-server) |

## 快速开始

1. 初始化 PostgreSQL 数据库,执行 `script/sql/postgres/postgres_sm_vue_5.X.sql`、`postgres_sm_job.sql`、`postgres_sm_workflow.sql`
2. 修改 `sm-admin/src/main/resources/application-dev.yml` 中数据库、Redis 连接配置
3. 启动 `SmApplication`(端口默认 8080)
4. 接口文档: http://localhost:8080/swagger-ui/index.html

## 致谢

本项目基于 [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus)(MIT License)构建,保留其开源协议文件,感谢原作者与社区的贡献。
