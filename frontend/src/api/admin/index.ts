import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TableDataInfo } from '@/types/global';
import { BrandVO, CategoryTreeVO, OrderVO, ProductVO } from '@/api/trade/types';

/* ---------- 仪表盘 ---------- */

export interface DashboardData {
  overview: {
    userCount: number;
    productCount: number;
    onSaleCount: number;
    orderCount: number;
    todayOrderCount: number;
    reportCount: number;
  };
  orderTrend: { day: string; cnt: number }[];
  orderStatusDist: { name: string; value: number }[];
  categoryDist: { name: string; value: number }[];
}

export const getDashboard = (): AxiosPromise<DashboardData> => {
  return request({ url: '/admin/dashboard', method: 'get' });
};

/* ---------- 商品 ---------- */

export const pageAdminProduct = (query: Record<string, any>): AxiosPromise<TableDataInfo<ProductVO>> => {
  return request({ url: '/admin/product/page', method: 'get', params: query });
};

export const adminOffShelf = (id: number | string): AxiosPromise<void> => {
  return request({ url: `/admin/product/${id}/off-shelf`, method: 'post' });
};

export const adminDelProduct = (id: number | string): AxiosPromise<void> => {
  return request({ url: `/admin/product/${id}`, method: 'delete' });
};

/* ---------- 分类 ---------- */

export const adminCategoryTree = (): AxiosPromise<CategoryTreeVO[]> => {
  return request({ url: '/admin/category/tree', method: 'get' });
};

export const adminAddCategory = (data: Record<string, any>): AxiosPromise<number> => {
  return request({ url: '/admin/category', method: 'post', data });
};

export const adminEditCategory = (data: Record<string, any>): AxiosPromise<void> => {
  return request({ url: '/admin/category', method: 'put', data });
};

export const adminDelCategory = (id: number | string): AxiosPromise<void> => {
  return request({ url: `/admin/category/${id}`, method: 'delete' });
};

/* ---------- 品牌 ---------- */

export const adminBrandList = (categoryId?: number | string): AxiosPromise<BrandVO[]> => {
  return request({ url: '/admin/brand/list', method: 'get', params: { categoryId } });
};

export const adminAddBrand = (data: Record<string, any>): AxiosPromise<number> => {
  return request({ url: '/admin/brand', method: 'post', data });
};

export const adminEditBrand = (data: Record<string, any>): AxiosPromise<void> => {
  return request({ url: '/admin/brand', method: 'put', data });
};

export const adminDelBrand = (id: number | string): AxiosPromise<void> => {
  return request({ url: `/admin/brand/${id}`, method: 'delete' });
};

/* ---------- 订单 ---------- */

export const pageAdminOrder = (query: Record<string, any>): AxiosPromise<TableDataInfo<OrderVO>> => {
  return request({ url: '/admin/order/page', method: 'get', params: query });
};

export const getAdminOrder = (id: number | string): AxiosPromise<OrderVO> => {
  return request({ url: `/admin/order/${id}`, method: 'get' });
};

/* ---------- 质检报告 ---------- */

export const pageAdminReport = (query: Record<string, any>): AxiosPromise<TableDataInfo<any>> => {
  return request({ url: '/admin/inspection/report/page', method: 'get', params: query });
};

export const getAdminReport = (id: number | string): AxiosPromise<any> => {
  return request({ url: `/admin/inspection/report/${id}`, method: 'get' });
};
