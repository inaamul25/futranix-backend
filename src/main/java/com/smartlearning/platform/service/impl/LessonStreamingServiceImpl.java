package com.smartlearning.platform.service.impl;

import com.smartlearning.platform.entity.Lesson;
import com.smartlearning.platform.entity.User;
import com.smartlearning.platform.exception.UnauthorizedException;
import com.smartlearning.platform.service.EnrollmentService;
import com.smartlearning.platform.service.LessonStreamingService;
import com.smartlearning.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonStreamingServiceImpl implements LessonStreamingService {

    private final SupportService supportService;
    private final EnrollmentService enrollmentService;
    private final StorageService storageService;

    @Override
    public ResponseEntity<Resource> streamLesson(Long lessonId, String rangeHeader, String userEmail) {
        User user = supportService.getUserByEmail(userEmail);
        Lesson lesson = supportService.getLesson(lessonId);
        Long courseId = lesson.getModule().getCourse().getId();
        if (!enrollmentService.isEnrolled(courseId, user.getId())
                && !lesson.getModule().getCourse().getCreator().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only enrolled users can access lesson videos");
        }

        Resource resource = storageService.loadAsResource(lesson.getVideoPath());
        try {
            long contentLength = resource.contentLength();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(lesson.getContentType()));
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

            if (rangeHeader != null) {
                List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
                if (!ranges.isEmpty()) {
                    HttpRange range = ranges.get(0);
                    headers.setContentLength(range.getRangeEnd(contentLength) - range.getRangeStart(contentLength) + 1);
                    headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + range.getRangeStart(contentLength) + "-" + range.getRangeEnd(contentLength) + "/" + contentLength);
                    return new ResponseEntity<>(resource, headers, HttpStatus.PARTIAL_CONTENT);
                }
            }

            headers.setContentLength(contentLength);
            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to stream lesson");
        }
    }
}
