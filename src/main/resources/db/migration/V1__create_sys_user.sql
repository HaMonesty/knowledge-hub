CREATE TABLE sys_user (
                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                          username VARCHAR(32) NOT NULL COMMENT '规范化后的用户名',
                          password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
                          nickname VARCHAR(32) NOT NULL COMMENT '昵称',
                          role VARCHAR(16) NOT NULL DEFAULT 'USER' COMMENT '用户角色',
                          status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态',
                          created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        COMMENT '创建时间，按UTC保存',
                          updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT '更新时间，按UTC保存',

                          PRIMARY KEY (id),
                          UNIQUE KEY uk_sys_user_username (username),
                          CONSTRAINT ck_sys_user_role CHECK (role IN ('USER', 'ADMIN')),
                          CONSTRAINT ck_sys_user_status CHECK (status IN ('ACTIVE', 'DISABLED'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户表';

