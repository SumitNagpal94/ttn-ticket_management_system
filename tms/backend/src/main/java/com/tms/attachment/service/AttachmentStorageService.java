package com.tms.attachment.service;

import com.tms.common.config.TmsConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class AttachmentStorageService {

    private final Path storageRoot;

    public AttachmentStorageService(TmsConfig tmsConfig) throws IOException {
        this.storageRoot = Path.of(tmsConfig.getAttachments().getStoragePath()).toAbsolutePath().normalize();
        Files.createDirectories(storageRoot);
    }

    public void store(String storageKey, InputStream content) throws IOException {
        Path target = resolve(storageKey);
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public Path resolvePath(String storageKey) {
        return resolve(storageKey);
    }

    public void delete(String storageKey) throws IOException {
        Files.deleteIfExists(resolve(storageKey));
    }

    private Path resolve(String storageKey) {
        Path target = storageRoot.resolve(storageKey).normalize();
        if (!target.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return target;
    }
}
