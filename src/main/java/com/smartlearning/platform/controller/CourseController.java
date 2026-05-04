package com.smartlearning.platform.controller;

import com.smartlearning.platform.dto.course.CourseCreateRequest;
import com.smartlearning.platform.dto.course.CourseResponse;
import com.smartlearning.platform.dto.course.ModuleRequest;
import com.smartlearning.platform.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseResponse>> listCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Authentication authentication) {
        return ResponseEntity.ok(courseService.getCourses(search, category, authentication != null ? authentication.getName() : null));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long courseId, Authentication authentication) {
        return ResponseEntity.ok(courseService.getCourse(courseId, authentication != null ? authentication.getName() : null));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestPart("course") CourseCreateRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            Authentication authentication) {
        return ResponseEntity.ok(courseService.createCourse(request, thumbnail, authentication.getName()));
    }

    @PutMapping(value = "/{courseId}", consumes = {"multipart/form-data"})
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestPart("course") CourseCreateRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            Authentication authentication) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request, thumbnail, authentication.getName()));
    }

    @PutMapping("/{courseId}/structure")
    public ResponseEntity<Void> updateStructure(
            @PathVariable Long courseId,
            @Valid @RequestBody List<ModuleRequest> modules,
            Authentication authentication) {
        courseService.updateCourseStructure(courseId, modules, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId, Authentication authentication) {
        courseService.deleteCourse(courseId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
