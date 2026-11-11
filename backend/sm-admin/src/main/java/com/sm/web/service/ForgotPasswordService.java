package com.sm.web.service;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.extra.mail.MailAccount;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.core.exception.ServiceException;
import com.sm.common.core.utils.StringUtils;
import com.sm.common.mail.utils.MailUtils;
import com.sm.system.domain.SysUser;
import com.sm.system.mapper.SysUserMapper;
import com.sm.web.domain.EmailCode;
import com.sm.web.mapper.EmailCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 找回密码服务(邮箱验证码)
 *
 * @author sm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ForgotPasswordService {

    private static final String BIZ_RESET = "RESET";
    /** 验证码有效期(毫秒) */
    private static final long CODE_TTL = 5 * 60 * 1000L;

    private final SysUserMapper sysUserMapper;
    private final EmailCodeMapper emailCodeMapper;

    /** 邮件账号 Bean(未启用邮件时为空,退化为本地演示模式) */
    private final ObjectProvider<MailAccount> mailAccountProvider;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    /**
     * 发送重置密码验证码
     *
     * @return 邮件未启用时返回验证码(本地演示),发送成功返回 null
     */
    public String sendResetCode(String email) {
        validateEmail(email);
        if (findUserByEmail(email) == null) {
            throw new ServiceException("该邮箱未绑定任何账号");
        }
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        EmailCode emailCode = new EmailCode();
        emailCode.setEmail(email);
        emailCode.setCode(code);
        emailCode.setBizType(BIZ_RESET);
        emailCode.setExpireTime(new Date(System.currentTimeMillis() + CODE_TTL));
        emailCode.setUsed("0");
        emailCode.setCreateTime(new Date());
        emailCodeMapper.insert(emailCode);

        if (mailEnabled && mailAccountProvider.getIfAvailable() != null) {
            try {
                MailUtils.sendText(email, "二手数码交易平台 - 重置密码",
                    "您正在重置登录密码,验证码:" + code + ",5 分钟内有效。如非本人操作请忽略。");
                return null;
            } catch (Throwable t) {
                log.warn("邮件发送失败,验证码降级返回: {}", email, t);
            }
        } else {
            log.info("未启用邮件服务,验证码降级返回(演示模式): {}", email);
        }
        // 本地未启用邮件时,将验证码返回前端提示,便于毕业设计演示
        return code;
    }

    /**
     * 校验验证码并重置密码
     */
    public void resetPassword(String email, String code, String newPassword) {
        validateEmail(email);
        if (StringUtils.isBlank(code) || StringUtils.isBlank(newPassword)) {
            throw new ServiceException("验证码与新密码不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new ServiceException("密码长度需在 6~20 位之间");
        }
        SysUser user = findUserByEmail(email);
        if (user == null) {
            throw new ServiceException("该邮箱未绑定任何账号");
        }
        EmailCode emailCode = emailCodeMapper.selectOne(Wrappers.<EmailCode>lambdaQuery()
            .eq(EmailCode::getEmail, email)
            .eq(EmailCode::getCode, code)
            .eq(EmailCode::getBizType, BIZ_RESET)
            .eq(EmailCode::getUsed, "0")
            .orderByDesc(EmailCode::getCreateTime)
            .last("limit 1"));
        if (emailCode == null) {
            throw new ServiceException("验证码错误");
        }
        if (emailCode.getExpireTime() == null || emailCode.getExpireTime().before(new Date())) {
            throw new ServiceException("验证码已过期,请重新获取");
        }
        // 注解 SQL 更新:不触发创建/更新人自动填充(匿名接口无登录上下文)
        sysUserMapper.updatePassword(user.getUserId(), BCrypt.hashpw(newPassword));
        // 验证码置为已使用(EmailCode 非 BaseEntity,不涉及登录人填充)
        EmailCode update = new EmailCode();
        update.setId(emailCode.getId());
        update.setUsed("1");
        emailCodeMapper.updateById(update);
    }

    private SysUser findUserByEmail(String email) {
        return sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
            .eq(SysUser::getEmail, email)
            .eq(SysUser::getDelFlag, "0")
            .last("limit 1"));
    }

    private void validateEmail(String email) {
        if (StringUtils.isBlank(email) || !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new ServiceException("请输入正确的邮箱地址");
        }
    }
}
