package com.smartlearning.platform.service;

import com.smartlearning.platform.dto.course.CourseCreateRequest;
import com.smartlearning.platform.dto.course.CourseResponse;
import com.smartlearning.platform.dto.course.ModuleRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {
    CourseResponse createCourse(CourseCreateRequest request, MultipartFile thumbnail, String userEmail);
    CourseResponse updateCourse(Long courseId, CourseCreateRequest request, MultipartFile thumbnail, String userEmail);
    void updateCourseStructure(Long courseId, List<ModuleRequest> modules, String userEmail);
    List<CourseResponse> getCourses(String search, String category, String userEmail);
    CourseResponse getCourse(Long courseId, String userEmail);
    void deleteCourse(Long courseId, String userEmail);
}
