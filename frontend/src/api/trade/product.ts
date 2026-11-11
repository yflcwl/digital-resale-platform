import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ProductForm, ProductQuery, ProductVO } from './types';
import { TableDataInfo } from '@/types/global';

/**
 * 在售商品分页列表
 * @param query
 */
export const listOnSaleProduct = (query: ProductQuery): AxiosPromise<TableDataInfo<ProductVO>> => {
  return request({
    url: '/trade/product/page',
    method: 'get',
    params: query
  });
};

/**
 * 我的商品分页列表
 * @param query
 */
export const listMyProduct = (query: ProductQuery): AxiosPromise<TableDataInfo<ProductVO>> => {
  return request({
    url: '/trade/product/my',
    method: 'get',
    params: query
  });
};

/**
 * 商品详情
 * @param id
 */
export const getProduct = (id: number | string): AxiosPromise<ProductVO> => {
  return request({
    url: '/trade/product/' + id,
    method: 'get'
  });
};

/**
 * 发布商品(草稿)
 * @param data
 */
export const publishProduct = (data: ProductForm): AxiosPromise<number> => {
  return request({
    url: '/trade/product',
    method: 'post',
    data
  });
};

/**
 * 编辑商品
 * @param data
 */
export const editProduct = (data: ProductForm): AxiosPromise<void> => {
  return request({
    url: '/trade/product',
    method: 'put',
    data
  });
};

/**
 * 下架商品
 * @param id
 */
export const offShelfProduct = (id: number | string): AxiosPromise<void> => {
  return request({
    url: '/trade/product/' + id + '/off-shelf',
    method: 'post'
  });
};

/**
 * 重新上架
 * @param id
 */
export const onShelfProduct = (id: number | string): AxiosPromise<void> => {
  return request({
    url: '/trade/product/' + id + '/on-shelf',
    method: 'post'
  });
};

/**
 * 删除商品
 * @param id
 */
export const delProduct = (id: number | string): AxiosPromise<void> => {
  return request({
    url: '/trade/product/' + id,
    method: 'delete'
  });
};

/**
 * 收藏商品
 * @param id
 */
export const favoriteProduct = (id: number | string): AxiosPromise<void> => {
  return request({
    url: '/trade/product/' + id + '/favorite',
    method: 'post'
  });
};

/**
 * 取消收藏
 * @param id
 */
export const cancelFavoriteProduct = (id: number | string): AxiosPromise<void> => {
  return request({
    url: '/trade/product/' + id + '/favorite',
    method: 'delete'
  });
};

/**
 * 我的收藏分页
 * @param query
 */
export const listFavoriteProduct = (query: { pageNum: number; pageSize: number }): AxiosPromise<TableDataInfo<ProductVO>> => {
  return request({
    url: '/trade/product/favorite/page',
    method: 'get',
    params: query
  });
};

/**
 * 我的浏览历史
 */
export const listBrowseHistory = (): AxiosPromise<ProductVO[]> => {
  return request({
    url: '/trade/product/browse',
    method: 'get'
  });
};

/**
 * 首页精选商品
 */
export const listFeaturedProduct = (limit = 8): AxiosPromise<ProductVO[]> => {
  return request({
    url: '/trade/product/featured',
    method: 'get',
    params: { limit }
  });
};