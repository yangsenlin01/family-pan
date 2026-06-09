package com.homecloud.photo.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homecloud.common.result.Result;
import com.homecloud.file.entity.FileInfo;
import com.homecloud.file.mapper.FileInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final FileInfoMapper fileInfoMapper;

    @GetMapping("/timeline")
    public Result<Map<String, Object>> timeline(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId)
               .eq(FileInfo::getIsDeleted, 0)
               .in(FileInfo::getFileType, "IMAGE", "VIDEO")
               .orderByDesc(FileInfo::getCreatedAt);

        // Paginate server-side, limit to 500 per request
        Page<FileInfo> mpPage = new Page<>(1, 500);
        Page<FileInfo> result = fileInfoMapper.selectPage(mpPage, wrapper);

        // Group by year-month
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (FileInfo f : result.getRecords()) {
            String dateLabel = f.getCreatedAt() != null
                    ? f.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy年M月"))
                    : "未知日期";
            grouped.computeIfAbsent(dateLabel, k -> new ArrayList<>())
                    .add(toPhotoMap(f));
        }

        List<Map<String, Object>> groups = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            groups.add(Map.of("dateLabel", entry.getKey(), "photos", entry.getValue()));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("groups", groups);
        return Result.ok(data);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String type) {

        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId)
               .eq(FileInfo::getIsDeleted, 0);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(FileInfo::getFileType, type);
        } else {
            wrapper.in(FileInfo::getFileType, "IMAGE", "VIDEO");
        }
        wrapper.orderByDesc(FileInfo::getCreatedAt);

        Page<FileInfo> mpPage = new Page<>(page, size);
        Page<FileInfo> result = fileInfoMapper.selectPage(mpPage, wrapper);

        List<Map<String, Object>> list = result.getRecords().stream()
                .map(this::toPhotoMap)
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        data.put("list", list);
        return Result.ok(data);
    }

    private Map<String, Object> toPhotoMap(FileInfo f) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", f.getId());
        map.put("fileName", f.getFileName());
        map.put("fileType", f.getFileType());
        map.put("fileSize", f.getFileSize());
        map.put("mimeType", f.getMimeType());
        map.put("width", f.getWidth());
        map.put("height", f.getHeight());
        map.put("duration", f.getDuration());
        map.put("dateTaken", f.getDateTaken());
        map.put("createdAt", f.getCreatedAt());
        return map;
    }
}
