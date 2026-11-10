/**
 * 分类树 VO
 */
export interface CategoryTreeVO {
  id: number;
  parentId: number;
  name: string;
  icon?: string;
  level: number;
  sort: number;
  children?: CategoryTreeVO[];
}

/**
 * 品牌 VO
 */
export interface BrandVO {
  id: number;
  name: string;
  logo?: string;
  sort: number;
}

/**
 * 商品图片
 */
export interface ProductImageVO {
  id?: number;
  url: string;
  sort?: number;
  isMain?: string;
}

/**
 * 商品 VO
 */
export interface ProductVO {
  id: number;
  sellerId: number;
  categoryId: number;
  brandId?: number;
  title: string;
  description?: string;
  price: number;
  originalPrice?: number;
  conditionSelf?: string;
  qualityGrade?: string;
  status: string;
  viewCount?: number;
  favoriteCount?: number;
  latestReportId?: number;
  createTime?: string;
  images?: ProductImageVO[];
  categoryName?: string;
  brandName?: string;
  sellerNickname?: string;
  sellerAvatar?: string;
  sellerRating?: number;
  favorite?: boolean;
}

/**
 * 商品查询参数
 */
export interface ProductQuery {
  pageNum: number;
  pageSize: number;
  orderByColumn?: string;
  isAsc?: string;
  title?: string;
  categoryId?: number;
  brandId?: number;
  conditionSelf?: string;
  qualityGrade?: string;
  priceMin?: number;
  priceMax?: number;
  status?: string;
}

/**
 * 商品表单(发布/编辑)
 */
export interface ProductForm {
  id?: number;
  title: string;
  description?: string;
  categoryId: number;
  brandId?: number;
  price: number;
  originalPrice?: number;
  conditionSelf?: string;
  images: string[];
}

/**
 * 新旧程度选项
 */
export const conditionSelfOptions = [
  { label: '99新(EXCELLENT)', value: 'EXCELLENT' },
  { label: '95新(GOOD)', value: 'GOOD' },
  { label: '9成新(FAIR)', value: 'FAIR' },
  { label: '8成新及以下(POOR)', value: 'POOR' }
];

/**
 * 商品状态选项
 */
export const productStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '待质检', value: 'WAITING_QC' },
  { label: '在售', value: 'ON_SALE' },
  { label: '已锁定', value: 'LOCKED' },
  { label: '已售出', value: 'SOLD' },
  { label: '已下架', value: 'OFF_SHELF' }
];

/**
 * 商品状态字典
 */
export const productStatusDict: Record<string, string> = {
  DRAFT: '草稿',
  WAITING_QC: '待质检',
  ON_SALE: '在售',
  LOCKED: '已锁定',
  SOLD: '已售出',
  OFF_SHELF: '已下架',
  AUDITING: '审核中',
  REJECTED: '审核拒绝'
};
