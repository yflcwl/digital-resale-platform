import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TableDataInfo } from '@/types/global';
import { NotificationVO } from './types';

/**
 * 我的通知分页
 */
export const pageNotification = (query: { pageNum: number; pageSize: number; type?: string }): AxiosPromise<TableDataInfo<NotificationVO>> => {
  return request({
    url: '/message/notification/page',
    method: 'get',
    params: query
  });
};

/**
 * 单条已读
 */
export const readNotification = (id: number | string): AxiosPromise<void> => {
  return request({
    url: `/message/notification/${id}/read`,
    method: 'post'
  });
};

/**
 * 全部已读
 */
export const readAllNotifications = (): AxiosPromise<void> => {
  return request({
    url: '/message/notification/read-all',
    method: 'post'
  });
};

/**
 * 通知未读数
 */
export const notificationUnreadCount = (): AxiosPromise<number> => {
  return request({
    url: '/message/notification/unread-count',
    method: 'get'
  });
};
