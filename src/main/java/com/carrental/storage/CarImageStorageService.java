package com.carrental.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class CarImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(CarImageStorageService.class);

    @Value("${app.upload.cars.dir:uploads/cars}")
    private String uploadDirRelative;

    private Path uploadRoot;

    @PostConstruct
    public void init() {
        Path configured = Paths.get(uploadDirRelative);
        uploadRoot = configured.isAbsolute()
                ? configured.normalize()
                : configured.toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
            log.info("Car image uploads directory: {}", uploadRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create car image upload directory", e);
        }
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "car.jpg");
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot);
            if (ext.length() > 8) {
                ext = "";
            }
        }
        if (ext.isEmpty()) {
            ext = ".jpg";
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext.toLowerCase(Locale.ROOT);
        Path dest = uploadRoot.resolve(filename).normalize();
        if (!dest.getParent().equals(uploadRoot)) {
            throw new IOException("Invalid path.");
        }
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/cars/" + filename;
    }

    /**
     * Removes the stored image file for a vehicle, if the path is under this app's upload directory.
     */
    public void deleteIfPresent(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }
        String normalized = imagePath.trim().replace('\\', '/');
        if (!normalized.startsWith("/uploads/cars/")) {
            return;
        }
        String filename = normalized.substring("/uploads/cars/".length());
        if (filename.isEmpty() || filename.contains("..")) {
            return;
        }
        try {
            Path file = uploadRoot.resolve(filename).normalize();
            if (!file.startsWith(uploadRoot)) {
                return;
            }
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Could not delete car image {}: {}", imagePath, e.getMessage());
        }
    }
}
