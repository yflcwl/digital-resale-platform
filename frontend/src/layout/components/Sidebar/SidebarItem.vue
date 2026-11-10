<template>
  <div v-if="!item.hidden">
    <template v-if="hasOneShowingChild(item, item.children) && (!onlyOneChild.children || onlyOneChild.noShowingChildren) && !item.alwaysShow">
      <app-link v-if="onlyOneChild.meta" :to="resolvePath(onlyOneChild.path, onlyOneChild.query)">
        <!-- 横向导航不折叠，el-menu-item 的 #title 插槽不会渲染，文字必须放进默认插槽 -->
        <el-menu-item v-if="horizontal" :index="resolvePath(onlyOneChild.path)" :class="{ 'submenu-title-noDropdown': !isNest }">
          <svg-icon v-if="onlyOneChild.meta.icon || (item.meta && item.meta.icon)" :icon-class="onlyOneChild.meta.icon || (item.meta && item.meta.icon)" />
          <i v-else class="menu-dot"></i>
          <span class="menu-title" :title="hasTitle(onlyOneChild.meta.title)">{{ onlyOneChild.meta.title }}</span>
        </el-menu-item>
        <el-menu-item v-else :index="resolvePath(onlyOneChild.path)" :class="{ 'submenu-title-noDropdown': !isNest }">
          <svg-icon v-if="onlyOneChild.meta.icon || (item.meta && item.meta.icon)" :icon-class="onlyOneChild.meta.icon || (item.meta && item.meta.icon)" />
          <i v-else class="menu-dot"></i>
          <template #title>
            <span class="menu-title" :title="hasTitle(onlyOneChild.meta.title)">{{ onlyOneChild.meta.title }}</span>
          </template>
        </el-menu-item>
      </app-link>
    </template>

    <el-sub-menu v-else ref="subMenu" :index="resolvePath(item.path)" teleported>
      <template v-if="item.meta" #title>
        <svg-icon v-if="item.meta && item.meta.icon" :icon-class="item.meta.icon" />
        <i v-else class="menu-dot"></i>
        <span class="menu-title" :title="hasTitle(item.meta?.title)">{{ item.meta?.title }}</span>
      </template>

      <sidebar-item
        v-for="(child, index) in item.children"
        :key="child.path + index"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-sub-menu>
  </div>
</template>

<script setup lang="ts">
import { isExternal } from '@/utils/validate';
import AppLink from './Link.vue';
import { getNormalPath } from '@/utils/sm';
import { RouteRecordRaw } from 'vue-router';

const props = defineProps({
  item: {
    type: Object as PropType<RouteRecordRaw>,
    required: true
  },
  isNest: {
    type: Boolean,
    default: false
  },
  /**
   * 是否用于顶部横向导航（横向菜单不折叠，标题需放进默认插槽）
   */
  horizontal: {
    type: Boolean,
    default: false
  },
  basePath: {
    type: String,
    default: ''
  }
});

const onlyOneChild = ref<any>({});

const hasOneShowingChild = (parent: RouteRecordRaw, children?: RouteRecordRaw[]) => {
  if (!children) {
    children = [];
  }
  const showingChildren = children.filter((item) => {
    if (item.hidden) {
      return false;
    }
    onlyOneChild.value = item;
    return true;
  });

  // When there is only one child router, the child router is displayed by default
  if (showingChildren.length === 1) {
    return true;
  }

  // Show parent if there are no child router to display
  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true };
    return true;
  }

  return false;
};

const resolvePath = (routePath: string, routeQuery?: string): any => {
  if (isExternal(routePath)) {
    return routePath;
  }
  if (isExternal(props.basePath as string)) {
    return props.basePath;
  }
  if (routeQuery) {
    const query = JSON.parse(routeQuery);
    return { path: getNormalPath(props.basePath + '/' + routePath), query: query };
  }
  return getNormalPath(props.basePath + '/' + routePath);
};

const hasTitle = (title: string | undefined): string => {
  if (!title || title.length <= 5) {
    return '';
  }
  return title;
};
</script>
