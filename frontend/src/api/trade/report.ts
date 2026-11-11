import request from '@/utils/request';
import { AxiosPromise } from 'axios';

/**
 * 提交商品举报
 */
export const submitReport = (productId: number | string, reason: string, content?: string): AxiosPromise<void> => {
  return request({
    url: '/trade/report',
    method: 'post',
    data: { productId, reason, content }
  });
};
