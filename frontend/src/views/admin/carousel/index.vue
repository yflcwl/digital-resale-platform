<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>轮播图管理(展示于用户首页)</span>
          <el-button type="primary" @click="openDialog()">新增轮播图</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list">
        <el-table-column label="图片" width="180" align="center">
          <template #default="scope">
            <el-image :src="scope.row.imageUrl" fit="cover" style="width: 150px; height: 60px; border-radius: 6px" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="link" label="跳转链接" min-width="140" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
              {{ scope.row.status === '0' ? '展示中' : '已隐藏' }}
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑轮播图' : '新增轮播图'" width="520px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="100" placeholder="展示在横幅上的标题" />
        </el-form-item>
        <el-form-item label="图片" required>
          <ImageUpload v-model="imageUrls" :limit="1" />
          <div class="tip">建议尺寸 1400x300 以上的横图</div>
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.link" placeholder="如 /trade/market,留空跳转市场页" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">展示中</el-radio>
            <el-radio value="1">隐藏</el-radio>
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

<script setup name="AdminCarousel" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { adminCarouselList, adminAddCarousel, adminEditCarousel, adminDelCarousel } from '@/api/admin';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const list = ref<any[]>([]);
const imageUrls = ref<string[]>([]);

const form = reactive<Record<string, any>>({
  id: undefined,
  title: '',
  link: '',
  sort: 0,
  status: '0'
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await adminCarouselList();
    list.value = res.data || [];
  } finally {
    loading.value = false;
  }
};

const openDialog = (row?: any) => {
  form.id = row?.id;
  form.title = row?.title || '';
  form.link = row?.link || '';
  form.sort = row?.sort ?? 0;
  form.status = row?.status || '0';
  imageUrls.value = row?.imageUrl ? [row.imageUrl] : [];
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.title) {
    ElMessage.warning('请输入标题');
    return;
  }
  if (imageUrls.value.length === 0) {
    ElMessage.warning('请上传图片');
    return;
  }
  submitting.value = true;
  try {
    const payload = { ...form, imageUrl: imageUrls.value[0] };
    if (form.id) {
      await adminEditCarousel(payload);
      ElMessage.success('修改成功');
    } else {
      await adminAddCarousel(payload);
      ElMessage.success('新增成功');
    }
    dialogVisible.value = false;
    getList();
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除轮播图「${row.title}」?`, '提示', { type: 'warning' });
  await adminDelCarousel(row.id);
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
.tip {
  color: var(--sm-stone);
  font-size: 12px;
}
</style>
