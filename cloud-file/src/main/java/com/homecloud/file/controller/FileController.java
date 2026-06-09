package com.homecloud.file.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}
