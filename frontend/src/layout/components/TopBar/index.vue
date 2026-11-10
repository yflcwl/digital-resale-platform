<template>
  <el-menu class="topbar-menu" :ellipsis="false" :default-active="activeMenu" :active-text-color="theme" mode="horizontal">
    <sidebar-item :key="route.path + index" v-for="(route, index) in topMenus" :item="route" :base-path="route.path" :horizontal="true" />

    <el-sub-menu index="more" class="el-sub-menu__hide-arrow" v-if="moreRoutes.length > 0">
      <template #title>
        <span>更多菜单</span>
      </template>
      <sidebar-item :key="route.path + index" v-for="(route, index) in moreRoutes" :item="route" :base-path="route.path" :horizontal="true" />
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import SidebarItem from '../Sidebar/SidebarItem'
import {useAppStore} from '@/store/modules/app'
import {useSettingsStore} from '@/store/modules/settings'
import {usePermissionStore} from '@/store/modules/permission'

const route = useRoute()
const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

const sidebarRouters = computed(() => permissionStore.sidebarRouters)
const theme = computed(() => settingsStore.theme)
const device = computed(() => appStore.device)
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

const visibleNumber = ref(7)
const topMenus = computed(() => {
  return permissionStore.sidebarRouters.filter((f) => !f.hidden).slice(0, visibleNumber.value)
})
const moreRoutes = computed(() => {
  return permissionStore.sidebarRouters.filter((f) => !f.hidden).slice(visibleNumber.value, sidebarRouters.value.length - visibleNumber.value)
})
function setVisibleNumber() {
  let width = document.body.getBoundingClientRect().width
  if (width >= 1000) {
    width -= 500
  }
  visibleNumber.value = parseInt(width / 3 / 85)
}

onMounted(() => {
  window.addEventListener('resize', setVisibleNumber)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', setVisibleNumber)
})

onMounted(() => {
  setVisibleNumber()
})
</script>

<style lang="scss">
/**
 * 顶部横向菜单：胶囊化。
 * 这份样式接管全部菜单项外观（原若依的 float/固定色/固定高度规则已移除）。
 */
.topbar-menu.el-menu--horizontal {
  border-bottom: none !important;
  background: transparent !important;
  height: 100%;
  display: flex;
  align-items: center;

  /* 菜单项被 div > a 两层包裹，必须按内容宽度排布 */
  > div {
    display: inline-flex;
    align-items: center;
    height: 100%;
    flex: 0 0 auto;
  }

  > div > a {
    display: inline-flex;
    align-items: center;
    height: 100%;
    flex: 0 0 auto;
  }

  .el-menu-item,
  .el-sub-menu__title {
    display: inline-flex !important;
    align-items: center;
    justify-content: center;
    gap: 6px;
    height: 38px !important;
    line-height: 1 !important;
    margin: 0 2px !important;
    padding: 0 15px !important;
    border: none !important;
    border-bottom: none !important;
    border-radius: var(--sm-r-pill);
    font-size: 13.5px !important;
    font-weight: 600;
    color: var(--sm-ink-2) !important;
    background: transparent;
    white-space: nowrap;
    box-sizing: border-box;
    transition:
      background-color 0.18s ease,
      color 0.18s ease,
      transform 0.18s cubic-bezier(0.34, 1.56, 0.64, 1);

    .svg-icon {
      width: 16px !important;
      height: 16px !important;
      margin: 0 !important;
      vertical-align: middle;
      flex-shrink: 0;
    }

    span {
      color: inherit !important;
    }

    &:hover {
      background: var(--sm-bg-2) !important;
      color: var(--sm-ink) !important;
      transform: translateY(-1px);
    }

    &:active {
      transform: scale(0.97);
    }
  }

  /* 选中：蓝色实体胶囊 */
  .el-menu-item.is-active,
  .el-sub-menu.is-active > .el-sub-menu__title {
    background: var(--sm-brand) !important;
    color: #fff !important;
    font-weight: 700 !important;
    box-shadow: 0 2px 0 var(--sm-brand-deep);

    .svg-icon,
    span {
      color: #fff !important;
    }

    &:hover {
      background: var(--sm-brand) !important;
      color: #fff !important;
    }
  }

  .el-sub-menu__icon-arrow {
    position: static !important;
    margin: 0 0 0 2px !important;
    display: block !important;
    color: currentColor;
    font-size: 11px;
  }
}
</style>
