import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { CategoryTreeVO } from './types';

/**
 * 查询分类树
 */
export const listCategoryTree = (): AxiosPromise<CategoryTreeVO[]> => {
  return request({
    url: '/trade/category/tree',
    method: 'get'
  });
};
