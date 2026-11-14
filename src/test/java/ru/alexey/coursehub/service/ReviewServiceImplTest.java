package ru.alexey.coursehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alexey.coursehub.dto.request.ReviewRequest;
import ru.alexey.coursehub.dto.request.UpdateReviewRequest;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.CourseReview;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.BadRequestException;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.CourseReviewRepository;
import ru.alexey.coursehub.repository.EnrollmentRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.impl.ReviewServiceImpl;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private CourseReviewRepository courseReviewRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void updatesRatingAndComment() {
        Course course = new Course();
        course.setId(1L);
        User student = new User();
        student.setId(2L);
        student.setName("Anna");
        CourseReview review = new CourseReview();
        review.setId(1L);
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(3);
        review.setComment("ok");
        when(courseReviewRepository.findById(1L)).thenReturn(Optional.of(review));

        CourseReview updated = reviewService.update(1L, new UpdateReviewRequest(5, "great"));

        assertThat(updated.getRating()).isEqualTo(5);
        assertThat(updated.getComment()).isEqualTo("great");
    }

    @Test
    void rejectsReviewFromStudentWithoutEnrollment() {
        Course course = new Course();
        course.setId(1L);
        User student = new User();
        student.setId(2L);
        student.setRole(Role.STUDENT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByCourseIdAndStudentId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.add(1L, 2L, new ReviewRequest(5, "nice")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not enrolled");
    }
}
