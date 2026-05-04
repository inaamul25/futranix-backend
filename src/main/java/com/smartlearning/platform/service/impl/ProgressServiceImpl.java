package com.smartlearning.platform.service.impl;

import com.smartlearning.platform.dto.progress.CourseProgressCard;
import com.smartlearning.platform.dto.progress.DashboardResponse;
import com.smartlearning.platform.dto.progress.ProgressUpdateRequest;
import com.smartlearning.platform.entity.*;
import com.smartlearning.platform.repository.CourseRepository;
import com.smartlearning.platform.repository.EnrollmentRepository;
import com.smartlearning.platform.repository.LessonProgressRepository;
import com.smartlearning.platform.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final SupportService supportService;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseRepository courseRepository;
    private final LocalStorageService storageService;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String userEmail) {
        User user = supportService.getUserByEmail(userEmail);
        List<CourseProgressCard> enrolledCourses = enrollmentRepository.findByUser_Id(user.getId()).stream()
                .map(Enrollment::getCourse)
                .map(course -> new CourseProgressCard(
                        course.getId(),
                        course.getTitle(),
                        course.getCategory(),
                        course.getThumbnailPath() == null ? null : storageService.publicUrl(course.getThumbnailPath()),
                        calculateCourseCompletion(course.getId(), user.getId())
                )).toList();

        List<String> activeCategories = enrolledCourses.stream().map(CourseProgressCard::category).distinct().toList();
        List<CourseProgressCard> recommended = courseRepository.findAll().stream()
                .filter(course -> !enrollmentRepository.existsByUser_IdAndCourse_Id(user.getId(), course.getId()))
                .filter(course -> activeCategories.isEmpty() || activeCategories.contains(course.getCategory()))
                .limit(4)
                .map(course -> new CourseProgressCard(
                        course.getId(),
                        course.getTitle(),
                        course.getCategory(),
                        course.getThumbnailPath() == null ? null : storageService.publicUrl(course.getThumbnailPath()),
                        0
                )).toList();

        return new DashboardResponse(enrolledCourses, recommended);
    }

    @Override
    @Transactional
    public void updateProgress(ProgressUpdateRequest request, String userEmail) {
        User user = supportService.getUserByEmail(userEmail);
        Lesson lesson = supportService.getLesson(request.lessonId());
        LessonProgress progress = lessonProgressRepository.findByUser_IdAndLesson_Id(user.getId(), lesson.getId())
                .orElseGet(LessonProgress::new);
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setCompleted(request.completed());
        progress.setLastWatchedSecond(request.lastWatchedSecond());
        lessonProgressRepository.save(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCourseCompletion(Long courseId, Long userId) {
        Course course = supportService.getCourse(courseId);
        long totalLessons = course.getModules().stream().mapToLong(module -> module.getLessons().size()).sum();
        if (totalLessons == 0) {
            return 0;
        }
        long completed = course.getModules().stream()
                .flatMap(module -> module.getLessons().stream())
                .filter(lesson -> lessonProgressRepository.findByUser_IdAndLesson_Id(userId, lesson.getId())
                        .map(LessonProgress::isCompleted).orElse(false))
                .count();
        return Math.round((completed * 10000.0) / totalLessons) / 100.0;
    }
}
