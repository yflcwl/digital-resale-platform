<template>
  <div class="sidebar-logo-container" :class="{ collapse: collapse }">
    <router-link class="sidebar-logo-link" to="/">
      <span class="logo-mark">
        <svg class="onion-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 1.55v3.4" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" />
          <path d="M12 4.9c4.3 0 8.3 3.4 8.3 8.8 0 5.6-3.7 8.7-8.3 8.7S3.7 19.3 3.7 13.7C3.7 8.3 7.7 4.9 12 4.9Z" fill="currentColor" />
          <path
            d="M9.2 7c-1.5 1.8-2.2 3.8-2.2 6.1 0 3.3 1.3 5.9 3 7.3"
            stroke="var(--sm-brand)"
            stroke-width="1.5"
            stroke-linecap="round"
          />
          <path
            d="M14.8 7c1.5 1.8 2.2 3.8 2.2 6.1 0 3.3-1.3 5.9-3 7.3"
            stroke="var(--sm-brand)"
            stroke-width="1.5"
            stroke-linecap="round"
          />
        </svg>
      </span>
      <span v-if="!collapse" class="logo-text">
        <span class="logo-title">洋葱二手</span>
        <span class="logo-sub">智能平台</span>
      </span>
    </router-link>
  </div>
</template>

<script setup lang="ts">
defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
});
</script>

<style lang="scss" scoped>
.sidebar-logo-container {
  height: 62px;
  flex: 0 0 62px;
  /* 顶部留内边距，方块避开轨道圆角 */
  padding: 12px 0 0;
  display: flex;
  align-items: center;
  overflow: hidden;

  .sidebar-logo-link {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    width: 100%;
    height: 44px;
    padding: 0;
    text-decoration: none;
  }

  /* 洋葱标识 */
  .logo-mark {
    flex: 0 0 34px;
    width: 34px;
    height: 34px;
    border-radius: 11px;
    background: var(--sm-brand);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform var(--sm-dur) var(--sm-ease);
  }

  .onion-icon {
    width: 22px;
    height: 22px;
    display: block;
  }

  .sidebar-logo-link:hover .logo-mark {
    transform: translateY(-1px);
  }

  .logo-text {
    display: flex;
    flex-direction: column;
    min-width: 0;
    overflow: hidden;
    line-height: 1.15;
  }

  .logo-title {
    font-size: 15px;
    font-weight: 800;
    letter-spacing: 0.01em;
    color: var(--sm-ink);
    white-space: nowrap;
  }

  .logo-sub {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.18em;
    color: var(--sm-stone);
    white-space: nowrap;
  }
}

/* 收起态：只留方块，整行居中 */
.sidebar-logo-container.collapse .sidebar-logo-link {
  gap: 0;
  justify-content: center;
}

/* 展开态：方块居左，文字跟在右侧 */
.sidebar-logo-container:not(.collapse) .sidebar-logo-link {
  justify-content: flex-start;
  padding: 0 12px;
  box-sizing: border-box;
}
</style>
