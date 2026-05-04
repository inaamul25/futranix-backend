package com.smartlearning.platform.dto.upload;

public record UploadMetadataResponse(
        String title,
        String videoPath,
        String contentType,
        Long fileSize,
        Integer durationSeconds,
        String suggestedModule
) {
}
