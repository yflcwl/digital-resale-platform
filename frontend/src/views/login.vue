<template>
  <div class="login-page">
    <!-- ============ 左：品牌叙事 + 产品证据 ============ -->
    <section class="login-brand">
      <div class="brand-glow"></div>

      <header class="brand-head">
        <span class="brand-mark">
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
        <span class="brand-name">{{ title }}</span>
      </header>

      <div class="brand-body">
        <div class="brand-copy">
          <div class="copy-main">
            <h1 class="brand-title">看准成色<br />再谈价格</h1>
            <p class="brand-sub">拍几张照片，AI 标出缺陷、定成色、给区间。</p>
          </div>

          <ul class="brand-stats">
            <li>
              <b>12,480</b>
              <span>台已质检</span>
            </li>
            <li>
              <b>42<span class="unit">s</span></b>
              <span>平均出报告</span>
            </li>
            <li>
              <b>8</b>
              <span>大品类覆盖</span>
            </li>
          </ul>
        </div>

        <div class="brand-visual" aria-hidden="true">
          <!-- 后卡：机型识别 + 置信度 -->
          <aside class="qc-card qc-card--back">
            <div class="qc-head">
              <span class="qc-label">机型识别</span>
              <span class="qc-time">0.96</span>
            </div>
            <div class="mini-device">iPhone 13<span>128G</span></div>
            <div class="mini-bar"><i style="width: 96%"></i></div>
            <div class="mini-foot">型号与配置已确认</div>
          </aside>

          <!-- 前卡：质检报告 -->
          <aside class="qc-card qc-card--front">
            <div class="qc-head">
              <span class="qc-live"></span>
              <span class="qc-label">质检报告</span>
              <span class="qc-time">刚刚</span>
            </div>

            <div class="qc-device">iPhone 13 · 128G 星光色</div>

            <div class="qc-grades">
              <span class="chip">S</span>
              <span class="chip on">A</span>
              <span class="chip">B</span>
              <span class="chip">C</span>
              <span class="chip">D</span>
            </div>

            <ul class="qc-defects">
              <li><i class="dot mild"></i><span>屏幕划痕 · 轻微</span><b>−2</b></li>
              <li><i class="dot mid"></i><span>边框磕碰 · 中等</span><b>−8</b></li>
            </ul>

            <div class="qc-foot">
              <div>
                <span class="qc-price">¥3,610</span>
                <span class="qc-range">区间 3,249 – 3,790</span>
              </div>
              <span class="qc-badge">AI 辅助</span>
            </div>
          </aside>
        </div>
      </div>

      <footer class="brand-foot">© 2026 洋葱二手智能平台</footer>
    </section>

    <!-- ============ 右：登录 ============ -->
    <section class="login-panel">
      <div class="login-card">
        <div class="card-head">
          <h2 class="card-title">登录</h2>
          <lang-select />
        </div>
        <p class="card-sub">管理商品、订单与质检报告</p>

        <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form" @submit.prevent>
          <el-form-item v-if="tenantEnabled" prop="tenantId">
            <el-select v-model="loginForm.tenantId" filterable :placeholder="proxy.$t('login.selectPlaceholder')" style="width: 100%">
              <el-option v-for="item in tenantList" :key="item.tenantId" :label="item.companyName" :value="item.tenantId"></el-option>
              <template #prefix><svg-icon icon-class="company" class="el-input__icon input-icon" /></template>
            </el-select>
          </el-form-item>

          <el-form-item prop="username">
            <el-input v-model="loginForm.username" type="text" size="large" auto-complete="off" :placeholder="proxy.$t('login.username')">
              <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              size="large"
              auto-complete="off"
              show-password
              :placeholder="proxy.$t('login.password')"
              @keyup.enter="handleLogin"
            >
              <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="captchaEnabled" prop="code">
            <div class="code-row">
              <el-input
                v-model="loginForm.code"
                size="large"
                auto-complete="off"
                :placeholder="proxy.$t('login.code')"
                @keyup.enter="handleLogin"
              >
                <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
              </el-input>
              <button type="button" class="code-box" title="点击换一张" @click="getCode">
                <img :src="codeUrl" alt="验证码" />
              </button>
            </div>
          </el-form-item>

          <div class="form-meta">
            <el-checkbox v-model="loginForm.rememberMe">{{ proxy.$t('login.rememberPassword') }}</el-checkbox>
          </div>

          <el-button :loading="loading" size="large" type="primary" class="submit-btn" @click.prevent="handleLogin">
            <span v-if="!loading">{{ proxy.$t('login.login') }}</span>
            <span v-else>{{ proxy.$t('login.logging') }}</span>
          </el-button>

          <p v-if="register" class="register-row">
            <router-link class="link-type" :to="'/register'">{{ proxy.$t('login.switchRegisterPage') }}</router-link>
          </p>
        </el-form>

        <p class="card-note">登录即表示同意平台交易规则</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { getCodeImg, getTenantList } from '@/api/login';
import { useUserStore } from '@/store/modules/user';
import { LoginData, TenantVO } from '@/api/types';
import { to } from 'await-to-js';
import { useI18n } from 'vue-i18n';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const title = import.meta.env.VITE_APP_TITLE;
const userStore = useUserStore();
const router = useRouter();
const { t } = useI18n();

const loginForm = ref<LoginData>({
  tenantId: '000000',
  username: 'admin',
  password: 'admin123',
  rememberMe: false,
  code: '',
  uuid: ''
} as LoginData);

const loginRules: ElFormRules = {
  tenantId: [{ required: true, trigger: 'blur', message: t('login.rule.tenantId.required') }],
  username: [{ required: true, trigger: 'blur', message: t('login.rule.username.required') }],
  password: [{ required: true, trigger: 'blur', message: t('login.rule.password.required') }],
  code: [{ required: true, trigger: 'change', message: t('login.rule.code.required') }]
};

const codeUrl = ref('');
const loading = ref(false);
// 验证码开关
const captchaEnabled = ref(true);
// 租户开关
const tenantEnabled = ref(true);

// 注册开关
const register = ref(false);
const redirect = ref('/');
const loginRef = ref<ElFormInstance>();
// 租户列表
const tenantList = ref<TenantVO[]>([]);

watch(
  () => router.currentRoute.value,
  (newRoute: any) => {
    redirect.value = newRoute.query && newRoute.query.redirect && decodeURIComponent(newRoute.query.redirect);
  },
  { immediate: true }
);

const handleLogin = () => {
  loginRef.value?.validate(async (valid: boolean, fields: any) => {
    if (valid) {
      loading.value = true;
      // 勾选了需要记住密码设置在 localStorage 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        localStorage.setItem('tenantId', String(loginForm.value.tenantId));
        localStorage.setItem('username', String(loginForm.value.username));
        localStorage.setItem('password', String(loginForm.value.password));
        localStorage.setItem('rememberMe', String(loginForm.value.rememberMe));
      } else {
        // 否则移除
        localStorage.removeItem('tenantId');
        localStorage.removeItem('username');
        localStorage.removeItem('password');
        localStorage.removeItem('rememberMe');
      }
      // 调用action的登录方法
      const [err] = await to(userStore.login(loginForm.value));
      if (!err) {
        const redirectUrl = redirect.value || '/';
        await router.push(redirectUrl);
        loading.value = false;
      } else {
        loading.value = false;
        // 重新获取验证码
        if (captchaEnabled.value) {
          await getCode();
        }
      }
    } else {
      console.log('error submit!', fields);
    }
  });
};

/**
 * 获取验证码
 */
const getCode = async () => {
  const res = await getCodeImg();
  const { data } = res;
  captchaEnabled.value = data.captchaEnabled === undefined ? true : data.captchaEnabled;
  if (captchaEnabled.value) {
    // 刷新验证码时清空输入框
    loginForm.value.code = '';
    codeUrl.value = 'data:image/gif;base64,' + data.img;
    loginForm.value.uuid = data.uuid;
  }
};

const getLoginData = () => {
  const tenantId = localStorage.getItem('tenantId');
  const username = localStorage.getItem('username');
  const password = localStorage.getItem('password');
  const rememberMe = localStorage.getItem('rememberMe');
  loginForm.value = {
    tenantId: tenantId === null ? String(loginForm.value.tenantId) : tenantId,
    username: username === null ? String(loginForm.value.username) : username,
    password: password === null ? String(loginForm.value.password) : String(password),
    rememberMe: rememberMe === null ? false : Boolean(rememberMe)
  } as LoginData;
};

/**
 * 获取租户列表
 */
const initTenantList = async () => {
  const { data } = await getTenantList(false);
  tenantEnabled.value = data.tenantEnabled === undefined ? true : data.tenantEnabled;
  if (tenantEnabled.value) {
    tenantList.value = data.voList;
    if (tenantList.value != null && tenantList.value.length !== 0) {
      loginForm.value.tenantId = tenantList.value[0].tenantId;
    }
  }
};

onMounted(() => {
  getCode();
  initTenantList();
  getLoginData();
});
</script>

<style lang="scss" scoped>
.login-page {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  min-height: 100vh;
  background: transparent;
}

/* ============ 左栏 ============ */
.login-brand {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 46px 56px 34px;
  overflow: hidden;
}

/* 左上角一片蓝色光晕，代替生硬的实心圆 */
.brand-glow {
  position: absolute;
  top: -300px;
  left: -180px;
  width: 780px;
  height: 780px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(43, 156, 240, 0.42) 0%, rgba(43, 156, 240, 0.16) 42%, transparent 68%);
  pointer-events: none;
}

.brand-head {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 11px;
}

.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 13px;
  background: var(--sm-brand);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 0 var(--sm-brand-deep);

  .onion-icon {
    width: 24px;
    height: 24px;
    display: block;
  }
}

.brand-name {
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.01em;
  color: var(--sm-ink);
}

.brand-body {
  position: relative;
  z-index: 2;
  flex: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) clamp(280px, 24vw, 400px);
  align-items: stretch;
  gap: 28px;
  padding: 8px 0;
}

/* 左列：标题在上、数据在下，把纵向空间撑满 */
.brand-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 46px 0 8px;
}

.copy-main {
  padding-top: 0;
}

/* 左下角：三条真实数据，把这块空白锚住 */
.brand-stats {
  list-style: none;
  display: flex;
  margin: 0;
  padding: 0;

  li {
    position: relative;
    display: flex;
    flex-direction: column;
    gap: 1px;
    padding-right: 22px;
    margin-right: 22px;

    &::after {
      content: '';
      position: absolute;
      right: 0;
      top: 7px;
      bottom: 5px;
      width: 1px;
      background: var(--sm-line);
    }

    &:last-child {
      padding-right: 0;
      margin-right: 0;

      &::after {
        display: none;
      }
    }
  }

  b {
    font-family: var(--sm-font-num);
    font-size: 23px;
    font-weight: 800;
    letter-spacing: -0.02em;
    line-height: 1.15;
    color: var(--sm-ink);
    font-variant-numeric: tabular-nums;

    .unit {
      margin-left: 1px;
      font-size: 13px;
      font-weight: 700;
      color: var(--sm-stone);
    }
  }

  span {
    font-size: 11.5px;
    font-weight: 600;
    color: var(--sm-stone);
    white-space: nowrap;
  }
}

/* 大标题：字距收紧、行高压缩，是「高级感」最直接的来源 */
.brand-title {
  margin: 0;
  font-size: clamp(36px, 4vw, 62px);
  line-height: 1.06;
  font-weight: 800;
  letter-spacing: -0.035em;
  color: var(--sm-ink);
}

.brand-sub {
  margin: 22px 0 0;
  font-size: 15px;
  line-height: 1.72;
  font-weight: 500;
  color: var(--sm-ink-3);
  max-width: 23em;
  text-wrap: balance;
}

/* ---------- 右侧卡片堆叠：产品证据 ---------- */
.brand-visual {
  position: relative;
  align-self: center;
  height: 508px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qc-card {
  border-radius: 22px;
  background: var(--sm-glass-strong);
  backdrop-filter: var(--sm-glass-blur);
  -webkit-backdrop-filter: var(--sm-glass-blur);
  border: 1px solid var(--sm-glass-border);
  box-shadow: var(--sm-shadow-3);
  transition: transform 0.5s var(--sm-ease);
}

.qc-card--front {
  position: relative;
  z-index: 2;
  width: 378px;
  margin-top: 56px;
  padding: 18px 20px 16px;
  transform: rotate(-3.5deg);

  &:hover {
    transform: rotate(-1.5deg) translateY(-4px);
  }
}

/* 后卡：露出一角，制造层次，同时填掉右上空白 */
.qc-card--back {
  position: absolute;
  z-index: 1;
  top: 2px;
  right: -2px;
  width: 332px;
  padding: 15px 17px 14px;
  transform: rotate(5deg);
  opacity: 0.97;

  &:hover {
    transform: rotate(3deg) translateY(-4px);
  }
}

.mini-device {
  margin-top: 9px;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--sm-ink);

  span {
    margin-left: 8px;
    font-size: 12px;
    font-weight: 700;
    color: var(--sm-stone);
  }
}

.mini-bar {
  margin-top: 11px;
  height: 6px;
  border-radius: var(--sm-r-pill);
  background: rgba(219, 231, 244, 0.95);
  overflow: hidden;

  i {
    display: block;
    height: 100%;
    border-radius: var(--sm-r-pill);
    background: linear-gradient(90deg, var(--sm-brand) 0%, #5cc0f7 100%);
  }
}

.mini-foot {
  margin-top: 9px;
  font-size: 11px;
  font-weight: 600;
  color: var(--sm-stone);
}

.qc-head {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  color: var(--sm-stone);
}

.qc-live {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--sm-accent);
  box-shadow: 0 0 0 3px rgba(79, 201, 138, 0.18);
}

.qc-time {
  margin-left: auto;
  letter-spacing: 0;
  font-weight: 600;
}

.qc-device {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--sm-ink);
}

.qc-grades {
  display: flex;
  gap: 6px;
  margin-top: 14px;
}

.chip {
  width: 28px;
  height: 28px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--sm-font-num);
  font-size: 12px;
  font-weight: 800;
  color: var(--sm-stone);
  background: rgba(226, 238, 252, 0.75);

  &.on {
    background: var(--sm-brand);
    color: #fff;
    box-shadow: 0 2px 0 var(--sm-brand-deep);
  }
}

.qc-defects {
  list-style: none;
  margin: 16px 0 0;
  padding: 14px 0 0;
  border-top: 1px solid var(--sm-line);

  li {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12.5px;
    font-weight: 600;
    color: var(--sm-ink-2);
    padding: 5px 0;

    span {
      flex: 1;
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    b {
      font-family: var(--sm-font-num);
      font-weight: 800;
      color: var(--sm-ink);
      font-variant-numeric: tabular-nums;
    }
  }

  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;

    &.mild {
      background: var(--sm-accent);
    }

    &.mid {
      background: var(--sm-warning);
    }
  }
}

.qc-foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  padding-top: 12px;
  border-top: 1px solid var(--sm-line);
}

.qc-price {
  display: block;
  font-family: var(--sm-font-num);
  font-size: 25px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;
}

.qc-range {
  display: block;
  margin-top: 1px;
  font-size: 11px;
  font-weight: 600;
  color: var(--sm-stone);
  font-variant-numeric: tabular-nums;
}

.qc-badge {
  flex-shrink: 0;
  font-size: 10.5px;
  font-weight: 800;
  letter-spacing: 0.06em;
  color: var(--sm-brand-deep);
  background: var(--sm-brand-soft);
  border-radius: var(--sm-r-pill);
  padding: 4px 10px;
}

.brand-foot {
  position: relative;
  z-index: 2;
  margin-top: 18px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.03em;
  color: var(--sm-stone);
}

/* ============ 右栏 ============ */
.login-panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 40px;
  overflow: hidden;
}

.login-panel::before {
  content: '';
  position: absolute;
  width: 780px;
  height: 660px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 30% 62%, rgba(43, 156, 240, 0.34), transparent 58%),
    radial-gradient(circle at 76% 18%, rgba(79, 201, 138, 0.2), transparent 56%);
  filter: blur(14px);
  pointer-events: none;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 34px 32px 26px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: var(--sm-glass-blur);
  -webkit-backdrop-filter: var(--sm-glass-blur);
  border: 1px solid var(--sm-glass-border);
  border-radius: var(--sm-r-xl);
  box-shadow: var(--sm-shadow-3);
  animation: sm-card-in 0.55s var(--sm-ease) both;
}

@keyframes sm-card-in {
  from {
    opacity: 0;
    transform: translateY(18px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--sm-ink);
}

.card-sub {
  margin: 6px 0 24px;
  font-size: 13px;
  font-weight: 500;
  color: var(--sm-stone);
}

.login-form {
  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    height: 46px;
    border-radius: var(--sm-r-sm);
    padding-left: 12px;
    background: rgba(255, 255, 255, 0.6);
    box-shadow: 0 0 0 1.5px var(--sm-line) inset;
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused) {
    background: rgba(255, 255, 255, 0.92);
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    font-weight: 600;
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item__error) {
    padding-top: 4px;
    font-weight: 600;
    color: var(--sm-danger);
  }

  :deep(.input-icon) {
    width: 16px;
    height: 16px;
    color: var(--sm-stone);
  }
}

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;

  :deep(.el-input) {
    flex: 1;
    min-width: 0;
  }
}

.code-box {
  flex: 0 0 112px;
  height: 46px;
  padding: 0;
  border: 1.5px solid var(--sm-line);
  border-radius: var(--sm-r-sm);
  background: rgba(255, 255, 255, 0.8);
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 0 var(--sm-line);
  transition:
    transform var(--sm-dur) var(--sm-ease),
    box-shadow var(--sm-dur) var(--sm-ease);

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 3px 0 var(--sm-brand);
    border-color: var(--sm-brand);
  }

  &:active {
    transform: translateY(2px);
    box-shadow: 0 0 0 var(--sm-line);
  }
}

.form-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;

  :deep(.el-checkbox__label) {
    font-size: 13px;
    font-weight: 600;
    color: var(--sm-ink-3);
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.08em;
  border-radius: var(--sm-r-pill);
}

.register-row {
  margin: 16px 0 0;
  text-align: center;
  font-size: 13px;
}

.card-note {
  margin: 22px 0 0;
  padding-top: 16px;
  border-top: 1px solid var(--sm-line);
  text-align: center;
  font-size: 11.5px;
  font-weight: 500;
  color: var(--sm-stone);
}

/* ============ 响应式 ============ */
@media (max-width: 1280px) {
  .brand-visual {
    display: none;
  }

  .brand-body {
    grid-template-columns: minmax(0, 1fr);
    align-items: center;
  }

  .brand-stats {
    margin-top: 34px;
  }
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-brand {
    display: none;
  }

  .login-panel {
    padding: 28px 20px;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 28px 22px 22px;
    border-radius: var(--sm-r-lg);
  }

  .card-title {
    font-size: 23px;
  }
}
</style>
