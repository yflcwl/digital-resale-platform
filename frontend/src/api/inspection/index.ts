import request from '@/utils/request';
import { AxiosPromise } from 'axios';

/**
 * 拍照引导清单
 */
export const getGuide = (categoryId: number | string): AxiosPromise<{ angle: string; tip: string }[]> => {
  return request({ url: `/inspection/guide/${categoryId}`, method: 'get' });
};

/**
 * 提交质检
 */
export const submitInspection = (productId: number | string, photoUrls: string[]): AxiosPromise<number> => {
  return request({ url: '/inspection/task', method: 'post', data: { productId, photoUrls } });
};

/**
 * 补拍提交
 */
export const reshootInspection = (taskId: number | string, photoUrls: string[]): AxiosPromise<number> => {
  return request({ url: `/inspection/task/${taskId}/reshoot`, method: 'post', data: { photoUrls } });
};

/**
 * 任务状态查询
 */
export const getTask = (taskId: number | string): AxiosPromise<any> => {
  return request({ url: `/inspection/task/${taskId}`, method: 'get' });
};

/**
 * 报告详情
 */
export const getReport = (reportId: number | string): AxiosPromise<any> => {
  return request({ url: `/inspection/report/${reportId}`, method: 'get' });
};

/**
 * 商品最新有效报告
 */
export const getProductReport = (productId: number | string): AxiosPromise<any> => {
  return request({ url: `/inspection/product/${productId}/report`, method: 'get' });
};

/**
 * 报告反馈
 */
export const feedbackReport = (reportId: number | string, content: string): AxiosPromise<void> => {
  return request({ url: `/inspection/report/${reportId}/feedback`, method: 'post', data: { content } });
};
