package com.smartlearning.platform.service.impl;

import com.smartlearning.platform.dto.course.*;
import com.smartlearning.platform.entity.*;
import com.smartlearning.platform.exception.UnauthorizedException;
import com.smartlearning.platform.repository.CourseRepository;
import com.smartlearning.platform.repository.EnrollmentRepository;
import com.smartlearning.platform.repository.LessonProgressRepository;
import com.smartlearning.platform.service.CourseService;
import com.smartlearning.platform.service.ProgressService;
import com.smartlearning.platform.service.StorageService;
import org.hibernate.Hibernate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final SupportService supportService;
    private final StorageService storageService;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ProgressService progressService;

    @Override
    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request, MultipartFile thumbnail, String userEmail) {
        User creator = supportService.getUserByEmail(userEmail);
        validateCreator(creator);
        Course course = new Course();
        applyCourseData(course, request, thumbnail, creator);
        courseRepository.save(course);
        return toResponse(course, creator.getId());
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseCreateRequest request, MultipartFile thumbnail, String userEmail) {
        User creator = supportService.getUserByEmail(userEmail);
        Course course = supportService.getCourse(courseId);
        validateOwnership(course, creator);
        applyCourseData(course, request, thumbnail, creator);
        return toResponse(courseRepository.save(course), creator.getId());
    }

    @Override
    @Transactional
    public void updateCourseStructure(Long courseId, List<ModuleRequest> modules, String userEmail) {
        User creator = supportService.getUserByEmail(userEmail);
        Course course = supportService.getCourse(courseId);
        validateOwnership(course, creator);
        course.getModules().clear();
        for (ModuleRequest moduleRequest : modules) {
            CourseModule module = new CourseModule();
            module.setCourse(course);
            module.setTitle(moduleRequest.title());
            module.setSortOrder(moduleRequest.sortOrder());
            Set<Lesson> lessons = new LinkedHashSet<>();
            for (LessonRequest lessonRequest : moduleRequest.lessons()) {
                Lesson lesson = new Lesson();
                lesson.setModule(module);
                lesson.setTitle(lessonRequest.title());
                lesson.setSortOrder(lessonRequest.sortOrder());
                lesson.setVideoPath(lessonRequest.videoPath());
                lesson.setContentType(lessonRequest.contentType() == null ? "video/mp4" : lessonRequest.contentType());
                lesson.setFileSize(lessonRequest.fileSize() == null ? 0L : lessonRequest.fileSize());
                lesson.setDurationSeconds(lessonRequest.durationSeconds() == null ? 0 : lessonRequest.durationSeconds());
                lessons.add(lesson);
            }
            module.setLessons(lessons);
            course.getModules().add(module);
        }
        courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCourses(String search, String category, String userEmail) {
        Long userId = resolveUserId(userEmail);
        List<Course> courses;
        if (search != null && !search.isBlank()) {
            courses = courseRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        } else if (category != null && !category.isBlank()) {
            courses = courseRepository.findByCategoryIgnoreCase(category);
        } else {
            courses = courseRepository.findAll();
        }
        return courses.stream().map(course -> toResponse(course, userId)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long courseId, String userEmail) {
        Long userId = resolveUserId(userEmail);
        return toResponse(supportService.getCourse(courseId), userId);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId, String userEmail) {
        User creator = supportService.getUserByEmail(userEmail);
        Course course = supportService.getCourse(courseId);
        validateOwnership(course, creator);
        courseRepository.delete(course);
    }

    private void applyCourseData(Course course, CourseCreateRequest request, MultipartFile thumbnail, User creator) {
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setPrice(request.price());
        course.setCategory(request.category());
        course.setCreator(creator);
        if (thumbnail != null && !thumbnail.isEmpty()) {
            course.setThumbnailPath(storageService.store(thumbnail, "thumbnails"));
        }
    }

    private void validateCreator(User creator) {
        boolean creatorRole = creator.getRoles().stream().anyMatch(role -> role.getName().name().equals("CREATOR") || role.getName().name().equals("ADMIN"));
        if (!creatorRole) {
            throw new UnauthorizedException("Only creators can manage courses");
        }
    }

    private void validateOwnership(Course course, User creator) {
        validateCreator(creator);
        if (!course.getCreator().getId().equals(creator.getId())) {
            boolean admin = creator.getRoles().stream().anyMatch(role -> role.getName().name().equals("ADMIN"));
            if (!admin) {
                throw new UnauthorizedException("You cannot manage this course");
            }
        }
    }

    private CourseResponse toResponse(Course course, Long userId) {
        Hibernate.initialize(course.getCreator());
        Hibernate.initialize(course.getModules());
        course.getModules().forEach(module -> Hibernate.initialize(module.getLessons()));

        boolean creatorViewing = userId != null && course.getCreator().getId().equals(userId);
        boolean enrolled = creatorViewing || (userId != null && enrollmentRepository.existsByUser_IdAndCourse_Id(userId, course.getId()));
        double completion = userId == null ? 0 : progressService.calculateCourseCompletion(course.getId(), userId);
        List<ModuleResponse> modules = course.getModules().stream()
                .sorted(Comparator.comparing(CourseModule::getSortOrder))
                .map(module -> {
                    List<LessonResponse> lessons = module.getLessons().stream()
                            .sorted(Comparator.comparing(Lesson::getSortOrder))
                            .map(lesson -> {
                                LessonProgress progress = userId == null ? null :
                                        lessonProgressRepository.findByUser_IdAndLesson_Id(userId, lesson.getId()).orElse(null);
                                return new LessonResponse(
                                        lesson.getId(),
                                        lesson.getTitle(),
                                        lesson.getSortOrder(),
                                        enrolled ? "/api/lessons/" + lesson.getId() + "/stream" : null,
                                        lesson.getDurationSeconds(),
                                        progress != null && progress.isCompleted(),
                                        progress != null ? progress.getLastWatchedSecond() : 0
                                );
                            }).toList();
                    boolean completed = !lessons.isEmpty() && lessons.stream().allMatch(LessonResponse::completed);
                    return new ModuleResponse(module.getId(), module.getTitle(), module.getSortOrder(), completed, lessons);
                })
                .toList();
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getThumbnailPath() == null ? null : storageService.publicUrl(course.getThumbnailPath()),
                course.getCategory(),
                course.getCreator().getId(),
                course.getCreator().getFullName(),
                modules,
                enrolled,
                completion
        );
    }

    private Long resolveUserId(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return supportService.getUserByEmail(email).getId();
    }
}
