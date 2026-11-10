<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <span>{{ isEdit ? '编辑商品' : '发布商品' }}</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 760px">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-cascader
            v-model="categoryValue"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'name', checkStrictly: true, emitPath: true }"
            placeholder="请选择分类"
            style="width: 100%"
            @change="handleCategoryChange"
          />
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="form.brandId" placeholder="请选择品牌" clearable style="width: 100%">
            <el-option v-for="b in brandList" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="10" style="width: 200px" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="10" style="width: 200px" />
        </el-form-item>
        <el-form-item label="新旧程度">
          <el-select v-model="form.conditionSelf" placeholder="请选择新旧程度" style="width: 200px">
            <el-option v-for="c in conditionSelfOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品描述" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <ImageUpload v-model="form.images" :limit="8" />
          <div class="upload-tip">最多 8 张,第一张为主图;更换图片后需重新质检</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="ProductPublish" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { listCategoryTree } from '@/api/trade/category';
import { listBrand } from '@/api/trade/brand';
import { getProduct, publishProduct, editProduct } from '@/api/trade/product';
import { conditionSelfOptions, ProductForm, CategoryTreeVO, BrandVO } from '@/api/trade/types';
import { ElMessage, FormInstance, FormRules } from 'element-plus';

const route = useRoute();
const router = useRouter();
const isEdit = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const categoryOptions = ref<CategoryTreeVO[]>([]);
const brandList = ref<BrandVO[]>([]);
const categoryValue = ref<number[]>([]);

const form = reactive<ProductForm>({
  title: '',
  description: '',
  categoryId: 0,
  price: 0,
  images: []
});

const rules: FormRules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  images: [{ type: 'array', min: 1, message: '请至少上传一张图片', trigger: 'change' }]
};

const handleCategoryChange = (val: number[]) => {
  form.categoryId = val && val.length ? val[val.length - 1] : 0;
  form.brandId = undefined;
  brandList.value = [];
  if (form.categoryId) {
    listBrand(form.categoryId).then((res) => {
      brandList.value = res.data;
    });
  }
};

const submit = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    if (isEdit.value) {
      await editProduct(form);
      ElMessage.success('保存成功');
    } else {
      await publishProduct(form);
      ElMessage.success('发布成功,请等待质检后上架');
    }
    router.push('/trade/my');
  } finally {
    submitting.value = false;
  }
};

const goBack = () => {
  router.back();
};

onMounted(async () => {
  const catRes = await listCategoryTree();
  categoryOptions.value = catRes.data;
  const editId = route.query.editId as string;
  if (editId) {
    isEdit.value = true;
    const res = await getProduct(editId);
    const detail = res.data;
    form.id = detail.id;
    form.title = detail.title;
    form.description = detail.description;
    form.categoryId = detail.categoryId;
    form.brandId = detail.brandId;
    form.price = detail.price;
    form.originalPrice = detail.originalPrice;
    form.conditionSelf = detail.conditionSelf;
    form.images = (detail.images || []).map((i) => i.url);
    categoryValue.value = [detail.categoryId];
    if (detail.categoryId) {
      listBrand(detail.categoryId).then((res) => {
        brandList.value = res.data;
      });
    }
  }
});
</script>

<style lang="scss" scoped>
.upload-tip {
  color: var(--sm-ink-3);
  font-size: 12px;
  line-height: 1.6;
}
</style>
