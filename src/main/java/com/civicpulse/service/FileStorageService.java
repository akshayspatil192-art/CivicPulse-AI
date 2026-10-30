package com.civicpulse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageDirectory;

    public FileStorageService(@Value("${civicpulse.upload.dir:uploads}") String uploadDir) {
        this.storageDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage folder", e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String safeFileName = UUID.randomUUID().toString() + extension;
        Path targetLocation = this.storageDirectory.resolve(safeFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + safeFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + safeFileName, e);
        }
    }
}
