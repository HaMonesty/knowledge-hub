CREATE TABLE knowledge (
                           id BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识ID',
                           title VARCHAR(200) NOT NULL COMMENT '标题',
                           summary VARCHAR(500) DEFAULT NULL COMMENT '摘要',
                           content MEDIUMTEXT COMMENT 'Markdown正文',
                           category_id BIGINT DEFAULT NULL COMMENT '分类ID',
                           author_id BIGINT NOT NULL COMMENT '作者ID',
                           status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
                           created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                           updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                               ON UPDATE CURRENT_TIMESTAMP(3),
                           published_at DATETIME(3) DEFAULT NULL COMMENT '发布时间',

                           PRIMARY KEY (id),
                           KEY idx_knowledge_author_status (author_id, status),
                           KEY idx_knowledge_category (category_id),

                           CONSTRAINT ck_knowledge_status
                               CHECK (status IN ('DRAFT', 'PUBLISHED')),
                           CONSTRAINT fk_knowledge_category
                               FOREIGN KEY (category_id) REFERENCES category (id),
                           CONSTRAINT fk_knowledge_author
                               FOREIGN KEY (author_id) REFERENCES sys_user (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='知识表';