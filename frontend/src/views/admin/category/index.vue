<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>分类管理(最多三级)</span>
          <el-button type="primary" @click="openDialog()">新增顶级分类</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tree" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="分类名称" min-width="220" />
        <el-table-column prop="level" label="层级" width="80" align="center" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
              {{ scope.row.status === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.level < 3" link type="primary" @click="openDialog(undefined, scope.row)">新增子分类</el-button>
            <el-button link type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="440px" append-to-body>
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="上级分类">
          <span>{{ parentName || '顶级分类' }}</span>
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="图标URL">
          <el-input v-model="form.icon" placeholder="可选" />
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

<script setup name="AdminCategory" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { adminCategoryTree, adminAddCategory, adminEditCategory, adminDelCategory } from '@/api/admin';
import { CategoryTreeVO } from '@/api/trade/types';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const tree = ref<CategoryTreeVO[]>([]);
const parentName = ref('');

const form = reactive<Record<string, any>>({
  id: undefined,
  parentId: 0,
  name: '',
  icon: '',
  sort: 0,
  status: '0'
});

const getTree = async () => {
  loading.value = true;
  try {
    const res = await adminCategoryTree();
    tree.value = res.data;
  } finally {
    loading.value = false;
  }
};

const openDialog = (item?: CategoryTreeVO, parent?: CategoryTreeVO) => {
  form.id = item?.id;
  form.parentId = item ? item.parentId : (parent?.id ?? 0);
  form.name = item?.name || '';
  form.icon = item?.icon || '';
  form.sort = item?.sort ?? 0;
  form.status = item?.status || '0';
  parentName.value = item ? findName(tree.value, item.parentId) : parent?.name || '';
  dialogVisible.value = true;
};

const findName = (nodes: CategoryTreeVO[], id: number): string => {
  for (const n of nodes) {
    if (n.id === id) return n.name;
    if (n.children && n.children.length) {
      const r = findName(n.children, id);
      if (r) return r;
    }
  }
  return '';
};

const submit = async () => {
  if (!form.name) {
    ElMessage.warning('请输入分类名称');
    return;
  }
  submitting.value = true;
  try {
    if (form.id) {
      await adminEditCategory(form);
      ElMessage.success('修改成功');
    } else {
      await adminAddCategory(form);
      ElMessage.success('新增成功');
    }
    dialogVisible.value = false;
    getTree();
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (item: CategoryTreeVO) => {
  await ElMessageBox.confirm(`确认删除分类「${item.name}」?`, '提示', { type: 'warning' });
  await adminDelCategory(item.id);
  ElMessage.success('已删除');
  getTree();
};

onMounted(() => {
  getTree();
});
</script>

<style lang="scss" scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
