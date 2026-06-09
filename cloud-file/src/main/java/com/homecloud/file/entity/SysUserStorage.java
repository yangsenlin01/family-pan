package com.homecloud.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUserStorage {
    private Long id;
    private Long storageUsed;
    private Long storageLimit;
}
