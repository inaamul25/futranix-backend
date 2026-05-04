package com.smartlearning.platform.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String folder);
    Resource loadAsResource(String path);
    String publicUrl(String path);
}
