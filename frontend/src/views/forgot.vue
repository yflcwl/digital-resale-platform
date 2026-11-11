<template>
  <div class="forgot-page">
    <el-card class="forgot-card" shadow="always">
      <h2>找回密码</h2>
      <p class="tip">通过绑定邮箱验证码重置登录密码</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入账号绑定的邮箱" size="large" />
        </el-form-item>

        <el-form-item prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="请输入验证码" size="large" maxlength="6" />
            <el-button size="large" :loading="sending" :disabled="countdown > 0" @click="handleSendCode">
              {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-alert
          v-if="devCode"
          type="info"
          :closable="false"
          show-icon
          :title="'本地未配置邮件服务,验证码:' + devCode"
          style="margin-bottom: 14px"
        />

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入新密码(6~20位)" size="large" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" size="large" />
        </el-form-item>

        <el-button type="primary" size="large" style="width: 100%" :loading="submitting" @click="handleReset">
          重置密码
        </el-button>
        <div class="back-row">
          <el-button link type="primary" @click="goLogin">返回登录</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="ForgotPassword" lang="ts">
import { onBeforeUnmount, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { sendResetCode, resetPassword } from '@/api/forgot';
import { ElMessage, FormInstance, FormRules } from 'element-plus';

const router = useRouter();
const formRef = ref<FormInstance>();
const sending = ref(false);
const submitting = ref(false);
const countdown = ref(0);
const devCode = ref('');
let timer: number | undefined;

const form = reactive({
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
});

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/, message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6~20 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const startCountdown = () => {
  countdown.value = 60;
  timer = window.setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0 && timer) {
      window.clearInterval(timer);
      timer = undefined;
    }
  }, 1000);
};

const handleSendCode = async () => {
  await formRef.value?.validateField('email');
  sending.value = true;
  try {
    const res = await sendResetCode(form.email);
    devCode.value = res.data || '';
    ElMessage.success('验证码已发送,请查收邮箱');
    startCountdown();
  } finally {
    sending.value = false;
  }
};

const handleReset = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await resetPassword(form.email, form.code, form.password);
    ElMessage.success('密码重置成功,请重新登录');
    router.push('/login');
  } finally {
    submitting.value = false;
  }
};

const goLogin = () => {
  router.push('/login');
};

onBeforeUnmount(() => {
  if (timer) {
    window.clearInterval(timer);
  }
});
</script>

<style lang="scss" scoped>
.forgot-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 20% 20%, #e4f2ff 0, #f5f9fe 45%, #eaf2fc 100%);
  .forgot-card {
    width: 420px;
    border-radius: var(--sm-r-lg);
    padding: 8px;
    h2 {
      margin: 4px 0 6px;
      font-size: 24px;
    }
    .tip {
      margin: 0 0 20px;
      color: var(--sm-ink-3);
      font-size: 13px;
    }
    .code-row {
      display: flex;
      gap: 10px;
      width: 100%;
    }
    .back-row {
      margin-top: 12px;
      text-align: center;
    }
  }
}
</style>
