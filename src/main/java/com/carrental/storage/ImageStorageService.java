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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

    @Value("${app.upload.cars.dir:uploads/cars}")
    private String carsDir;

    @Value("${app.upload.employees.dir:uploads/employees}")
    private String employeesDir;

    private final Map<ImageType, String> urlPrefixes = new HashMap<>();
    {
        urlPrefixes.put(ImageType.CAR, "/uploads/cars/");
        urlPrefixes.put(ImageType.EMPLOYEE, "/uploads/employees/");
    }

    private Path carUploadRoot;
    private Path employeeUploadRoot;

    @PostConstruct
    public void init() {
        carUploadRoot = createDirectory(carsDir);
        employeeUploadRoot = createDirectory(employeesDir);

        log.info("Car image uploads directory: {}", carUploadRoot);
        log.info("Employee image uploads directory: {}", employeeUploadRoot);
    }

    private Path createDirectory(String path) {
        Path root = Paths.get(path);
        root = root.isAbsolute()
                ? root.normalize()
                : root.toAbsolutePath().normalize();

        try {
            Files.createDirectories(root);
            return root;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory: " + path, e);
        }
    }

    public String storeCarImage(MultipartFile file) throws IOException {
        return store(file, ImageType.CAR);
    }

    public String storeEmployeeImage(MultipartFile file) throws IOException {
        return store(file, ImageType.EMPLOYEE);
    }

    public String store(MultipartFile file, ImageType imageType) throws IOException {
        validateImage(file);

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() != null
                        ? file.getOriginalFilename()
                        : "image.jpg"
        );

        String ext = getExtension(original);

        String filename =
                UUID.randomUUID().toString().replace("-", "") +
                        ext.toLowerCase(Locale.ROOT);

        Path root = getRoot(imageType);
        Path dest = root.resolve(filename).normalize();

        if (!dest.getParent().equals(root)) {
            throw new IOException("Invalid path.");
        }

        Files.copy(
                file.getInputStream(),
                dest,
                StandardCopyOption.REPLACE_EXISTING
        );

        return urlPrefixes.get(imageType) + filename;
    }

    public void deleteIfPresent(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        String normalized = imagePath.trim().replace('\\', '/');

        try {
            if (normalized.startsWith("/uploads/cars/")) {
                delete(normalized, ImageType.CAR);
            } else if (normalized.startsWith("/uploads/employees/")) {
                delete(normalized, ImageType.EMPLOYEE);
            }
        } catch (Exception e) {
            log.warn("Could not delete image {}: {}", imagePath, e.getMessage());
        }
    }

    private void delete(String imagePath, ImageType imageType) throws IOException {
        String prefix = urlPrefixes.get(imageType);

        String filename = imagePath.substring(prefix.length());

        if (filename.isEmpty() || filename.contains("..")) {
            return;
        }

        Path root = getRoot(imageType);
        Path file = root.resolve(filename).normalize();

        if (!file.startsWith(root)) {
            return;
        }

        Files.deleteIfExists(file);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');

        if (dot >= 0) {
            String ext = filename.substring(dot);

            if (ext.length() <= 8) {
                return ext;
            }
        }

        return ".jpg";
    }

    private Path getRoot(ImageType type) {
        return type == ImageType.CAR
                ? carUploadRoot
                : employeeUploadRoot;
    }

    public enum ImageType {
        CAR,
        EMPLOYEE
    }
}