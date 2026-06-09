package com.homecloud.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homecloud.file.entity.SysUserStorage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserStorageMapper extends BaseMapper<SysUserStorage> {

    @Update("UPDATE sys_user SET storage_used = storage_used + #{size} WHERE id = #{userId} AND storage_used + #{size} <= storage_limit AND status = 1")
    int checkAndIncrementStorage(@Param("userId") Long userId, @Param("size") long size);

    @Update("UPDATE sys_user SET storage_used = GREATEST(0, storage_used - #{size}) WHERE id = #{userId}")
    int decrementStorage(@Param("userId") Long userId, @Param("size") long size);
}
