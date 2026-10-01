package com.contractexposed.backend.service;

import com.contractexposed.backend.entity.Contract;
import com.contractexposed.backend.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * Handles saving/retrieving uploaded contract files from local disk.
 */
@Service
@Slf4j
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/tiff"
    );

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024L; // 20 MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Validates and persists the uploaded file, returns stored file name.
     */
    public String store(MultipartFile file) {
        validateFile(file);

        String originalName = sanitize(file.getOriginalFilename());
        String extension    = getExtension(originalName);
        String storedName   = UUID.randomUUID() + "." + extension;
        Path   targetPath   = resolveUploadDir().resolve(storedName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file {} → {}", originalName, targetPath);
            return storedName;
        } catch (IOException ex) {
            throw new FileProcessingException("Failed to store file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Resolves the absolute path for a stored file name.
     */
    public Path resolve(String storedFileName) {
        return resolveUploadDir().resolve(storedFileName).normalize();
    }

    /**
     * Deletes a stored file; logs but does not throw on failure.
     */
    public void delete(String storedFileName) {
        try {
            Files.deleteIfExists(resolve(storedFileName));
            log.info("Deleted stored file {}", storedFileName);
        } catch (IOException ex) {
            log.warn("Could not delete file {}: {}", storedFileName, ex.getMessage());
        }
    }

    /**
     * Maps a MultipartFile content-type / extension to our FileType enum.
     */
    public Contract.FileType detectFileType(MultipartFile file) {
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (ct.contains("pdf"))  return Contract.FileType.PDF;
        if (ct.contains("wordprocessingml") || ct.contains("msword")) return Contract.FileType.DOCX;
        if (ct.contains("image")) return Contract.FileType.IMAGE;

        // fall back to extension
        String ext = getExtension(sanitize(file.getOriginalFilename())).toLowerCase();
        return switch (ext) {
            case "pdf"  -> Contract.FileType.PDF;
            case "docx" -> Contract.FileType.DOCX;
            case "doc"  -> Contract.FileType.DOC;
            default     -> Contract.FileType.IMAGE;
        };
    }

    // ── private helpers ──────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("Uploaded file is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileProcessingException("File size exceeds 20 MB limit.");
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_CONTENT_TYPES.contains(ct.toLowerCase())) {
            throw new FileProcessingException(
                    "Unsupported file type: " + ct + ". Allowed: PDF, DOCX, DOC, PNG, JPG, TIFF");
        }
    }

    private Path resolveUploadDir() {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException ex) {
            throw new FileProcessingException("Cannot create upload directory: " + ex.getMessage(), ex);
        }
        return dir;
    }

    private String sanitize(String name) {
        if (name == null) return "unknown";
        return Paths.get(name).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot >= 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "bin";
    }
}
