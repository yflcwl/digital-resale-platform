-- ----------------------------
-- 开通用户注册(注册后由 SysRegisterService 自动绑定"普通用户"角色)
-- ----------------------------
update sys_config set config_value = 'true' where config_key = 'sys.account.registerUser';
