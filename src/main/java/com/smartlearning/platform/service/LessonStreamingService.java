package com.smartlearning.platform.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface LessonStreamingService {
    ResponseEntity<Resource> streamLesson(Long lessonId, String rangeHeader, String userEmail);
}
