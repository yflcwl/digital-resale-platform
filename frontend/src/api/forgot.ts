import request from '@/utils/request';
import { AxiosPromise } from 'axios';

/**
 * 发送重置密码验证码
 */
export const sendResetCode = (email: string): AxiosPromise<string> => {
  return request({
    url: '/auth/forgot/send-code',
    method: 'post',
    data: { email }
  });
};

/**
 * 重置密码
 */
export const resetPassword = (email: string, code: string, password: string): AxiosPromise<void> => {
  return request({
    url: '/auth/forgot/reset',
    method: 'post',
    data: { email, code, password }
  });
};
