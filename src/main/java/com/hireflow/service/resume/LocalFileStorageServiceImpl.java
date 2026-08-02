package com.hireflow.service.resume;

import com.hireflow.config.HireFlowProperties;
import com.hireflow.exception.BusinessRuleException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

/**
 * Local filesystem implementation of {@link FileStorageService}.
 */
@Service
@ConditionalOnProperty(name = "hireflow.storage.provider", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final HireFlowProperties props;
    private Path baseUploadLocation;

    @PostConstruct
    void init() {
        this.baseUploadLocation = Paths.get(props.getStorage().getLocal().getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseUploadLocation);
            log.info("Local file storage initialized at: {}", this.baseUploadLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create local upload directory: " + this.baseUploadLocation, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        try {
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String storedFileName = UUID.randomUUID() + extension;
            Path targetDir = this.baseUploadLocation.resolve(subDirectory).normalize();
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String relativeKey = subDirectory + "/" + storedFileName;
            log.info("Stored file locally at: {}", relativeKey);
            return relativeKey;
        } catch (IOException e) {
            throw new BusinessRuleException("Failed to store file locally: " + e.getMessage());
        }
    }

    @Override
    public InputStream getFileAsStream(String storageKey) {
        try {
            Path filePath = this.baseUploadLocation.resolve(storageKey).normalize();
            if (!Files.exists(filePath)) {
                throw new BusinessRuleException("File not found at path: " + storageKey);
            }
            return new FileInputStream(filePath.toFile());
        } catch (IOException e) {
            throw new BusinessRuleException("Failed to read file from local storage: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String storageKey) {
        try {
            Path filePath = this.baseUploadLocation.resolve(storageKey).normalize();
            Files.deleteIfExists(filePath);
            log.info("Deleted local file: {}", storageKey);
        } catch (IOException e) {
            log.warn("Failed to delete local file: {}", storageKey, e);
        }
    }
}
