import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { OrderForm, OrderQuery, OrderVO } from './types';
import { TableDataInfo } from '@/types/global';

/**
 * 创建订单
 */
export const createOrder = (data: OrderForm): AxiosPromise<number> => {
  return request({
    url: '/trade/order',
    method: 'post',
    data
  });
};

/**
 * 模拟支付
 */
export const payOrder = (id: number | string, payType: string): AxiosPromise<void> => {
  return request({
    url: '/trade/order/' + id + '/pay',
    method: 'post',
    params: { payType }
  });
};

/**
 * 取消订单
 */
export const cancelOrder = (id: number | string): AxiosPromise<void> => {
  return request({
    url: '/trade/order/' + id + '/cancel',
    method: 'post'
  });
};

/**
 * 卖家发货
 */
export const shipOrder = (id: number | string, data: { expressCompany: string; expressNo: string }): AxiosPromise<void> => {
  return request({
    url: '/trade/order/' + id + '/ship',
    method: 'post',
    data
  });
};

/**
 * 确认收货
 */
export const receiveOrder = (id: number | string): AxiosPromise<void> => {
  return request({
    url: '/trade/order/' + id + '/receive',
    method: 'post'
  });
};

/**
 * 评价
 */
export const reviewOrder = (id: number | string, data: { rating: number; content?: string }): AxiosPromise<void> => {
  return request({
    url: '/trade/order/' + id + '/review',
    method: 'post',
    data
  });
};

/**
 * 我的订单分页
 */
export const listMyOrder = (query: OrderQuery): AxiosPromise<TableDataInfo<OrderVO>> => {
  return request({
    url: '/trade/order/page',
    method: 'get',
    params: query
  });
};

/**
 * 订单详情
 */
export const getOrder = (id: number | string): AxiosPromise<OrderVO> => {
  return request({
    url: '/trade/order/' + id,
    method: 'get'
  });
};
