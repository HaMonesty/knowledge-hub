CREATE TABLE category (
                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                          name VARCHAR(32) NOT NULL COMMENT '分类名称',
                          created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                          updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                              ON UPDATE CURRENT_TIMESTAMP(3),

                          PRIMARY KEY (id),
                          UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='知识分类表';