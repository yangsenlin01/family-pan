# 家庭云盘 — 整体规划方案 v1.2

---

## 一、竞品调研

### 1.1 主流自建云盘/照片管理软件

| 软件 | 定位 | 核心优势 | 核心劣势 | 适用场景 |
|------|------|----------|----------|----------|
| **Immich** | 照片/视频管理 | AI 人脸识别、语义搜索、原生 App、Google Photos 体验 | 照片专用，不处理通用文件 | 家庭照片备份首选 |
| **Nextcloud** | 全能云协作 | 文件同步、日历、Office、插件生态、成熟 | 重(微服务)、照片 AI 弱 | 需要全套办公协作 |
| **PhotoPrism** | 照片 DAM | EXIF 管理、WebDAV、大库性能好 | 无原生 App、无多用户 | 摄影爱好者归档 |
| **Cloudreve** | 通用文件管理 | 多云存储(S3/OneDrive)、WebDAV、Aria2 | 照片浏览体验一般 | 网盘/下载器二合一 |
| **Seafile** | 文件同步 | 同步快、版本管理、客户端全 | 照片管理弱、社区版受限 | 团队文件同步 |
| **Piwigo** | 相册管理 | 50万+照片无压力、插件丰富 | UI 旧、AI 弱 | 大型相册归档展示 |

### 1.2 竞品核心功能矩阵

| 功能 | Immich | Nextcloud | Cloudreve | PhotoPrism |
|------|:------:|:---------:|:---------:|:----------:|
| 手机自动备份 | ✅ | ✅(App) | ❌ | ❌(第三方) |
| 时间线浏览 | ✅ | ✅(插件) | ❌ | ✅ |
| AI 人脸识别 | ✅ | ⚠️(差) | ❌ | ⚠️(基础) |
| 语义搜索 | ✅ | ❌ | ❌ | ❌ |
| 多用户 | ✅ | ✅ | ❌(单用户) | ❌ |
| 分享链接 | ✅ | ✅ | ✅ | ✅ |
| 通用文件存储 | ❌(照片专用) | ✅ | ✅ | ❌(照片专用) |
| WebDAV | ❌ | ✅ | ✅ | ✅ |
| 视频播放 | ✅ | ✅ | ✅(预览) | ✅ |
| RAW 支持 | ✅ | ❌ | ❌ | ✅ |
| 操作日志 | ⚠️(基础) | ✅(详细) | ❌ | ❌ |

### 1.3 用户需求排序（根据社区反馈）

1. **照片/视频自动备份** — 手机打开 App 自动上传
2. **时间线浏览** — 按年月滑动浏览，类似 Google Photos
3. **智能相册** — 人脸聚类、地点聚合、事物识别
4. **多用户/家庭组** — 各自私密空间 + 家庭共享空间
5. **外网分享** — 生成链接、设有效期/密码、查看次数
6. **通用文件管理** — 不限于照片，文档/压缩包等也要
7. **多端访问** — PC Web / 手机 H5 / 未来 App
8. **操作日志** — 谁在什么时候做了什么

---

## 二、技术选型（已确认）

### 2.1 后端

| 层面 | 选型 | 版本 | 理由 |
|------|------|------|------|
| 语言 | Java | 21 LTS | 虚拟线程、Record 类、Switch 模式匹配 |
| 框架 | Spring Boot | 3.x | 生态成熟 |
| 构建 | **Maven** | 3.9+ | 你熟悉，父 POM 统一管理版本和多模块 |
| 数据库 | **MySQL** | 8.0+ | 你熟悉，全文索引够用；EXIF 字段后续按需加独立列 |
| 缓存 | Redis | 7.x | Session、验证码、热点数据 |
| 对象存储 | **MinIO** | latest | S3 兼容 API，后续无缝迁移到云 S3 |
| 认证 | **SaToken** | 2.x | 注解驱动，内置 JWT + Token 刷新 + 踢人下线，配置量极少 |
| ORM | **MyBatis-Plus** | 3.5+ | 你熟悉，Lambda QueryWrapper 写查询快 |
| DB 迁移 | Flyway | - | 版本化 SQL |
| 日志 | **Log4j2** | 2.x | 异步日志性能好，取代 Spring Boot 默认的 Logback |
| 图片处理 | imgscalr + metadata-extractor | - | 缩略图 + EXIF 解析(Phase 2) |
| 视频转码 | FFmpeg (命令行调用) | - | 转码 + 封面截取 |

### 2.2 前端

| 层面 | 选型 | 理由 |
|------|------|------|
| 框架 | Vue 3 + TypeScript | 响应式数据流天然适合这类应用 |
| 构建 | Vite 6 | 极快 |
| PC 端 UI | **Naive UI** | 内置 n-upload，支持拖拽/文件列表/进度/自定义上传 |
| 移动端 UI | **Vant 4** | 内置 van-uploader，支持图片选择/预览/上传进度 |
| 路由 | Vue Router 4 | - |
| 状态管理 | Pinia | Vue 3 官方推荐 |
| HTTP | Axios | 拦截器、取消请求、上传进度 |
| 图片预览 | viewerjs | 类似微信的图片浏览器 |
| 文件 MD5 | spark-md5 | 浏览器端分片计算 MD5，秒传去重 |
| 视频播放 | ArtPlayer | 比 Video.js 更现代、支持 MP4/MOV Range 流，后续可扩展 HLS |
| 图表 | ECharts (按需引入) | 存储统计 |
| PWA | vite-plugin-pwa | 移动端可安装到桌面 |
| 移动打包 | **Capacitor** | 将 Vue 3 Web 应用打包为 APK/AAB（Android）和 IPA（iOS），比 Cordova 更现代、维护活跃 |
| OTA 更新 | **capacitor-updater** | 无需应用商店审核即可推送 Web 资源热更新，原生层（Capacitor 插件）变更才需重新打包 |

### 2.3 为什么不选

| 选项 | 原因 |
|------|------|
| Element Plus / Ant Design Vue | 纯 PC 后台库，移动端没法看 |
| React + shadcn/ui | 一个人开发搭建成本高，Naive UI 开箱即用更划算 |
| Quasar | 一套 UI 跨端，但 PC 端设计感不如 Naive UI |
| PostgreSQL | 你对 MySQL 更熟，不需要 JSONB 高级特性 |
| 本地磁盘存储 | 扩展性差，后续不能无缝切 S3 |
| JPA | 你对 MyBatis-Plus 更熟，Lambda QueryWrapper 写复杂查询更顺手 |
| JSON 列 | Phase 1 不需要存 EXIF；Phase 2 加独立列查询更快，索引更直接 |
| Spring Security | SaToken 配置量少太多，注解驱动，个人项目没必要上 Security |
| React Native / Flutter | 需要单独学一套技术栈，Capacitor 复用现有 Vue 3 代码，一套 Web 代码同时 Web + App |
| PWA 纯靠浏览器 | 国内安卓浏览器对 PWA 支持差，无法后台自动备份照片；APK 安装体验更好 |

---

## 三、Phase 1 MVP 详细设计（本次目标）

### 3.1 功能清单

```
Phase 1 — 核心 MVP
│
├── 用户模块
│   ├── 注册（用户名 + 密码 + 昵称）
│   ├── 登录（返回 JWT accessToken + refreshToken）
│   ├── Token 刷新（无感知续期）
│   └── 修改密码
│
├── 文件模块
│   ├── 文件上传
│   │   ├── 小文件（<100MB）：单次上传
│   │   ├── 大文件（>=100MB）：分片上传，断点续传
│   │   └── 秒传（MD5 去重，相同文件不重复存）
│   ├── 文件下载（流式，支持 Range）
│   ├── 文件删除（软删除）
│   ├── 文件重命名
│   ├── 文件夹创建
│   ├── 文件夹内文件列表（分页、排序）
│   └── 文件移动/复制（同用户内）
│
├── 照片/视频浏览
│   ├── 时间线视图（按月份分组，瀑布流/网格）
│   ├── 图片预览（上一张/下一张、缩放、旋转、全屏、下载）
│   ├── 视频在线播放（HTTP Range 直流播放）
│   ├── 缩略图（上传时自动生成 200x200 / 800x800 两档）
│   └── 媒体类型过滤（照片/视频/全部）
│
├── 通用
│   ├── 存储空间统计（已用/总量，先写死 50G 上限）
│   ├── PC / 移动端自适应布局
│   └── 暗色模式（跟系统）
```

### 3.2 暂不在 Phase 1 实现

- 多用户（Phase 2）
- 分享链接（Phase 2）
- 相册管理（Phase 2）
- EXIF 提取与展示（Phase 2）
- 全文搜索（Phase 2）
- AI 功能（Phase 3）
- 回收站（Phase 3）
- 操作日志（Phase 3）

---

## 四、数据库设计（MySQL 8）

### 4.1 ER 图（核心表）

```
┌──────────────┐       ┌──────────────────┐       ┌───────────────────┐
│   sys_user   │       │    file_info     │       │   file_chunk      │
├──────────────┤       ├──────────────────┤       ├───────────────────┤
│ id (PK)      │──┐    │ id (PK)          │       │ id (PK)           │
│ username     │  │    │ user_id (FK)     │──┐    │ file_md5          │
│ password     │  │    │ parent_id (自关联)│  │    │ chunk_index       │
│ nickname     │  │    │ file_name        │  │    │ chunk_md5         │
│ avatar       │  │    │ file_type        │  │    │ chunk_size        │
│ storage_used │  │    │ file_size        │  │    │ storage_path      │
│ storage_limit│  │    │ file_md5         │──┼──→ │ created_at        │
│ status       │  │    │ mime_type        │  │    └───────────────────┘
│ created_at   │  │    │ is_dir           │  │
│ updated_at   │  │    │ storage_path     │  │    ┌───────────────────┐
└──────────────┘  │    │ thumbnail_200    │  │    │   upload_task     │
                  │    │ thumbnail_800    │  │    ├───────────────────┤
                  │    │ cover_time       │  │    │ id (PK)           │
                  │    │ width            │  │    │ user_id (FK)      │
                  │    │ height           │  │    │ file_md5          │
                  │    │ duration         │  │    │ file_name         │
                  │    │ is_deleted       │  │    │ file_size         │
                  │    │ created_at       │  │    │ total_chunks      │
                  │    │ updated_at       │  │    │ uploaded_chunks   │
                  │    └──────────────────┘  │    │ status            │
                  │                          │    │ created_at        │
                  └──────────────────────────┘    └───────────────────┘
```

### 4.2 表结构说明

**sys_user — 用户表**
```
id              BIGINT PK AUTO_INCREMENT
username        VARCHAR(50) UNIQUE NOT NULL    -- 登录名
password        VARCHAR(255) NOT NULL          -- BCrypt 加密
nickname        VARCHAR(50)                    -- 显示昵称
avatar          VARCHAR(500)                   -- 头像 URL
storage_used    BIGINT DEFAULT 0               -- 已用存储 (bytes)
storage_limit   BIGINT DEFAULT 53687091200     -- 50G 上限
status          TINYINT DEFAULT 1              -- 1正常 0禁用
created_at      DATETIME
updated_at      DATETIME
```

**file_info — 文件/文件夹表**
```
id              BIGINT PK AUTO_INCREMENT
user_id         BIGINT NOT NULL                -- 所属用户
parent_id       BIGINT DEFAULT 0               -- 父文件夹ID(0=根目录)
file_name       VARCHAR(255) NOT NULL          -- 文件名/文件夹名
file_type       VARCHAR(20)                    -- IMAGE/VIDEO/DOCUMENT/OTHER
file_size       BIGINT DEFAULT 0               -- 文件大小(bytes)
file_md5        VARCHAR(32)                    -- 文件MD5(秒传+去重)
mime_type       VARCHAR(100)                   -- MIME类型
is_dir          TINYINT DEFAULT 0              -- 0文件 1文件夹
storage_path    VARCHAR(500)                   -- MinIO存储路径
thumbnail_200   VARCHAR(500)                   -- 小缩略图路径
thumbnail_800   VARCHAR(500)                   -- 大缩略图路径
cover_time        VARCHAR(20)                    -- 视频封面时间点
date_taken        DATETIME                       -- 拍摄日期（Phase 1 预留，fallback created_at）
thumbnail_status  TINYINT DEFAULT 0              -- 缩略图状态: 0=生成中 1=完成 2=失败
width           INT                            -- 图片/视频宽度
height          INT                            -- 图片/视频高度
duration        INT                            -- 视频时长(秒)
is_deleted      TINYINT DEFAULT 0              -- 0正常 1已删除
created_at      DATETIME
updated_at      DATETIME

INDEX idx_user_parent  (user_id, parent_id)
INDEX idx_user_type    (user_id, file_type)
INDEX idx_user_md5     (user_id, file_md5)
INDEX idx_user_deleted           (user_id, is_deleted)
INDEX idx_user_type_deleted_date (user_id, file_type, is_deleted, created_at)
```

> **关于 date_taken**：Phase 1 已预留 `date_taken` 字段，默认 NULL 时 fallback `created_at`。Phase 2 读取 EXIF 填充。其余 EXIF 字段（相机型号/GPS/ISO）Phase 2 再加。
> 
> **关于 EXIF 数据**：Phase 1 不存 EXIF 元数据。Phase 2 需要时，加独立列而非 JSON：
> ```sql
> ALTER TABLE file_info ADD COLUMN date_taken DATETIME;       -- 拍摄时间
> ALTER TABLE file_info ADD COLUMN camera_model VARCHAR(100); -- 相机型号
> ALTER TABLE file_info ADD COLUMN latitude DOUBLE;           -- GPS 纬度
> ALTER TABLE file_info ADD COLUMN longitude DOUBLE;          -- GPS 经度
> ALTER TABLE file_info ADD COLUMN iso INT;                   -- ISO
> ```
> 独立列的优点：查询快、能建索引、不需要 `JSON_EXTRACT()`，MyBatis-Plus 映射也直接。

**file_chunk — 分片上传记录表**
```
id              BIGINT PK AUTO_INCREMENT
task_id         BIGINT                         -- 关联 upload_task.id
file_md5        VARCHAR(32) NOT NULL          -- 完整文件MD5
chunk_index     INT NOT NULL                  -- 分片序号(从0开始)
chunk_md5       VARCHAR(32)                   -- 分片MD5
chunk_size      BIGINT                        -- 分片大小
storage_path    VARCHAR(500)                  -- MinIO临时路径
created_at      DATETIME

UNIQUE KEY uk_md5_chunk (file_md5, chunk_index)
```

**upload_task — 上传任务表**
```
id              BIGINT PK AUTO_INCREMENT
user_id         BIGINT NOT NULL
file_md5        VARCHAR(32) NOT NULL
file_name       VARCHAR(255) NOT NULL
file_size       BIGINT NOT NULL
total_chunks    INT NOT NULL
uploaded_chunks INT DEFAULT 0
status          TINYINT DEFAULT 0             -- 0上传中 1合并中 2完成 3失败
created_at      DATETIME
```

---

## 五、API 设计（Phase 1）

### 5.1 通用约定

```
API 路由规则: /api/{version}/{channel}/{resource}

  version  — API 版本号: v1, v2, ...
  channel  — 调用端标识:
    /api/v1/app/    移动端 App（app-api）
    /api/v1/admin/  PC 管理端（admin-api）
    /api/v1/open/   开放接口（open-api，供第三方集成，Phase 2+）
    /api/v1/        通用接口（auth 等无需区分 channel，直接挂在版本号下）

  channel 之间的区别:
    - app-api:   面向移动端，响应精简，接口按移动端交互设计
    - admin-api: 面向 PC 管理端，可能返回更全的字段、支持批量操作
    - open-api:  对外暴露，需独立鉴权（API Key/OAuth），限流更严格
    - 通用接口:  认证（login/register/refresh/logout）不区分 channel

统一响应格式:
{
  "code": 200,
  "message": "success",
  "data": {},
  "traceId": "uuid",
  "timestamp": 1711234567890
}

错误码规则:
  200   — 成功
  400   — 参数错误
  401   — 未登录/Token过期
  403   — 无权限
  404   — 资源不存在
  500   — 服务器内部错误

  1xxxx — 用户模块 (10001=用户名已存在, 10002=密码错误)
  2xxxx — 文件模块 (20001=文件不存在, 20002=存储空间不足)
  3xxxx — 分享模块
  4xxxx — 系统模块
```

### 5.2 接口列表

**认证模块（通用，不区分 channel）**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/auth/register | 注册 |
| POST | /api/v1/auth/login | 登录 |
| POST | /api/v1/auth/refresh | 刷新 Token |
| POST | /api/v1/auth/logout | 登出 |
| PUT | /api/v1/auth/password | 修改密码 |

**文件模块 — app-api（移动端 + PC 个人文件管理）**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/app/files/upload | 小文件上传（multipart/form-data） |
| POST | /api/v1/app/files/upload/check | 秒传检查（MD5 查询） |
| POST | /api/v1/app/files/upload/chunk | 分片上传初始化 + 上传分片 |
| GET  | /api/v1/app/files/upload/progress | 查询分片上传进度 |
| POST | /api/v1/app/files/upload/merge | 分片合并 |
| GET | /api/v1/app/files/download/{id} | 下载文件 |
| GET | /api/v1/app/files/list | 文件列表（分页） |
| GET | /api/v1/app/files/detail/{id} | 文件详情 |
| DELETE | /api/v1/app/files/{id} | 删除文件（软删除） |
| PUT | /api/v1/app/files/{id}/rename | 重命名 |
| PUT | /api/v1/app/files/{id}/move | 移动文件 |
| PUT | /api/v1/app/files/{id}/copy | 复制文件 |
| POST | /api/v1/app/files/folder | 创建文件夹 |
| GET | /api/v1/app/files/thumbnail/{id} | 获取缩略图 |
| GET | /api/v1/app/files/stream/{id} | 视频流（支持 Range） |

**照片模块 — app-api**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/app/photos/timeline | 时间线列表（按月分组） |
| GET | /api/v1/app/photos/list | 照片列表（支持类型过滤） |

**管理端 — admin-api（Phase 2+，预留路径结构）**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/admin/users | 用户管理（Phase 2） |
| GET | /api/v1/admin/stats | 系统统计（Phase 2） |

**开放接口 — open-api（Phase 2+，预留路径结构）**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/open/share/{token} | 公开分享访问（Phase 2） |

**通用模块（不区分 channel）**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/common/health | 健康检查 |
| GET | /api/v1/common/storage | 存储空间统计 |

### 5.3 关键接口详解

**POST /api/v1/auth/login**
```
Request:  { "username": "admin", "password": "123456" }

Response:
{
  "code": 200,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 3600,
    "userInfo": {
      "id": 1, "username": "admin", "nickname": "管理员", "avatar": null
    }
  }
}
```

**POST /api/v1/files/upload（小文件单次上传）**
```
Request: multipart/form-data
  - file: 文件
  - parentId: 父文件夹ID (可选，默认0=根目录)

Response:
{
  "code": 200,
  "data": {
    "id": 123,
    "fileName": "IMG_20260115.jpg",
    "fileSize": 5242880,
    "thumbnail_200": "/api/v1/files/thumbnail/123?size=200"
  }
}
```

**POST /api/v1/files/upload/chunk（分片上传）**
```
Request: multipart/form-data
  - chunk: 分片数据
  - fileMd5: 完整文件MD5
  - chunkIndex: 分片序号(0-based)
  - totalChunks: 总分片数
  - fileName: 原始文件名
  - fileSize: 完整文件大小

Response:
{
  "code": 200,
  "data": {
    "uploadedChunks": 5,
    "totalChunks": 10,
    "allUploaded": false
  }
}
```

**GET /api/v1/files/list**
```
Request: ?parentId=0&page=1&size=30&sort=created_at&order=desc&type=IMAGE

Response:
{
  "code": 200,
  "data": {
    "total": 150, "page": 1, "size": 30,
    "list": [
      {
        "id": 123,
        "fileName": "IMG_20260115.jpg",
        "fileType": "IMAGE",
        "fileSize": 5242880,
        "isDir": false,
        "thumbnail_200": "/api/v1/files/thumbnail/123?size=200",
        "width": 4032,
        "height": 3024,
        "createdAt": "2026-01-15T14:30:00"
      }
    ]
  }
}
```

**GET /api/v1/photos/timeline**
```
Request: ?year=2026&month=1

Response:
{
  "code": 200,
  "data": {
    "groups": [
      {
        "dateLabel": "2026年1月",
        "photos": [
          { "id": 123, "fileName": "...", "thumbnail_200": "...", "width": 4032, "height": 3024 }
        ]
      },
      {
        "dateLabel": "2025年12月",
        "photos": [...]
      }
    ]
  }
}
```

---

## 六、项目结构

### 6.1 后端 (Maven 多模块)

```
home-cloud/
├── pom.xml                        # 父 POM（统一版本管理）
│
├── cloud-common/                  # 公共模块
│   └── src/main/java/com/homecloud/common/
│       ├── exception/            # BusinessException + GlobalExceptionHandler
│       ├── result/               # Result<T>
│       ├── constant/             # ErrorCode 枚举
│       └── util/
│
├── cloud-auth/                    # 认证模块
│   └── src/main/java/com/homecloud/auth/
│       ├── controller/           # AuthController
│       ├── service/              # AuthService + impl
│       ├── dto/                  # LoginRequest, RegisterRequest, TokenResponse
│       ├── config/               # SaToken 配置 + StpInterface 权限实现
│       └── entity/               # SysUser
│
├── cloud-file/                    # 文件管理模块
│   └── src/main/java/com/homecloud/file/
│       ├── controller/           # FileController
│       ├── service/              # FileService, MinioService, ThumbnailService
│       ├── dto/
│       ├── storage/              # StorageService 接口 + MinioStorageServiceImpl
│       └── entity/               # FileInfo, FileChunk, UploadTask
│
├── cloud-photo/                   # 照片模块（依赖 cloud-file）
│   └── src/main/java/com/homecloud/photo/
│       ├── controller/           # PhotoController
│       ├── service/              # PhotoService
│       └── dto/                  # TimelineResponse
│
├── cloud-share/                   # 分享模块（Phase 2）
│   └── （预留目录结构）
│
├── cloud-log/                     # 日志模块（Phase 3）
│   └── （预留目录结构）
│
└── cloud-server/                  # 启动模块
    └── src/main/java/com/homecloud/
        ├── HomeCloudApplication.java
        └── resources/
            ├── application.yml
            ├── application-dev.yml
            └── application-prod.yml
```

### 6.2 前端 (Vue 3 + Vite)

```
home-cloud-web/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
│
├── src/
│   ├── main.ts
│   ├── App.vue
│   │
│   ├── layouts/
│   │   ├── DesktopLayout.vue     # PC：左侧导航 + 顶栏
│   │   └── MobileLayout.vue      # 移动：顶部 + 底部 TabBar
│   │
│   ├── router/
│   │   ├── index.ts
│   │   └── guards.ts             # Token 检查
│   │
│   ├── stores/                   # Pinia
│   │   ├── user.ts
│   │   ├── file.ts
│   │   └── upload.ts
│   │
│   ├── api/                      # Axios 封装
│   │   ├── request.ts            # 拦截器 + Token 自动刷新
│   │   ├── auth.ts
│   │   ├── file.ts
│   │   └── photo.ts
│   │
│   ├── views/
│   │   ├── desktop/
│   │   │   ├── photos/           # 照片时间线
│   │   │   ├── files/            # 文件管理
│   │   │   └── settings/         # 个人设置
│   │   ├── mobile/
│   │   │   ├── photos/           # 移动端照片
│   │   │   ├── files/            # 移动端文件
│   │   │   └── me/               # 我的
│   │   └── shared/
│   │       ├── LoginView.vue
│   │       └── ShareView.vue     # Phase 2
│   │
│   ├── components/
│   │   ├── desktop/
│   │   │   ├── SideMenu.vue
│   │   │   ├── FileTable.vue
│   │   │   ├── PhotoGrid.vue
│   │   │   ├── PhotoViewer.vue   # 基于 viewerjs
│   │   │   ├── VideoPlayer.vue   # ArtPlayer
│   │   │   └── UploadDrawer.vue
│   │   ├── mobile/
│   │   │   ├── BottomTab.vue
│   │   │   ├── PhotoList.vue
│   │   │   └── UploadButton.vue
│   │   └── common/
│   │       └── FileIcon.vue
│   │
│   ├── composables/
│   │   ├── useDevice.ts          # PC/移动端检测
│   │   └── useFileUpload.ts
│   │
│   ├── utils/
│   │   ├── format.ts
│   │   └── constants.ts
│   │
│   └── styles/
│       ├── variables.css
│       └── global.css
│
└── public/
    └── favicon.ico
```

---

## 七、部署架构

### 7.1 架构图

```
                         Internet
                            │
                     ┌──────▼──────┐
                     │   Nginx     │  反向代理 + HTTPS + 前端静态资源
                     │   :80/443   │
                     └──────┬──────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
     ┌────────▼────────┐   │   ┌─────────▼─────────┐
     │ Vue 前端静态文件 │   │   │ /api/* → Spring   │
     │ (Nginx 直接返回) │   │   │      Boot App     │
     └─────────────────┘   │   │      :8080         │
                           │   └─────────┬──────────┘
                           │             │
                    ┌──────▼──┐   ┌─────▼──────┐
                    │  MinIO  │   │   MySQL    │
                    │  :9000  │   │   :3306    │
                    │(Console)│   └────────────┘
                    │  :9001  │
                    └────┬────┘   ┌────────────┐
                         │        │   Redis    │
                         │        │   :6379    │
                         │        └────────────┘
```

### 7.2 生产环境部署

**方式：jar 直跑 + Nginx 反向代理**

```
服务器安装：
  - MySQL 8、Redis 7、MinIO（和开发环境一样手动装）
  - OpenJDK 21
  - Nginx (HTTPS 用 Let's Encrypt 免费证书)

启动：
  java -jar cloud-server.jar --spring.profiles.active=prod

Nginx 配置要点：
  - 前端静态文件直接 serve
  - /api/* 代理到 localhost:8080（含 v1/v2 版本 + app/admin/open channel）
  - 文件上传超时设大（client_max_body_size 2g）
  - WebSocket 升级头（后续如果有实时通知）
  - OTA 资源包: /ota/* 指向静态目录
```

### 7.3 资源预估

| 组件 | 最低 | 推荐 | 说明 |
|------|------|------|------|
| Spring Boot | 512M | 2G | JVM 建议 -Xmx1g |
| MySQL | 512M | 2G | 数据量上去后要加 |
| MinIO | 512M | 2G | 大文件读写吃 IO |
| Redis | 128M | 512M | 缓存 + Session |
| Nginx | 128M | 512M | 静态文件 + 代理 |
| **磁盘** | 系统 50G + 数据盘按需 | - | 照片/视频吃空间 |
| **合计** | ~2G 内存 | ~7G 内存 | 8G 云服务器可跑 |

### 7.4 移动端 APK 打包与 OTA 更新

```
构建流程:

  Vue 3 源码 ──┬── vite build ──→ Web 静态资源 (Nginx serve)
               │
               └── vite build + Capacitor ──→ Android APK/AAB + iOS IPA

OTA 更新流程 (capacitor-updater):

  1. 服务端发布新版本 Web 资源包（zip）
  2. App 启动时检查版本号，有新版本则后台下载
  3. 下载完成后替换本地 Web 资源，下次启动生效
  4. 仅更新 Web 层（Vue/JS/CSS），原生插件变更才需重新下载 APK

版本策略:
  - Web 资源版本: 跟随应用版本号，每次发版自动构建
  - APK 原生版本: 仅 Capacitor 插件或原生配置变更时重新打包
  - OTA 更新地址: /api/v1/app/version/check（返回最新版本号和下载 URL）
```

### 7.5 代码仓库

```
远程仓库: GitHub（待创建）
仓库名:   family-pan（或 home-cloud）
结构:
  ├── cloud-server/     — 后端 Maven 多模块（Java 21）
  ├── home-cloud-web/   — 前端 Vue 3（Web + Capacitor 移动端）
  ├── docker/           — Docker Compose（开发环境）
  └── docs/             — 文档
```

---

## 八、实施顺序

```
Step 1 — 脚手架搭建
  ├── 后端：Maven 多模块空项目，能编译通过
  ├── 前端：Vite + Vue3 空项目，能启动
  └── 本地 MySQL/Redis/MinIO 安装并连通

Step 2 — 后端基础设施
  ├── cloud-common: Result<T>、BusinessException、GlobalExceptionHandler、ErrorCode
  ├── cloud-auth: 用户实体 + SaToken 配置（JWT 模式）
  └── Flyway 初始化数据库

Step 3 — 文件存储核心
  ├── cloud-file: MinIO 集成 + 单文件上传/下载
  ├── 分片上传 + 断点续传 + 秒传
  └── 缩略图生成（异步）

Step 4 — 前端框架搭建
  ├── 布局切换（DesktopLayout / MobileLayout）
  ├── 路由 + Pinia + Axios 封装
  └── 登录/注册页面

Step 5 — 串联第一个完整功能
  ├── PC 端：文件管理页 + 照片时间线
  ├── 移动端：照片浏览 + 文件列表
  └── 端到端跑通：上传 → 缩略图 → 浏览 → 预览
```

---

## 九、版本规划

| 版本 | 时间 | 目标 |
|------|------|------|
| v0.1.0 | 2 周 | 脚手架 + 基础设施 (Result/JWT/MinIO 联通) |
| v0.2.0 | 3 周 | 文件上传下载 + 前端登录 + 文件列表 |
| v0.3.0 | 2 周 | 照片时间线 + 图片预览 + 视频播放 |
| **v1.0.0** | **~7 周** | **Phase 1 MVP 完成** |
| v1.1.0 | 4 周 | Phase 2: 多用户 + 家庭组 + 分享 + 搜索 |
| v1.2.0 | 6 周 | Phase 3: AI + 日志 + 回收站 |
| v1.3.0 | 4 周 | Phase 4: WebDAV + 加密 + 性能优化 |

---

*文档版本: v1.2 | 日期: 2026-06-09 | 状态: 待确认*

---

## 十、功能设计补漏清单（v1.2 新增）

> 以下问题按模块分组，严重程度：🔴 必须修复 | 🟡 应该修复 | 🟢 建议优化
> 每个问题包含：问题描述 + 建议方案 + 状态（待实施）

### 10.1 认证模块

| # | 问题 | 严重度 | 建议方案 |
|---|------|:------:|----------|
| 1.1 | **Token 刷新竞态**：前端多个 401 并发触发刷新，导致 Token 互相覆盖 | 🔴 | Axios 拦截器加刷新锁 + pending 队列，同时只允许一个刷新请求，完成后批量重放 |
| 1.2 | **RefreshToken Rotation 缺失**：refreshToken 泄漏后无法作废 | 🟡 | 每次使用 refreshToken 时发放新 Token 并作废旧 Token，SaToken 2.x 支持 |
| 1.3 | **防暴力破解缺失**：登录/注册无频率限制 | 🟡 | Redis 记录 IP+用户名失败次数，5 次锁定 15 分钟；或使用 SaToken 自带失败计数 |
| 1.4 | **密码强度无要求**：可能接受弱密码 | 🟡 | 最小 8 位、含字母+数字 |
| 1.5 | **JWT 续期策略未明确**：Token 过期时间、主动刷新窗口未定义 | 🟢 | accessToken 15min / refreshToken 7d；前端启动时用 refreshToken 静默恢复登录态 |

### 10.2 文件模块

| # | 问题 | 严重度 | 建议方案 |
|---|------|:------:|----------|
| 2.1 | **秒传检查接口缺失**：客户端不知道何时可以秒传 | 🔴 | 新增 `POST /api/v1/files/upload/check`，入参 `{ fileMd5, fileName, parentId }`，MD5 已存在则直接返回秒传成功 |
| 2.2 | **同名文件处理策略缺失**：同文件夹上传同名文件行为未定义 | 🔴 | 采用自动重命名策略：`IMG_001(1).jpg`（Google Drive 做法） |
| 2.3 | **存储容量并发控制缺失**：同时上传可能突破配额 | 🔴 | MySQL 原子更新：`UPDATE sys_user SET storage_used = storage_used + ? WHERE id = ? AND storage_used + ? <= storage_limit`，检查 affected rows |
| 2.4 | **分片上传并发冲突**：不同用户上传相同 MD5 文件，分片唯一约束冲突 | 🟡 | 合并时做幂等处理——已合并则直接返回 file_info；或 file_chunk 加 task_id 关联归属 |
| 2.5 | **孤立分片清理缺失**：上传取消/崩溃/失败后的分片残留 | 🟡 | 定时任务清理超过 24h 未完成的 upload_task 及关联 file_chunk 和 MinIO 对象 |
| 2.6 | **文件夹删除无级联**：删除文件夹不会标记子文件为已删除 | 🟡 | 递归标记子孙节点 `is_deleted=1`；或加 `path VARCHAR(500)` 用 LIKE 查询 |
| 2.7 | **文件复制物理存储策略不明确**：复制文件时是否复制 MinIO 对象 | 🟡 | Phase 1 简化：每个文件独立存储（保留秒传去重但不共享物理路径），避免引用计数复杂度 |

### 10.3 照片/视频模块

| # | 问题 | 严重度 | 建议方案 |
|---|------|:------:|----------|
| 3.1 | **HLS 视频转码成本被低估**：CPU 密集 + 存储膨胀 1.5x+ | 🔴 | **Phase 1 放弃 HLS**，改用 HTTP Range 直接流式播放。ArtPlayer 原生支持 MP4/MOV Range 流。HLS 留到 Phase 2+ |
| 3.2 | **缩略图生成失败处理缺失**：异步生成时前端不知道何时就绪 | 🟡 | 异步生成；file_info 加 `thumbnail_status` 字段（0=生成中, 1=完成, 2=失败）；前端展示占位图 |
| 3.3 | **视频元数据提取未定义**：duration/width/height/cover 什么时候提取 | 🟡 | 上传完成后异步调用 ffprobe 提取并更新 file_info，失败不阻塞上传 |
| 3.4 | **时间线分组依据不明确**：照片按 created_at 还是 date_taken 分组 | 🟡 | 提前建 `date_taken DATETIME` 字段（Phase 1 用 created_at fallback，Phase 2 读取 EXIF 填充） |
| 3.5 | **图片预览流可能绕过权限**：直接用 MinIO 预签名 URL 可能泄漏 | 🟡 | 所有图片/视频流走 `/api/v1/files/stream/{id}` 后端代理，校验登录态后从 MinIO 读取 |
| 3.6 | **上传时无拍摄日期概念**：今天上传一年前的照片，时间线按上传时间排序不对 | 🟡 | 上传接口加可选参数 `dateTaken`；照片列表和时间线默认按 `date_taken`（fallback `created_at`）排序 |

### 10.4 通用设计与安全

| # | 问题 | 严重度 | 建议方案 |
|---|------|:------:|----------|
| 4.1 | **文件类型校验缺失**：可能上传恶意文件（.jsp/.exe 等） | 🔴 | 后端校验扩展名白名单 + MIME 类型校验；Phase 1 先做扩展名白名单 |
| 4.2 | **下载/流式接口越权风险**：自增 ID 可枚举遍历 | 🔴 | 下载和流式接口强制校验 `user_id`，非文件所属用户返回 403 |
| 4.3 | **用户间数据隔离**：Phase 1 单用户但字段已预留 user_id，需确保查询都带条件 | 🟡 | Service 层或 MyBatis-Plus 拦截器统一注入 `user_id` 条件 |
| 4.4 | **CORS 配置缺失**：前后端分离部署时浏览器阻止请求 | 🟡 | Spring Boot 加 CORS 配置；开发环境宽松，生产环境收紧 |
| 4.5 | **Spring 端上传大小限制未配置**：默认约 1MB | 🟡 | 配置 `spring.servlet.multipart.max-file-size`（单文件 100MB）和 `max-request-size`（分片 10MB） |
| 4.6 | **关键操作审计缺失**：登录失败、文件删除、密码修改无记录 | 🟡 | Phase 1 加极简日志表 `audit_log(id, user_id, action, target, ip, created_at)`，安全敏感操作写入，无需 UI |
| 4.7 | **异常分类层次不明确**：BusinessException 子类未定义 | 🟢 | 定义 `BusinessException(code, message)` 基类，子类按模块细分；GlobalExceptionHandler 统一捕获映射 |

### 10.5 前端

| # | 问题 | 严重度 | 建议方案 |
|---|------|:------:|----------|
| 5.1 | **大文件 MD5 计算内存溢出**：spark-md5 直接加载大文件计算会爆内存 | 🟡 | 用 FileReader 分片读取（每片 2-10MB），spark-md5 增量计算 |
| 5.2 | **上传进度恢复缺失**：浏览器崩溃后不知道哪些分片已上传 | 🟡 | 新增 `GET /api/v1/files/upload/progress?fileMd5=xxx`，返回已上传分片索引列表 |
| 5.3 | **移动端照片未压缩**：手机原图 4-10MB 直接上传浪费流量 | 🟡 | 移动端提供"原图 / 压缩上传"选项；压缩用 Canvas API，限制最大宽高 2048px |
| 5.4 | **PC/移动端代码重复**：desktop 和 mobile 完全分开导致逻辑重复 | 🟡 | 共享逻辑抽到 composables/stores；如 `usePhotoList()` 同时服务 PhotoGrid.vue 和 PhotoList.vue |
| 5.5 | **暗色模式实现**：Naive UI 和 Vant 4 暗色模式机制不同 | 🟢 | `prefers-color-scheme` 媒体查询 + CSS 变量 + Naive UI darkTheme；Pinia 存主题状态 |
| 5.6 | **Token 刷新锁实现**：前端并发刷新问题（与 1.1 对应） | 🔴 | isRefreshing 标志 + pending 队列，刷新后批量重放 |

### 10.6 数据库

| # | 问题 | 严重度 | 建议方案 |
|---|------|:------:|----------|
| 6.1 | **时间线查询缺少覆盖索引**：`ORDER BY created_at` 需全表扫描 | 🟡 | 加 `INDEX idx_user_type_deleted_date (user_id, file_type, is_deleted, created_at)` |
| 6.2 | **树形结构递归查询效率**：parent_id 自关联需 CTE 递归 | 🟡 | Phase 1 层级不深，CTE 够用；后续可加 `path VARCHAR(500)` 字段优化 |
| 6.3 | **upload_task 与 file_chunk 关系模糊**：仅靠 file_md5 关联 | 🟡 | file_chunk 加 `task_id` 字段关联 upload_task.id |

### 10.7 部署运维

| # | 问题 | 严重度 | 建议方案 |
|---|------|:------:|----------|
| 7.1 | **开发环境搭建繁琐**：需手动安装 MySQL/Redis/MinIO | 🟡 | 项目根目录加 `docker-compose.yml`，一键启动 MySQL 8 + Redis 7 + MinIO |
| 7.2 | **健康检查端点缺失**：无可用性监控 | 🟢 | 加 `GET /api/v1/common/health` 返回 `{"status": "UP"}` |
| 7.3 | **并发上传数未限制**：同时大量上传可能打爆 IO | 🟡 | upload_task 检查当前用户 status=0 的任务数，超过上限（如 5 个）拒绝新任务 |
| 7.4 | **日志策略未定义**：Log4j2 级别/滚动/保留天数 | 🟢 | 开发：控制台 DEBUG；生产：INFO 按天滚动保留 30 天；关键操作单独记 INFO |

### 10.8 统计

| 严重度 | 数量 | 分布 |
|:------:|:----:|------|
| 🔴 必须修复 | 7 | 认证 1 + 文件 3 + 照片 1 + 安全 2 |
| 🟡 应该修复 | 24 | 各模块均有 |
| 🟢 建议优化 | 7 | 认证 1 + 通用 1 + 前端 1 + 部署 2 + 数据库 0 |

---

## 十一、Phase 1 调整建议（v1.2 新增）

基于以上分析，对 Phase 1 范围做以下调整：

### 11.1 视频播放方案降级

```
原方案：HLS 流 + FFmpeg 转码
调整后：HTTP Range 直接流式播放 + ffprobe 元数据提取
影响：前端 ArtPlayer 配置从 HLS 模式改为原生 MP4 模式，后端无需管理 .m3u8/.ts 文件
```

### 11.2 新增接口

```
POST /api/v1/files/upload/check       — 秒传检查
GET  /api/v1/files/upload/progress    — 分片上传进度查询
GET  /api/v1/common/health            — 健康检查
```

### 11.3 新增字段

```
file_info.date_taken          DATETIME   — 拍摄日期（预留，Phase 1 fallback created_at）
file_info.thumbnail_status    TINYINT    — 缩略图生成状态
audit_log (新表)              — 关键操作审计
file_chunk.task_id            BIGINT     — 关联上传任务
```

### 11.4 新增数据库索引

```sql
CREATE INDEX idx_user_type_deleted_date ON file_info(user_id, file_type, is_deleted, created_at);
```

### 11.5 新增中间件/配置

```
- 登录频率限制（Redis 或 SaToken 失败计数）
- CORS 配置
- Spring multipart 上传大小限制
- 文件扩展名白名单校验
```
