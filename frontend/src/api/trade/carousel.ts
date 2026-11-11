import request from '@/utils/request';
import { AxiosPromise } from 'axios';

export interface CarouselVO {
  id: number;
  title: string;
  imageUrl: string;
  link?: string;
  sort?: number;
}

/**
 * 首页轮播图列表(公开)
 */
export const listCarousel = (): AxiosPromise<CarouselVO[]> => {
  return request({ url: '/trade/carousel/list', method: 'get' });
};
