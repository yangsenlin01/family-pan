package com.homecloud.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_info")
public class FileInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long parentId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileMd5;
    private String mimeType;
    private Integer isDir;
    private String storagePath;
    private String thumbnail200;
    private String thumbnail800;
    private Integer thumbnailStatus;
    private String coverTime;
    private LocalDateTime dateTaken;
    private Integer width;
    private Integer height;
    private Integer duration;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
