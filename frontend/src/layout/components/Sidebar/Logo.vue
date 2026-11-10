<template>
  <div class="sidebar-logo-container" :class="{ collapse: collapse }">
    <router-link class="sidebar-logo-link" to="/">
      <span class="logo-mark">
        <svg class="onion-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 1.7c.15 1.7.6 2.9 1.35 3.6" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" />
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
  height: 64px;
  flex-shrink: 0;
  padding: 0 14px;
  display: flex;
  align-items: center;
  overflow: hidden;

  .sidebar-logo-link {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    height: 100%;
    text-decoration: none;
  }

  /* 洋葱标识：圆角方砖 + 线条洋葱，带底部厚度呼应按钮的实体感 */
  .logo-mark {
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    border-radius: 12px;
    background: var(--sm-brand);
    color: #fff;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 0 var(--sm-brand-deep);
    transition: transform var(--sm-dur) var(--sm-ease);
  }

  .onion-icon {
    width: 23px;
    height: 23px;
    display: block;
  }

  .sidebar-logo-link:hover .logo-mark {
    transform: translateY(-1px) rotate(-4deg);
  }

  .logo-text {
    display: flex;
    flex-direction: column;
    min-width: 0;
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

  &.collapse {
    padding: 0;
    justify-content: center;

    .sidebar-logo-link {
      justify-content: center;
      gap: 0;
    }
  }
}
</style>
