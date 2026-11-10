<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>收货地址</span>
          <el-button type="primary" @click="openDialog()">新增地址</el-button>
        </div>
      </template>

      <el-empty v-if="!loading && list.length === 0" description="还没有收货地址" />
      <el-row :gutter="16" v-loading="loading">
        <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom: 16px">
          <el-card shadow="hover" :class="{ 'default-card': item.isDefault === '1' }">
            <div class="addr-head">
              <span class="receiver">{{ item.receiver }}</span>
              <span class="phone">{{ item.phone }}</span>
              <el-tag v-if="item.isDefault === '1'" type="success" size="small">默认</el-tag>
            </div>
            <div class="addr-detail">{{ item.province }}{{ item.city }}{{ item.district || '' }}{{ item.detail }}</div>
            <div class="addr-actions">
              <el-button v-if="item.isDefault !== '1'" link type="primary" @click="handleSetDefault(item)">设为默认</el-button>
              <el-button link type="primary" @click="openDialog(item)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(item)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑地址' : '新增地址'" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" placeholder="如:四川省" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="如:成都市" />
        </el-form-item>
        <el-form-item label="区/县">
          <el-input v-model="form.district" placeholder="如:高新区(可选)" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="街道、门牌号等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Address" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { listAddress, addAddress, editAddress, delAddress, setDefaultAddress } from '@/api/trade/address';
import { AddressForm, AddressVO } from '@/api/trade/types';
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus';

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const list = ref<AddressVO[]>([]);
const formRef = ref<FormInstance>();

const form = reactive<AddressForm>({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: ''
});

const rules: FormRules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await listAddress();
    list.value = res.data;
  } finally {
    loading.value = false;
  }
};

const openDialog = (item?: AddressVO) => {
  form.id = item?.id;
  form.receiver = item?.receiver || '';
  form.phone = item?.phone || '';
  form.province = item?.province || '';
  form.city = item?.city || '';
  form.district = item?.district || '';
  form.detail = item?.detail || '';
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    if (form.id) {
      await editAddress(form);
      ElMessage.success('修改成功');
    } else {
      await addAddress(form);
      ElMessage.success('新增成功');
    }
    dialogVisible.value = false;
    getList();
  } finally {
    submitting.value = false;
  }
};

const handleSetDefault = async (item: AddressVO) => {
  await setDefaultAddress(item.id);
  ElMessage.success('已设为默认地址');
  getList();
};

const handleDelete = async (item: AddressVO) => {
  await ElMessageBox.confirm('确认删除该地址?', '提示', { type: 'warning' });
  await delAddress(item.id);
  ElMessage.success('已删除');
  getList();
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.default-card {
  border-color: var(--el-color-success);
}
.addr-head {
  display: flex;
  align-items: center;
  gap: 10px;
  .receiver {
    font-weight: 600;
  }
  .phone {
    color: #909399;
  }
}
.addr-detail {
  margin-top: 10px;
  color: #606266;
  line-height: 1.6;
  min-height: 44px;
}
.addr-actions {
  margin-top: 10px;
  display: flex;
  gap: 4px;
}
</style>
