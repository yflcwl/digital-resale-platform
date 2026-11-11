<template>
  <div class="app-container">
    <header class="sm-page-head">
      <div>
        <h1 class="sm-page-title">收货地址</h1>
        <p class="sm-page-desc">下单时默认选中的地址，会优先展示在订单确认页。</p>
      </div>
      <el-button type="primary" @click="openDialog()">新增地址</el-button>
    </header>

    <div v-loading="loading" class="grid">
      <article v-for="item in list" :key="item.id" :class="['card', { default: item.isDefault === '1' }]">
        <header class="c-head">
          <span class="receiver">{{ item.receiver }}</span>
          <span class="phone">{{ item.phone }}</span>
          <span v-if="item.isDefault === '1'" class="badge">默认</span>
        </header>

        <p class="addr">{{ item.province }}{{ item.city }}{{ item.district || '' }}{{ item.detail }}</p>

        <footer class="c-foot">
          <button v-if="item.isDefault !== '1'" type="button" class="act" @click="handleSetDefault(item)">设为默认</button>
          <button type="button" class="act" @click="openDialog(item)">编辑</button>
          <button type="button" class="act is-danger" @click="handleDelete(item)">删除</button>
        </footer>
      </article>

      <button v-if="!loading" type="button" class="add-card" @click="openDialog()">
        <span class="plus">＋</span>
        <span>新增收货地址</span>
      </button>
    </div>

    <p v-if="!loading && list.length === 0" class="empty">还没有收货地址。填一个，下单时就不用再输。</p>

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
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 14px;
}

/* ---------- 地址卡 ---------- */
.card {
  display: flex;
  flex-direction: column;
  padding: 18px 20px 12px;
  border: 1px solid var(--sm-card-border);
  border-radius: 18px;
  background: var(--sm-paper);
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease;

  &:hover {
    border-color: var(--sm-line);
    box-shadow: 0 8px 24px -14px rgba(16, 45, 80, 0.26);
  }

  /* 默认地址：左侧一道蓝色竖条，不用整块描边抢戏 */
  &.default {
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 18px;
      bottom: 18px;
      width: 3px;
      border-radius: 0 3px 3px 0;
      background: var(--sm-brand);
    }
  }
}

.c-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.receiver {
  font-size: 15px;
  font-weight: 800;
  color: var(--sm-ink);
}

.phone {
  font-family: var(--sm-font-num);
  font-size: 13px;
  font-weight: 600;
  color: var(--sm-ink-3);
  font-variant-numeric: tabular-nums;
}

.badge {
  margin-left: auto;
  padding: 2px 8px;
  border-radius: var(--sm-r-pill);
  background: var(--sm-brand-soft);
  color: var(--sm-brand-deep);
  font-size: 10.5px;
  font-weight: 700;
}

.addr {
  flex: 1;
  margin: 10px 0 0;
  min-height: 42px;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.65;
  color: var(--sm-ink-2);
}

.c-foot {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px solid var(--sm-line-soft);
}

.act {
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 12px;
  font-weight: 600;
  color: var(--sm-ink-3);
  cursor: pointer;
  transition: color 0.16s ease;

  &:hover {
    color: var(--sm-brand-deep);
  }

  &.is-danger:hover {
    color: var(--sm-danger);
  }
}

/* ---------- 新增卡：虚线框 ---------- */
.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 148px;
  border: 1.5px dashed var(--sm-line);
  border-radius: 18px;
  background: transparent;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--sm-stone);
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    color 0.18s ease,
    background-color 0.18s ease;

  .plus {
    font-size: 22px;
    line-height: 1;
  }

  &:hover {
    border-color: var(--sm-brand);
    background: var(--sm-brand-tint);
    color: var(--sm-brand-deep);
  }
}

.empty {
  margin: 0;
  padding: 56px 0 0;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--sm-stone);
}

@media (max-width: 700px) {
  .grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
