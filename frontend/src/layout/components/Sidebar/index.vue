<template>
  <div
    :class="{ 'has-logo': showLogo, 'is-expanded': expanded }"
    class="sidebar-root"
    @mouseenter="expanded = true"
    @mouseleave="expanded = false"
  >
    <div class="sidebar-inner">
      <logo v-if="showLogo" :collapse="!expanded" />
      <el-scrollbar :class="sideTheme" wrap-class="scrollbar-wrapper">
        <transition :enter-active-class="proxy?.animate.menuSearchAnimate.enter" mode="out-in">
          <el-menu
            :default-active="activeMenu"
            :collapse="!expanded"
            :unique-opened="true"
            :collapse-transition="false"
            :popper-offset="14"
            mode="vertical"
          >
            <sidebar-item v-for="(r, index) in sidebarRouters" :key="r.path + index" :item="r" :base-path="r.path" />
          </el-menu>
        </transition>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup lang="ts">
import Logo from './Logo.vue';
import SidebarItem from './SidebarItem.vue';
import { useSettingsStore } from '@/store/modules/settings';
import { usePermissionStore } from '@/store/modules/permission';
import { RouteRecordRaw } from 'vue-router';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const route = useRoute();
const settingsStore = useSettingsStore();
const permissionStore = usePermissionStore();
const sidebarRouters = computed<RouteRecordRaw[]>(() => permissionStore.getSidebarRoutes());
const showLogo = computed(() => settingsStore.sidebarLogo);
const sideTheme = computed(() => settingsStore.sideTheme);

/**
 * 图标轨：默认收起只显示图标，鼠标移入展开显示文字（浮在内容之上，不挤压布局）
 */
const expanded = ref(false);

const activeMenu = computed(() => {
  const { meta, path } = route;
  // if set path, the sidebar will highlight the path you set
  if (meta.activeMenu) {
    return meta.activeMenu;
  }
  return path;
});
</script>
