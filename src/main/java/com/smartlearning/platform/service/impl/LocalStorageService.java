package com.smartlearning.platform.service.impl;

import com.smartlearning.platform.config.AppProperties;
import com.smartlearning.platform.exception.BadRequestException;
import com.smartlearning.platform.service.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final AppProperties appProperties;
    private Path rootPath;

    @PostConstruct
    public void init() throws IOException {
        rootPath = Paths.get(appProperties.getStorage().getRoot()).toAbsolutePath().normalize();
        Files.createDirectories(rootPath);
    }

    @Override
    public String store(MultipartFile file, String folder) {
        try {
            Path directory = rootPath.resolve(folder).normalize();
            Files.createDirectories(directory);
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String filename = UUID.randomUUID() + "-" + original.replace(" ", "_");
            Path target = directory.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return rootPath.relativize(target).toString().replace("\\", "/");
        } catch (IOException ex) {
            throw new BadRequestException("Unable to store file");
        }
    }

    @Override
    public Resource loadAsResource(String path) {
        try {
            Path file = rootPath.resolve(path).normalize();
            return new UrlResource(file.toUri());
        } catch (MalformedURLException ex) {
            throw new BadRequestException("Unable to read file");
        }
    }

    @Override
    public String publicUrl(String path) {
        return appProperties.getStorage().getPublicBaseUrl() + "/api/files?path=" + path;
    }
}
