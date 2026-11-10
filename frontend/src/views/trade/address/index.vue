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
        <el-form-item label="智能识别">
          <div class="parse-box">
            <el-input
              v-model="smartText"
              type="textarea"
              :rows="2"
              placeholder="粘贴一整段地址,自动识别,如:张三 13800000000 四川省成都市高新区天府大道100号"
              maxlength="300"
            />
            <el-button type="primary" plain :loading="parsing" style="margin-top: 8px" @click="handleParse">识别并填充</el-button>
          </div>
        </el-form-item>
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="所在地区" prop="province">
          <el-cascader
            v-model="regionValue"
            :options="regionOptions"
            :props="{ value: 'value', label: 'label' }"
            placeholder="请选择省 / 市 / 区"
            clearable
            style="width: 100%"
            @change="handleRegionChange"
          />
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
import { listAddress, addAddress, editAddress, delAddress, setDefaultAddress, parseAddress } from '@/api/trade/address';
import { AddressForm, AddressVO } from '@/api/trade/types';
import { pcaTextArr } from 'element-china-area-data';
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus';

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const list = ref<AddressVO[]>([]);
const formRef = ref<FormInstance>();
const regionOptions = pcaTextArr;
const regionValue = ref<string[]>([]);
const smartText = ref('');
const parsing = ref(false);

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
  province: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
};

const handleParse = async () => {
  if (!smartText.value.trim()) {
    ElMessage.warning('请先粘贴地址文字');
    return;
  }
  parsing.value = true;
  try {
    const res = await parseAddress(smartText.value.trim());
    const r = res.data || {};
    if (r.receiver) form.receiver = r.receiver;
    if (r.phone) form.phone = r.phone;
    if (r.province) form.province = r.province;
    if (r.city) form.city = r.city;
    if (r.district) form.district = r.district;
    if (r.detail) form.detail = r.detail;

    // 尝试按名称回填省市区级联选择器
    const matchRegion = (province?: string, city?: string, district?: string) => {
      const p = pcaTextArr.find((x: any) => x.label === province);
      if (!p) return [];
      const c = (p as any).children?.find((x: any) => x.label === city);
      if (!c) return [province];
      const d = c.children?.find((x: any) => x.label === district);
      return d ? [province, city, district] : [province, city];
    };
    regionValue.value = matchRegion(r.province, r.city, r.district) as string[];

    const filled = [r.receiver, r.phone, r.province, r.city, r.detail].filter(Boolean).length;
    if (filled === 0) {
      ElMessage.warning('未能识别出有效信息,请手动填写');
    } else {
      ElMessage.success('识别完成,请核对信息' + (regionValue.value.length < 3 ? '并确认地区' : ''));
    }
  } finally {
    parsing.value = false;
  }
};

const handleRegionChange = (val: string[] | null) => {
  form.province = val?.[0] || '';
  form.city = val?.[1] || '';
  form.district = val?.[2] || '';
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
  regionValue.value = item ? [item.province, item.city, item.district || ''].filter(Boolean) : [];
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
    color: var(--sm-ink-3);
  }
}
.addr-detail {
  margin-top: 10px;
  color: var(--sm-ink-2);
  line-height: 1.6;
  min-height: 44px;
}
.addr-actions {
  margin-top: 10px;
  display: flex;
  gap: 4px;
}
</style>
