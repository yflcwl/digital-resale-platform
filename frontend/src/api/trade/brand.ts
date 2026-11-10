import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { BrandVO } from './types';

/**
 * 查询品牌列表
 * @param categoryId 分类ID(可选)
 */
export const listBrand = (categoryId?: number | string): AxiosPromise<BrandVO[]> => {
  return request({
    url: '/trade/brand/list',
    method: 'get',
    params: { categoryId }
  });
};
