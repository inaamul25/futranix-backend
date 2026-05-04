package com.smartlearning.platform.service.impl;

import com.smartlearning.platform.dto.common.ApiResponse;
import com.smartlearning.platform.entity.Course;
import com.smartlearning.platform.entity.Enrollment;
import com.smartlearning.platform.entity.User;
import com.smartlearning.platform.exception.BadRequestException;
import com.smartlearning.platform.repository.EnrollmentRepository;
import com.smartlearning.platform.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final SupportService supportService;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public ApiResponse enroll(Long courseId, String userEmail) {
        User user = supportService.getUserByEmail(userEmail);
        Course course = supportService.getCourse(courseId);
        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("This is a paid course. Complete payment to unlock access.");
        }
        if (!enrollmentRepository.existsByUser_IdAndCourse_Id(user.getId(), courseId)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setUser(user);
            enrollment.setCourse(course);
            enrollmentRepository.save(enrollment);
        }
        return new ApiResponse(true, "Enrollment successful");
    }

    @Override
    public boolean isEnrolled(Long courseId, Long userId) {
        return enrollmentRepository.existsByUser_IdAndCourse_Id(userId, courseId);
    }
}
