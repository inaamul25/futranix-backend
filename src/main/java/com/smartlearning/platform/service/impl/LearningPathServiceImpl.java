package com.smartlearning.platform.service.impl;

import com.smartlearning.platform.dto.common.ApiResponse;
import com.smartlearning.platform.dto.course.CourseResponse;
import com.smartlearning.platform.dto.learningpath.LearningPathRequest;
import com.smartlearning.platform.dto.learningpath.LearningPathResponse;
import com.smartlearning.platform.entity.LearningPath;
import com.smartlearning.platform.entity.PathCourse;
import com.smartlearning.platform.entity.User;
import com.smartlearning.platform.entity.UserLearningPath;
import com.smartlearning.platform.repository.LearningPathRepository;
import com.smartlearning.platform.repository.UserLearningPathRepository;
import com.smartlearning.platform.service.CourseService;
import com.smartlearning.platform.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningPathServiceImpl implements LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final UserLearningPathRepository userLearningPathRepository;
    private final SupportService supportService;
    private final CourseService courseService;

    @Override
    @Transactional
    public LearningPathResponse create(LearningPathRequest request) {
        LearningPath learningPath = new LearningPath();
        learningPath.setTitle(request.title());
        learningPath.setDescription(request.description());
        List<PathCourse> pathCourses = new ArrayList<>();
        int order = 0;
        for (Long courseId : request.courseIds()) {
            PathCourse pathCourse = new PathCourse();
            pathCourse.setLearningPath(learningPath);
            pathCourse.setCourse(supportService.getCourse(courseId));
            pathCourse.setSortOrder(order++);
            pathCourses.add(pathCourse);
        }
        learningPath.setCourses(pathCourses);
        learningPathRepository.save(learningPath);
        return toResponse(learningPath, null);
    }

    @Override
    public List<LearningPathResponse> list(String userEmail) {
        Long userId = userEmail == null || userEmail.isBlank() ? null : supportService.getUserByEmail(userEmail).getId();
        return learningPathRepository.findAll().stream().map(path -> toResponse(path, userId)).toList();
    }

    @Override
    @Transactional
    public ApiResponse follow(Long pathId, String userEmail) {
        User user = supportService.getUserByEmail(userEmail);
        if (!userLearningPathRepository.existsByUser_IdAndLearningPath_Id(user.getId(), pathId)) {
            UserLearningPath userLearningPath = new UserLearningPath();
            userLearningPath.setUser(user);
            userLearningPath.setLearningPath(supportService.getLearningPath(pathId));
            userLearningPathRepository.save(userLearningPath);
        }
        return new ApiResponse(true, "Learning path followed");
    }

    private LearningPathResponse toResponse(LearningPath path, Long userId) {
        List<CourseResponse> courses = path.getCourses().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(pathCourse -> courseService.getCourse(pathCourse.getCourse().getId(), null))
                .toList();
        boolean followed = userId != null && userLearningPathRepository.existsByUser_IdAndLearningPath_Id(userId, path.getId());
        return new LearningPathResponse(path.getId(), path.getTitle(), path.getDescription(), courses, followed);
    }
}
