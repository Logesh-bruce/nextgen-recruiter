package com.hireflow.service.resume;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Strategy interface for storing and retrieving uploaded resume files (Local or AWS S3).
 */
public interface FileStorageService {

    String storeFile(MultipartFile file, String subDirectory);

    InputStream getFileAsStream(String storageKey);

    void deleteFile(String storageKey);
}
