CREATE TABLE sys_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE COMMENT '登录名',
    password        VARCHAR(255) NOT NULL COMMENT 'BCrypt加密',
    nickname        VARCHAR(50) COMMENT '显示昵称',
    avatar          VARCHAR(500) COMMENT '头像URL',
    storage_used    BIGINT DEFAULT 0 COMMENT '已用存储(bytes)',
    storage_limit   BIGINT DEFAULT 53687091200 COMMENT '50G上限',
    status          TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE file_info (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL COMMENT '所属用户',
    parent_id        BIGINT DEFAULT 0 COMMENT '父文件夹ID(0=根目录)',
    file_name        VARCHAR(255) NOT NULL COMMENT '文件名/文件夹名',
    file_type        VARCHAR(20) COMMENT 'IMAGE/VIDEO/DOCUMENT/OTHER',
    file_size        BIGINT DEFAULT 0 COMMENT '文件大小(bytes)',
    file_md5         VARCHAR(32) COMMENT '文件MD5',
    mime_type        VARCHAR(100) COMMENT 'MIME类型',
    is_dir           TINYINT DEFAULT 0 COMMENT '0文件 1文件夹',
    storage_path     VARCHAR(500) COMMENT 'MinIO存储路径',
    thumbnail_200    VARCHAR(500) COMMENT '小缩略图路径',
    thumbnail_800    VARCHAR(500) COMMENT '大缩略图路径',
    thumbnail_status TINYINT DEFAULT 0 COMMENT '0生成中 1完成 2失败',
    cover_time       VARCHAR(20) COMMENT '视频封面时间点',
    date_taken       DATETIME COMMENT '拍摄日期',
    width            INT COMMENT '图片/视频宽度',
    height           INT COMMENT '图片/视频高度',
    duration         INT COMMENT '视频时长(秒)',
    is_deleted       TINYINT DEFAULT 0 COMMENT '0正常 1已删除',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_parent (user_id, parent_id),
    INDEX idx_user_type (user_id, file_type),
    INDEX idx_user_md5 (user_id, file_md5),
    INDEX idx_user_deleted (user_id, is_deleted),
    INDEX idx_user_type_deleted_date (user_id, file_type, is_deleted, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件/文件夹表';

CREATE TABLE upload_task (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    file_md5        VARCHAR(32) NOT NULL COMMENT '完整文件MD5',
    file_name       VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size       BIGINT NOT NULL COMMENT '文件大小',
    total_chunks    INT NOT NULL COMMENT '总分片数',
    uploaded_chunks INT DEFAULT 0 COMMENT '已上传分片数',
    status          TINYINT DEFAULT 0 COMMENT '0上传中 1合并中 2完成 3失败',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传任务表';

CREATE TABLE file_chunk (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id      BIGINT COMMENT '关联upload_task.id',
    file_md5     VARCHAR(32) NOT NULL COMMENT '完整文件MD5',
    chunk_index  INT NOT NULL COMMENT '分片序号(从0开始)',
    chunk_md5    VARCHAR(32) COMMENT '分片MD5',
    chunk_size   BIGINT COMMENT '分片大小',
    storage_path VARCHAR(500) COMMENT 'MinIO临时路径',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_md5_chunk (file_md5, chunk_index),
    INDEX idx_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传记录表';

CREATE TABLE audit_log (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT COMMENT '用户ID',
    action     VARCHAR(50) NOT NULL COMMENT '操作类型',
    target     VARCHAR(500) COMMENT '操作对象',
    ip         VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';
