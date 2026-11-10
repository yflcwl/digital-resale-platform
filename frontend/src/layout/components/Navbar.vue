<template>
  <div class="navbar" :class="'nav' + navType">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggle-click="toggleSideBar" />

    <breadcrumb v-if="navType == NavTypeEnum.LEFT" id="breadcrumb-container" class="breadcrumb-container" />
    <top-nav v-if="navType == NavTypeEnum.MIX" id="topmenu-container" class="topmenu-container" />

    <template v-if="navType == NavTypeEnum.TOP">
      <logo v-show="showLogo" :collapse="false"></logo>
      <top-bar id="topbar-container" class="topbar-container" />
    </template>
    <div class="right-menu flex align-center">
      <template v-if="appStore.device !== 'mobile'">
        <el-select
          v-if="userId === 1 && tenantEnabled"
          v-model="companyName"
          class="min-w-244px mr-2"
          clearable
          filterable
          reserve-keyword
          :placeholder="proxy.$t('navbar.selectTenant')"
          @change="dynamicTenantEvent"
          @clear="dynamicClearEvent"
        >
          <el-option v-for="item in tenantList" :key="item.tenantId" :label="item.companyName" :value="item.tenantId"> </el-option>
          <template #prefix><svg-icon icon-class="company" class="el-input__icon input-icon" /></template>
        </el-select>

        <search-menu ref="searchMenuRef" />
        <el-tooltip content="搜索" effect="dark" placement="bottom">
          <div class="right-menu-item hover-effect" @click="openSearchMenu">
            <svg-icon class-name="search-icon" icon-class="search" />
          </div>
        </el-tooltip>
        <!-- 消息 -->
        <el-tooltip :content="proxy.$t('navbar.message')" effect="dark" placement="bottom">
          <div style="display:flex;align-items:center">
            <el-popover placement="bottom" trigger="click" transition="el-zoom-in-top" :width="300" :persistent="false">
              <template #reference>
                <el-badge :value="newNotice > 0 ? newNotice : ''" :max="99">
                  <div class="right-menu-item hover-effect"><svg-icon icon-class="message" /></div>
                </el-badge>
              </template>
              <template #default>
                <notice></notice>
              </template>
            </el-popover>
          </div>
        </el-tooltip>
        <el-tooltip :content="proxy.$t('navbar.full')" effect="dark" placement="bottom">
          <screenfull id="screenfull" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip :content="proxy.$t('navbar.language')" effect="dark" placement="bottom">
          <lang-select id="lang-select" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip :content="proxy.$t('navbar.layoutSize')" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>
      </template>
      <div class="avatar-container">
        <el-dropdown class="right-menu-item hover-effect" trigger="click" @command="handleCommand">
          <div class="avatar-wrapper">
            <img :src="userStore.avatar" class="user-avatar" />
            <el-icon><caret-bottom /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <router-link v-if="!dynamic" to="/user/profile">
                <el-dropdown-item>{{ proxy.$t('navbar.personalCenter') }}</el-dropdown-item>
              </router-link>
              <el-dropdown-item v-if="settingsStore.showSettings" command="setLayout">
                <span>{{ proxy.$t('navbar.layoutSetting') }}</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <span>{{ proxy.$t('navbar.logout') }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import SearchMenu from './TopBar/search.vue';
import { useAppStore } from '@/store/modules/app';
import { useUserStore } from '@/store/modules/user';
import { useSettingsStore } from '@/store/modules/settings';
import { useNoticeStore } from '@/store/modules/notice';
import { getTenantList } from '@/api/login';
import { dynamicClear, dynamicTenant } from '@/api/system/tenant';
import { TenantVO } from '@/api/types';
import notice from './notice/index.vue';
import router from '@/router';
import { ElMessageBoxOptions } from 'element-plus/es/components/message-box/src/message-box.type';
import { NavTypeEnum } from '@/enums/NavTypeEnum';
import Logo from "@/layout/components/Sidebar/Logo.vue";
import TopBar from './TopBar'

const appStore = useAppStore();
const userStore = useUserStore();
const settingsStore = useSettingsStore();
const noticeStore = storeToRefs(useNoticeStore());
const newNotice = ref(<number>0);

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const userId = ref(userStore.userId);
const navType = computed(() => settingsStore.navType);
const showLogo = computed(() => settingsStore.sidebarLogo);

const companyName = ref(undefined);
const tenantList = ref<TenantVO[]>([]);
// 是否切换了租户
const dynamic = ref(false);
// 租户开关
const tenantEnabled = ref(true);
// 搜索菜单
const searchMenuRef = ref<InstanceType<typeof SearchMenu>>();

const openSearchMenu = () => {
  searchMenuRef.value?.openSearch();
};

// 动态切换
const dynamicTenantEvent = async (tenantId: string) => {
  if (companyName.value != null && companyName.value !== '') {
    await dynamicTenant(tenantId);
    dynamic.value = true;
    await proxy?.$router.push('/');
    await proxy?.$tab.closeAllPage();
    await proxy?.$tab.refreshPage();
  }
};

const dynamicClearEvent = async () => {
  await dynamicClear();
  dynamic.value = false;
  await proxy?.$router.push('/');
  await proxy?.$tab.closeAllPage();
  await proxy?.$tab.refreshPage();
};

/** 租户列表 */
const initTenantList = async () => {
  const { data } = await getTenantList(true);
  tenantEnabled.value = data.tenantEnabled === undefined ? true : data.tenantEnabled;
  if (tenantEnabled.value) {
    tenantList.value = data.voList;
  }
};

defineExpose({
  initTenantList
});

const toggleSideBar = () => {
  appStore.toggleSideBar(false);
};

const logout = async () => {
  await ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  } as ElMessageBoxOptions);
  userStore.logout().then(() => {
    router.replace({
      path: '/login',
      query: {
        redirect: encodeURIComponent(router.currentRoute.value.fullPath || '/')
      }
    });
    proxy?.$tab.closeAllPage();
  });
};

const emits = defineEmits(['setLayout']);
const setLayout = () => {
  emits('setLayout');
};
// 定义Command方法对象 通过key直接调用方法
const commandMap: { [key: string]: any } = {
  setLayout,
  logout
};
const handleCommand = (command: string) => {
  // 判断是否存在该方法
  if (commandMap[command]) {
    commandMap[command]();
  }
};
//用深度监听 消息
watch(
  () => noticeStore.state.value.notices,
  (newVal) => {
    newNotice.value = newVal.filter((item: any) => !item.read).length;
  },
  { deep: true }
);
</script>

<style lang="scss" scoped>
/* ============ 顶部导航模式 ============ */
.navbar.navtop {
  padding-left: 16px;
  gap: 2px;

  .hamburger-container {
    display: none !important;
  }

  /* Logo 收进顶栏：横排、压缩高度 */
  :deep(.sidebar-logo-container) {
    height: 60px;
    padding: 0 10px 0 0;
    flex-shrink: 0;
  }

  :deep(.sidebar-logo-container .logo-text) {
    flex-direction: row;
    align-items: baseline;
    gap: 5px;
  }

  :deep(.sidebar-logo-container .logo-sub) {
    display: none;
  }

  .topbar-container {
    flex: 1;
    min-width: 0;
    margin-left: 8px;
    overflow: hidden;
  }

  :deep(.topbar-menu),
  :deep(.el-menu--horizontal) {
    border-bottom: none !important;
    background: transparent !important;
    height: 100%;
  }

  /* 一级菜单：胶囊 */
  /* 菜单项被 div > a 两层包裹，必须让它们按内容宽度排布，否则胶囊里的文字会被挤出去 */
  :deep(.topbar-menu > div) {
    display: inline-flex;
    align-items: center;
    height: 100%;
    flex: 0 0 auto;
  }

  :deep(.topbar-menu > div > a) {
    display: inline-flex;
    align-items: center;
    height: 100%;
    flex: 0 0 auto;
  }

  /* 菜单项被 <a> 包裹，必须用后代选择器 */
  :deep(.el-menu--horizontal .el-menu-item),
  :deep(.el-menu--horizontal .el-sub-menu > .el-sub-menu__title) {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    flex: 0 0 auto;
    white-space: nowrap;
    height: 38px;
    line-height: 38px;
    margin: 11px 2px 0;
    padding: 0 15px !important;
    border: none !important;
    border-radius: var(--sm-r-pill);
    font-size: 13.5px;
    font-weight: 600;
    color: var(--sm-ink-2);
    transition:
      background-color var(--sm-dur) ease,
      color var(--sm-dur) ease,
      transform var(--sm-dur) var(--sm-ease);

    .svg-icon {
      margin-right: 6px;
      width: 16px;
      height: 16px;
    }

    &:hover {
      background: var(--sm-bg-2) !important;
      color: var(--sm-ink) !important;
      transform: translateY(-1px);
    }

    &.is-active {
      background: var(--sm-brand) !important;
      color: #fff !important;
      font-weight: 700;
      box-shadow: 0 2px 0 var(--sm-brand-deep);

      .svg-icon {
        color: #fff;
      }

      &:hover {
        background: var(--sm-brand) !important;
        color: #fff !important;
      }
    }
  }

  :deep(.el-menu--horizontal .el-sub-menu.is-active > .el-sub-menu__title) {
    color: var(--sm-brand) !important;
    background: var(--sm-brand-soft) !important;
    box-shadow: none;
  }
}

:deep(.el-select .el-input__wrapper) {
  height: 32px;
  border-radius: var(--sm-r-pill);
}

:deep(.el-badge__content.is-fixed) {
  top: 8px;
}

.flex {
  display: flex;
}

.align-center {
  align-items: center;
}

.navbar {
  height: var(--sm-navbar-h);
  overflow: hidden;
  position: relative;
  background: transparent;
  border-bottom: none;
  box-shadow: none;
  display: flex;
  align-items: center;
  padding: 0 8px 0 4px;
  box-sizing: border-box;

  /* 汉堡键：圆角实体键，hover 上浮 */
  .hamburger-container {
    line-height: 1;
    height: 38px;
    width: 38px;
    justify-content: center;
    cursor: pointer;
    border-radius: var(--sm-r-sm);
    color: var(--sm-ink-2);
    transition:
      transform var(--sm-dur) var(--sm-ease),
      background-color var(--sm-dur) ease;
    -webkit-tap-highlight-color: transparent;
    display: flex;
    align-items: center;
    flex-shrink: 0;
    margin-right: 10px;

    &:hover {
      background: var(--sm-bg-2);
      transform: translateY(-1px);
    }

    &:active {
      transform: scale(0.92);
    }
  }

  .breadcrumb-container {
    //float: left;
    flex-shrink: 0;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .topbar-container {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    overflow: hidden;
    margin-left: 8px;
  }


  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    height: 100%;
    display: flex;
    align-items: center;
    margin-left: auto;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 38px;
      height: 38px;
      padding: 0;
      font-size: 18px;
      color: var(--sm-ink-2);
      border-radius: var(--sm-r-sm);
      transition:
        transform var(--sm-dur) var(--sm-ease),
        background-color var(--sm-dur) ease,
        color var(--sm-dur) ease;

      &.hover-effect {
        cursor: pointer;

        &:hover {
          background: var(--sm-brand-soft);
          color: var(--sm-brand);
          transform: translateY(-2px);
        }

        &:active {
          transform: scale(0.92);
        }
      }
    }

    .avatar-container {
      margin-right: 12px;
      padding-left: 10px;
      margin-left: 6px;
      border-left: 1.5px solid var(--sm-line);

      .avatar-wrapper {
        margin-top: 0;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 38px;
          height: 38px;
          border-radius: var(--sm-r-sm);
          margin-top: 0;
          display: block;
          box-shadow: 0 0 0 2px var(--sm-paper), 0 0 0 3.5px var(--sm-line);
          transition:
            transform var(--sm-dur) var(--sm-ease),
            box-shadow var(--sm-dur) ease;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 0 2px var(--sm-paper), 0 0 0 3.5px var(--sm-brand);
          }
        }

        i {
          cursor: pointer;
          position: absolute;
          right: -4px;
          bottom: -4px;
          font-size: 11px;
          background: var(--sm-ink);
          color: var(--sm-bg);
          border-radius: var(--sm-r-pill);
          width: 16px;
          height: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
    }
  }
}
</style>
