import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TableDataInfo } from '@/types/global';
import { ProductVO } from './types';

export interface SellerHomeVO {
  userId: number;
  nickname: string;
  avatar?: string;
  bio?: string;
  sellerRating?: number;
  ratingCount?: number;
  dealCount?: number;
  onSaleCount?: number;
}

export interface SellerReviewVO {
  id: number;
  orderId: number;
  rating: number;
  content?: string;
  createTime?: string;
  reviewerNickname?: string;
  reviewerAvatar?: string;
}

/**
 * 卖家主页信息
 */
export const getSellerHome = (sellerId: number | string): AxiosPromise<SellerHomeVO> => {
  return request({ url: `/trade/seller/${sellerId}`, method: 'get' });
};

/**
 * 卖家在售商品
 */
export const pageSellerProducts = (sellerId: number | string, pageNum = 1, pageSize = 12): AxiosPromise<TableDataInfo<ProductVO>> => {
  return request({ url: `/trade/seller/${sellerId}/products`, method: 'get', params: { pageNum, pageSize } });
};

/**
 * 卖家收到的评价
 */
export const pageSellerReviews = (sellerId: number | string, pageNum = 1, pageSize = 10): AxiosPromise<TableDataInfo<SellerReviewVO>> => {
  return request({ url: `/trade/seller/${sellerId}/reviews`, method: 'get', params: { pageNum, pageSize } });
};
