package com.homecloud.file.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homecloud.common.constant.ErrorCode;
import com.homecloud.common.exception.BusinessException;
import com.homecloud.common.result.Result;
import com.homecloud.file.entity.FileInfo;
import com.homecloud.file.mapper.FileInfoMapper;
import com.homecloud.file.storage.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;
    private final FileInfoMapper fileInfoMapper;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif",
        "mp4", "mov", "avi", "mkv", "wmv", "flv",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "csv", "zip", "rar", "7z"
    );

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", defaultValue = "0") Long parentId) {

        Long userId = StpUtil.getLoginIdAsLong();
        String originalName = file.getOriginalFilename();

        String ext = getExtension(originalName);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String fileName = resolveFileName(userId, parentId, originalName);
        String fileType = categorizeFile(ext.toLowerCase());

        String md5;
        try (InputStream is = file.getInputStream()) {
            md5 = calculateMD5(is);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        }

        // Instant transfer check
        LambdaQueryWrapper<FileInfo> md5Query = new LambdaQueryWrapper<>();
        md5Query.eq(FileInfo::getUserId, userId)
                .eq(FileInfo::getFileMd5, md5)
                .eq(FileInfo::getIsDeleted, 0);
        FileInfo existing = fileInfoMapper.selectOne(md5Query);
        if (existing != null) {
            FileInfo instant = buildFileInfo(userId, parentId, fileName, fileType,
                    file.getSize(), md5, file.getContentType(), existing.getStoragePath());
            fileInfoMapper.insert(instant);
            Map<String, Object> result = new HashMap<>();
            result.put("id", instant.getId());
            result.put("fileName", fileName);
            result.put("instantTransfer", true);
            return Result.ok(result);
        }

        // Upload to MinIO
        String objectName = userId + "/" + UUID.randomUUID() + "." + ext;
        try (InputStream is = file.getInputStream()) {
            storageService.upload(objectName, is, file.getSize(), file.getContentType());
        } catch (Exception e) {
            log.error("Upload failed", e);
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        }

        FileInfo fileInfo = buildFileInfo(userId, parentId, fileName, fileType,
                file.getSize(), md5, file.getContentType(), objectName);
        fileInfoMapper.insert(fileInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("id", fileInfo.getId());
        result.put("fileName", fileName);
        result.put("fileSize", fileInfo.getFileSize());
        result.put("instantTransfer", false);
        return Result.ok(result);
    }

    // --- private helpers ---

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return null;
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private String resolveFileName(Long userId, Long parentId, String fileName) {
        LambdaQueryWrapper<FileInfo> query = new LambdaQueryWrapper<>();
        query.eq(FileInfo::getUserId, userId)
             .eq(FileInfo::getParentId, parentId)
             .eq(FileInfo::getFileName, fileName)
             .eq(FileInfo::getIsDeleted, 0);
        long count = fileInfoMapper.selectCount(query);
        if (count == 0) return fileName;
        String name = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
        return name + "(" + count + ")" + ext;
    }

    private String categorizeFile(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif" -> "IMAGE";
            case "mp4", "mov", "avi", "mkv", "wmv", "flv" -> "VIDEO";
            case "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv" -> "DOCUMENT";
            default -> "OTHER";
        };
    }

    private String calculateMD5(InputStream is) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = is.read(buffer)) != -1) {
            md.update(buffer, 0, read);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private FileInfo buildFileInfo(Long userId, Long parentId, String fileName,
            String fileType, long fileSize, String md5, String mimeType, String storagePath) {
        FileInfo fi = new FileInfo();
        fi.setUserId(userId);
        fi.setParentId(parentId);
        fi.setFileName(fileName);
        fi.setFileType(fileType);
        fi.setFileSize(fileSize);
        fi.setFileMd5(md5);
        fi.setMimeType(mimeType);
        fi.setIsDir(0);
        fi.setStoragePath(storagePath);
        fi.setThumbnailStatus(0);
        fi.setIsDeleted(0);
        fi.setCreatedAt(LocalDateTime.now());
        fi.setUpdatedAt(LocalDateTime.now());
        return fi;
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || !fileInfo.getUserId().equals(userId)
                || fileInfo.getIsDeleted() == 1 || fileInfo.getIsDir() == 1) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        response.setContentType(fileInfo.getMimeType() != null ? fileInfo.getMimeType() : "application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8).replace("+", "%20") + "\"");
        try (InputStream is = storageService.download(fileInfo.getStoragePath());
             OutputStream os = response.getOutputStream()) {
            is.transferTo(os);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @GetMapping("/stream/{id}")
    public void stream(@PathVariable Long id, HttpServletResponse response) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || !fileInfo.getUserId().equals(userId)
                || fileInfo.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        response.setContentType(fileInfo.getMimeType() != null ? fileInfo.getMimeType() : "application/octet-stream");
        response.setHeader("Accept-Ranges", "bytes");
        try (InputStream is = storageService.download(fileInfo.getStoragePath());
             OutputStream os = response.getOutputStream()) {
            is.transferTo(os);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") Long parentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String type) {

        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId)
               .eq(FileInfo::getParentId, parentId)
               .eq(FileInfo::getIsDeleted, 0);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(FileInfo::getFileType, type);
        }
        wrapper.orderByDesc(FileInfo::getIsDir)
               .orderByDesc(FileInfo::getCreatedAt);

        Page<FileInfo> mpPage = new Page<>(page, size);
        Page<FileInfo> result = fileInfoMapper.selectPage(mpPage, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        data.put("list", result.getRecords());
        return Result.ok(data);
    }

    @GetMapping("/detail/{id}")
    public Result<FileInfo> detail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || !fileInfo.getUserId().equals(userId)
                || fileInfo.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return Result.ok(fileInfo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || !fileInfo.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        if (fileInfo.getIsDir() == 1) {
            deleteRecursive(userId, id);
        }
        fileInfo.setIsDeleted(1);
        fileInfoMapper.updateById(fileInfo);
        return Result.ok();
    }

    private void deleteRecursive(Long userId, Long parentId) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId)
               .eq(FileInfo::getParentId, parentId)
               .eq(FileInfo::getIsDeleted, 0);
        var children = fileInfoMapper.selectList(wrapper);
        for (FileInfo child : children) {
            if (child.getIsDir() == 1) {
                deleteRecursive(userId, child.getId());
            }
            child.setIsDeleted(1);
            fileInfoMapper.updateById(child);
        }
    }

    @PutMapping("/{id}/rename")
    public Result<Void> rename(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || !fileInfo.getUserId().equals(userId)
                || fileInfo.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        fileInfo.setFileName(body.get("fileName"));
        fileInfoMapper.updateById(fileInfo);
        return Result.ok();
    }

    @PostMapping("/folder")
    public Result<Map<String, Object>> createFolder(@RequestBody Map<String, Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long parentId = Long.valueOf(body.getOrDefault("parentId", 0).toString());
        String folderName = body.get("folderName").toString();

        FileInfo folder = new FileInfo();
        folder.setUserId(userId);
        folder.setParentId(parentId);
        folder.setFileName(folderName);
        folder.setIsDir(1);
        folder.setIsDeleted(0);
        folder.setCreatedAt(LocalDateTime.now());
        folder.setUpdatedAt(LocalDateTime.now());
        fileInfoMapper.insert(folder);

        Map<String, Object> result = new HashMap<>();
        result.put("id", folder.getId());
        result.put("folderName", folderName);
        return Result.ok(result);
    }

    @GetMapping("/thumbnail/{id}")
    public void thumbnail(@PathVariable Long id, @RequestParam(defaultValue = "200") int size,
            HttpServletResponse response) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || !fileInfo.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        String thumbPath = size <= 200 ? fileInfo.getThumbnail200() : fileInfo.getThumbnail800();
        if (thumbPath == null) {
            thumbPath = fileInfo.getStoragePath();
        }
        try (InputStream is = storageService.download(thumbPath);
             OutputStream os = response.getOutputStream()) {
            response.setContentType("image/jpeg");
            is.transferTo(os);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }
}
