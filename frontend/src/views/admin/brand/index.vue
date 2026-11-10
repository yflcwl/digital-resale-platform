<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>品牌管理</span>
          <el-button type="primary" @click="openDialog()">新增品牌</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="品牌名称" min-width="160" />
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
              {{ scope.row.status === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑品牌' : '新增品牌'" width="420px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入品牌名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="form.logo" placeholder="可选" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminBrand" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { adminBrandList, adminAddBrand, adminEditBrand, adminDelBrand } from '@/api/admin';
import { BrandVO } from '@/api/trade/types';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const list = ref<BrandVO[]>([]);

const form = reactive<Record<string, any>>({
  id: undefined,
  name: '',
  logo: '',
  sort: 0,
  status: '0'
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await adminBrandList();
    list.value = res.data;
  } finally {
    loading.value = false;
  }
};

const openDialog = (item?: BrandVO) => {
  form.id = item?.id;
  form.name = item?.name || '';
  form.logo = item?.logo || '';
  form.sort = item?.sort ?? 0;
  form.status = item?.status || '0';
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.name) {
    ElMessage.warning('请输入品牌名称');
    return;
  }
  submitting.value = true;
  try {
    if (form.id) {
      await adminEditBrand(form);
      ElMessage.success('修改成功');
    } else {
      await adminAddBrand(form);
      ElMessage.success('新增成功');
    }
    dialogVisible.value = false;
    getList();
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (item: BrandVO) => {
  await ElMessageBox.confirm(`确认删除品牌「${item.name}」?`, '提示', { type: 'warning' });
  await adminDelBrand(item.id);
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
</style>
