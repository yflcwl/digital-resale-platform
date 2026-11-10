import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AddressVO, AddressForm } from './types';

/**
 * 我的地址列表
 */
export const listAddress = (): AxiosPromise<AddressVO[]> => {
  return request({
    url: '/trade/address/list',
    method: 'get'
  });
};

/**
 * 新增地址
 */
export const addAddress = (data: AddressForm): AxiosPromise<number> => {
  return request({
    url: '/trade/address',
    method: 'post',
    data
  });
};

/**
 * 修改地址
 */
export const editAddress = (data: AddressForm): AxiosPromise<void> => {
  return request({
    url: '/trade/address',
    method: 'put',
    data
  });
};

/**
 * 删除地址
 */
export const delAddress = (id: number | string): AxiosPromise<void> => {
  return request({
    url: '/trade/address/' + id,
    method: 'delete'
  });
};

/**
 * 设为默认
 */
export const setDefaultAddress = (id: number | string): AxiosPromise<void> => {
  return request({
    url: '/trade/address/' + id + '/default',
    method: 'post'
  });
};

/**
 * 整段地址智能识别
 */
export const parseAddress = (text: string): AxiosPromise<{ receiver?: string; phone?: string; province?: string; city?: string; district?: string; detail?: string }> => {
  return request({
    url: '/trade/address/parse',
    method: 'post',
    data: { text }
  });
};