<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :span="24" style="padding-left: 20px">
        <h2>二手数码交易平台与智能质检辅助系统</h2>
        <p>
          本系统为毕业设计项目,面向二手数码设备交易场景,提供商品发布、交易、聊天等完整功能,
          并通过智能质检辅助卖家完成商品成色评定与合理定价。
          <br />
          * 前端框架 Vue 3、TypeScript、Element Plus<br />
          * 后端框架 Spring Boot 3、MyBatis Plus<br />
          * 数据库 PostgreSQL<br />
          * 缓存 Redis、消息队列 RocketMQ<br />
          * 权限认证 Sa-Token(JWT + RBAC)<br />
          * 实时通信 WebSocket<br />
          * 智能质检 AgentScope Java 智能体 + 多模态大模型(ReAct 模式)<br />
          * 文件存储 MinIO / 本地磁盘<br />
          * 接口文档 Knife4j(SpringDoc)<br />
        </p>
        <p>
          <el-button type="primary" icon="Cloudy" plain @click="goTarget('https://github.com/yflcwl/digital-resale-platform')">访问 GitHub</el-button>
        </p>
      </el-col>
    </el-row>
    <el-divider />
  </div>
</template>

<script setup name="Index" lang="ts">
const goTarget = (url: string) => {
  window.open(url, '__blank');
};
</script>

<style lang="scss" scoped>
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }
  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }
  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: 'open sans', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }
}
</style>
