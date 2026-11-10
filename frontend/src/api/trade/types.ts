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
  brandName?: string;
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
  brandName?: string;
  categoryId?: number;
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
  brandName?: string;
  price: number;
  originalPrice?: number;
  conditionSelf?: string;
  images: string[];
}



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

/**
 * 收货地址 VO
 */
export interface AddressVO {
  id: number;
  receiver: string;
  phone: string;
  province: string;
  city: string;
  district?: string;
  detail: string;
  isDefault?: string;
}

/**
 * 收货地址表单
 */
export interface AddressForm {
  id?: number;
  receiver: string;
  phone: string;
  province: string;
  city: string;
  district?: string;
  detail: string;
}

/**
 * 订单 VO
 */
export interface OrderVO {
  id: number;
  orderNo: string;
  buyerId: number;
  sellerId: number;
  productId: number;
  productSnapshot?: string;
  price: number;
  status: string;
  addressSnapshot?: string;
  expressCompany?: string;
  expressNo?: string;
  timeoutTime?: string;
  payTime?: string;
  shipTime?: string;
  receiveTime?: string;
  finishTime?: string;
  cancelTime?: string;
  createTime?: string;
  statusLogs?: OrderStatusLogVO[];
  buyerNickname?: string;
  sellerNickname?: string;
  productTitle?: string;
  productImage?: string;
}

export interface OrderStatusLogVO {
  id: number;
  fromStatus?: string;
  toStatus: string;
  operatorType: string;
  remark?: string;
  createTime: string;
}

export interface OrderForm {
  productId: number;
  addressId: number;
}

export interface OrderQuery {
  pageNum: number;
  pageSize: number;
  role?: string;
  status?: string;
}

/**
 * 订单状态字典
 */
export const orderStatusDict: Record<string, string> = {
  PENDING_PAY: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  RECEIVED: '已收货',
  REVIEWED: '已评价',
  FINISHED: '已完成',
  CANCELED: '已取消'
};

export const orderStatusOptions = [
  { label: '待支付', value: 'PENDING_PAY' },
  { label: '已支付', value: 'PAID' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已收货', value: 'RECEIVED' },
  { label: '已完成', value: 'FINISHED' },
  { label: '已取消', value: 'CANCELED' }
];
