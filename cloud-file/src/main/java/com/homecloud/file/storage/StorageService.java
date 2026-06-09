package com.homecloud.file.storage;

import java.io.InputStream;

public interface StorageService {
    void upload(String objectName, InputStream inputStream, long size, String contentType);
    InputStream download(String objectName);
    void delete(String objectName);
    String getUrl(String objectName);
    boolean exists(String objectName);
}
