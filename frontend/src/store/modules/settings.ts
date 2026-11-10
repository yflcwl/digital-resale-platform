import { defineStore } from 'pinia';
import defaultSettings from '@/settings';
import { useDynamicTitle } from '@/utils/dynamicTitle';
import { useStorage } from '@vueuse/core';
import { ref } from 'vue';
import { NavTypeEnum } from '@/enums/NavTypeEnum';

export const useSettingsStore = defineStore('setting', () => {
  // 换了 key，避免旧的 layout-setting（若依蓝 + 深色菜单）覆盖新主题
  const storageSetting = useStorage<LayoutSetting>('sm-layout-setting-v3', {
    tagsView: defaultSettings.tagsView,
    tagsIcon: defaultSettings.tagsIcon,
    fixedHeader: defaultSettings.fixedHeader,
    sidebarLogo: defaultSettings.sidebarLogo,
    dynamicTitle: defaultSettings.dynamicTitle,
    sideTheme: defaultSettings.sideTheme,
    theme: defaultSettings.theme,
    radiusBase: defaultSettings.radiusBase
  });
  const title = ref<string>(defaultSettings.title);
  const theme = ref<string>(storageSetting.value.theme);
  const sideTheme = ref<string>(storageSetting.value.sideTheme);
  const showSettings = ref<boolean>(defaultSettings.showSettings);
  const tagsView = ref<boolean>(storageSetting.value.tagsView);
  const tagsIcon = ref<boolean>(storageSetting.value.tagsIcon);
  const fixedHeader = ref<boolean>(storageSetting.value.fixedHeader);
  const sidebarLogo = ref<boolean>(storageSetting.value.sidebarLogo);
  const dynamicTitle = ref<boolean>(storageSetting.value.dynamicTitle);
  const animationEnable = ref<boolean>(defaultSettings.animationEnable);
  const dark = ref<boolean>(defaultSettings.dark);
  // 布局形态由代码决定，不从本地存储读取：避免历史缓存把新布局覆盖掉
  const navType = ref<NavTypeEnum>(defaultSettings.navType);
  const radiusBase = ref<number>(storageSetting.value.radiusBase ?? defaultSettings.radiusBase);

  const setTitle = (value: string) => {
    title.value = value;
    useDynamicTitle();
  };
  return {
    title,
    theme,
    sideTheme,
    showSettings,
    tagsView,
    tagsIcon,
    fixedHeader,
    sidebarLogo,
    dynamicTitle,
    animationEnable,
    dark,
    navType,
    radiusBase,
    setTitle
  };
});
